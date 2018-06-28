/*
 * Copyright 2015-2018 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v20.html
 */

package org.junit.platform.engine.support.hierarchical;

import static java.util.stream.Collectors.toCollection;
import static org.junit.platform.commons.util.BlacklistedExceptions.rethrowIfBlacklisted;
import static org.junit.platform.engine.TestExecutionResult.Status.FAILED;
import static org.junit.platform.engine.TestExecutionResult.failed;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.Future;

import org.junit.platform.commons.JUnitException;
import org.junit.platform.engine.EngineExecutionListener;
import org.junit.platform.engine.TestDescriptor;
import org.junit.platform.engine.TestExecutionResult;
import org.junit.platform.engine.support.hierarchical.HierarchicalTestExecutorService.TestTask;
import org.junit.platform.engine.support.hierarchical.Node.ExecutionMode;
import org.junit.platform.engine.support.hierarchical.Node.SkipResult;

/**
 * @since 1.3
 */
class NodeTestTask<C extends EngineExecutionContext> implements TestTask {

	private static final SingleTestExecutor singleTestExecutor = new SingleTestExecutor();

	private final List<Throwable> executionErrors = new ArrayList<>();
	private final TestDescriptor testDescriptor;
	private final EngineExecutionListener listener;
	private final HierarchicalTestExecutorService executorService;
	private final Node<C> node;
	private final ExecutionMode executionMode;
	private final Set<ExclusiveResource> exclusiveResources;
	private final List<NodeTestTask<C>> children;

	private ResourceLock resourceLock = NopLock.INSTANCE;
	private Optional<ExecutionMode> forcedExecutionMode = Optional.empty();

	private C parentContext;
	private C context;

	private SkipResult skipResult;
	private TestExecutionResult executionResult;

	NodeTestTask(TestDescriptor testDescriptor, EngineExecutionListener listener,
			HierarchicalTestExecutorService executorService) {
		this.testDescriptor = testDescriptor;
		this.listener = listener;
		this.executorService = executorService;
		node = asNode(testDescriptor);
		executionMode = node.getExecutionMode();
		exclusiveResources = node.getExclusiveResources();
		// @formatter:off
		children = testDescriptor.getChildren().stream()
				.map(descriptor -> new NodeTestTask<C>(descriptor, listener, executorService))
				.collect(toCollection(ArrayList::new));
		// @formatter:on
	}

	public Set<ExclusiveResource> getExclusiveResources() {
		return exclusiveResources;
	}

	public List<NodeTestTask<C>> getChildren() {
		return children;
	}

	@Override
	public ResourceLock getResourceLock() {
		return resourceLock;
	}

	public void setResourceLock(ResourceLock resourceLock) {
		this.resourceLock = resourceLock;
	}

	@Override
	public ExecutionMode getExecutionMode() {
		return forcedExecutionMode.orElse(executionMode);
	}

	public void setForcedExecutionMode(ExecutionMode forcedExecutionMode) {
		this.forcedExecutionMode = Optional.of(forcedExecutionMode);
	}

	public void setParentContext(C parentContext) {
		this.parentContext = parentContext;
	}

	@Override
	public void execute() {
		prepare();
		if (executionErrors.isEmpty()) {
			checkWhetherSkipped();
		}
		if (executionErrors.isEmpty() && !skipResult.isSkipped()) {
			executeRecursively();
		}
		if (context != null) {
			cleanUp();
		}
		reportCompletion();
	}

	private void prepare() {
		executeSafely(() -> context = node.prepare(parentContext));
	}

	private void checkWhetherSkipped() {
		executeSafely(() -> skipResult = node.shouldBeSkipped(context));
	}

	private void executeRecursively() {
		listener.executionStarted(testDescriptor);

		executionResult = singleTestExecutor.executeSafely(() -> {
			Throwable failure = null;
			try {
				context = node.before(context);

				List<Future<?>> futures = new ArrayList<>();
				context = node.execute(context,
					dynamicTestDescriptor -> executeDynamicTest(dynamicTestDescriptor, futures));

				children.forEach(child -> child.setParentContext(context));
				executorService.invokeAll(children);

				// using a for loop for the sake for ForkJoinPool's work stealing
				for (Future<?> future : futures) {
					future.get();
				}
			}
			catch (Throwable t) {
				failure = t;
			}
			finally {
				executeAfter(failure);
			}
		});
	}

	private void executeDynamicTest(TestDescriptor dynamicTestDescriptor, List<Future<?>> futures) {
		listener.dynamicTestRegistered(dynamicTestDescriptor);
		NodeTestTask<C> nodeTestTask = new NodeTestTask<>(dynamicTestDescriptor, listener, executorService);
		Set<ExclusiveResource> exclusiveResources = nodeTestTask.getExclusiveResources();
		if (!exclusiveResources.isEmpty()) {
			listener.executionStarted(dynamicTestDescriptor);
			String message = "Dynamic test descriptors must not declare exclusive resources: " + exclusiveResources;
			listener.executionFinished(dynamicTestDescriptor, failed(new JUnitException(message)));
		}
		else {
			nodeTestTask.setParentContext(context);
			futures.add(executorService.submit(nodeTestTask));
		}
	}

	private void executeAfter(Throwable failure) throws Throwable {
		try {
			node.after(context);
		}
		catch (Throwable t) {
			if (failure != null && failure != t) {
				failure.addSuppressed(t);
			}
			else {
				throw t;
			}
		}
		if (failure != null) {
			throw failure;
		}
	}

	private void cleanUp() {
		executeSafely(() -> node.cleanUp(context));
	}

	private void reportCompletion() {
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
		return failed(throwable);
	}

	private void executeSafely(Action action) {
		try {
			action.execute();
		}
		catch (Throwable t) {
			rethrowIfBlacklisted(t);
			executionErrors.add(t);
		}
	}

	@FunctionalInterface
	private interface Action {
		void execute() throws Exception;
	}

	@SuppressWarnings("unchecked")
	private Node<C> asNode(TestDescriptor testDescriptor) {
		return (testDescriptor instanceof Node ? (Node<C>) testDescriptor : noOpNode);
	}

	@SuppressWarnings("rawtypes")
	private static final Node noOpNode = new Node() {
	};

}
