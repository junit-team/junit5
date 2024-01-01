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
import static org.junit.jupiter.api.AssertionUtils.doublesAreEqual;
import static org.junit.jupiter.api.AssertionUtils.floatsAreEqual;
import static org.junit.jupiter.api.AssertionUtils.objectsAreEqual;

import java.util.function.Supplier;

/**
 * {@code AssertNotEquals} is a collection of utility methods that support asserting
 * inequality in objects and primitive values in tests.
 *
 * @since 5.0
 */
class AssertNotEquals {

	private AssertNotEquals() {
		/* no-op */
	}

	/**
	 * @since 5.4
	 */
	static void assertNotEquals(byte unexpected, byte actual) {
		assertNotEquals(unexpected, actual, (String) null);
	}

	/**
	 * @since 5.4
	 */
	static void assertNotEquals(byte unexpected, byte actual, String message) {
		if (unexpected == actual) {
			failEqual(actual, message);
		}
	}

	/**
	 * @since 5.4
	 */
	static void assertNotEquals(byte unexpected, byte actual, Supplier<String> messageSupplier) {
		if (unexpected == actual) {
			failEqual(actual, messageSupplier);
		}
	}

	/**
	 * @since 5.4
	 */
	static void assertNotEquals(short unexpected, short actual) {
		assertNotEquals(unexpected, actual, (String) null);
	}

	/**
	 * @since 5.4
	 */
	static void assertNotEquals(short unexpected, short actual, String message) {
		if (unexpected == actual) {
			failEqual(actual, message);
		}
	}

	/**
	 * @since 5.4
	 */
	static void assertNotEquals(short unexpected, short actual, Supplier<String> messageSupplier) {
		if (unexpected == actual) {
			failEqual(actual, messageSupplier);
		}
	}

	/**
	 * @since 5.4
	 */
	static void assertNotEquals(int unexpected, int actual) {
		assertNotEquals(unexpected, actual, (String) null);
	}

	/**
	 * @since 5.4
	 */
	static void assertNotEquals(int unexpected, int actual, String message) {
		if (unexpected == actual) {
			failEqual(actual, message);
		}
	}

	/**
	 * @since 5.4
	 */
	static void assertNotEquals(int unexpected, int actual, Supplier<String> messageSupplier) {
		if (unexpected == actual) {
			failEqual(actual, messageSupplier);
		}
	}

	/**
	 * @since 5.4
	 */
	static void assertNotEquals(long unexpected, long actual) {
		assertNotEquals(unexpected, actual, (String) null);
	}

	/**
	 * @since 5.4
	 */
	static void assertNotEquals(long unexpected, long actual, String message) {
		if (unexpected == actual) {
			failEqual(actual, message);
		}
	}

	/**
	 * @since 5.4
	 */
	static void assertNotEquals(long unexpected, long actual, Supplier<String> messageSupplier) {
		if (unexpected == actual) {
			failEqual(actual, messageSupplier);
		}
	}

	/**
	 * @since 5.4
	 */
	static void assertNotEquals(float unexpected, float actual) {
		assertNotEquals(unexpected, actual, (String) null);
	}

	/**
	 * @since 5.4
	 */
	static void assertNotEquals(float unexpected, float actual, String message) {
		if (floatsAreEqual(unexpected, actual)) {
			failEqual(actual, message);
		}
	}

	/**
	 * @since 5.4
	 */
	static void assertNotEquals(float unexpected, float actual, Supplier<String> messageSupplier) {
		if (floatsAreEqual(unexpected, actual)) {
			failEqual(actual, messageSupplier);
		}
	}

	/**
	 * @since 5.4
	 */
	static void assertNotEquals(float unexpected, float actual, float delta) {
		assertNotEquals(unexpected, actual, delta, (String) null);
	}

	/**
	 * @since 5.4
	 */
	static void assertNotEquals(float unexpected, float actual, float delta, String message) {
		if (floatsAreEqual(unexpected, actual, delta)) {
			failEqual(actual, message);
		}
	}

	/**
	 * @since 5.4
	 */
	static void assertNotEquals(float unexpected, float actual, float delta, Supplier<String> messageSupplier) {
		if (floatsAreEqual(unexpected, actual, delta)) {
			failEqual(actual, messageSupplier);
		}
	}

	/**
	 * @since 5.4
	 */
	static void assertNotEquals(double unexpected, double actual) {
		assertNotEquals(unexpected, actual, (String) null);
	}

	/**
	 * @since 5.4
	 */
	static void assertNotEquals(double unexpected, double actual, String message) {
		if (doublesAreEqual(unexpected, actual)) {
			failEqual(actual, message);
		}
	}

	/**
	 * @since 5.4
	 */
	static void assertNotEquals(double unexpected, double actual, Supplier<String> messageSupplier) {
		if (doublesAreEqual(unexpected, actual)) {
			failEqual(actual, messageSupplier);
		}
	}

	/**
	 * @since 5.4
	 */
	static void assertNotEquals(double unexpected, double actual, double delta) {
		assertNotEquals(unexpected, actual, delta, (String) null);
	}

	/**
	 * @since 5.4
	 */
	static void assertNotEquals(double unexpected, double actual, double delta, String message) {
		if (doublesAreEqual(unexpected, actual, delta)) {
			failEqual(actual, message);
		}
	}

	/**
	 * @since 5.4
	 */
	static void assertNotEquals(double unexpected, double actual, double delta, Supplier<String> messageSupplier) {
		if (doublesAreEqual(unexpected, actual, delta)) {
			failEqual(actual, messageSupplier);
		}
	}

	/**
	 * @since 5.4
	 */
	static void assertNotEquals(char unexpected, char actual) {
		assertNotEquals(unexpected, actual, (String) null);
	}

	/**
	 * @since 5.4
	 */
	static void assertNotEquals(char unexpected, char actual, String message) {
		if (unexpected == actual) {
			failEqual(actual, message);
		}
	}

	/**
	 * @since 5.4
	 */
	static void assertNotEquals(char unexpected, char actual, Supplier<String> messageSupplier) {
		if (unexpected == actual) {
			failEqual(actual, messageSupplier);
		}
	}

	static void assertNotEquals(Object unexpected, Object actual) {
		assertNotEquals(unexpected, actual, (String) null);
	}

	static void assertNotEquals(Object unexpected, Object actual, String message) {
		if (objectsAreEqual(unexpected, actual)) {
			failEqual(actual, message);
		}
	}

	static void assertNotEquals(Object unexpected, Object actual, Supplier<String> messageSupplier) {
		if (objectsAreEqual(unexpected, actual)) {
			failEqual(actual, messageSupplier);
		}
	}

	private static void failEqual(Object actual, Object messageOrSupplier) {
		assertionFailure() //
				.message(messageOrSupplier) //
				.reason("expected: not equal but was: <" + actual + ">") //
				.buildAndThrow();
	}

}
