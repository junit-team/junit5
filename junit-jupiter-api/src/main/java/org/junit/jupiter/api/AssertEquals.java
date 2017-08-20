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

import static org.junit.jupiter.api.AssertionUtils.doublesAreEqual;
import static org.junit.jupiter.api.AssertionUtils.fail;
import static org.junit.jupiter.api.AssertionUtils.floatsAreEqual;
import static org.junit.jupiter.api.AssertionUtils.format;
import static org.junit.jupiter.api.AssertionUtils.nullSafeGet;
import static org.junit.jupiter.api.AssertionUtils.objectsAreEqual;

import java.util.function.Supplier;

/**
 * {@code AssertEquals} is a collection of utility methods that support asserting
 * equality on objects and primitives in tests.
 *
 * @since 5.0
 */
class AssertEquals {

	///CLOVER:OFF
	private AssertEquals() {
		/* no-op */
	}
	///CLOVER:ON

	static void assertEquals(byte expected, byte actual) {
		assertEquals(expected, actual, () -> null);
	}

	static void assertEquals(byte expected, byte actual, String message) {
		assertEquals(expected, actual, () -> message);
	}

	static void assertEquals(byte expected, byte actual, Supplier<String> messageSupplier) {
		if (expected != actual) {
			failNotEqual(expected, actual, nullSafeGet(messageSupplier));
		}
	}

	static void assertEquals(char expected, char actual) {
		assertEquals(expected, actual, () -> null);
	}

	static void assertEquals(char expected, char actual, String message) {
		assertEquals(expected, actual, () -> message);
	}

	static void assertEquals(char expected, char actual, Supplier<String> messageSupplier) {
		if (expected != actual) {
			failNotEqual(expected, actual, nullSafeGet(messageSupplier));
		}
	}

	static void assertEquals(double expected, double actual) {
		assertEquals(expected, actual, () -> null);
	}

	static void assertEquals(double expected, double actual, String message) {
		assertEquals(expected, actual, () -> message);
	}

	static void assertEquals(double expected, double actual, Supplier<String> messageSupplier) {
		if (!doublesAreEqual(expected, actual)) {
			failNotEqual(expected, actual, nullSafeGet(messageSupplier));
		}
	}

	static void assertEquals(double expected, double actual, double delta) {
		assertEquals(expected, actual, delta, () -> null);
	}

	static void assertEquals(double expected, double actual, double delta, String message) {
		assertEquals(expected, actual, delta, () -> message);
	}

	static void assertEquals(double expected, double actual, double delta, Supplier<String> messageSupplier) {
		if (!doublesAreEqual(expected, actual, delta)) {
			failNotEqual(expected, actual, nullSafeGet(messageSupplier));
		}
	}

	static void assertEquals(float expected, float actual) {
		assertEquals(expected, actual, () -> null);
	}

	static void assertEquals(float expected, float actual, String message) {
		assertEquals(expected, actual, () -> message);
	}

	static void assertEquals(float expected, float actual, Supplier<String> messageSupplier) {
		if (!floatsAreEqual(expected, actual)) {
			failNotEqual(expected, actual, nullSafeGet(messageSupplier));
		}
	}

	static void assertEquals(float expected, float actual, float delta) {
		assertEquals(expected, actual, delta, () -> null);
	}

	static void assertEquals(float expected, float actual, float delta, String message) {
		assertEquals(expected, actual, delta, () -> message);
	}

	static void assertEquals(float expected, float actual, float delta, Supplier<String> messageSupplier) {
		if (!floatsAreEqual(expected, actual, delta)) {
			failNotEqual(expected, actual, nullSafeGet(messageSupplier));
		}
	}

	static void assertEquals(short expected, short actual) {
		assertEquals(expected, actual, () -> null);
	}

	static void assertEquals(short expected, short actual, String message) {
		assertEquals(expected, actual, () -> message);
	}

	static void assertEquals(short expected, short actual, Supplier<String> messageSupplier) {
		if (expected != actual) {
			failNotEqual(expected, actual, nullSafeGet(messageSupplier));
		}
	}

	static void assertEquals(int expected, int actual) {
		assertEquals(expected, actual, () -> null);
	}

	static void assertEquals(int expected, int actual, String message) {
		assertEquals(expected, actual, () -> message);
	}

	static void assertEquals(int expected, int actual, Supplier<String> messageSupplier) {
		if (expected != actual) {
			failNotEqual(expected, actual, nullSafeGet(messageSupplier));
		}
	}

	static void assertEquals(long expected, long actual) {
		assertEquals(expected, actual, () -> null);
	}

	static void assertEquals(long expected, long actual, String message) {
		assertEquals(expected, actual, () -> message);
	}

	static void assertEquals(long expected, long actual, Supplier<String> messageSupplier) {
		if (expected != actual) {
			failNotEqual(expected, actual, nullSafeGet(messageSupplier));
		}
	}

	static void assertEquals(Object expected, Object actual) {
		assertEquals(expected, actual, () -> null);
	}

	static void assertEquals(Object expected, Object actual, String message) {
		assertEquals(expected, actual, () -> message);
	}

	static void assertEquals(Object expected, Object actual, Supplier<String> messageSupplier) {
		if (!objectsAreEqual(expected, actual)) {
			failNotEqual(expected, actual, nullSafeGet(messageSupplier));
		}
	}

	private static void failNotEqual(Object expected, Object actual, String message) {
		fail(format(expected, actual, message), expected, actual);
	}

}
