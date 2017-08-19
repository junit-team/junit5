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

import java.util.function.Supplier;

/**
 * {@code AssertNull} is a collection of utility methods that support asserting
 * there is no object.
 *
 * @since 5.0
 */
class AssertNull {

	///CLOVER:OFF
	private AssertNull() {
		/* no-op */
	}
	///CLOVER:ON

	static void assertNull(Object actual) {
		assertNull(actual, () -> null);
	}

	static void assertNull(Object actual, String message) {
		assertNull(actual, () -> message);
	}

	static void assertNull(Object actual, Supplier<String> messageSupplier) {
		if (actual != null) {
			failNotNull(actual, nullSafeGet(messageSupplier));
		}
	}

	private static void failNotNull(Object actual, String message) {
		fail(buildPrefix(message) + "expected: <null> but was: <" + actual + ">", null, actual);
	}
}
