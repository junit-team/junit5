/*
 * Copyright 2015-2017 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.junit.jupiter.api;

import static org.junit.jupiter.api.AssertionUtils.fail;
import static org.junit.jupiter.api.AssertionUtils.format;
import static org.junit.jupiter.api.AssertionUtils.nullSafeGet;

import java.util.function.Supplier;

/**
 * {@code AssertSame} is a collection of utility methods that support asserting
 * two objects are the same.
 *
 * @since 5.0
 */
class AssertSame {

	///CLOVER:OFF
	private AssertSame() {
		/* no-op */
	}
	///CLOVER:ON

	static void assertSame(Object expected, Object actual) {
		assertSame(expected, actual, () -> null);
	}

	static void assertSame(Object expected, Object actual, String message) {
		assertSame(expected, actual, () -> message);
	}

	static void assertSame(Object expected, Object actual, Supplier<String> messageSupplier) {
		if (expected != actual) {
			failNotSame(expected, actual, nullSafeGet(messageSupplier));
		}
	}

	private static void failNotSame(Object expected, Object actual, String message) {
		fail(format(expected, actual, message), expected, actual);
	}

}
