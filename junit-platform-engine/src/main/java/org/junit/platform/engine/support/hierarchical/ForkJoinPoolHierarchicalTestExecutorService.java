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

import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.Future;

public class ForkJoinPoolHierarchicalTestExecutorService<C extends EngineExecutionContext>
		implements HierarchicalTestExecutorService<C> {

	private final ForkJoinPool forkJoinPool;

	public ForkJoinPoolHierarchicalTestExecutorService() {
		forkJoinPool = new ForkJoinPool();
	}

	@Override
	public Future<Void> submit(TestTask<C> testTask) {
		return forkJoinPool.submit(() -> {
			testTask.execute();
			return null;
		});
	}

	@Override
	public void close() {
		forkJoinPool.shutdownNow();
	}

}
