/*
 * Copyright 2015-2022 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * https://www.eclipse.org/legal/epl-v20.html
 */

package org.junit.jupiter.api;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assumptions.*;
import static org.junit.jupiter.api.Assumptions.assumeInstanceOf;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.function.Executable;
import org.opentest4j.TestAbortedException;

/**
 * Unit tests for JUnit Jupiter {@link Assumptions}.
 *
 * @since 5.0
 */
class AssumptionsTests {

	// --- assumeTrue ----------------------------------------------------

	@Test
	void assumeTrueWithBooleanTrue() {
		String foo = null;
		try {
			assumeTrue(true);
			assumeTrue(true, "message");
			assumeTrue(true, () -> "message");
			foo = "foo";
		}
		finally {
			assertNotNull(foo);
		}
	}

	@Test
	void assumeTrueWithBooleanSupplierTrue() {
		String foo = null;
		try {
			assumeTrue(() -> true);
			assumeTrue(() -> true, "message");
			assumeTrue(() -> true, () -> "message");
			foo = "foo";
		}
		finally {
			assertNotNull(foo);
		}
	}

	@Test
	void assumeTrueWithBooleanFalse() {
		assertAssumptionFailure("assumption is not true", () -> assumeTrue(false));
	}

	@Test
	void assumeTrueWithBooleanSupplierFalse() {
		assertAssumptionFailure("assumption is not true", () -> assumeTrue(() -> false));
	}

	@Test
	void assumeTrueWithBooleanFalseAndStringMessage() {
		assertAssumptionFailure("test", () -> assumeTrue(false, "test"));
	}

	@Test
	void assumeTrueWithBooleanFalseAndNullStringMessage() {
		assertAssumptionFailure(null, () -> assumeTrue(false, (String) null));
	}

	@Test
	void assumeTrueWithBooleanSupplierFalseAndStringMessage() {
		assertAssumptionFailure("test", () -> assumeTrue(() -> false, "test"));
	}

	@Test
	void assumeTrueWithBooleanSupplierFalseAndMessageSupplier() {
		assertAssumptionFailure("test", () -> assumeTrue(() -> false, () -> "test"));
	}

	@Test
	void assumeTrueWithBooleanFalseAndMessageSupplier() {
		assertAssumptionFailure("test", () -> assumeTrue(false, () -> "test"));
	}

	// --- assumeFalse ----------------------------------------------------

	@Test
	void assumeFalseWithBooleanFalse() {
		String foo = null;
		try {
			assumeFalse(false);
			assumeFalse(false, "message");
			assumeFalse(false, () -> "message");
			foo = "foo";
		}
		finally {
			assertNotNull(foo);
		}
	}

	@Test
	void assumeFalseWithBooleanSupplierFalse() {
		String foo = null;
		try {
			assumeFalse(() -> false);
			assumeFalse(() -> false, "message");
			assumeFalse(() -> false, () -> "message");
			foo = "foo";
		}
		finally {
			assertNotNull(foo);
		}
	}

	@Test
	void assumeFalseWithBooleanTrue() {
		assertAssumptionFailure("assumption is not false", () -> assumeFalse(true));
	}

	@Test
	void assumeFalseWithBooleanSupplierTrue() {
		assertAssumptionFailure("assumption is not false", () -> assumeFalse(() -> true));
	}

	@Test
	void assumeFalseWithBooleanTrueAndStringMessage() {
		assertAssumptionFailure("test", () -> assumeFalse(true, "test"));
	}

	@Test
	void assumeFalseWithBooleanSupplierTrueAndMessage() {
		assertAssumptionFailure("test", () -> assumeFalse(() -> true, "test"));
	}

	@Test
	void assumeFalseWithBooleanSupplierTrueAndMessageSupplier() {
		assertAssumptionFailure("test", () -> assumeFalse(() -> true, () -> "test"));
	}

	@Test
	void assumeFalseWithBooleanTrueAndMessageSupplier() {
		assertAssumptionFailure("test", () -> assumeFalse(true, () -> "test"));
	}

	// --- assumingThat --------------------------------------------------

	@Test
	void assumingThatWithBooleanTrue() {
		List<String> list = new ArrayList<>();
		assumingThat(true, () -> list.add("test"));
		assertEquals(1, list.size());
		assertEquals("test", list.get(0));
	}

	@Test
	void assumingThatWithBooleanSupplierTrue() {
		List<String> list = new ArrayList<>();
		assumingThat(() -> true, () -> list.add("test"));
		assertEquals(1, list.size());
		assertEquals("test", list.get(0));
	}

	@Test
	void assumingThatWithBooleanFalse() {
		List<String> list = new ArrayList<>();
		assumingThat(false, () -> list.add("test"));
		assertEquals(0, list.size());
	}

