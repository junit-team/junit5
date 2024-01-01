/*
 * Copyright 2015-2024 the original author or authors.
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
import java.util.concurrent.TimeUnit;
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
		Preconditions.condition(threadMode != ThreadMode.INFERRED, "thread mode must not be INFERRED");
		Preconditions.notNull(timeoutInvocationParameters, "timeout invocation parameters must not be null");
		if (threadMode == ThreadMode.SEPARATE_THREAD) {
			return new SeparateThreadTimeoutInvocation<>(timeoutInvocationParameters.getInvocation(),
				timeoutInvocationParameters.getTimeoutDuration(), timeoutInvocationParameters.getDescriptionSupplier());
		}
		return new SameThreadTimeoutInvocation<>(timeoutInvocationParameters.getInvocation(),
			timeoutInvocationParameters.getTimeoutDuration(), getThreadExecutorForSameThreadInvocation(),
			timeoutInvocationParameters.getDescriptionSupplier());
	}

	private ScheduledExecutorService getThreadExecutorForSameThreadInvocation() {
		return store.getOrComputeIfAbsent(SingleThreadExecutorResource.class).get();
	}

	private static abstract class ExecutorResource implements CloseableResource {

		protected final ScheduledExecutorService executor;

		ExecutorResource(ScheduledExecutorService executor) {
			this.executor = executor;
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

	static class SingleThreadExecutorResource extends ExecutorResource {

		@SuppressWarnings("unused")
		SingleThreadExecutorResource() {
			super(Executors.newSingleThreadScheduledExecutor(runnable -> {
				Thread thread = new Thread(runnable, "junit-jupiter-timeout-watcher");
				thread.setPriority(Thread.MAX_PRIORITY);
				return thread;
			}));
		}
	}

	static class TimeoutInvocationParameters<T> {

		private final Invocation<T> invocation;
		private final TimeoutDuration timeout;
		private final Supplier<String> descriptionSupplier;

		TimeoutInvocationParameters(Invocation<T> invocation, TimeoutDuration timeout,
				Supplier<String> descriptionSupplier) {
			this.invocation = Preconditions.notNull(invocation, "invocation must not be null");
			this.timeout = Preconditions.notNull(timeout, "timeout must not be null");
			this.descriptionSupplier = Preconditions.notNull(descriptionSupplier,
				"description supplier must not be null");
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
