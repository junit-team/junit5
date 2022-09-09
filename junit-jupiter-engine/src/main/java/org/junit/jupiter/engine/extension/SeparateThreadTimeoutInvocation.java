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

import static org.junit.jupiter.api.Assertions.assertTimeoutPreemptivelyThrowingTimeoutException;

import java.util.concurrent.TimeoutException;
import java.util.function.Supplier;

import org.junit.jupiter.api.extension.InvocationInterceptor.Invocation;

/**
 * @since 5.9
 */
class SeparateThreadTimeoutInvocation<T> implements Invocation<T> {

	private final Invocation<T> delegate;
	private final TimeoutDuration timeout;
	private final Supplier<String> descriptionSupplier;

	SeparateThreadTimeoutInvocation(Invocation<T> delegate, TimeoutDuration timeout,
			Supplier<String> descriptionSupplier) {
		this.delegate = delegate;
		this.timeout = timeout;
		this.descriptionSupplier = descriptionSupplier;
	}

	@Override
	public T proceed() throws Throwable {
		try {
			return assertTimeoutPreemptivelyThrowingTimeoutException(timeout.toDuration(), delegate::proceed,
				descriptionSupplier);
		}
		catch (TimeoutException failure) {
			TimeoutException exception = TimeoutExceptionFactory.create(descriptionSupplier.get(), timeout, null);
			exception.initCause(failure.getCause());
			throw exception;
		}
	}
}
