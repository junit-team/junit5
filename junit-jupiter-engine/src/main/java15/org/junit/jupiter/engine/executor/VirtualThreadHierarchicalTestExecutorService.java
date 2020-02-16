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

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

import org.junit.platform.engine.support.hierarchical.HierarchicalTestExecutorService;

class VirtualThreadHierarchicalTestExecutorService implements HierarchicalTestExecutorService {

	private final ExecutorService executorService;

	VirtualThreadHierarchicalTestExecutorService() {
		ThreadFactory threadFactory = Thread.builder().virtual().name("junit-executor", 1).factory();
		executorService = Executors.newUnboundedExecutor(threadFactory);
	}

	@Override
	public CompletableFuture<Void> submit(TestTask testTask) {
		return CompletableFuture.runAsync(testTask::execute, executorService);
	}

	@Override
	public void invokeAll(List<? extends TestTask> testTasks) {
		CompletableFuture.allOf(testTasks.stream().map(this::submit).toArray(CompletableFuture<?>[]::new)).join();
	}

	@Override
	public void close() {
		executorService.close();
	}
}
