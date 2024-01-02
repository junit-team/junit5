/*
 * Copyright 2015-2024 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * https://www.eclipse.org/legal/epl-v20.html
 */

package org.junit.jupiter.api;

import static org.junit.jupiter.api.AssertionFailureBuilder.assertionFailure;

import java.util.function.Supplier;

/**
 * {@code AssertInstanceOf} is a collection of utility methods that support
 * asserting that an object is of an expected type &mdash; in other words, if it
 * can be assigned to the expected type.
 *
 * @since 5.8
 */
class AssertInstanceOf {

	private AssertInstanceOf() {
		/* no-op */
	}

	static <T> T assertInstanceOf(Class<T> expectedType, Object actualValue) {
		return assertInstanceOf(expectedType, actualValue, (Object) null);
	}

	static <T> T assertInstanceOf(Class<T> expectedType, Object actualValue, String message) {
		return assertInstanceOf(expectedType, actualValue, (Object) message);
	}

	static <T> T assertInstanceOf(Class<T> expectedType, Object actualValue, Supplier<String> messageSupplier) {
		return assertInstanceOf(expectedType, actualValue, (Object) messageSupplier);
	}

	private static <T> T assertInstanceOf(Class<T> expectedType, Object actualValue, Object messageOrSupplier) {
		if (!expectedType.isInstance(actualValue)) {
			assertionFailure() //
					.message(messageOrSupplier) //
					.reason(actualValue == null ? "Unexpected null value" : "Unexpected type") //
					.expected(expectedType) //
					.actual(actualValue == null ? null : actualValue.getClass()) //
					.buildAndThrow();
		}
		return expectedType.cast(actualValue);
	}

}
