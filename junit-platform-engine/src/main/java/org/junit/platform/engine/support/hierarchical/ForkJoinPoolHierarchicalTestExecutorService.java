/*
 * Copyright 2015-2024 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * https://www.eclipse.org/legal/epl-v20.html
 */

package org.junit.platform.engine.support.hierarchical;

import static java.util.concurrent.CompletableFuture.completedFuture;
import static org.apiguardian.api.API.Status.STABLE;
import static org.junit.platform.engine.support.hierarchical.ExclusiveResource.GLOBAL_READ_WRITE;
import static org.junit.platform.engine.support.hierarchical.Node.ExecutionMode.CONCURRENT;
import static org.junit.platform.engine.support.hierarchical.Node.ExecutionMode.SAME_THREAD;

import java.lang.Thread.UncaughtExceptionHandler;
import java.lang.reflect.Constructor;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.Callable;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.ForkJoinPool.ForkJoinWorkerThreadFactory;
import java.util.concurrent.ForkJoinTask;
import java.util.concurrent.ForkJoinWorkerThread;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
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
 * @since 1.3
 * @see ForkJoinPool
 * @see DefaultParallelExecutionConfigurationStrategy
 */
@API(status = STABLE, since = "1.10")
public class ForkJoinPoolHierarchicalTestExecutorService implements HierarchicalTestExecutorService {

	// package-private for testing
	final ForkJoinPool forkJoinPool;

	private final TaskEventListener taskEventListener;
	private final int parallelism;
	private final ThreadLocal<ThreadLock> threadLocks = ThreadLocal.withInitial(ThreadLock::new);

	/**
	 * Create a new {@code ForkJoinPoolHierarchicalTestExecutorService} based on
	 * the supplied {@link ConfigurationParameters}.
	 *
	 * @see DefaultParallelExecutionConfigurationStrategy
	 */
	public ForkJoinPoolHierarchicalTestExecutorService(ConfigurationParameters configurationParameters) {
		this(createConfiguration(configurationParameters));
	}

	/**
	 * Create a new {@code ForkJoinPoolHierarchicalTestExecutorService} based on
	 * the supplied {@link ParallelExecutionConfiguration}.
	 *
	 * @since 1.7
	 */
	@API(status = STABLE, since = "1.10")
	public ForkJoinPoolHierarchicalTestExecutorService(ParallelExecutionConfiguration configuration) {
		this(configuration, TaskEventListener.NOOP);
	}

	ForkJoinPoolHierarchicalTestExecutorService(ParallelExecutionConfiguration configuration,
			TaskEventListener taskEventListener) {
		forkJoinPool = createForkJoinPool(configuration);
		this.taskEventListener = taskEventListener;
		parallelism = forkJoinPool.getParallelism();
		LoggerFactory.getLogger(getClass()).config(() -> "Using ForkJoinPool with parallelism of " + parallelism);
	}

	private static ParallelExecutionConfiguration createConfiguration(ConfigurationParameters configurationParameters) {
		ParallelExecutionConfigurationStrategy strategy = DefaultParallelExecutionConfigurationStrategy.getStrategy(
			configurationParameters);
		return strategy.createConfiguration(configurationParameters);
	}

	private ForkJoinPool createForkJoinPool(ParallelExecutionConfiguration configuration) {
		ForkJoinWorkerThreadFactory threadFactory = new WorkerThreadFactory();
		// Try to use constructor available in Java >= 9
		Callable<ForkJoinPool> constructorInvocation = sinceJava9Constructor() //
				.map(sinceJava9ConstructorInvocation(configuration, threadFactory))
				// Fallback for Java 8
				.orElse(sinceJava7ConstructorInvocation(configuration, threadFactory));
		return Try.call(constructorInvocation) //
				.getOrThrow(cause -> new JUnitException("Failed to create ForkJoinPool", cause));
	}

	private static Optional<Constructor<ForkJoinPool>> sinceJava9Constructor() {
		return Try.call(() -> ForkJoinPool.class.getDeclaredConstructor(int.class, ForkJoinWorkerThreadFactory.class,
			UncaughtExceptionHandler.class, boolean.class, int.class, int.class, int.class, Predicate.class, long.class,
			TimeUnit.class)) //
				.toOptional();
	}

	private static Function<Constructor<ForkJoinPool>, Callable<ForkJoinPool>> sinceJava9ConstructorInvocation(
			ParallelExecutionConfiguration configuration, ForkJoinWorkerThreadFactory threadFactory) {
		return constructor -> () -> constructor.newInstance(configuration.getParallelism(), threadFactory, null, false,
			configuration.getCorePoolSize(), configuration.getMaxPoolSize(), configuration.getMinimumRunnable(),
			configuration.getSaturatePredicate(), configuration.getKeepAliveSeconds(), TimeUnit.SECONDS);
	}

	private static Callable<ForkJoinPool> sinceJava7ConstructorInvocation(ParallelExecutionConfiguration configuration,
			ForkJoinWorkerThreadFactory threadFactory) {
		return () -> new ForkJoinPool(configuration.getParallelism(), threadFactory, null, false);
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
		exclusiveTask.execSync();
		return completedFuture(null);
	}

	private boolean isAlreadyRunningInForkJoinPool() {
		return ForkJoinTask.getPool() == forkJoinPool;
	}

