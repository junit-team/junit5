/*
 * Copyright 2015-2020 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * https://www.eclipse.org/legal/epl-v20.html
 */

package org.junit.jupiter.engine.executor;

import static java.util.concurrent.CompletableFuture.completedFuture;
import static org.junit.platform.engine.support.hierarchical.Node.ExecutionMode.CONCURRENT;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.TimeUnit;

import org.junit.platform.commons.util.ExceptionUtils;
import org.junit.platform.engine.ConfigurationParameters;
import org.junit.platform.engine.support.hierarchical.DefaultParallelExecutionConfigurationStrategy;
import org.junit.platform.engine.support.hierarchical.ForkJoinPoolHierarchicalTestExecutorService;
import org.junit.platform.engine.support.hierarchical.HierarchicalTestExecutorService;
import org.junit.platform.engine.support.hierarchical.ResourceLock;

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
		var virtualThreadFactory = Thread.builder().virtual(forkJoinPool).name("junit-executor", 1).factory();
		executorService = Executors.newUnboundedExecutor(virtualThreadFactory);
	}

	@SuppressWarnings("try")
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
		try (ResourceLock lock = testTask.getResourceLock().acquire()) {
			testTask.execute();
		}
		catch (InterruptedException e) {
			ExceptionUtils.throwAsUncheckedException(e);
		}
	}

	@Override
	public void invokeAll(List<? extends TestTask> testTasks) {
		var futures = new ArrayList<CompletableFuture<?>>();
		var sequentialTasks = new ArrayList<TestTask>();
		for (var task : testTasks) {
			if (task.getExecutionMode() == CONCURRENT) {
				futures.add(submit(task));
			}
			else {
				sequentialTasks.add(task);
			}
		}
		for (var task : sequentialTasks) {
			futures.add(submit(task));
		}
		CompletableFuture.allOf(futures.toArray(CompletableFuture<?>[]::new)).join();
	}

	@Override
	public void close() {
		executorService.close();
		forkJoinPool.close();
	}
}
