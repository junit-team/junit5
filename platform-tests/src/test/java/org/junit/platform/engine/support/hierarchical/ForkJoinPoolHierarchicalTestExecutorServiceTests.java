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

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.params.provider.Arguments.arguments;
import static org.junit.platform.engine.support.hierarchical.ExclusiveResource.GLOBAL_READ;
import static org.junit.platform.engine.support.hierarchical.ExclusiveResource.GLOBAL_READ_WRITE;
import static org.junit.platform.engine.support.hierarchical.Node.ExecutionMode.CONCURRENT;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicReference;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.junit.jupiter.api.function.Executable;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.platform.commons.JUnitException;
import org.junit.platform.engine.support.hierarchical.ExclusiveResource.LockMode;
import org.junit.platform.engine.support.hierarchical.ForkJoinPoolHierarchicalTestExecutorService.TaskEventListener;
import org.junit.platform.engine.support.hierarchical.HierarchicalTestExecutorService.TestTask;
import org.junit.platform.engine.support.hierarchical.Node.ExecutionMode;

@Timeout(5)
class ForkJoinPoolHierarchicalTestExecutorServiceTests {

	@Test
	void exceptionsFromInvalidConfigurationAreNotSwallowed() {
		var configuration = new DefaultParallelExecutionConfiguration(2, 1, 1, 1, 0, __ -> true);

		JUnitException exception = assertThrows(JUnitException.class, () -> {
			try (var pool = new ForkJoinPoolHierarchicalTestExecutorService(configuration)) {
				assertNotNull(pool, "we won't get here");
			}
		});

		assertThat(exception).hasMessage("Failed to create ForkJoinPool");
		assertThat(exception).rootCause().isInstanceOf(IllegalArgumentException.class);
	}

	static List<Arguments> incompatibleLockCombinations() {
		return List.of(//
			arguments(//
				Set.of(GLOBAL_READ), //
				Set.of(GLOBAL_READ_WRITE) //
			), //
			arguments(//
				Set.of(new ExclusiveResource("a", LockMode.READ)), //
				Set.of(new ExclusiveResource("a", LockMode.READ_WRITE)) //
			), //
			arguments(//
				Set.of(new ExclusiveResource("a", LockMode.READ_WRITE)), //
				Set.of(new ExclusiveResource("a", LockMode.READ_WRITE)) //
			), //
			arguments(//
				Set.of(GLOBAL_READ, new ExclusiveResource("a", LockMode.READ_WRITE)), //
				Set.of(GLOBAL_READ, new ExclusiveResource("b", LockMode.READ_WRITE)) //
			), //
			arguments(//
				Set.of(new ExclusiveResource("b", LockMode.READ)), //
				Set.of(new ExclusiveResource("a", LockMode.READ)) //
			), //
			arguments(//
				Set.of(GLOBAL_READ, new ExclusiveResource("a", LockMode.READ_WRITE)), //
				Set.of(GLOBAL_READ, new ExclusiveResource("a", LockMode.READ)) //
			), //
			arguments(//
				Set.of(GLOBAL_READ_WRITE), //
				Set.of(GLOBAL_READ) //
			), //
			arguments(//
				Set.of(GLOBAL_READ, new ExclusiveResource("a", LockMode.READ),
					new ExclusiveResource("b", LockMode.READ), new ExclusiveResource("d", LockMode.READ)),
				//
				Set.of(GLOBAL_READ, new ExclusiveResource("a", LockMode.READ),
					new ExclusiveResource("c", LockMode.READ)) //
			)//
		);
	}

	@ParameterizedTest
	@MethodSource("incompatibleLockCombinations")
	void defersTasksWithIncompatibleLocks(Set<ExclusiveResource> initialResources,
			Set<ExclusiveResource> incompatibleResources) throws Exception {

		var lockManager = new LockManager();
		var initialLock = lockManager.getLockForResources(initialResources);
		var incompatibleLock = lockManager.getLockForResources(incompatibleResources);

		var deferred = new CountDownLatch(1);
		var deferredTask = new AtomicReference<TestTask>();

		TaskEventListener taskEventListener = testTask -> {
			deferredTask.set(testTask);
			deferred.countDown();
		};

		var incompatibleTask = new DummyTestTask("incompatibleTask", incompatibleLock);

		var tasks = runWithAttemptedWorkStealing(taskEventListener, incompatibleTask, initialLock, () -> {
			try {
				deferred.await();
			}
			catch (InterruptedException e) {
				System.out.println("Interrupted while waiting for task to be deferred");
			}
		});

		assertEquals(incompatibleTask, deferredTask.get());
		assertEquals(tasks.get("nestedTask").threadName, tasks.get("leafTask2").threadName);
		assertNotEquals(tasks.get("leafTask1").threadName, tasks.get("leafTask2").threadName);
	}

