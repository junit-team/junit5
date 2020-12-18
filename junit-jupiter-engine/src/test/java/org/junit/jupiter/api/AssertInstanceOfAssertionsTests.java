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

import static org.junit.jupiter.api.Assertions.*;

import java.io.IOException;

import org.opentest4j.AssertionFailedError;

/**
 * Unit tests for JUnit Jupiter {@link Assertions#assertInstanceOf(Class, Object)}.
 *
 * @since 5.6.4
 */
class AssertInstanceOfAssertionsTests {

	@Test
	void assertInstanceOfFailsNullValue() {
		assertInstanceOfFails(String.class, null, "null value");
	}

	@Test
	void assertInstanceOfFailsWrongTypeValue() {
		assertInstanceOfFails(String.class, 1, "instance type");
	}

	@Test
	void assertInstanceOfWrongExceptionValue() {
		assertInstanceOfFails(RuntimeException.class, new IOException(), "exception type");
	}

	private static class SuperClass {
	}

	private static class ExpectedClass extends SuperClass {

	}

	@Test
	void assertInstanceOfFailsSuperTypeValue() {
		assertInstanceOfFails(ExpectedClass.class, new SuperClass(), "instance type");
	}

	@Test
	void assertInstanceOfSucceedsSameTypeValue() {
		assertInstanceOfSucceeds(String.class, "indeed a String");
	}

	@Test
	void assertInstanceOfSucceedsExpectSuperClassOfValue() {
		assertInstanceOfSucceeds(CharSequence.class, "indeed a CharSequence");
	}

	@Test
	void assertInstanceOfSucceedsSameTypeExceptionValue() {
		assertInstanceOfSucceeds(UnsupportedOperationException.class, new UnsupportedOperationException());
	}

	@Test
	void assertInstanceOfSucceedsExpectSuperClassOfExceptionValue() {
		assertInstanceOfSucceeds(RuntimeException.class, new IllegalArgumentException("is a RuntimeException"));
	}

	private void assertInstanceOfSucceeds(Class<?> expectedType, Object actualValue) {
		assertInstanceOf(expectedType, actualValue);
	}

	private void assertInstanceOfFails(Class<?> expectedType, Object actualValue, String unexpectedType) {
		Error error = assertThrows(AssertionFailedError.class, () -> assertInstanceOf(expectedType, actualValue));
		String valueType = actualValue == null ? "null" : actualValue.getClass().getCanonicalName();
		String expectedMessage = String.format("Unexpected %s ==> expected: <%s> but was: <%s>", unexpectedType,
			expectedType.getCanonicalName(), valueType);
		assertEquals(expectedMessage, error.getMessage());
	}
}
