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

import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeoutException;
import java.util.function.Supplier;

import org.junit.jupiter.api.extension.InvocationInterceptor.Invocation;
import org.junit.platform.commons.util.UnrecoverableExceptions;

/**
 * @since 5.9
 */
class SeparateThreadTimeoutInvocation<T> implements Invocation<T> {

	private final Invocation<T> delegate;
	private final TimeoutDuration timeout;
	private final ScheduledExecutorService executor;
	private final Supplier<String> descriptionSupplier;

	SeparateThreadTimeoutInvocation(Invocation<T> delegate, TimeoutDuration timeout, ScheduledExecutorService executor,
			Supplier<String> descriptionSupplier) {
		this.delegate = delegate;
		this.timeout = timeout;
		this.executor = executor;
		this.descriptionSupplier = descriptionSupplier;
	}

	@Override
	public T proceed() throws Throwable {
		DelegateCallable<T> delegateCallable = new DelegateCallable<>(delegate);

		InvocationResult<T> result = invoke(delegateCallable);

		return result.getResultOrThrowFailure();
	}

	private InvocationResult<T> invoke(DelegateCallable<T> delegateCallable) {
		Future<T> future = executor.submit(delegateCallable);
		try {
			return InvocationResult.success(future.get(timeout.getValue(), timeout.getUnit()));
		}
		catch (TimeoutException ignored) {
			return InvocationResult.failure(TimeoutExceptionFactory.create(descriptionSupplier.get(), timeout));
		}
		catch (Throwable throwable) {
			Throwable wrappedInvocationException = throwable.getCause();
			UnrecoverableExceptions.rethrowIfUnrecoverable(wrappedInvocationException.getCause());
			return InvocationResult.failure(wrappedInvocationException.getCause());
		}
	}

	private static class InvocationResult<T> {

		private T success;
		private Throwable failure;

		InvocationResult(T success) {
			this.success = success;
		}

		InvocationResult(Throwable failure) {
			this.failure = failure;
		}

		static <T> InvocationResult<T> success(T result) {
			return new InvocationResult<>(result);
		}

		static <T> InvocationResult<T> failure(Throwable throwable) {
			return new InvocationResult<>(throwable);
		}

		T getResultOrThrowFailure() throws Throwable {
			if (failure != null) {
				throw failure;
			}
			return success;
		}
	}

	private static class DelegateCallable<T> implements Callable<T> {

		private final Invocation<T> delegate;

		DelegateCallable(Invocation<T> delegate) {
			this.delegate = delegate;
		}

		@Override
		public T call() {
			try {
				return delegate.proceed();
			}
			catch (Throwable e) {
				throw new ThrowableWrapperException(e);
			}
		}
	}

	private static class ThrowableWrapperException extends RuntimeException {

		private static final long serialVersionUID = -2713519172711648957L;

		ThrowableWrapperException(Throwable e) {
			super(e);
		}
	}
}
