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

import static org.junit.jupiter.api.AssertionUtils.buildPrefix;
import static org.junit.jupiter.api.AssertionUtils.fail;
import static org.junit.jupiter.api.AssertionUtils.nullSafeGet;
import static org.junit.jupiter.api.AssertionUtils.objectsAreEqual;

import java.util.function.Supplier;

/**
 * {@code AssertNotEquals} is a collection of utility methods that support asserting
 * inequality on objects in tests.
 *
 * @since 5.0
 */
class AssertNotEquals {

	static void assertNotEquals(Object unexpected, Object actual) {
		assertNotEquals(unexpected, actual, () -> null);
	}

	static void assertNotEquals(Object unexpected, Object actual, String message) {
		assertNotEquals(unexpected, actual, () -> message);
	}

	static void assertNotEquals(Object unexpected, Object actual, Supplier<String> messageSupplier) {
		if (objectsAreEqual(unexpected, actual)) {
			failEqual(actual, nullSafeGet(messageSupplier));
		}
	}

	private static void failEqual(Object actual, String message) {
		fail(buildPrefix(message) + "expected: not equal but was: <" + actual + ">");
	}
}
