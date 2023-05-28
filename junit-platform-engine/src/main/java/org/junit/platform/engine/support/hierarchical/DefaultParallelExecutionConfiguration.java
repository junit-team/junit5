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

import java.util.concurrent.ForkJoinPool;
import java.util.function.Predicate;

/**
 * @since 1.3
 */
class DefaultParallelExecutionConfiguration implements ParallelExecutionConfiguration {

	private final int parallelism;
	private final int minimumRunnable;
	private final int maxPoolSize;
	private final int corePoolSize;
	private final int keepAliveSeconds;
	private final Predicate<? super ForkJoinPool> saturate;
	private final TestExecutor testExecutor;

	DefaultParallelExecutionConfiguration(int parallelism, int minimumRunnable, int maxPoolSize, int corePoolSize,
			int keepAliveSeconds, Predicate<? super ForkJoinPool> saturate, TestExecutor testExecutor) {
		this.parallelism = parallelism;
		this.minimumRunnable = minimumRunnable;
		this.maxPoolSize = maxPoolSize;
		this.corePoolSize = corePoolSize;
		this.keepAliveSeconds = keepAliveSeconds;
		this.saturate = saturate;
		this.testExecutor = testExecutor;
	}

	@Override
	public int getParallelism() {
		return parallelism;
	}

	@Override
	public int getMinimumRunnable() {
		return minimumRunnable;
	}

	@Override
	public int getMaxPoolSize() {
		return maxPoolSize;
	}

	@Override
	public int getCorePoolSize() {
		return corePoolSize;
	}

	@Override
	public int getKeepAliveSeconds() {
		return keepAliveSeconds;
	}

	@Override
	public Predicate<? super ForkJoinPool> getSaturatePredicate() {
		return saturate;
	}

	@Override
	public TestExecutor getTestExecutor() {
		return testExecutor;
	}
}