	static List<Arguments> compatibleLockCombinations() {
		return List.of(//
			arguments(//
				Set.of(GLOBAL_READ), //
				Set.of(new ExclusiveResource("a", LockMode.READ)) //
			), //
			arguments(//
				Set.of(GLOBAL_READ), //
				Set.of(GLOBAL_READ, new ExclusiveResource("a", LockMode.READ)) //
			), //
			arguments(//
				Set.of(GLOBAL_READ, new ExclusiveResource("a", LockMode.READ)), //
				Set.of(GLOBAL_READ, new ExclusiveResource("a", LockMode.READ),
					new ExclusiveResource("b", LockMode.READ), new ExclusiveResource("c", LockMode.READ)) //
			), //
			arguments(//
				Set.of(GLOBAL_READ, new ExclusiveResource("a", LockMode.READ)), //
				Set.of(GLOBAL_READ, new ExclusiveResource("b", LockMode.READ)) //
			), //
			arguments(//
				Set.of(GLOBAL_READ, new ExclusiveResource("a", LockMode.READ)), //
				Set.of(new ExclusiveResource("a", LockMode.READ), new ExclusiveResource("b", LockMode.READ),
					new ExclusiveResource("c", LockMode.READ)) //
			)//
		);
	}

	@ParameterizedTest
	@MethodSource("compatibleLockCombinations")
	void canWorkStealTaskWithCompatibleLocks(Set<ExclusiveResource> initialResources,
			Set<ExclusiveResource> compatibleResources) throws Exception {

		var lockManager = new LockManager();
		var initialLock = lockManager.getLockForResources(initialResources);
		var compatibleLock = lockManager.getLockForResources(compatibleResources);

		var deferredTask = new AtomicReference<TestTask>();

		var workStolen = new CountDownLatch(1);
		var compatibleTask = new DummyTestTask("compatibleTask", compatibleLock, workStolen::countDown);

		var tasks = runWithAttemptedWorkStealing(deferredTask::set, compatibleTask, initialLock, () -> {
			try {
				workStolen.await();
			}
			catch (InterruptedException e) {
				System.out.println("Interrupted while waiting for work to be stolen");
			}
		});

		assertNull(deferredTask.get());
		assertEquals(tasks.get("nestedTask").threadName, tasks.get("leafTask2").threadName);
		assertNotEquals(tasks.get("leafTask1").threadName, tasks.get("leafTask2").threadName);
	}

	private static Map<String, DummyTestTask> runWithAttemptedWorkStealing(TaskEventListener taskEventListener,
			DummyTestTask taskToBeStolen, ResourceLock initialLock, Runnable waitAction)
			throws InterruptedException, ExecutionException {

		var tasks = new HashMap<String, DummyTestTask>();
		tasks.put(taskToBeStolen.identifier, taskToBeStolen);

		var configuration = new DefaultParallelExecutionConfiguration(2, 2, 2, 2, 1, __ -> true);

		try (var pool = new ForkJoinPoolHierarchicalTestExecutorService(configuration, taskEventListener)) {

			var extraTask = pool.new ExclusiveTask(taskToBeStolen);
			var bothLeafTasksAreRunning = new CountDownLatch(2);
			var nestedTask = new DummyTestTask("nestedTask", initialLock, () -> {
				var leafTask1 = new DummyTestTask("leafTask1", NopLock.INSTANCE, () -> {
					extraTask.fork();
					bothLeafTasksAreRunning.countDown();
					bothLeafTasksAreRunning.await();
					waitAction.run();
				});
				tasks.put(leafTask1.identifier, leafTask1);
				var leafTask2 = new DummyTestTask("leafTask2", NopLock.INSTANCE, () -> {
					bothLeafTasksAreRunning.countDown();
					bothLeafTasksAreRunning.await();
				});
				tasks.put(leafTask2.identifier, leafTask2);

				pool.invokeAll(List.of(leafTask1, leafTask2));
			});
			tasks.put(nestedTask.identifier, nestedTask);

			pool.submit(nestedTask).get();
			extraTask.join();
		}

		return tasks;
	}

	static final class DummyTestTask implements TestTask {

		private final String identifier;
		private final ResourceLock resourceLock;
		private final Executable action;

		private String threadName;

		DummyTestTask(String identifier, ResourceLock resourceLock) {
			this(identifier, resourceLock, () -> {
			});
		}

		DummyTestTask(String identifier, ResourceLock resourceLock, Executable action) {
			this.identifier = identifier;
			this.resourceLock = resourceLock;
			this.action = action;
		}

		@Override
		public ExecutionMode getExecutionMode() {
			return CONCURRENT;
		}

		@Override
		public ResourceLock getResourceLock() {
			return resourceLock;
		}

		@Override
		public void execute() {
			threadName = Thread.currentThread().getName();
			try {
				action.execute();
			}
			catch (Throwable e) {
				throw new RuntimeException("Action " + identifier + " failed", e);
			}
		}

		@Override
		public String toString() {
			return identifier;
		}
	}
}
