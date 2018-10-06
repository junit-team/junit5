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

import static org.junit.jupiter.api.AssertionTestUtils.assertMessageEndsWith;
import static org.junit.jupiter.api.AssertionTestUtils.assertMessageEquals;
import static org.junit.jupiter.api.AssertionTestUtils.assertMessageStartsWith;
import static org.junit.jupiter.api.AssertionTestUtils.expectAssertionFailedError;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.opentest4j.AssertionFailedError;

/**
 * Unit tests for JUnit Jupiter {@link Assertions}.
 *
 * @since 5.0
 */
class AssertNotEqualsAssertionsTests {

	@Nested
	class AssertNotEqualsShort {

		@Test
		void assertNotEqualsShort() {
			short unexpected = 1;
			short actual = 2;
			assertNotEquals(unexpected, actual);
			assertNotEquals(unexpected, actual, "message");
			assertNotEquals(unexpected, actual, () -> "message");
		}

		@Test
		void withEqualValues() {
			short unexpected = 1;
			short actual = 1;
			try {
				assertNotEquals(unexpected, actual);
				expectAssertionFailedError();
			}
			catch (AssertionFailedError ex) {
				assertMessageEquals(ex, "expected: not equal but was: <1>");
			}
		}

		@Test
		void withEqualValuesWithMessage() {
			short unexpected = 1;
			short actual = 1;
			try {
				assertNotEquals(unexpected, actual, "custom message");
				expectAssertionFailedError();
			}
			catch (AssertionFailedError ex) {
				assertMessageStartsWith(ex, "custom message");
				assertMessageEndsWith(ex, "expected: not equal but was: <1>");
			}
		}

		@Test
		void withEqualValuesWithMessageProvider() {
			byte unexpected = 1;
			byte actual = 1;
			try {
				assertNotEquals(unexpected, actual, () -> "custom message from provider");
				expectAssertionFailedError();
			}
			catch (AssertionFailedError ex) {
				assertMessageStartsWith(ex, "custom message from provider");
				assertMessageEndsWith(ex, "expected: not equal but was: <1>");
			}
		}

	}

	@Nested
	class AssertNotEqualsByte {

		@Test
		void assertNotEqualsByte() {
			byte unexpected = 1;
			byte actual = 2;
			assertNotEquals(unexpected, actual);
			assertNotEquals(unexpected, actual, "message");
			assertNotEquals(unexpected, actual, () -> "message");
		}

		@Test
		void withEqualValues() {
			byte unexpected = 1;
			byte actual = 1;
			try {
				assertNotEquals(unexpected, actual);
				expectAssertionFailedError();
			}
			catch (AssertionFailedError ex) {
				assertMessageEquals(ex, "expected: not equal but was: <1>");
			}
		}

		@Test
		void withEqualValuesWithMessage() {
			byte unexpected = 1;
			byte actual = 1;
			try {
				assertNotEquals(unexpected, actual, "custom message");
				expectAssertionFailedError();
			}
			catch (AssertionFailedError ex) {
				assertMessageStartsWith(ex, "custom message");
				assertMessageEndsWith(ex, "expected: not equal but was: <1>");
			}
		}

		@Test
		void withEqualValuesWithMessageProvider() {
			byte unexpected = 1;
			byte actual = 1;
			try {
				assertNotEquals(unexpected, actual, () -> "custom message from provider");
				expectAssertionFailedError();
			}
			catch (AssertionFailedError ex) {
				assertMessageStartsWith(ex, "custom message from provider");
				assertMessageEndsWith(ex, "expected: not equal but was: <1>");
			}
		}

	}

	@Nested
	class AssertNotEqualsInt {

		@Test
		void assertNotEqualsInt() {
			int unexpected = 1;
			int actual = 2;
			assertNotEquals(unexpected, actual);
			assertNotEquals(unexpected, actual, "message");
			assertNotEquals(unexpected, actual, () -> "message");
		}

		@Test
		void withEqualValues() {
			int unexpected = 1;
			int actual = 1;
			try {
				assertNotEquals(unexpected, actual);
				expectAssertionFailedError();
			}
			catch (AssertionFailedError ex) {
				assertMessageEquals(ex, "expected: not equal but was: <1>");
			}
		}

		@Test
		void withEqualValuesWithMessage() {
			int unexpected = 1;
			int actual = 1;
			try {
				assertNotEquals(unexpected, actual, "custom message");
				expectAssertionFailedError();
			}
			catch (AssertionFailedError ex) {
				assertMessageStartsWith(ex, "custom message");
				assertMessageEndsWith(ex, "expected: not equal but was: <1>");
			}
		}

		@Test
		void withEqualValuesWithMessageProvider() {
			byte unexpected = 1;
			byte actual = 1;
			try {
				assertNotEquals(unexpected, actual, () -> "custom message from provider");
				expectAssertionFailedError();
			}
			catch (AssertionFailedError ex) {
				assertMessageStartsWith(ex, "custom message from provider");
				assertMessageEndsWith(ex, "expected: not equal but was: <1>");
			}
		}

	}

	@Test
	void assertNotEqualsWithNullVsObject() {
		assertNotEquals(null, "foo");
	}

	@Test
	void assertNotEqualsWithObjectVsNull() {
		assertNotEquals("foo", null);
	}

	@Test
	void assertNotEqualsWithDifferentObjects() {
		assertNotEquals(new Object(), new Object());
		assertNotEquals(new Object(), new Object(), "message");
		assertNotEquals(new Object(), new Object(), () -> "message");
	}

	@Test
	void assertNotEqualsWithNullVsObjectAndMessageSupplier() {
		assertNotEquals(null, "foo", () -> "test");
	}

	@Test
	void assertNotEqualsWithEquivalentStringsAndMessage() {
		try {
			assertNotEquals(new String("foo"), new String("foo"), "test");
			expectAssertionFailedError();
		}
		catch (AssertionFailedError ex) {
			assertMessageStartsWith(ex, "test");
			assertMessageEndsWith(ex, "expected: not equal but was: <foo>");
		}
	}

	@Test
	void assertNotEqualsWithEquivalentStringsAndMessageSupplier() {
		try {
			assertNotEquals(new String("foo"), new String("foo"), () -> "test");
			expectAssertionFailedError();
		}
		catch (AssertionFailedError ex) {
			assertMessageStartsWith(ex, "test");
			assertMessageEndsWith(ex, "expected: not equal but was: <foo>");
		}
	}

	@Test
	void assertNotEqualsInvokesEqualsMethodForIdenticalObjects() {
		Object obj = new EqualsThrowsExceptionClass();
		assertThrows(NumberFormatException.class, () -> assertNotEquals(obj, obj));
	}

	private static class EqualsThrowsExceptionClass {

		@Override
		public boolean equals(Object obj) {
			throw new NumberFormatException();
		}
	}

}
