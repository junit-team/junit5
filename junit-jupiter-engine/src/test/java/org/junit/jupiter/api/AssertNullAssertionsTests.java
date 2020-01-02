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

import static org.junit.jupiter.api.AssertionTestUtils.assertExpectedAndActualValues;
import static org.junit.jupiter.api.AssertionTestUtils.assertMessageEndsWith;
import static org.junit.jupiter.api.AssertionTestUtils.assertMessageStartsWith;
import static org.junit.jupiter.api.AssertionTestUtils.expectAssertionFailedError;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.opentest4j.AssertionFailedError;

/**
 * Unit tests for JUnit Jupiter {@link Assertions}.
 *
 * @since 5.0
 */
class AssertNullAssertionsTests {

	@Test
	void assertNullWithNull() {
		assertNull(null);
		assertNull(null, "message");
		assertNull(null, () -> "message");
	}

	@Test
	void assertNullWithNullAndMessageSupplier() {
		assertNull(null, () -> "test");
	}

	@Test
	void assertNullWithNonNullObject() {
		try {
			assertNull("foo");
			expectAssertionFailedError();
		}
		catch (AssertionFailedError ex) {
			assertMessageEndsWith(ex, "expected: <null> but was: <foo>");
			assertExpectedAndActualValues(ex, null, "foo");
		}
	}

	@Test
	void assertNullWithNonNullObjectAndMessage() {
		try {
			assertNull("foo", "a message");
			expectAssertionFailedError();
		}
		catch (AssertionFailedError ex) {
			assertMessageStartsWith(ex, "a message");
			assertMessageEndsWith(ex, "expected: <null> but was: <foo>");
			assertExpectedAndActualValues(ex, null, "foo");
		}
	}

	@Test
	void assertNullWithNonNullObjectAndMessageSupplier() {
		try {
			assertNull("foo", () -> "test");
			expectAssertionFailedError();
		}
		catch (AssertionFailedError ex) {
			assertMessageStartsWith(ex, "test");
			assertMessageEndsWith(ex, "expected: <null> but was: <foo>");
			assertExpectedAndActualValues(ex, null, "foo");
		}
	}

}
