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
import static java.util.concurrent.ForkJoinPool.defaultForkJoinWorkerThreadFactory;
import static org.junit.platform.engine.support.hierarchical.Node.ExecutionMode.Concurrent;

import java.lang.Thread.UncaughtExceptionHandler;
import java.lang.reflect.Constructor;
import java.util.Deque;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.ForkJoinPool.ForkJoinWorkerThreadFactory;
import java.util.concurrent.Future;
import java.util.concurrent.RecursiveAction;
import java.util.concurrent.TimeUnit;
import java.util.function.Predicate;

import org.junit.platform.commons.logging.LoggerFactory;
import org.junit.platform.commons.util.ExceptionUtils;
import org.junit.platform.engine.ConfigurationParameters;

public class ForkJoinPoolHierarchicalTestExecutorService implements HierarchicalTestExecutorService {

	private final ForkJoinPool forkJoinPool;

	public ForkJoinPoolHierarchicalTestExecutorService(ConfigurationParameters configurationParameters) {
		forkJoinPool = createForkJoinPool(configurationParameters);
		LoggerFactory.getLogger(ForkJoinPoolHierarchicalTestExecutorService.class) //
				.config(() -> "Using ForkJoinPool with parallelism of " + forkJoinPool.getParallelism());
	}

	private ForkJoinPool createForkJoinPool(ConfigurationParameters configurationParameters) {
		ParallelExecutionConfigurationStrategy strategy = DefaultParallelExecutionConfigurationStrategy.getStrategy(
			configurationParameters);
		ParallelExecutionConfiguration configuration = strategy.createConfiguration(configurationParameters);
		try {
			// Try to use constructor available in Java >= 9
			Constructor<ForkJoinPool> constructor = ForkJoinPool.class.getDeclaredConstructor(Integer.TYPE,
				ForkJoinWorkerThreadFactory.class, UncaughtExceptionHandler.class, Boolean.TYPE, Integer.TYPE,
				Integer.TYPE, Integer.TYPE, Predicate.class, Long.TYPE, TimeUnit.class);
			return constructor.newInstance(configuration.getParallelism(), defaultForkJoinWorkerThreadFactory, null,
				false, configuration.getCorePoolSize(), configuration.getMaxPoolSize(),
				configuration.getMinimumRunnable(), null, configuration.getKeepAlive(), TimeUnit.SECONDS);
		}
		catch (Exception e) {
			// Fallback for Java 8
			return new ForkJoinPool(configuration.getParallelism());
		}
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
	public void invokeAll(List<? extends TestTask> tasks) {
		if (tasks.size() == 1) {
			new ExclusiveTask(tasks.get(0)).compute();
			return;
		}
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

	// this class cannot not be serialized because TestTask is not Serializable
	@SuppressWarnings("serial")
	static class ExclusiveTask extends RecursiveAction {

		private final TestTask testTask;

		ExclusiveTask(TestTask testTask) {
			this.testTask = testTask;
		}

		@SuppressWarnings("try")
		@Override
		public void compute() {
			try (AcquiredResourceLock acquiredLock = testTask.getResourceLock().acquire()) {
				testTask.execute();
			}
			catch (InterruptedException e) {
				ExceptionUtils.throwAsUncheckedException(e);
			}
		}
	}
}
