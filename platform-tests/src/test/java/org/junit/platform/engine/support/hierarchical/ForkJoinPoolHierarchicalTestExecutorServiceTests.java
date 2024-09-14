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
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.platform.engine.support.hierarchical.ExclusiveResource.GLOBAL_READ;
import static org.junit.platform.engine.support.hierarchical.ExclusiveResource.GLOBAL_READ_WRITE;
import static org.junit.platform.engine.support.hierarchical.Node.ExecutionMode.CONCURRENT;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicReference;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.junit.jupiter.api.function.ThrowingConsumer;
import org.junit.platform.commons.JUnitException;
import org.junit.platform.engine.support.hierarchical.ForkJoinPoolHierarchicalTestExecutorService.TaskEventListener;
import org.junit.platform.engine.support.hierarchical.HierarchicalTestExecutorService.TestTask;
import org.junit.platform.engine.support.hierarchical.Node.ExecutionMode;

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

	@Test
	@Timeout(5)
	void defersTasksWithIncompatibleLocks() throws Exception {
		var configuration = new DefaultParallelExecutionConfiguration(2, 2, 3, 1, 1, __ -> true);

		var lockManager = new LockManager();
		var globalReadLock = lockManager.getLockForResource(GLOBAL_READ);
		var globalReadWriteLock = lockManager.getLockForResource(GLOBAL_READ_WRITE);
		var nopLock = NopLock.INSTANCE;

		var threadNamesByTaskIdentifier = new ConcurrentHashMap<String, String>();
		var deferred = new CountDownLatch(1);
		var deferredTask = new AtomicReference<TestTask>();

		TaskEventListener taskEventListener = testTask -> {
			deferredTask.set(testTask);
			deferred.countDown();
		};

		var isolatedTask = new DummyTestTask("isolatedTask", globalReadWriteLock,
			t -> threadNamesByTaskIdentifier.put(t.identifier(), Thread.currentThread().getName()));

		try (var pool = new ForkJoinPoolHierarchicalTestExecutorService(configuration, taskEventListener)) {

			var bothLeafTasksAreRunning = new CountDownLatch(2);
			var nestedTask = new DummyTestTask("nestedTask", globalReadLock, t -> {
				threadNamesByTaskIdentifier.put(t.identifier(), Thread.currentThread().getName());
				var leafTask1 = new DummyTestTask("leafTask1", nopLock, t1 -> {
					threadNamesByTaskIdentifier.put(t1.identifier(), Thread.currentThread().getName());
					bothLeafTasksAreRunning.countDown();
					bothLeafTasksAreRunning.await();
					pool.new ExclusiveTask(isolatedTask).fork();
					deferred.await();
				});
				var leafTask2 = new DummyTestTask("leafTask2", nopLock, t2 -> {
					threadNamesByTaskIdentifier.put(t2.identifier(), Thread.currentThread().getName());
					bothLeafTasksAreRunning.countDown();
					bothLeafTasksAreRunning.await();
				});
				pool.invokeAll(List.of(leafTask1, leafTask2));
			});

			pool.submit(nestedTask).get();
		}

		assertEquals(isolatedTask, deferredTask.get());
		assertEquals(threadNamesByTaskIdentifier.get("nestedTask"), threadNamesByTaskIdentifier.get("leafTask2"));
		assertNotEquals(threadNamesByTaskIdentifier.get("leafTask1"), threadNamesByTaskIdentifier.get("leafTask2"));
	}

	record DummyTestTask(String identifier, ResourceLock resourceLock, ThrowingConsumer<DummyTestTask> action)
			implements TestTask {
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
			try {
				action.accept(this);
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
