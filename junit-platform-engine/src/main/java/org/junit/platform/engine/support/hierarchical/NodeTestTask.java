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
import static org.junit.platform.engine.TestExecutionResult.failed;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.Future;

import org.junit.platform.commons.JUnitException;
import org.junit.platform.engine.EngineExecutionListener;
import org.junit.platform.engine.TestDescriptor;
import org.junit.platform.engine.support.hierarchical.HierarchicalTestExecutorService.TestTask;
import org.junit.platform.engine.support.hierarchical.Node.ExecutionMode;
import org.junit.platform.engine.support.hierarchical.Node.SkipResult;

/**
 * @since 1.3
 */
class NodeTestTask<C extends EngineExecutionContext> implements TestTask {

	private final TestDescriptor testDescriptor;
	private final EngineExecutionListener listener;
	private final HierarchicalTestExecutorService executorService;
	private final ThrowableCollector.Factory throwableCollectorFactory;
	private final Node<C> node;
	private final ExecutionMode executionMode;
	private final Set<ExclusiveResource> exclusiveResources;
	private final List<NodeTestTask<C>> children;

	private ResourceLock resourceLock = NopLock.INSTANCE;
	private Optional<ExecutionMode> forcedExecutionMode = Optional.empty();

	private C parentContext;
	private C context;

	private SkipResult skipResult;
	private boolean started;
	private ThrowableCollector throwableCollector;

	NodeTestTask(TestDescriptor testDescriptor, EngineExecutionListener listener,
			HierarchicalTestExecutorService executorService, ThrowableCollector.Factory throwableCollectorFactory) {
		this.testDescriptor = testDescriptor;
		this.listener = listener;
		this.executorService = executorService;
		this.throwableCollectorFactory = throwableCollectorFactory;
		node = asNode(testDescriptor);
		executionMode = node.getExecutionMode();
		exclusiveResources = node.getExclusiveResources();
		// @formatter:off
		children = testDescriptor.getChildren().stream()
				.map(descriptor -> new NodeTestTask<C>(descriptor, listener, executorService, throwableCollectorFactory))
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
		throwableCollector = throwableCollectorFactory.create();
		prepare();
		if (throwableCollector.isEmpty()) {
			checkWhetherSkipped();
		}
		if (throwableCollector.isEmpty() && !skipResult.isSkipped()) {
			executeRecursively();
		}
		if (context != null) {
			cleanUp();
		}
		reportCompletion();
	}

	private void prepare() {
		throwableCollector.execute(() -> context = node.prepare(parentContext));
	}

	private void checkWhetherSkipped() {
		throwableCollector.execute(() -> skipResult = node.shouldBeSkipped(context));
	}

	private void executeRecursively() {
		listener.executionStarted(testDescriptor);
		started = true;

		throwableCollector.execute(() -> {
			context = node.before(context);

			List<Future<?>> futures = new ArrayList<>();
			context = node.execute(context,
				dynamicTestDescriptor -> executeDynamicTest(dynamicTestDescriptor, futures));

			if (!children.isEmpty()) {
				children.forEach(child -> child.setParentContext(context));
				executorService.invokeAll(children);
			}

			// using a for loop for the sake for ForkJoinPool's work stealing
			for (Future<?> future : futures) {
				future.get();
			}
		});

		throwableCollector.execute(() -> node.after(context));
	}

	private void executeDynamicTest(TestDescriptor dynamicTestDescriptor, List<Future<?>> futures) {
		listener.dynamicTestRegistered(dynamicTestDescriptor);
		NodeTestTask<C> nodeTestTask = new NodeTestTask<>(dynamicTestDescriptor, listener, executorService,
			throwableCollectorFactory);
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

	private void cleanUp() {
		throwableCollector.execute(() -> node.cleanUp(context));
	}

	private void reportCompletion() {
		if (throwableCollector.isEmpty() && skipResult.isSkipped()) {
			listener.executionSkipped(testDescriptor, skipResult.getReason().orElse("<unknown>"));
			return;
		}
		if (!started) {
			// Call executionStarted first to comply with the contract of EngineExecutionListener.
			listener.executionStarted(testDescriptor);
		}
		listener.executionFinished(testDescriptor, throwableCollector.toTestExecutionResult());
		throwableCollector = null;
	}

	@SuppressWarnings("unchecked")
	private Node<C> asNode(TestDescriptor testDescriptor) {
		return (testDescriptor instanceof Node ? (Node<C>) testDescriptor : noOpNode);
	}

	@SuppressWarnings("rawtypes")
	private static final Node noOpNode = new Node() {
	};

}
