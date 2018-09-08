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
import java.util.Set;
import java.util.concurrent.Future;

import org.junit.platform.commons.JUnitException;
import org.junit.platform.engine.TestDescriptor;
import org.junit.platform.engine.support.hierarchical.HierarchicalTestExecutorService.TestTask;
import org.junit.platform.engine.support.hierarchical.Node.ExecutionMode;
import org.junit.platform.engine.support.hierarchical.Node.SkipResult;

/**
 * @since 1.3
 */
class NodeTestTask<C extends EngineExecutionContext> implements TestTask {

	private final NodeTestTaskContext taskContext;
	private final TestDescriptor testDescriptor;
	private final Node<C> node;

	private C parentContext;
	private C context;

	private SkipResult skipResult;
	private boolean started;
	private ThrowableCollector throwableCollector;

	NodeTestTask(NodeTestTaskContext taskContext, TestDescriptor testDescriptor) {
		this.taskContext = taskContext;
		this.testDescriptor = testDescriptor;
		this.node = NodeUtils.asNode(testDescriptor);
	}

	@Override
	public ResourceLock getResourceLock() {
		return taskContext.getExecutionAdvisor().getResourceLock(testDescriptor);
	}

	@Override
	public ExecutionMode getExecutionMode() {
		return taskContext.getExecutionAdvisor().getForcedExecutionMode(testDescriptor).orElse(node.getExecutionMode());
	}

	void setParentContext(C parentContext) {
		this.parentContext = parentContext;
	}

	@Override
	public void execute() {
		throwableCollector = taskContext.getThrowableCollectorFactory().create();
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

		// Clear reference to parent context to allow it to be garbage collected.
		// See https://github.com/junit-team/junit5/issues/1578
		parentContext = null;
	}

	private void checkWhetherSkipped() {
		throwableCollector.execute(() -> skipResult = node.shouldBeSkipped(context));
	}

	private void executeRecursively() {
		taskContext.getListener().executionStarted(testDescriptor);
		started = true;

		throwableCollector.execute(() -> {
			// @formatter:off
			List<NodeTestTask<C>> children = testDescriptor.getChildren().stream()
					.map(descriptor -> new NodeTestTask<C>(taskContext, descriptor))
					.collect(toCollection(ArrayList::new));
			// @formatter:on

			context = node.before(context);

			List<Future<?>> futures = new ArrayList<>();
			context = node.execute(context,
				dynamicTestDescriptor -> executeDynamicTest(dynamicTestDescriptor, futures));

			if (!children.isEmpty()) {
				children.forEach(child -> child.setParentContext(context));
				taskContext.getExecutorService().invokeAll(children);
			}

			// using a for loop for the sake for ForkJoinPool's work stealing
			for (Future<?> future : futures) {
				future.get();
			}
		});

		throwableCollector.execute(() -> node.after(context));
	}

	private void executeDynamicTest(TestDescriptor dynamicTestDescriptor, List<Future<?>> futures) {
		taskContext.getListener().dynamicTestRegistered(dynamicTestDescriptor);
		Set<ExclusiveResource> exclusiveResources = NodeUtils.asNode(dynamicTestDescriptor).getExclusiveResources();
		if (!exclusiveResources.isEmpty()) {
			taskContext.getListener().executionStarted(dynamicTestDescriptor);
			String message = "Dynamic test descriptors must not declare exclusive resources: " + exclusiveResources;
			taskContext.getListener().executionFinished(dynamicTestDescriptor, failed(new JUnitException(message)));
		}
		else {
			NodeTestTask<C> nodeTestTask = new NodeTestTask<>(taskContext, dynamicTestDescriptor);
			nodeTestTask.setParentContext(context);
			futures.add(taskContext.getExecutorService().submit(nodeTestTask));
		}
	}

	private void cleanUp() {
		throwableCollector.execute(() -> node.cleanUp(context));

		// Clear reference to context to allow it to be garbage collected.
		// See https://github.com/junit-team/junit5/issues/1578
		context = null;
	}

	private void reportCompletion() {
		if (throwableCollector.isEmpty() && skipResult.isSkipped()) {
			taskContext.getListener().executionSkipped(testDescriptor, skipResult.getReason().orElse("<unknown>"));
			return;
		}
		if (!started) {
			// Call executionStarted first to comply with the contract of EngineExecutionListener.
			taskContext.getListener().executionStarted(testDescriptor);
		}
		taskContext.getListener().executionFinished(testDescriptor, throwableCollector.toTestExecutionResult());
		throwableCollector = null;
	}

}
