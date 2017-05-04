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

import static org.junit.jupiter.api.AssertionTestUtils.assertMessageEndsWith;
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
class AssertionsAssertNotEqualsTests {

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
