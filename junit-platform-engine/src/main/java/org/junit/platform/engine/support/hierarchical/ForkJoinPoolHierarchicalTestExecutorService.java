/*
 * Copyright 2015-2020 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * https://www.eclipse.org/legal/epl-v20.html
 */

package org.junit.platform.engine.support.hierarchical;

import static java.util.concurrent.CompletableFuture.completedFuture;
import static org.apiguardian.api.API.Status.EXPERIMENTAL;
import static org.junit.platform.engine.support.hierarchical.Node.ExecutionMode.CONCURRENT;

import java.lang.Thread.UncaughtExceptionHandler;
import java.lang.reflect.Constructor;
import java.util.Deque;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.ForkJoinPool.ForkJoinWorkerThreadFactory;
import java.util.concurrent.ForkJoinTask;
import java.util.concurrent.ForkJoinWorkerThread;
import java.util.concurrent.Future;
import java.util.concurrent.RecursiveAction;
import java.util.concurrent.TimeUnit;
import java.util.function.Predicate;

import org.apiguardian.api.API;
import org.junit.platform.commons.JUnitException;
import org.junit.platform.commons.function.Try;
import org.junit.platform.commons.logging.LoggerFactory;
import org.junit.platform.commons.util.ExceptionUtils;
import org.junit.platform.engine.ConfigurationParameters;

/**
 * A {@link ForkJoinPool}-based
 * {@linkplain HierarchicalTestExecutorService executor service} that executes
 * {@linkplain TestTask test tasks} with the configured parallelism.
 *
 * @see ForkJoinPool
 * @see DefaultParallelExecutionConfigurationStrategy
 * @since 1.3
 */
@API(status = EXPERIMENTAL, since = "1.3")
public class ForkJoinPoolHierarchicalTestExecutorService implements HierarchicalTestExecutorService {

	private final ForkJoinPool forkJoinPool;
	private final int parallelism;

	/**
	 * Create a new {@code ForkJoinPoolHierarchicalTestExecutorService} based on
	 * the supplied {@link ConfigurationParameters}.
	 *
	 * @see DefaultParallelExecutionConfigurationStrategy
	 */
	public ForkJoinPoolHierarchicalTestExecutorService(ConfigurationParameters configurationParameters) {
		forkJoinPool = createForkJoinPool(configurationParameters);
		parallelism = forkJoinPool.getParallelism();
		LoggerFactory.getLogger(getClass()).config(() -> "Using ForkJoinPool with parallelism of " + parallelism);
	}

	private ForkJoinPool createForkJoinPool(ConfigurationParameters configurationParameters) {
		ParallelExecutionConfigurationStrategy strategy = DefaultParallelExecutionConfigurationStrategy.getStrategy(
			configurationParameters);
		ParallelExecutionConfiguration configuration = strategy.createConfiguration(configurationParameters);
		ForkJoinWorkerThreadFactory threadFactory = new WorkerThreadFactory();
		return Try.call(() -> {
			// Try to use constructor available in Java >= 9
			Constructor<ForkJoinPool> constructor = ForkJoinPool.class.getDeclaredConstructor(Integer.TYPE,
				ForkJoinWorkerThreadFactory.class, UncaughtExceptionHandler.class, Boolean.TYPE, Integer.TYPE,
				Integer.TYPE, Integer.TYPE, Predicate.class, Long.TYPE, TimeUnit.class);
			return constructor.newInstance(configuration.getParallelism(), threadFactory, null, false,
				configuration.getCorePoolSize(), configuration.getMaxPoolSize(), configuration.getMinimumRunnable(),
				null, configuration.getKeepAliveSeconds(), TimeUnit.SECONDS);
		}).orElseTry(() -> {
			// Fallback for Java 8
			return new ForkJoinPool(configuration.getParallelism(), threadFactory, null, false);
		}).getOrThrow(cause -> new JUnitException("Failed to create ForkJoinPool", cause));
	}

	@Override
	public Future<Void> submit(TestTask testTask) {
		ExclusiveTask exclusiveTask = new ExclusiveTask(testTask);
		if (!isAlreadyRunningInForkJoinPool()) {
			// ensure we're running inside the ForkJoinPool so we
			// can use ForkJoinTask API in invokeAll etc.
			return forkJoinPool.submit(exclusiveTask);
		}
		// Limit the amount of queued work so we don't consume dynamic tests too eagerly
		// by forking only if the current worker thread's queue length is below the
		// desired parallelism. This optimistically assumes that the already queued tasks
		// can be stolen by other workers and the new task requires about the same
		// execution time as the already queued tasks. If the other workers are busy,
		// the parallelism is already at its desired level. If all already queued tasks
		// can be stolen by otherwise idle workers and the new task takes significantly
		// longer, parallelism will drop. However, that only happens if the enclosing test
		// task is the only one remaining which should rarely be the case.
		if (testTask.getExecutionMode() == CONCURRENT && ForkJoinTask.getSurplusQueuedTaskCount() < parallelism) {
			return exclusiveTask.fork();
		}
		exclusiveTask.compute();
		return completedFuture(null);
	}

	private boolean isAlreadyRunningInForkJoinPool() {
		return ForkJoinTask.getPool() == forkJoinPool;
	}

	@Override
	public void invokeAll(List<? extends TestTask> tasks) {
		if (tasks.size() == 1) {
			new ExclusiveTask(tasks.get(0)).compute();
			return;
		}
		Deque<ExclusiveTask> nonConcurrentTasks = new LinkedList<>();
		Deque<ExclusiveTask> concurrentTasksInReverseOrder = new LinkedList<>();
		forkConcurrentTasks(tasks, nonConcurrentTasks, concurrentTasksInReverseOrder);
		executeNonConcurrentTasks(nonConcurrentTasks);
		joinConcurrentTasksInReverseOrderToEnableWorkStealing(concurrentTasksInReverseOrder);
	}

	private void forkConcurrentTasks(List<? extends TestTask> tasks, Deque<ExclusiveTask> nonConcurrentTasks,
			Deque<ExclusiveTask> concurrentTasksInReverseOrder) {
		for (TestTask testTask : tasks) {
			ExclusiveTask exclusiveTask = new ExclusiveTask(testTask);
			if (testTask.getExecutionMode() == CONCURRENT) {
				exclusiveTask.fork();
				concurrentTasksInReverseOrder.addFirst(exclusiveTask);
			}
			else {
				nonConcurrentTasks.add(exclusiveTask);
			}
		}
	}

	private void executeNonConcurrentTasks(Deque<ExclusiveTask> nonConcurrentTasks) {
		for (ExclusiveTask task : nonConcurrentTasks) {
			task.compute();
		}
	}

	private void joinConcurrentTasksInReverseOrderToEnableWorkStealing(
			Deque<ExclusiveTask> concurrentTasksInReverseOrder) {
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
			try (ResourceLock lock = testTask.getResourceLock().acquire()) {
				testTask.execute();
			}
			catch (InterruptedException e) {
				ExceptionUtils.throwAsUncheckedException(e);
			}
		}

	}

	static class WorkerThreadFactory implements ForkJoinPool.ForkJoinWorkerThreadFactory {

		private final ClassLoader contextClassLoader = Thread.currentThread().getContextClassLoader();

		@Override
		public ForkJoinWorkerThread newThread(ForkJoinPool pool) {
			return new WorkerThread(pool, contextClassLoader);
		}
	}

	static class WorkerThread extends ForkJoinWorkerThread {

		WorkerThread(ForkJoinPool pool, ClassLoader contextClassLoader) {
			super(pool);
			setContextClassLoader(contextClassLoader);
		}
	}

}
