/*
 * Copyright 2015-2020 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * https://www.eclipse.org/legal/epl-v20.html
 */

package org.junit.jupiter.engine.extension;

import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeoutException;
import java.util.function.Supplier;

import org.junit.jupiter.api.extension.InvocationInterceptor.Invocation;
import org.junit.platform.commons.util.BlacklistedExceptions;

/**
 * @since 5.5
 */
class TimeoutInvocation<T> implements Invocation<T> {

	private final Invocation<T> delegate;
	private final TimeoutDuration timeout;
	private final ScheduledExecutorService executor;
	private final Supplier<String> descriptionSupplier;

	TimeoutInvocation(Invocation<T> delegate, TimeoutDuration timeout, ScheduledExecutorService executor,
			Supplier<String> descriptionSupplier) {
		this.delegate = delegate;
		this.timeout = timeout;
		this.executor = executor;
		this.descriptionSupplier = descriptionSupplier;
	}

	@Override
	public T proceed() throws Throwable {
		InterruptTask interruptTask = new InterruptTask(Thread.currentThread());
		ScheduledFuture<?> future = executor.schedule(interruptTask, timeout.getValue(), timeout.getUnit());
		Throwable failure = null;
		T result = null;
		try {
			result = delegate.proceed();
		}
		catch (Throwable t) {
			BlacklistedExceptions.rethrowIfBlacklisted(t);
			failure = t;
		}
		finally {
			boolean cancelled = future.cancel(false);
			if (!cancelled) {
				future.get();
			}
			if (interruptTask.executed) {
				Thread.interrupted();
				failure = createTimeoutException(failure);
			}
		}
		if (failure != null) {
			throw failure;
		}
		return result;
	}

	private TimeoutException createTimeoutException(Throwable failure) {
		String message = String.format("%s timed out after %s", descriptionSupplier.get(), timeout);
		TimeoutException timeoutError = new TimeoutException(message);
		if (failure != null) {
			timeoutError.addSuppressed(failure);
		}
		return timeoutError;
	}

	static class InterruptTask implements Runnable {

		private final Thread thread;
		private volatile boolean executed;

		InterruptTask(Thread thread) {
			this.thread = thread;
		}

		@Override
		public void run() {
			executed = true;
			thread.interrupt();
		}

	}

}
