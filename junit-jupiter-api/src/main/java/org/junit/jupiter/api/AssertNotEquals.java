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

	private AssertNotEquals() {
		/* no-op */
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
			failEqual(actual, nullSafeGet(messageSupplier));
		}
	}

	private static void failEqual(Object actual, String message) {
		fail(buildPrefix(message) + "expected: not equal but was: <" + actual + ">");
	}

}
