/*
 * Copyright 2015-2017 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v20.html
 */

package org.junit.platform.engine.support.hierarchical;

import static java.util.concurrent.CompletableFuture.completedFuture;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.Future;

public class ForkJoinPoolHierarchicalTestExecutorService<C extends EngineExecutionContext>
		implements HierarchicalTestExecutorService<C> {

	private final LockManager lockManager = new LockManager();

	private final ForkJoinPool forkJoinPool;

	public ForkJoinPoolHierarchicalTestExecutorService() {
		forkJoinPool = new ForkJoinPool();
	}

	@Override
	public Future<Void> submit(TestTask<C> testTask) {
		List<ExclusiveResource> resources = testTask.getExclusiveResources();
		ResourceLock resourceLock = lockManager.getLockForResources(resources);
		ExclusiveTask<Void> exclusiveTask = new ExclusiveTask<>(resourceLock, () -> {
			testTask.execute();
			return null;
		});

		switch (testTask.getExecutionMode()) {
			case Concurrent:
				return forkJoinPool.submit(exclusiveTask);

			case SameThread:
			default:
				try {
					exclusiveTask.call();
					return completedFuture(null);
				}
				catch (Exception e) {
					CompletableFuture<Void> exceptionFuture = new CompletableFuture<>();
					exceptionFuture.completeExceptionally(e);
					return exceptionFuture;
				}
		}
	}

	@Override
	public void close() {
		forkJoinPool.shutdownNow();
	}

}
