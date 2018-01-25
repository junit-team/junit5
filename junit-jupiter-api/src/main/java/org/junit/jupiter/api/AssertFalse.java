/*
 * Copyright 2015-2018 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v20.html
 */

package org.junit.jupiter.api;

import static org.junit.jupiter.api.AssertionUtils.fail;
import static org.junit.jupiter.api.AssertionUtils.format;
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

	/// CLOVER:OFF
	private AssertFalse() {
		/* no-op */
	}
	/// CLOVER:ON

	static void assertFalse(boolean condition) {
		assertFalse(condition, (String) null);
	}

	static void assertFalse(boolean condition, String message) {
		if (condition) {
			fail(format(false, true, message));
		}
	}

	static void assertFalse(boolean condition, Supplier<String> messageSupplier) {
		if (condition) {
			fail(format(false, true, nullSafeGet(messageSupplier)));
		}
	}

	static void assertFalse(BooleanSupplier booleanSupplier) {
		assertFalse(booleanSupplier, (String) null);
	}

	static void assertFalse(BooleanSupplier booleanSupplier, String message) {
		if (booleanSupplier.getAsBoolean()) {
			fail(format(false, true, message));
		}
	}

	static void assertFalse(BooleanSupplier booleanSupplier, Supplier<String> messageSupplier) {
		if (booleanSupplier.getAsBoolean()) {
			fail(format(false, true, nullSafeGet(messageSupplier)));
		}
	}

}
