/*
 * Copyright 2015-2017 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v20.html
 */

package org.junit.platform.engine.support.hierarchical;

import static java.util.stream.Collectors.toList;
import static org.junit.platform.commons.util.BlacklistedExceptions.rethrowIfBlacklisted;
import static org.junit.platform.engine.TestExecutionResult.Status.FAILED;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Future;
import java.util.function.BiFunction;

import org.junit.platform.commons.annotation.ExecutionMode;
import org.junit.platform.engine.EngineExecutionListener;
import org.junit.platform.engine.TestDescriptor;
import org.junit.platform.engine.TestExecutionResult;
import org.junit.platform.engine.support.hierarchical.HierarchicalTestExecutorService.TestTask;

class NodeExecutor<C extends EngineExecutionContext> {

	private final TestDescriptor testDescriptor;
	private final Node<C> node;
	private final List<Throwable> executionErrors = new ArrayList<>();
	private final List<NodeExecutor<C>> children;

	private C context;
	private Node.SkipResult skipResult;
	private TestExecutionResult executionResult;

	private ResourceLock resourceLock = NopLock.INSTANCE;
	private ExecutionMode executionMode;
	private Optional<ExecutionMode> forcedExecutionMode = Optional.empty();

	NodeExecutor(TestDescriptor testDescriptor) {
		this.testDescriptor = testDescriptor;
		node = asNode(testDescriptor);
		executionMode = node.getExecutionMode();
		children = testDescriptor.getChildren().stream().map(descriptor -> new NodeExecutor<C>(descriptor)).collect(
			toList());
	}

	public TestDescriptor getTestDescriptor() {
		return testDescriptor;
	}

	public Node<C> getNode() {
		return node;
	}

	public List<NodeExecutor<C>> getChildren() {
		return children;
	}

	public ResourceLock getResourceLock() {
		return resourceLock;
	}

	public void setResourceLock(ResourceLock resourceLock) {
		this.resourceLock = resourceLock;
	}

	public ExecutionMode getExecutionMode() {
		return forcedExecutionMode.orElse(executionMode);
	}

	public void setForcedExecutionMode(ExecutionMode forcedExecutionMode) {
		this.forcedExecutionMode = Optional.of(forcedExecutionMode);
	}

	void execute(C parentContext, EngineExecutionListener listener, HierarchicalTestExecutorService executorService,
			SingleTestExecutor singleTestExecutor, BiFunction<NodeExecutor<C>, C, TestTask> testTaskCreator) {
		prepare(parentContext);
		if (executionErrors.isEmpty()) {
			checkWhetherSkipped();
		}
		if (executionErrors.isEmpty() && !skipResult.isSkipped()) {
			executeRecursively(listener, executorService, singleTestExecutor, testTaskCreator);
		}
		if (context != null) {
			cleanUp();
		}
		reportDone(listener);
	}

	private void prepare(C parentContext) {
		try {
			context = node.prepare(parentContext);
		}
		catch (Throwable t) {
			addExecutionError(t);
		}
	}

	private void checkWhetherSkipped() {
		try {
			skipResult = node.shouldBeSkipped(context);
		}
		catch (Throwable t) {
			addExecutionError(t);
		}
	}

	private void executeRecursively(EngineExecutionListener listener, HierarchicalTestExecutorService executorService,
			SingleTestExecutor singleTestExecutor, BiFunction<NodeExecutor<C>, C, TestTask> testTaskCreator) {
		listener.executionStarted(testDescriptor);

		executionResult = singleTestExecutor.executeSafely(() -> {
			try {
				context = node.before(context);

				Map<NodeExecutor<C>, Future<?>> futures = new ConcurrentHashMap<>();

				context = node.execute(context, dynamicTestDescriptor -> {
					listener.dynamicTestRegistered(dynamicTestDescriptor);
					NodeExecutor<C> nodeExecutor = new NodeExecutor<>(dynamicTestDescriptor);
					// TODO Validate dynamic children do not add additional resource locks etc.
					children.add(nodeExecutor);
					TestTask testTask = testTaskCreator.apply(nodeExecutor, context);
					futures.put(nodeExecutor, executorService.submit(testTask));
				});

				// @formatter:off
				children
						.forEach(child -> futures.computeIfAbsent(child, d -> executorService.submit(testTaskCreator.apply(child, context))));
				children.stream()
						.map(futures::get)
						.forEach(HierarchicalTestExecutor::waitFor);
				// @formatter:on
			}
			finally {
				node.after(context);
			}
		});
	}

	private void cleanUp() {
		try {
			node.cleanUp(context);
		}
		catch (Throwable t) {
			addExecutionError(t);
		}
	}

	private void reportDone(EngineExecutionListener listener) {
		if (executionResult != null) {
			addExecutionErrorsToTestExecutionResult();
			listener.executionFinished(testDescriptor, executionResult);
		}
		else if (executionErrors.isEmpty() && skipResult.isSkipped()) {
			listener.executionSkipped(testDescriptor, skipResult.getReason().orElse("<unknown>"));
		}
		else {
			// Call executionStarted first to comply with the contract of EngineExecutionListener.
			listener.executionStarted(testDescriptor);
			listener.executionFinished(testDescriptor, createTestExecutionResultFromExecutionErrors());
		}
	}

	private void addExecutionErrorsToTestExecutionResult() {
		if (executionErrors.isEmpty()) {
			return;
		}
		if (executionResult.getStatus() == FAILED && executionResult.getThrowable().isPresent()) {
			Throwable throwable = executionResult.getThrowable().get();
			executionErrors.forEach(throwable::addSuppressed);
		}
		else {
			executionResult = createTestExecutionResultFromExecutionErrors();
		}
	}

	private TestExecutionResult createTestExecutionResultFromExecutionErrors() {
		Throwable throwable = executionErrors.get(0);
		executionErrors.stream().skip(1).forEach(throwable::addSuppressed);
		return TestExecutionResult.failed(throwable);
	}

	private void addExecutionError(Throwable throwable) {
		rethrowIfBlacklisted(throwable);
		executionErrors.add(throwable);
	}

	@SuppressWarnings("unchecked")
	private Node<C> asNode(TestDescriptor testDescriptor) {
		return (testDescriptor instanceof Node ? (Node<C>) testDescriptor : noOpNode);
	}

	@SuppressWarnings("rawtypes")
	private static final Node noOpNode = new Node() {
	};
}
