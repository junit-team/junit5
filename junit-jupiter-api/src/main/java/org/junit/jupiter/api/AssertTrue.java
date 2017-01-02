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

import java.util.function.BooleanSupplier;
import java.util.function.Supplier;

/**
 * {@code AssertTrue} is a collection of utility methods that support asserting
 * {@code true} in tests.
 *
 * @since 5.0
 */
class AssertTrue {

	static void assertTrue(boolean condition) {
		assertTrue(() -> condition, () -> null);
	}

	static void assertTrue(boolean condition, Supplier<String> messageSupplier) {
		assertTrue(() -> condition, messageSupplier);
	}

	static void assertTrue(BooleanSupplier booleanSupplier) {
		assertTrue(booleanSupplier, () -> null);
	}

	static void assertTrue(BooleanSupplier booleanSupplier, String message) {
		assertTrue(booleanSupplier, () -> message);
	}

	static void assertTrue(boolean condition, String message) {
		assertTrue(() -> condition, () -> message);
	}

	static void assertTrue(BooleanSupplier booleanSupplier, Supplier<String> messageSupplier) {
		if (!booleanSupplier.getAsBoolean()) {
			fail(messageSupplier);
		}
	}

}
