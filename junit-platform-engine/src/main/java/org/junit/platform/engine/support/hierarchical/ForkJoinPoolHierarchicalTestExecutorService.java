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

import org.junit.platform.commons.annotation.ExecutionMode;
import org.junit.platform.engine.TestDescriptor;

import static java.util.concurrent.CompletableFuture.completedFuture;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.Future;

public class ForkJoinPoolHierarchicalTestExecutorService<C extends EngineExecutionContext>
		implements HierarchicalTestExecutorService<C> {

	private final LockManager lockManager = new LockManager();

	private final ForkJoinPool forkJoinPool;

	private final Map<TestDescriptor, TestTaskContext> contextByTestDescriptor = new ConcurrentHashMap<>();

	static class TestTaskContext {

		private final ExecutionMode executionMode;
		private final Optional<ExecutionMode> forcedExecutionMode;

		public TestTaskContext(ExecutionMode executionMode, Optional<ExecutionMode> forcedExecutionMode) {
			this.executionMode = executionMode;
			this.forcedExecutionMode = forcedExecutionMode;
		}

		public ExecutionMode getExecutionMode() {
			return executionMode;
		}

		public Optional<ExecutionMode> getForcedExecutionMode() {
			return forcedExecutionMode;
		}
	}

	public ForkJoinPoolHierarchicalTestExecutorService() {
		forkJoinPool = new ForkJoinPool();
	}

	@Override
	public Future<Void> submit(TestTask<C> testTask) {
		List<ExclusiveResource> resources = testTask.getExclusiveResources();
		ResourceLock resourceLock = lockManager.getLockForResources(resources);
		ExclusiveTask<Void> exclusiveTask = new ExclusiveTask<>(resourceLock, () -> {
			testTask.execute();
			return null;
		});

		Optional<TestTaskContext> parentContext = testTask.getTestDescriptor().getParent().map(contextByTestDescriptor::get);
		ExecutionMode executionMode;
		if (testTask.getTestDescriptor().isContainer()) {
			executionMode = parentContext
					.map(TestTaskContext::getExecutionMode)
					.orElse(ExecutionMode.Concurrent);
		} else {
			executionMode = resourceLock.getForcedExecutionMode().orElseGet(() -> parentContext
					.flatMap(TestTaskContext::getForcedExecutionMode)
					.orElseGet(testTask::getExecutionMode));
		}

		contextByTestDescriptor.put(testTask.getTestDescriptor(), new TestTaskContext(executionMode, resourceLock.getForcedExecutionMode()));

		switch (executionMode) {
			case Concurrent:
				return forkJoinPool.submit(exclusiveTask);

			case SameThread:
			default:
				try {
					exclusiveTask.call();
					return completedFuture(null);
				}
				catch (Exception e) {
					CompletableFuture<Void> exceptionFuture = new CompletableFuture<>();
					exceptionFuture.completeExceptionally(e);
					return exceptionFuture;
				}
		}
	}

	@Override
	public void close() {
		forkJoinPool.shutdownNow();
	}

}
