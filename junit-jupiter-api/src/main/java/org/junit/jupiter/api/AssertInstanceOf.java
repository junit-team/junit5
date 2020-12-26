/*
 * Copyright 2015-2020 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * https://www.eclipse.org/legal/epl-v20.html
 */

package org.junit.jupiter.api;

import static org.junit.jupiter.api.AssertionUtils.*;

import java.util.function.Supplier;

import org.opentest4j.AssertionFailedError;

/**
 * {@code AssertInstanceOf} is a collection of utility methods that support
 * asserting that a value is of a expected instance type -
 * in other words, if it can be assigned to the expected type.
 *
 * @since 5.8
 */
class AssertInstanceOf {

	private AssertInstanceOf() {
		/* no-op */
	}

	static void assertInstanceOf(Class<?> expectedType, Object actualValue) {
		assertInstanceOf(expectedType, actualValue, (Object) null);
	}

	static void assertInstanceOf(Class<?> expectedType, Object actualValue, String message) {
		assertInstanceOf(expectedType, actualValue, (Object) message);
	}

	static void assertInstanceOf(Class<?> expectedType, Object actualValue, Supplier<String> messageSupplier) {
		assertInstanceOf(expectedType, actualValue, (Object) messageSupplier);
	}

	private static void assertInstanceOf(Class<?> expectedType, Object actualValue, Object messageOrSupplier) {
		if (!expectedType.isInstance(actualValue)) {
			String template = actualValue instanceof Throwable ? "Unexpected exception type"
					: "Unexpected instance type";
			if (actualValue == null)
				template = "Unexpected null value";
			String message = buildPrefix(nullSafeGet(messageOrSupplier))
					+ format(expectedType, actualValue == null ? null : actualValue.getClass(), template);
			throw new AssertionFailedError(message);
		}
	}
}
