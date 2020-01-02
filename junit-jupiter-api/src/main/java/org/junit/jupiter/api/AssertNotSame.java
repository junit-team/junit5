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

import static org.junit.jupiter.api.AssertionUtils.buildPrefix;
import static org.junit.jupiter.api.AssertionUtils.fail;
import static org.junit.jupiter.api.AssertionUtils.nullSafeGet;

import java.util.function.Supplier;

/**
 * {@code AssertNotSame} is a collection of utility methods that support asserting
 * two objects are not the same.
 *
 * @since 5.0
 */
class AssertNotSame {

	private AssertNotSame() {
		/* no-op */
	}

	static void assertNotSame(Object unexpected, Object actual) {
		assertNotSame(unexpected, actual, (String) null);
	}

	static void assertNotSame(Object unexpected, Object actual, String message) {
		if (unexpected == actual) {
			failSame(actual, message);
		}
	}

	static void assertNotSame(Object unexpected, Object actual, Supplier<String> messageSupplier) {
		if (unexpected == actual) {
			failSame(actual, nullSafeGet(messageSupplier));
		}
	}

	private static void failSame(Object actual, String message) {
		fail(buildPrefix(message) + "expected: not same but was: <" + actual + ">");
	}

}
