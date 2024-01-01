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

import static org.junit.jupiter.api.Assertions.assertTimeoutPreemptively;

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
		return assertTimeoutPreemptively(timeout.toDuration(), delegate::proceed, descriptionSupplier,
			(__, messageSupplier, cause) -> {
				TimeoutException exception = TimeoutExceptionFactory.create(messageSupplier.get(), timeout, null);
				exception.initCause(cause);
				return exception;
			});
	}
}
