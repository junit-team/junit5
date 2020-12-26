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

import org.junit.jupiter.api.function.Executable;
import org.opentest4j.AssertionFailedError;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for JUnit Jupiter {@link Assertions#assertInstanceOf(Class, Object)}.
 *
 * @since 5.8
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
	void assertInstanceOfFailsWrongExceptionValue() {
		assertInstanceOfFails(RuntimeException.class, new IOException(), "exception type");
	}

	@Test
	void assertInstanceOfFailsSuperTypeExceptionValue() {
		assertInstanceOfFails(IllegalArgumentException.class, new RuntimeException(), "exception type");
	}

	private static class BaseClass {
	}

	private static class SubClass extends BaseClass {

	}

	@Test
	void assertInstanceOfFailsSuperTypeValue() {
		assertInstanceOfFails(SubClass.class, new BaseClass(), "instance type");
	}

	@Test
	void assertInstanceOfSucceedsSameTypeValue() {
		assertInstanceOfSucceeds(String.class, "indeed a String");
		assertInstanceOfSucceeds(BaseClass.class, new BaseClass());
		assertInstanceOfSucceeds(SubClass.class, new SubClass());
	}

	@Test
	void assertInstanceOfSucceedsExpectSuperClassOfValue() {
		assertInstanceOfSucceeds(CharSequence.class, "indeed a CharSequence");
		assertInstanceOfSucceeds(BaseClass.class, new SubClass());
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
		assertInstanceOf(expectedType, actualValue, "extra");
		assertInstanceOf(expectedType, actualValue, () -> "extra");
	}

	private void assertInstanceOfFails(Class<?> expectedType, Object actualValue, String unexpectedSort) {
		String valueType = actualValue == null ? "null" : actualValue.getClass().getCanonicalName();
		String expectedMessage = String.format("Unexpected %s ==> expected: <%s> but was: <%s>", unexpectedSort,
				expectedType.getCanonicalName(), valueType);

		assertThrowsWithMessage(expectedMessage, () -> assertInstanceOf(expectedType, actualValue));
		assertThrowsWithMessage("extra ==> " + expectedMessage, () -> assertInstanceOf(expectedType, actualValue, "extra"));
		assertThrowsWithMessage("extra ==> " + expectedMessage, () -> assertInstanceOf(expectedType, actualValue, () -> "extra"));
	}

	private void assertThrowsWithMessage(String expectedMessage, Executable executable) {
		assertEquals(expectedMessage, assertThrows(AssertionFailedError.class, executable).getMessage());
	}
}
