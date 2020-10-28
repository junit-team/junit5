/*
 * Copyright 2015-2020 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * https://www.eclipse.org/legal/epl-v20.html
 */

package org.junit.platform.engine.support.hierarchical;

import java.lang.Thread.UncaughtExceptionHandler;
import java.lang.reflect.Constructor;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.ForkJoinPool.ForkJoinWorkerThreadFactory;
import java.util.concurrent.ForkJoinWorkerThread;
import java.util.concurrent.TimeUnit;
import java.util.function.Predicate;

import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;
import org.junit.platform.commons.JUnitException;
import org.junit.platform.commons.function.Try;

/**
 * A factory for creating {@link ForkJoinPool ForkJoinPools} given a {@link ParallelExecutionConfiguration}.
 *
 * @since 5.8
 * @see ParallelExecutionConfigurationStrategy
 */
@API(status = Status.INTERNAL, since = "5.8")
public class ForkJoinPoolFactory {

	public static ForkJoinPool create(ParallelExecutionConfiguration configuration) {
		ForkJoinWorkerThreadFactory threadFactory = new WorkerThreadFactory();
		return Try.call(() -> {
			// Try to use constructor available in Java >= 9
			Constructor<ForkJoinPool> constructor = ForkJoinPool.class.getDeclaredConstructor(Integer.TYPE,
				ForkJoinWorkerThreadFactory.class, UncaughtExceptionHandler.class, Boolean.TYPE, Integer.TYPE,
				Integer.TYPE, Integer.TYPE, Predicate.class, Long.TYPE, TimeUnit.class);
			return constructor.newInstance(configuration.getParallelism(), threadFactory, null, false,
				configuration.getCorePoolSize(), configuration.getMaxPoolSize(), configuration.getMinimumRunnable(),
				null, configuration.getKeepAliveSeconds(), TimeUnit.SECONDS);
		}).orElseTry(() -> {
			// Fallback for Java 8
			return new ForkJoinPool(configuration.getParallelism(), threadFactory, null, false);
		}).getOrThrow(cause -> new JUnitException("Failed to create ForkJoinPool", cause));
	}

	static class WorkerThreadFactory implements ForkJoinPool.ForkJoinWorkerThreadFactory {

		private final ClassLoader contextClassLoader = Thread.currentThread().getContextClassLoader();

		@Override
		public ForkJoinWorkerThread newThread(ForkJoinPool pool) {
			return new WorkerThread(pool, contextClassLoader);
		}
	}

	static class WorkerThread extends ForkJoinWorkerThread {

		WorkerThread(ForkJoinPool pool, ClassLoader contextClassLoader) {
			super(pool);
			setContextClassLoader(contextClassLoader);
		}
	}

}
