/*
 * Copyright 2015-2018 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v20.html
 */

package org.junit.platform.engine.support.hierarchical;

/**
 * @since 1.3
 */
class DefaultParallelExecutionConfiguration implements ParallelExecutionConfiguration {

	private final int parallelism;
	private final int minimumRunnable;
	private final int maxPoolSize;
	private final int corePoolSize;
	private final int keepAlive;

	DefaultParallelExecutionConfiguration(int parallelism, int minimumRunnable, int maxPoolSize, int corePoolSize,
			int keepAlive) {
		this.parallelism = parallelism;
		this.minimumRunnable = minimumRunnable;
		this.maxPoolSize = maxPoolSize;
		this.corePoolSize = corePoolSize;
		this.keepAlive = keepAlive;
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
	public int getKeepAlive() {
		return keepAlive;
	}
}