	@Override
	public void invokeAll(List<? extends TestTask> tasks) {
		if (tasks.size() == 1) {
			new ExclusiveTask(tasks.get(0)).execSync();
			return;
		}
		Deque<ExclusiveTask> isolatedTasks = new LinkedList<>();
		Deque<ExclusiveTask> sameThreadTasks = new LinkedList<>();
		Deque<ExclusiveTask> concurrentTasksInReverseOrder = new LinkedList<>();
		forkConcurrentTasks(tasks, isolatedTasks, sameThreadTasks, concurrentTasksInReverseOrder);
		executeSync(sameThreadTasks);
		joinConcurrentTasksInReverseOrderToEnableWorkStealing(concurrentTasksInReverseOrder);
		executeSync(isolatedTasks);
	}

	private void forkConcurrentTasks(List<? extends TestTask> tasks, Deque<ExclusiveTask> isolatedTasks,
			Deque<ExclusiveTask> sameThreadTasks, Deque<ExclusiveTask> concurrentTasksInReverseOrder) {
		for (TestTask testTask : tasks) {
			ExclusiveTask exclusiveTask = new ExclusiveTask(testTask);
			if (requiresGlobalReadWriteLock(testTask)) {
				isolatedTasks.add(exclusiveTask);
			}
			else if (testTask.getExecutionMode() == SAME_THREAD) {
				sameThreadTasks.add(exclusiveTask);
			}
			else {
				exclusiveTask.fork();
				concurrentTasksInReverseOrder.addFirst(exclusiveTask);
			}
		}
	}

	private static boolean requiresGlobalReadWriteLock(TestTask testTask) {
		return testTask.getResourceLock().getResources().contains(GLOBAL_READ_WRITE);
	}

	private void executeSync(Deque<ExclusiveTask> tasks) {
		for (ExclusiveTask task : tasks) {
			task.execSync();
		}
	}

	private void joinConcurrentTasksInReverseOrderToEnableWorkStealing(
			Deque<ExclusiveTask> concurrentTasksInReverseOrder) {
		for (ExclusiveTask forkedTask : concurrentTasksInReverseOrder) {
			forkedTask.join();
			resubmitDeferredTasks();
		}
	}

	private void resubmitDeferredTasks() {
		List<ExclusiveTask> deferredTasks = threadLocks.get().deferredTasks;
		for (ExclusiveTask deferredTask : deferredTasks) {
			if (!deferredTask.isDone()) {
				deferredTask.fork();
			}
		}
		deferredTasks.clear();
	}

	@Override
	public void close() {
		forkJoinPool.shutdownNow();
	}

	// this class cannot not be serialized because TestTask is not Serializable
	@SuppressWarnings({ "serial", "RedundantSuppression" })
	class ExclusiveTask extends ForkJoinTask<Void> {

		private final TestTask testTask;

		ExclusiveTask(TestTask testTask) {
			this.testTask = testTask;
		}

		/**
		 * Always returns {@code null}.
		 *
		 * @return {@code null} always
		 */
		public final Void getRawResult() {
			return null;
		}

		/**
		 * Requires null completion value.
		 */
		protected final void setRawResult(Void mustBeNull) {
		}

		void execSync() {
			boolean completed = exec();
			if (!completed) {
				throw new IllegalStateException(
					"Task was deferred but should have been executed synchronously: " + testTask);
			}
		}

		@SuppressWarnings("try")
		@Override
		public boolean exec() {
			// Check if this task is compatible with the current resource lock, if there is any.
			// If not, we put this task in the thread local as a deferred task
			// and let the worker thread fork it once it is done with the current task.
			ResourceLock resourceLock = testTask.getResourceLock();
			ThreadLock threadLock = threadLocks.get();
			if (!threadLock.areAllHeldLocksCompatibleWith(resourceLock)) {
				threadLock.addDeferredTask(this);
				taskEventListener.deferred(testTask);
				// Return false to indicate that this task is not done yet
				// this means that .join() will wait.
				return false;
			}
			try ( //
					ResourceLock lock = resourceLock.acquire(); //
					@SuppressWarnings("unused")
					ThreadLock.NestedResourceLock nested = threadLock.withNesting(lock) //
			) {
				testTask.execute();
				return true;
			}
			catch (InterruptedException e) {
				throw ExceptionUtils.throwAsUncheckedException(e);
			}
		}

		@Override
		public String toString() {
			return "ExclusiveTask [" + testTask + "]";
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

	static class ThreadLock {
		private final Deque<ResourceLock> locks = new ArrayDeque<>(2);
		private final List<ExclusiveTask> deferredTasks = new ArrayList<>();

		void addDeferredTask(ExclusiveTask task) {
			deferredTasks.add(task);
		}

		NestedResourceLock withNesting(ResourceLock lock) {
			locks.push(lock);
			return locks::pop;
		}

		boolean areAllHeldLocksCompatibleWith(ResourceLock lock) {
			return locks.stream().allMatch(l -> l.isCompatible(lock));
		}

		interface NestedResourceLock extends AutoCloseable {
			@Override
			void close();
		}
	}

	interface TaskEventListener {

		TaskEventListener NOOP = __ -> {
		};

		void deferred(TestTask testTask);
	}

}
