/*
 * Copyright 2015-2023 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * https://www.eclipse.org/legal/epl-v20.html
 */

package org.junit.platform.engine.support.hierarchical;

import static java.util.concurrent.CompletableFuture.completedFuture;
import static java.util.function.Predicate.isEqual;
import static java.util.stream.Collectors.toCollection;
import static org.junit.platform.commons.util.FunctionUtils.where;
import static org.junit.platform.engine.support.hierarchical.Node.ExecutionMode.CONCURRENT;
import static org.junit.platform.engine.support.hierarchical.Node.ExecutionMode.SAME_THREAD;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

import org.junit.platform.commons.util.ExceptionUtils;
import org.junit.platform.engine.ConfigurationParameters;
import org.junit.platform.engine.support.hierarchical.Node.ExecutionMode;

class VirtualThreadHierarchicalTestExecutorService implements HierarchicalTestExecutorService {

	private final ClassLoader contextClassLoader;
	private final ForkJoinPool forkJoinPool;
	private final ExecutorService executorService;

	VirtualThreadHierarchicalTestExecutorService(ConfigurationParameters configurationParameters) {
		contextClassLoader = Thread.currentThread().getContextClassLoader();
		var strategy = DefaultParallelExecutionConfigurationStrategy.getStrategy(configurationParameters);
		var configuration = strategy.createConfiguration(configurationParameters);
		var systemThreadFactory = new ForkJoinPoolHierarchicalTestExecutorService.WorkerThreadFactory();
		forkJoinPool = new ForkJoinPool(configuration.getParallelism(), systemThreadFactory, null, false,
			configuration.getCorePoolSize(), configuration.getMaxPoolSize(), configuration.getMinimumRunnable(), null,
			configuration.getKeepAliveSeconds(), TimeUnit.SECONDS);
		var virtualThreadFactory = Thread.ofVirtual().name("junit-executor", 1).factory();
		executorService = Executors.newThreadPerTaskExecutor(virtualThreadFactory);
	}

	@Override
	public CompletableFuture<Void> submit(TestTask testTask) {
		if (testTask.getExecutionMode() == CONCURRENT) {
			return CompletableFuture.runAsync(() -> executeWithLocksAndContextClassLoader(testTask), executorService);
		}
		executeWithLocks(testTask);
		return completedFuture(null);
	}

	private void executeWithLocksAndContextClassLoader(TestTask testTask) {
		Thread.currentThread().setContextClassLoader(contextClassLoader);
		executeWithLocks(testTask);
	}

	private void executeWithLocks(TestTask testTask) {
		var lock = testTask.getResourceLock();
		try {
			lock.acquire();
			testTask.execute();
		}
		catch (InterruptedException e) {
			ExceptionUtils.throwAsUncheckedException(e);
		}
		finally {
			lock.release();
		}
	}

	@Override
	public void invokeAll(List<? extends TestTask> testTasks) {
		var futures = submitAll(testTasks, CONCURRENT).collect(toCollection(ArrayList::new));
		submitAll(testTasks, SAME_THREAD).forEach(futures::add);
		allOf(futures).join();
	}

	private Stream<CompletableFuture<Void>> submitAll(List<? extends TestTask> testTasks, ExecutionMode mode) {
		return testTasks.stream().filter(where(TestTask::getExecutionMode, isEqual(mode))).map(this::submit);
	}

	private CompletableFuture<Void> allOf(List<CompletableFuture<Void>> futures) {
		return CompletableFuture.allOf(futures.toArray(CompletableFuture[]::new));
	}

	@Override
	public void close() {
		executorService.close();
		forkJoinPool.close();
	}
}
