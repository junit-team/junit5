/*
 * Copyright 2015-2021 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * https://www.eclipse.org/legal/epl-v20.html
 */

package org.junit.platform.engine.support.hierarchical;

import static org.apiguardian.api.API.Status.EXPERIMENTAL;

import java.util.concurrent.ForkJoinPool;
import java.util.function.Predicate;

import org.apiguardian.api.API;

/**
 * Configuration to use for parallel test execution.
 *
 * <p>Instances of this class are intended to be used to configure
 * implementations of {@link HierarchicalTestExecutorService}. Such
 * implementations may use all of the properties in this class or
 * only a subset.
 *
 * @since 1.3
 * @see ForkJoinPoolHierarchicalTestExecutorService
 * @see ParallelExecutionConfigurationStrategy
 * @see DefaultParallelExecutionConfigurationStrategy
 */
@API(status = EXPERIMENTAL, since = "1.3")
public interface ParallelExecutionConfiguration {

	/**
	 * Get the parallelism to be used.
	 *
	 * @see ForkJoinPool#getParallelism()
	 */
	int getParallelism();

	/**
	 * Get the minimum number of runnable threads to be used.
	 */
	int getMinimumRunnable();

	/**
	 * Get the maximum thread pool size to be used.
	 */
	int getMaxPoolSize();

	/**
	 * Get the core thread pool size to be used.
	 */
	int getCorePoolSize();

	/**
	 * Get the number of seconds for which inactive threads should be kept alive
	 * before terminating them and shrinking the thread pool.
	 */
	int getKeepAliveSeconds();

	/**
	 * Get the predicate called when the {@code ForkJoinPool} is saturated.
	 * This occurs when the ForkJoinPool has reached MaxPoolSize
	 * and there are no worker threads available. The predicate
	 * should return true if the calling thread should block.
	 * If the predicate is null or returns false a RejectedExcecutionException
	 * is thrown.
	 * @return a Predicate accepting a ForkJoinPool as the parameter or null.
	 * @since 1.9
	 * @API(since = "1.9")
	 */
	Predicate<? super ForkJoinPool> getSaturatePredicate();

}
