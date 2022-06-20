/*
 * Copyright 2015-2022 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * https://www.eclipse.org/legal/epl-v20.html
 */

package org.junit.jupiter.engine.extension;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;

import org.junit.jupiter.api.Timeout.ThreadMode;
import org.junit.jupiter.api.extension.ExtensionContext.Store;
import org.junit.jupiter.api.extension.ExtensionContext.Store.CloseableResource;
import org.junit.jupiter.api.extension.InvocationInterceptor.Invocation;
import org.junit.platform.commons.JUnitException;
import org.junit.platform.commons.util.Preconditions;

/**
 * @since 5.9
 */
class TimeoutInvocationFactory {

	private final Store store;

	TimeoutInvocationFactory(Store store) {
		this.store = Preconditions.notNull(store, "store must not be null");
	}

	<T> Invocation<T> create(ThreadMode threadMode, TimeoutInvocationParameters<T> timeoutInvocationParameters) {
		Preconditions.notNull(threadMode, "thread mode must not be null");
		Preconditions.notNull(timeoutInvocationParameters, "timeout invocation parameters must not be null");
		if (threadMode == ThreadMode.SEPARATE_THREAD) {
			return new SeparateThreadTimeoutInvocation<>(timeoutInvocationParameters.getInvocation(),
				timeoutInvocationParameters.getTimeoutDuration(), getThreadExecutorForSeparateThreadInvocation(),
				timeoutInvocationParameters.getDescriptionSupplier());
		}
		return new SameThreadTimeoutInvocation<>(timeoutInvocationParameters.getInvocation(),
			timeoutInvocationParameters.getTimeoutDuration(), getThreadExecutorForSameThreadInvocation(),
			timeoutInvocationParameters.getDescriptionSupplier());
	}

	private ScheduledExecutorService getThreadExecutorForSameThreadInvocation() {
		return store.getOrComputeIfAbsent(ExecutorResource.class).get();
	}

	private ScheduledExecutorService getThreadExecutorForSeparateThreadInvocation() {
		return Executors.newSingleThreadScheduledExecutor(new SeparateThreadFactory());
	}

	static class ExecutorResource implements CloseableResource {

		private final ScheduledExecutorService executor;

		ExecutorResource() {
			this.executor = Executors.newSingleThreadScheduledExecutor(runnable -> {
				Thread thread = new Thread(runnable, "junit-jupiter-timeout-watcher");
				thread.setPriority(Thread.MAX_PRIORITY);
				return thread;
			});
		}

		ScheduledExecutorService get() {
			return executor;
		}

		@Override
		public void close() throws Throwable {
			executor.shutdown();
			boolean terminated = executor.awaitTermination(5, TimeUnit.SECONDS);
			if (!terminated) {
				executor.shutdownNow();
				throw new JUnitException("Scheduled executor could not be stopped in an orderly manner");
			}
		}
	}

	private static class SeparateThreadFactory implements ThreadFactory {
		private static final AtomicInteger separateThreadNumber = new AtomicInteger(1);

		@Override
		public Thread newThread(Runnable runnable) {
			Thread thread = new Thread(runnable,
				"junit-jupiter-timeout-invocation-runner-" + separateThreadNumber.getAndIncrement());
			thread.setPriority(Thread.MAX_PRIORITY);
			return thread;
		}
	}

	static class TimeoutInvocationParameters<T> {

		private final Invocation<T> invocation;
		private final TimeoutDuration timeout;
		private final Supplier<String> descriptionSupplier;

		TimeoutInvocationParameters(Invocation<T> invocation, TimeoutDuration timeout,
				Supplier<String> descriptionSupplier) {
			this.invocation = invocation;
			this.timeout = timeout;
			this.descriptionSupplier = descriptionSupplier;
		}

		public Invocation<T> getInvocation() {
			return invocation;
		}

		public TimeoutDuration getTimeoutDuration() {
			return timeout;
		}

		public Supplier<String> getDescriptionSupplier() {
			return descriptionSupplier;
		}
	}
}
