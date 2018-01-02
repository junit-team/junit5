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

import java.util.List;
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
		return forkJoinPool.submit(() -> {
			List<ExclusiveResource> resources = testTask.getExclusiveResources();
			CompositeLock compositeLock = lockManager.getCompositeLock(resources);
			ForkJoinPool.managedBlock(new ForkJoinPool.ManagedBlocker() {
				private boolean acquired;

				@Override
				public boolean block() throws InterruptedException {
					compositeLock.acquire();
					acquired = true;
					return true;
				}

				@Override
				public boolean isReleasable() {
					return acquired;
				}
			});
			try {
				testTask.execute();
			}
			finally {
				compositeLock.release();
			}
			return null;
		});
	}

	@Override
	public void close() {
		forkJoinPool.shutdownNow();
	}

}
