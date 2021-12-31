/*
 * Copyright 2015-2021 the original author or authors.
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

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadPoolExecutor;

import org.apiguardian.api.API;
import org.junit.platform.commons.logging.LoggerFactory;
import org.junit.platform.commons.util.ExceptionUtils;
import org.junit.platform.engine.ConfigurationParameters;

/**
 * A {@link ThreadPoolExecutor}-based
 * {@linkplain HierarchicalTestExecutorService executor service} that executes
 * {@linkplain TestTask test tasks} with the configured parallelism.
 *
 * <p>This is an alternative to {@link ForkJoinPoolHierarchicalTestExecutorService} for usecases where using {@link java.util.concurrent.ForkJoinPool}
 * causes issues within your tests. (e.g. together with Selenium 4, see https://github.com/SeleniumHQ/selenium/issues/9359)</p>
 *
 * @since 1.9
 * @see ThreadPoolExecutor
 * @see DefaultParallelExecutionConfigurationStrategy
 */
@API(status = EXPERIMENTAL, since = "1.9")
public class ThreadPoolHierarchicalTestExecutorService implements HierarchicalTestExecutorService {

	private final ExecutorService executorService;
	private final int parallelism;

	/**
	 * Create a new {@code ForkJoinPoolHierarchicalTestExecutorService} based on
	 * the supplied {@link ConfigurationParameters}.
	 *
	 * @see DefaultParallelExecutionConfigurationStrategy
	 */
	public ThreadPoolHierarchicalTestExecutorService(ConfigurationParameters configurationParameters) {
		this(createConfiguration(configurationParameters));
	}

	/**
	 * Create a new {@code ForkJoinPoolHierarchicalTestExecutorService} based on
	 * the supplied {@link ParallelExecutionConfiguration}.
	 *
	 * @since 1.7
	 */
	@API(status = EXPERIMENTAL, since = "1.7")
	public ThreadPoolHierarchicalTestExecutorService(ParallelExecutionConfiguration configuration) {
		/*
		 * Reaching the exact defined parallelism with ThreadPoolExecutor is - as far as we currently know - tricky.
		 * This is because parent´s within the test-hierarchy may also consume threads from the thread-pool.
		 * Additional work is required to improve on this.
		 */
		executorService = Executors.newFixedThreadPool(
			configuration.getParallelism() + 1 /*, new WorkerThreadFactory()*/); // TODO: Plus one for the root JUnit-task? Do we have this one in all use-cases?
		parallelism = configuration.getParallelism();
		LoggerFactory.getLogger(getClass()).config(() -> "Using ThreadPoolExecutor with parallelism of " + parallelism);
	}

	private static ParallelExecutionConfiguration createConfiguration(ConfigurationParameters configurationParameters) {
		ParallelExecutionConfigurationStrategy strategy = DefaultParallelExecutionConfigurationStrategy.getStrategy(
			configurationParameters);
		return strategy.createConfiguration(configurationParameters);
	}

	@Override
	public Future<Void> submit(TestTask testTask) {
		ExclusiveTask<Void> exclusiveTask = new ExclusiveTask<>(testTask);

		if (testTask.getExecutionMode() == CONCURRENT) {
			return executorService.submit(exclusiveTask);
		}

		exclusiveTask.call();
		return completedFuture(null);
	}

	@Override
	public void invokeAll(List<? extends TestTask> tasks) {
		if (tasks.size() == 1) {
			tasks.get(0).execute();
			return;
		}
		List<ExclusiveTask<Void>> nonConcurrentTasks = new LinkedList<>();
		List<ExclusiveTask<Void>> concurrentTasksInReverseOrder = new LinkedList<>();
		splitTasks(tasks, nonConcurrentTasks, concurrentTasksInReverseOrder);
		executeNonConcurrentTasks(nonConcurrentTasks);
		executeConcurrentTasks(concurrentTasksInReverseOrder);
	}

	private void splitTasks(List<? extends TestTask> tasks, List<ExclusiveTask<Void>> nonConcurrentTasks,
			List<ExclusiveTask<Void>> concurrentTasksInReverseOrder) {
		for (TestTask testTask : tasks) {
			ExclusiveTask<Void> exclusiveTask = new ExclusiveTask<>(testTask);
			if (testTask.getExecutionMode() == CONCURRENT) {
				concurrentTasksInReverseOrder.add(0, exclusiveTask);
			}
			else {
				nonConcurrentTasks.add(exclusiveTask);
			}
		}
	}

	private void executeNonConcurrentTasks(List<ExclusiveTask<Void>> nonConcurrentTasks) {
		nonConcurrentTasks.forEach(ExclusiveTask::call);
	}

	private void executeConcurrentTasks(List<ExclusiveTask<Void>> concurrentTasks) {
		try {
			LoggerFactory.getLogger(getClass()).info(() -> "before invokeAll");
			List<Future<Void>> futures = executorService.invokeAll(concurrentTasks);
			LoggerFactory.getLogger(getClass()).info(() -> "after invokeAll");
		}
		catch (InterruptedException ex) {
			LoggerFactory.getLogger(getClass()).error(ex, () -> "error during executing concurrent tasks");
			throw new RuntimeException(ex);
		}
	}

	@Override
	public void close() {
		LoggerFactory.getLogger(getClass()).info(() -> "close");
		executorService.shutdownNow();
	}

	// this class cannot not be serialized because TestTask is not Serializable
	@SuppressWarnings("serial")
	static class ExclusiveTask<V> implements Callable<V> {

		private final TestTask testTask;

		ExclusiveTask(TestTask testTask) {
			this.testTask = testTask;
		}

		@SuppressWarnings("try")
		@Override
		public V call() {
			try (ResourceLock lock = testTask.getResourceLock().acquire()) {
				testTask.execute();
			}
			catch (InterruptedException e) {
				ExceptionUtils.throwAsUncheckedException(e);
			}
			return null;
		}
	}

	//	static class WorkerThreadFactory implements ThreadFactory {
	//
	//		private final ClassLoader contextClassLoader = Thread.currentThread().getContextClassLoader();
	//
	//		@Override
	//		public Thread newThread(Runnable runnable) {
	//			return new WorkerThread(runnable, contextClassLoader);
	//		}
	//	}
	//
	//	static class WorkerThread extends Thread {
	//
	//		WorkerThread(Runnable runnable, ClassLoader contextClassLoader) {
	//			super(runnable);
	//			setContextClassLoader(contextClassLoader);
	//		}
	//	}

}