	@Test
	void assumingThatWithBooleanSupplierFalse() {
		List<String> list = new ArrayList<>();
		assumingThat(() -> false, () -> list.add("test"));
		assertEquals(0, list.size());
	}

	@Test
	void assumingThatWithFailingExecutable() {
		assertThrows(EnigmaThrowable.class, () -> assumingThat(true, () -> {
			throw new EnigmaThrowable();
		}));
	}

	// --- assumeInstanceOf --------------------------------------------------

	@Test
	void assumeInstanceOfFailsNullValue(){
		assumeInstanceOfFails(String.class, null,"null value");
	}

	@Test
	void assumeInstanceOfFailsWrongTypeValue(){
		assumeInstanceOfFails(String.class, 1,"type");
	}

	@Test
	void assumeInstanceOfFailsWrongExceptionValue(){
		assumeInstanceOfFails(RuntimeException.class, new IOException(),"type");
	}

	@Test
	void assumeInstanceOfFailsSuperTypeExceptionValue(){
		assumeInstanceOfFails(IllegalArgumentException.class, new RuntimeException(),"type");
	}

	private static class BaseClass {
	}

	private static class SubClass extends BaseClass {

	}

	@Test
	void assumeInstanceOfFailsSuperTypeValue(){
		assumeInstanceOfFails(SubClass.class, new BaseClass(),"type");
	}

	@Test
	void assumeInstanceOfSucceedsSameTypeValue() {
		assumeInstanceOfSucceeds(String.class, "indeed a String");
		assumeInstanceOfSucceeds(BaseClass.class, new BaseClass());
		assumeInstanceOfSucceeds(SubClass.class, new SubClass());
	}

	@Test
	void assumeInstanceOfSucceedsExpectSuperClassOfValue() {
		assumeInstanceOfSucceeds(CharSequence.class, "indeed a CharSequence");
		assumeInstanceOfSucceeds(BaseClass.class, new SubClass());
	}

	@Test
	void assumeInstanceOfSucceedsSameTypeExceptionValue() {
		assumeInstanceOfSucceeds(UnsupportedOperationException.class, new UnsupportedOperationException());
	}

	@Test
	void assumeInstanceOfSucceedsExpectSuperClassOfExceptionValue() {
		assumeInstanceOfSucceeds(RuntimeException.class, new IllegalArgumentException("is a RuntimeException"));
	}

	private <T> void assumeInstanceOfSucceeds(Class<T> expectedType, Object actualValue) {
		T res = assumeInstanceOf(expectedType, actualValue);
		assertSame(res, actualValue);
		res = assumeInstanceOf(expectedType, actualValue, "extra");
		assertSame(res, actualValue);
		res = assumeInstanceOf(expectedType, actualValue, () -> "extra");
		assertSame(res, actualValue);
	}

	private void assumeInstanceOfFails(Class<?> expectedType, Object actualValue, String unexpectedSort) {
		String valueType = actualValue == null ? "null" : actualValue.getClass().getCanonicalName();
		String expectedMessage = String.format("Unexpected %s ==> expected: <%s> but was: <%s>", unexpectedSort,
				expectedType.getCanonicalName(), valueType);

		assertThrowsWithMessage(expectedMessage, () -> assumeInstanceOf(expectedType, actualValue));
		assertThrowsWithMessage("extra ==> " + expectedMessage,
				() -> assumeInstanceOf(expectedType, actualValue, "extra"));
		assertThrowsWithMessage("extra ==> " + expectedMessage,
				() -> assumeInstanceOf(expectedType, actualValue, () -> "extra"));
	}

	private void assertThrowsWithMessage(String expectedMessage, Executable executable) {
		assertEquals( "Assumption failed: " + expectedMessage, assertThrows(TestAbortedException.class, executable).getMessage());
	}

	// -------------------------------------------------------------------

	private static void assertAssumptionFailure(String msg, Executable executable) {
		try {
			executable.execute();
			expectTestAbortedException();
		}
		catch (Throwable ex) {
			assertTrue(ex instanceof TestAbortedException);
			assertMessageEquals((TestAbortedException) ex,
				msg == null ? "Assumption failed" : "Assumption failed: " + msg);
		}
	}

	private static void expectTestAbortedException() {
		throw new AssertionError("Should have thrown a " + TestAbortedException.class.getName());
	}

	private static void assertMessageEquals(TestAbortedException ex, String msg) throws AssertionError {
		if (!msg.equals(ex.getMessage())) {
			throw new AssertionError(
				"Message in TestAbortedException should be [" + msg + "], but was [" + ex.getMessage() + "].");
		}
	}

}
