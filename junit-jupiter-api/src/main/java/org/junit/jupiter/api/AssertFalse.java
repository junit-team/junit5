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

import java.util.function.BooleanSupplier;
import java.util.function.Supplier;

/**
 * {@code AssertFalse} is a collection of utility methods that support asserting
 * {@code false} in tests.
 *
 * @since 5.0
 */
class AssertFalse {

	private static final String EXPECTED_FALSE = "expected: <false> but was: <true>";

	private AssertFalse() {
		/* no-op */
	}

	static void assertFalse(boolean condition) {
		assertFalse(condition, (String) null);
	}

	static void assertFalse(boolean condition, String message) {
		if (condition) {
			fail(buildPrefix(message) + EXPECTED_FALSE, false, true);
		}
	}

	static void assertFalse(boolean condition, Supplier<String> messageSupplier) {
		if (condition) {
			fail(buildPrefix(nullSafeGet(messageSupplier)) + EXPECTED_FALSE, false, true);
		}
	}

	static void assertFalse(BooleanSupplier booleanSupplier) {
		assertFalse(booleanSupplier.getAsBoolean(), (String) null);
	}

	static void assertFalse(BooleanSupplier booleanSupplier, String message) {
		assertFalse(booleanSupplier.getAsBoolean(), message);
	}

	static void assertFalse(BooleanSupplier booleanSupplier, Supplier<String> messageSupplier) {
		assertFalse(booleanSupplier.getAsBoolean(), messageSupplier);
	}

}
