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

import static java.util.concurrent.CompletableFuture.completedFuture;
import static org.junit.platform.commons.annotation.ExecutionMode.Concurrent;

import java.util.Deque;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.Future;

import org.junit.platform.commons.logging.LoggerFactory;

public class ForkJoinPoolHierarchicalTestExecutorService implements HierarchicalTestExecutorService {

	private final ForkJoinPool forkJoinPool;

	public ForkJoinPoolHierarchicalTestExecutorService(Optional<Integer> parallelismLevel) {
		forkJoinPool = createForkJoinPool(parallelismLevel);
		LoggerFactory.getLogger(ForkJoinPoolHierarchicalTestExecutorService.class) //
				.config(() -> "Using ForkJoinPool with parallelism of " + forkJoinPool.getParallelism());
	}

	private ForkJoinPool createForkJoinPool(Optional<Integer> parallelismLevel) {
		if (parallelismLevel.isPresent()) {
			return new ForkJoinPool(parallelismLevel.get());
		}
		return new ForkJoinPool();
	}

	@Override
	public Future<Void> submit(TestTask testTask) {
		ExclusiveTask exclusiveTask = new ExclusiveTask(testTask);
		if (testTask.getTestDescriptor().isRoot()) {
			// ensure we're running inside the ForkJoinPool so we
			// can use ForkJoinTask API in invokeAll etc.
			return forkJoinPool.submit(exclusiveTask);
		}
		if (testTask.getExecutionMode() == Concurrent) {
			return exclusiveTask.fork();
		}
		exclusiveTask.compute();
		return completedFuture(null);
	}

	@Override
	public void invokeAll(List<TestTask> tasks) {
		Deque<ExclusiveTask> nonConcurrentTasks = new LinkedList<>();
		Deque<ExclusiveTask> concurrentTasksInReverseOrder = new LinkedList<>();
		for (TestTask testTask : tasks) {
			ExclusiveTask exclusiveTask = new ExclusiveTask(testTask);
			if (testTask.getExecutionMode() == Concurrent) {
				exclusiveTask.fork();
				concurrentTasksInReverseOrder.addFirst(exclusiveTask);
			}
			else {
				nonConcurrentTasks.add(exclusiveTask);
			}
		}
		for (ExclusiveTask task : nonConcurrentTasks) {
			task.compute();
		}
		for (ExclusiveTask forkedTask : concurrentTasksInReverseOrder) {
			forkedTask.join();
		}
	}

	@Override
	public void close() {
		forkJoinPool.shutdownNow();
	}

}
