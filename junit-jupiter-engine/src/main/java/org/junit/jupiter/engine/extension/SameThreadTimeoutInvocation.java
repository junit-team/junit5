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

import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.function.Supplier;

import org.junit.jupiter.api.extension.InvocationInterceptor.Invocation;
import org.junit.platform.commons.util.UnrecoverableExceptions;

/**
 * @since 5.5
 */
class SameThreadTimeoutInvocation<T> implements Invocation<T> {

	private final Invocation<T> delegate;
	private final TimeoutDuration timeout;
	private final ScheduledExecutorService executor;
	private final Supplier<String> descriptionSupplier;

	SameThreadTimeoutInvocation(Invocation<T> delegate, TimeoutDuration timeout, ScheduledExecutorService executor,
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
			UnrecoverableExceptions.rethrowIfUnrecoverable(t);
			failure = t;
		}
		finally {
			boolean cancelled = future.cancel(false);
			if (!cancelled) {
				future.get();
			}
			if (interruptTask.executed) {
				Thread.interrupted();
				failure = TimeoutExceptionFactory.create(descriptionSupplier.get(), timeout, failure);
			}
		}
		if (failure != null) {
			throw failure;
		}
		return result;
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
