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

import java.util.function.BooleanSupplier;
import java.util.function.Supplier;

/**
 * {@code AssertFalse} is a collection of utility methods that support asserting
 * {@code false} in tests.
 *
 * @since 5.0
 */
class AssertFalse {

	private AssertFalse() {
		/* no-op */
	}

	static void assertFalse(boolean condition) {
		assertFalse(condition, (String) null);
	}

	static void assertFalse(boolean condition, String message) {
		if (condition) {
			failNotFalse(message);
		}
	}

	static void assertFalse(boolean condition, Supplier<String> messageSupplier) {
		if (condition) {
			failNotFalse(messageSupplier);
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

	private static void failNotFalse(Object messageOrSupplier) {
		assertionFailure() //
				.message(messageOrSupplier) //
				.expected(false) //
				.actual(true) //
				.buildAndThrow();
	}

}
