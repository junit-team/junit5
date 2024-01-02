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
 * {@code AssertTrue} is a collection of utility methods that support asserting
 * {@code true} in tests.
 *
 * @since 5.0
 */
class AssertTrue {

	private AssertTrue() {
		/* no-op */
	}

	static void assertTrue(boolean condition) {
		assertTrue(condition, (String) null);
	}

	static void assertTrue(boolean condition, String message) {
		if (!condition) {
			failNotTrue(message);
		}
	}

	static void assertTrue(boolean condition, Supplier<String> messageSupplier) {
		if (!condition) {
			failNotTrue(messageSupplier);
		}
	}

	static void assertTrue(BooleanSupplier booleanSupplier) {
		assertTrue(booleanSupplier.getAsBoolean(), (String) null);
	}

	static void assertTrue(BooleanSupplier booleanSupplier, String message) {
		assertTrue(booleanSupplier.getAsBoolean(), message);
	}

	static void assertTrue(BooleanSupplier booleanSupplier, Supplier<String> messageSupplier) {
		assertTrue(booleanSupplier.getAsBoolean(), messageSupplier);
	}

	private static void failNotTrue(Object messageOrSupplier) {
		assertionFailure() //
				.message(messageOrSupplier) //
				.expected(true) //
				.actual(false) //
				.buildAndThrow();
	}

}
