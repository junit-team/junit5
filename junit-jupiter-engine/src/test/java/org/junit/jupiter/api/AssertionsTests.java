/*
 * Copyright 2015-2016 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.junit.jupiter.api;

import static java.time.Duration.ofMillis;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTimeout;
import static org.junit.jupiter.api.Assertions.assertTimeoutPreemptively;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.expectThrows;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.IOException;
import java.io.Serializable;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Supplier;
import java.util.stream.Stream;

import org.junit.jupiter.api.function.Executable;
import org.junit.platform.commons.util.PreconditionViolationException;
import org.opentest4j.AssertionFailedError;
import org.opentest4j.MultipleFailuresError;
import org.opentest4j.ValueWrapper;

/**
 * Unit tests for JUnit Jupiter {@link Assertions}.
 *
 * @since 5.0
 */
public class AssertionsTests {

	// --- fail ----------------------------------------------------------

	@Test
	void failWithString() {
		try {
			fail("test");
			expectAssertionFailedError();
		}
		catch (AssertionFailedError ex) {
			assertMessageEquals(ex, "test");
		}
	}

	@Test
	void failWithMessageSupplier() {
		try {
			fail(() -> "test");
			expectAssertionFailedError();
		}
		catch (AssertionFailedError ex) {
			assertMessageEquals(ex, "test");
		}
	}

	@Test
	void failWithNullString() {
		try {
			fail((String) null);
			expectAssertionFailedError();
		}
		catch (AssertionFailedError ex) {
			assertMessageEquals(ex, "");
		}
	}

	@Test
	void failWithNullMessageSupplier() {
		try {
			fail((Supplier<String>) null);
			expectAssertionFailedError();
		}
		catch (AssertionFailedError ex) {
			assertMessageEquals(ex, "");
		}
	}

	// --- assertTrue ----------------------------------------------------

	@Test
	void assertTrueWithBooleanTrue() {
		assertTrue(true);
	}

	@Test
	void assertTrueWithBooleanSupplierTrue() {
		assertTrue(() -> true);
	}

	@Test
	void assertTrueWithBooleanTrueAndString() {
		assertTrue(true, "test");
	}

	@Test
	void assertTrueWithBooleanFalse() {
		try {
			assertTrue(false);
			expectAssertionFailedError();
		}
		catch (AssertionFailedError ex) {
			assertMessageEquals(ex, "");
		}
	}

	@Test
	void assertTrueWithBooleanFalseAndString() {
		try {
			assertTrue(false, "test");
			expectAssertionFailedError();
		}
		catch (AssertionFailedError ex) {
			assertMessageEquals(ex, "test");
		}
	}

	@Test
	void assertTrueWithBooleanSupplierFalseAndString() {
		try {
			assertTrue(() -> false, "test");
			expectAssertionFailedError();
		}
		catch (AssertionFailedError ex) {
			assertMessageEquals(ex, "test");
		}
	}

	@Test
	void assertTrueWithBooleanSupplierFalseAndMessageSupplier() {
		try {
			assertTrue(() -> false, () -> "test");
			expectAssertionFailedError();
		}
		catch (AssertionFailedError ex) {
			assertMessageEquals(ex, "test");
		}
	}

	// --- assertFalse ---------------------------------------------------

	@Test
	void assertFalseWithBooleanFalse() {
		assertFalse(false);
	}

	@Test
	void assertFalseWithBooleanSupplierFalse() {
		assertFalse(() -> false);
	}

	@Test
	void assertFalseWithBooleanTrueAndString() {
		try {
			assertFalse(true, "test");
			expectAssertionFailedError();
		}
		catch (AssertionFailedError ex) {
			assertMessageEquals(ex, "test");
		}
	}

	@Test
	void assertFalseWithBooleanTrueAndMessageSupplier() {
		try {
			assertFalse(true, () -> "test");
			expectAssertionFailedError();
		}
		catch (AssertionFailedError ex) {
			assertMessageEquals(ex, "test");
		}
	}

	@Test
	void assertFalseWithBooleanSupplierTrueAndString() {
		try {
			assertFalse(() -> true, "test");
			expectAssertionFailedError();
		}
		catch (AssertionFailedError ex) {
			assertMessageEquals(ex, "test");
		}
	}

	@Test
	void assertFalseWithBooleanSupplierTrueAndMessageSupplier() {
		try {
			assertFalse(() -> true, () -> "test");
			expectAssertionFailedError();
		}
		catch (AssertionFailedError ex) {
			assertMessageEquals(ex, "test");
		}
	}

	// --- assertNull ----------------------------------------------------

	@Test
	void assertNullWithNull() {
		assertNull(null);
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

	// --- assertNotNull -------------------------------------------------

	@Test
	void assertNotNullWithNonNullObject() {
		assertNotNull("foo");
	}

	@Test
	void assertNotNullWithNull() {
		try {
			assertNotNull(null);
			expectAssertionFailedError();
		}
		catch (AssertionFailedError ex) {
			assertMessageEquals(ex, "expected: not <null>");
		}
	}

	@Test
	void assertNotNullWithNullAndMessageSupplier() {
		try {
			assertNotNull(null, () -> "test");
			expectAssertionFailedError();
		}
		catch (AssertionFailedError ex) {
			assertMessageStartsWith(ex, "test");
			assertMessageEndsWith(ex, "expected: not <null>");
		}
	}

	// --- assertEquals -------------------------------------------------

	@Test
	void assertEqualsByte() {
		byte expected = 1;
		byte actual = 1;
		assertEquals(expected, actual);
	}

	@Test
	void assertEqualsByteWithUnequalValues() {
		byte expected = 1;
		byte actual = 2;
		try {
			assertEquals(expected, actual);
			expectAssertionFailedError();
		}
		catch (AssertionFailedError ex) {
			assertMessageEquals(ex, "expected: <1> but was: <2>");
			assertExpectedAndActualValues(ex, expected, actual);
		}
	}

	@Test
	void assertEqualsByteWithUnequalValuesAndMessage() {
		byte expected = 1;
		byte actual = 2;
		try {
			assertEquals(expected, actual, "message");
			expectAssertionFailedError();
		}
		catch (AssertionFailedError ex) {
			assertMessageStartsWith(ex, "message");
			assertMessageEndsWith(ex, "expected: <1> but was: <2>");
			assertExpectedAndActualValues(ex, expected, actual);
		}
	}

	@Test
	void assertEqualsByteWithUnequalValuesAndMessageSupplier() {
		byte expected = 1;
		byte actual = 2;
		try {
			assertEquals(expected, actual, () -> "message");
			expectAssertionFailedError();
		}
		catch (AssertionFailedError ex) {
			assertMessageStartsWith(ex, "message");
			assertMessageEndsWith(ex, "expected: <1> but was: <2>");
			assertExpectedAndActualValues(ex, expected, actual);
		}
	}

	@Test
	void assertEqualsShort() {
		short expected = 1;
		short actual = 1;
		assertEquals(expected, actual);
	}

	@Test
	void assertEqualsShortWithUnequalValues() {
		short expected = 1;
		short actual = 2;
		try {
			assertEquals(expected, actual);
			expectAssertionFailedError();
		}
		catch (AssertionFailedError ex) {
			assertMessageEquals(ex, "expected: <1> but was: <2>");
			assertExpectedAndActualValues(ex, expected, actual);
		}
	}

	@Test
	void assertEqualsShortWithUnequalValuesAndMessage() {
		short expected = 1;
		short actual = 2;
		try {
			assertEquals(expected, actual, "message");
			expectAssertionFailedError();
		}
		catch (AssertionFailedError ex) {
			assertMessageStartsWith(ex, "message");
			assertMessageEndsWith(ex, "expected: <1> but was: <2>");
			assertExpectedAndActualValues(ex, expected, actual);
		}
	}

	@Test
	void assertEqualsShortWithUnequalValuesAndMessageSupplier() {
		short expected = 1;
		short actual = 2;
		try {
			assertEquals(expected, actual, () -> "message");
			expectAssertionFailedError();
		}
		catch (AssertionFailedError ex) {
			assertMessageStartsWith(ex, "message");
			assertMessageEndsWith(ex, "expected: <1> but was: <2>");
			assertExpectedAndActualValues(ex, expected, actual);
		}
	}

	@Test
	void assertEqualsInt() {
		assertEquals(1, 1);
	}

	@Test
	void assertEqualsIntWithUnequalValues() {
		try {
			assertEquals(1, 2);
			expectAssertionFailedError();
		}
		catch (AssertionFailedError ex) {
			assertMessageEquals(ex, "expected: <1> but was: <2>");
			assertExpectedAndActualValues(ex, 1, 2);
		}
	}

	@Test
	void assertEqualsIntWithUnequalValuesAndMessage() {
		try {
			assertEquals(1, 2, "message");
			expectAssertionFailedError();
		}
		catch (AssertionFailedError ex) {
			assertMessageStartsWith(ex, "message");
			assertMessageEndsWith(ex, "expected: <1> but was: <2>");
			assertExpectedAndActualValues(ex, 1, 2);
		}
	}

	@Test
	void assertEqualsIntWithUnequalValuesAndMessageSupplier() {
		try {
			assertEquals(1, 2, () -> "message");
			expectAssertionFailedError();
		}
		catch (AssertionFailedError ex) {
			assertMessageStartsWith(ex, "message");
			assertMessageEndsWith(ex, "expected: <1> but was: <2>");
			assertExpectedAndActualValues(ex, 1, 2);
		}
	}

	@Test
	void assertEqualsLong() {
		assertEquals(1L, 1L);
	}

	@Test
	void assertEqualsLongWithUnequalValues() {
		try {
			assertEquals(1L, 2L);
			expectAssertionFailedError();
		}
		catch (AssertionFailedError ex) {
			assertMessageEquals(ex, "expected: <1> but was: <2>");
			assertExpectedAndActualValues(ex, 1L, 2L);
		}
	}

	@Test
	void assertEqualsLongWithUnequalValuesAndMessage() {
		try {
			assertEquals(1L, 2L, "message");
			expectAssertionFailedError();
		}
		catch (AssertionFailedError ex) {
			assertMessageStartsWith(ex, "message");
			assertMessageEndsWith(ex, "expected: <1> but was: <2>");
			assertExpectedAndActualValues(ex, 1L, 2L);
		}
	}

	@Test
	void assertEqualsLongWithUnequalValuesAndMessageSupplier() {
		try {
			assertEquals(1L, 2L, () -> "message");
			expectAssertionFailedError();
		}
		catch (AssertionFailedError ex) {
			assertMessageStartsWith(ex, "message");
			assertMessageEndsWith(ex, "expected: <1> but was: <2>");
			assertExpectedAndActualValues(ex, 1L, 2L);
		}
	}

	@Test
	void assertEqualsChar() {
		assertEquals('a', 'a');
	}

	@Test
	void assertEqualsCharWithUnequalValues() {
		try {
			assertEquals('a', 'b');
			expectAssertionFailedError();
		}
		catch (AssertionFailedError ex) {
			assertMessageEquals(ex, "expected: <a> but was: <b>");
			assertExpectedAndActualValues(ex, 'a', 'b');
		}
	}

	@Test
	void assertEqualsCharWithUnequalValuesAndMessage() {
		try {
			assertEquals('a', 'b', "message");
			expectAssertionFailedError();
		}
		catch (AssertionFailedError ex) {
			assertMessageStartsWith(ex, "message");
			assertMessageEndsWith(ex, "expected: <a> but was: <b>");
			assertExpectedAndActualValues(ex, 'a', 'b');
		}
	}

	@Test
	void assertEqualsCharWithUnequalValuesAndMessageSupplier() {
		try {
			assertEquals('a', 'b', () -> "message");
			expectAssertionFailedError();
		}
		catch (AssertionFailedError ex) {
			assertMessageStartsWith(ex, "message");
			assertMessageEndsWith(ex, "expected: <a> but was: <b>");
			assertExpectedAndActualValues(ex, 'a', 'b');
		}
	}

	@Test
	void assertEqualsFloat() {
		assertEquals(1.0f, 1.0f);
		assertEquals(Float.NaN, Float.NaN);
		assertEquals(Float.NEGATIVE_INFINITY, Float.NEGATIVE_INFINITY);
		assertEquals(Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY);
		assertEquals(Float.MIN_VALUE, Float.MIN_VALUE);
		assertEquals(Float.MAX_VALUE, Float.MAX_VALUE);
		assertEquals(Float.MIN_NORMAL, Float.MIN_NORMAL);
		assertEquals(Double.NaN, Float.NaN);
	}

	@Test
	void assertEqualsFloatWithUnequalValues() {
		try {
			assertEquals(1.0f, 1.1f);
			expectAssertionFailedError();
		}
		catch (AssertionFailedError ex) {
			assertMessageEquals(ex, "expected: <1.0> but was: <1.1>");
			assertExpectedAndActualValues(ex, 1.0f, 1.1f);
		}
	}

	@Test
	void assertEqualsFloatWithUnequalValuesAndMessage() {
		try {
			assertEquals(1.0f, 1.1f, "message");
			expectAssertionFailedError();
		}
		catch (AssertionFailedError ex) {
			assertMessageStartsWith(ex, "message");
			assertMessageEndsWith(ex, "expected: <1.0> but was: <1.1>");
			assertExpectedAndActualValues(ex, 1.0f, 1.1f);
		}
	}

	@Test
	void assertEqualsFloatWithUnequalValuesAndMessageSupplier() {
		try {
			assertEquals(1.0f, 1.1f, () -> "message");
			expectAssertionFailedError();
		}
		catch (AssertionFailedError ex) {
			assertMessageStartsWith(ex, "message");
			assertMessageEndsWith(ex, "expected: <1.0> but was: <1.1>");
			assertExpectedAndActualValues(ex, 1.0f, 1.1f);
		}
	}

	@Test
	void assertEqualsFloatWithDelta() {
		assertEquals(0.56f, 0.6f, 0.05f);
		assertEquals(0.01f, 0.011f, 0.002f);
		assertEquals(Float.NaN, Float.NaN, 0.5f);
	}

	@Test
	void assertEqualsFloatWithIllegalDelta() {
		AssertionFailedError e1 = expectThrows(AssertionFailedError.class, () -> assertEquals(0.1f, 0.2f, -0.9f));
		assertMessageEndsWith(e1, "positive delta expected but was: <-0.9>");

		AssertionFailedError e2 = expectThrows(AssertionFailedError.class, () -> assertEquals(.0f, .0f, -10.5f));
		assertMessageEndsWith(e2, "positive delta expected but was: <-10.5>");

		AssertionFailedError e3 = expectThrows(AssertionFailedError.class, () -> assertEquals(4.5f, 4.6f, Float.NaN));
		assertMessageEndsWith(e3, "positive delta expected but was: <NaN>");
	}

	@Test
	void assertEqualsFloatWithDeltaWithUnequalValues() {
		AssertionFailedError e1 = expectThrows(AssertionFailedError.class, () -> assertEquals(0.5f, 0.2f, 0.2f));
		assertMessageEndsWith(e1, "expected: <0.5> but was: <0.2>");

		AssertionFailedError e2 = expectThrows(AssertionFailedError.class, () -> assertEquals(0.1f, 0.2f, 0.000001f));
		assertMessageEndsWith(e2, "expected: <0.1> but was: <0.2>");

		AssertionFailedError e3 = expectThrows(AssertionFailedError.class, () -> assertEquals(100.0f, 50.0f, 10.0f));
		assertMessageEndsWith(e3, "expected: <100.0> but was: <50.0>");

		AssertionFailedError e4 = expectThrows(AssertionFailedError.class, () -> assertEquals(-3.5f, -3.3f, 0.01f));
		assertMessageEndsWith(e4, "expected: <-3.5> but was: <-3.3>");

		AssertionFailedError e5 = expectThrows(AssertionFailedError.class, () -> assertEquals(+0.0f, -0.001f, .00001f));
		assertMessageEndsWith(e5, "expected: <0.0> but was: <-0.001>");
	}

	@Test
	void assertEqualsFloatWithDeltaWithUnequalValuesAndMessage() {
		Executable assertion = () -> assertEquals(0.5f, 0.45f, 0.03f, "message");

		AssertionFailedError e = expectThrows(AssertionFailedError.class, assertion);

		assertMessageStartsWith(e, "message");
		assertMessageEndsWith(e, "expected: <0.5> but was: <0.45>");
		assertExpectedAndActualValues(e, 0.5f, 0.45f);
	}

	@Test
	void assertEqualsFloatWithDeltaWithUnequalValuesAndMessageSupplier() {
		Executable assertion = () -> assertEquals(0.5f, 0.45f, 0.03f, () -> "message");

		AssertionFailedError e = expectThrows(AssertionFailedError.class, assertion);

		assertMessageStartsWith(e, "message");
		assertMessageEndsWith(e, "expected: <0.5> but was: <0.45>");
		assertExpectedAndActualValues(e, 0.5f, 0.45f);
	}

	@Test
	void assertEqualsDouble() {
		assertEquals(1.0d, 1.0d);
		assertEquals(Double.NaN, Double.NaN);
		assertEquals(Double.NEGATIVE_INFINITY, Double.NEGATIVE_INFINITY);
		assertEquals(Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY);
		assertEquals(Double.MIN_VALUE, Double.MIN_VALUE);
		assertEquals(Double.MAX_VALUE, Double.MAX_VALUE);
		assertEquals(Double.MIN_NORMAL, Double.MIN_NORMAL);
	}

	@Test
	void assertEqualsDoubleWithUnequalValues() {
		try {
			assertEquals(1.0d, 1.1d);
			expectAssertionFailedError();
		}
		catch (AssertionFailedError ex) {
			assertMessageEquals(ex, "expected: <1.0> but was: <1.1>");
			assertExpectedAndActualValues(ex, 1.0d, 1.1d);
		}
	}

	@Test
	void assertEqualsDoubleWithUnequalValuesAndMessage() {
		try {
			assertEquals(1.0d, 1.1d, "message");
			expectAssertionFailedError();
		}
		catch (AssertionFailedError ex) {
			assertMessageStartsWith(ex, "message");
			assertMessageEndsWith(ex, "expected: <1.0> but was: <1.1>");
			assertExpectedAndActualValues(ex, 1.0d, 1.1d);
		}
	}

	@Test
	void assertEqualsDoubleWithUnequalValuesAndMessageSupplier() {
		try {
			assertEquals(1.0d, 1.1d, () -> "message");
			expectAssertionFailedError();
		}
		catch (AssertionFailedError ex) {
			assertMessageStartsWith(ex, "message");
			assertMessageEndsWith(ex, "expected: <1.0> but was: <1.1>");
			assertExpectedAndActualValues(ex, 1.0d, 1.1d);
		}
	}

	@Test
	void assertEqualsDoubleWithDelta() {
		assertEquals(0.42d, 0.24d, 0.19d);
		assertEquals(0.02d, 0.011d, 0.01d);
		assertEquals(Double.NaN, Double.NaN, 0.2d);
	}

	@Test
	void assertEqualsDoubleWithIllegalDelta() {
		AssertionFailedError e1 = expectThrows(AssertionFailedError.class, () -> assertEquals(1.1d, 1.11d, -0.5d));
		assertMessageEndsWith(e1, "positive delta expected but was: <-0.5>");

		AssertionFailedError e2 = expectThrows(AssertionFailedError.class, () -> assertEquals(.55d, .56d, -10.5d));
		assertMessageEndsWith(e2, "positive delta expected but was: <-10.5>");

		AssertionFailedError e3 = expectThrows(AssertionFailedError.class, () -> assertEquals(1.1d, 1.1d, Double.NaN));
		assertMessageEndsWith(e3, "positive delta expected but was: <NaN>");
	}

	@Test
	void assertEqualsDoubleWithDeltaWithUnequalValues() {
		AssertionFailedError e1 = expectThrows(AssertionFailedError.class, () -> assertEquals(9.9d, 9.7d, 0.1d));
		assertMessageEndsWith(e1, "expected: <9.9> but was: <9.7>");
		assertExpectedAndActualValues(e1, 9.9d, 9.7d);

		AssertionFailedError e2 = expectThrows(AssertionFailedError.class, () -> assertEquals(0.1d, 0.05d, 0.001d));
		assertMessageEndsWith(e2, "expected: <0.1> but was: <0.05>");
		assertExpectedAndActualValues(e2, 0.1d, 0.05d);

		AssertionFailedError e3 = expectThrows(AssertionFailedError.class, () -> assertEquals(17.11d, 15.11d, 1.1d));
		assertMessageEndsWith(e3, "expected: <17.11> but was: <15.11>");
		assertExpectedAndActualValues(e3, 17.11d, 15.11d);

		AssertionFailedError e4 = expectThrows(AssertionFailedError.class, () -> assertEquals(-7.2d, -5.9d, 1.1d));
		assertMessageEndsWith(e4, "expected: <-7.2> but was: <-5.9>");
		assertExpectedAndActualValues(e4, -7.2d, -5.9d);

		AssertionFailedError e5 = expectThrows(AssertionFailedError.class, () -> assertEquals(+0.0d, -0.001d, .00001d));
		assertMessageEndsWith(e5, "expected: <0.0> but was: <-0.001>");
		assertExpectedAndActualValues(e5, +0.0d, -0.001d);
	}

	@Test
	void assertEqualsDoubleWithDeltaWithUnequalValuesAndMessage() {
		Executable assertion = () -> assertEquals(42.42d, 42.4d, 0.001d, "message");

		AssertionFailedError e = expectThrows(AssertionFailedError.class, assertion);

		assertMessageStartsWith(e, "message");
		assertMessageEndsWith(e, "expected: <42.42> but was: <42.4>");
		assertExpectedAndActualValues(e, 42.42d, 42.4d);
	}

	@Test
	void assertEqualsDoubleWithDeltaWithUnequalValuesAndMessageSupplier() {
		Executable assertion = () -> assertEquals(0.9d, 10.12d, 5.001d, () -> "message");

		AssertionFailedError e = expectThrows(AssertionFailedError.class, assertion);

		assertMessageStartsWith(e, "message");
		assertMessageEndsWith(e, "expected: <0.9> but was: <10.12>");
		assertExpectedAndActualValues(e, 0.9d, 10.12d);
	}

	@Test
	void assertEqualsWithTwoNulls() {
		assertEquals(null, null);
	}

	@Test
	void assertEqualsWithSameObject() {
		Object foo = new Object();
		assertEquals(foo, foo);
	}

	@Test
	void assertEqualsWithEquivalentStrings() {
		assertEquals(new String("foo"), new String("foo"));
	}

	@Test
	void assertEqualsWithNullVsObject() {
		try {
			assertEquals(null, "foo");
			expectAssertionFailedError();
		}
		catch (AssertionFailedError ex) {
			assertMessageEquals(ex, "expected: <null> but was: <foo>");
			assertExpectedAndActualValues(ex, null, "foo");
		}
	}

	@Test
	void assertEqualsWithObjectVsNull() {
		try {
			assertEquals("foo", null);
			expectAssertionFailedError();
		}
		catch (AssertionFailedError ex) {
			assertMessageEquals(ex, "expected: <foo> but was: <null>");
			assertExpectedAndActualValues(ex, "foo", null);
		}
	}

	@Test
	void assertEqualsWithNullVsObjectAndMessageSupplier() {
		try {
			assertEquals(null, "foo", () -> "test");
			expectAssertionFailedError();
		}
		catch (AssertionFailedError ex) {
			assertMessageStartsWith(ex, "test");
			assertMessageEndsWith(ex, "expected: <null> but was: <foo>");
			assertExpectedAndActualValues(ex, null, "foo");
		}
	}

	@Test
	void assertEqualsWithObjectVsNullAndMessageSupplier() {
		try {
			assertEquals("foo", null, () -> "test");
			expectAssertionFailedError();
		}
		catch (AssertionFailedError ex) {
			assertMessageStartsWith(ex, "test");
			assertMessageEndsWith(ex, "expected: <foo> but was: <null>");
			assertExpectedAndActualValues(ex, "foo", null);
		}
	}

	@Test
	void assertEqualsInvokesEqualsMethodForIdenticalObjects() {
		Object obj = new EqualsThrowsExceptionClass();
		assertThrows(NumberFormatException.class, () -> assertEquals(obj, obj));
	}

	// --- assertArrayEquals -------------------------------------------------

	@Test
	void assertArrayEqualsWithNulls() {
		assertArrayEquals((boolean[]) null, (boolean[]) null);
		assertArrayEquals((char[]) null, (char[]) null);
		assertArrayEquals((byte[]) null, (byte[]) null);
		assertArrayEquals((int[]) null, (int[]) null);
		assertArrayEquals((long[]) null, (long[]) null);
		assertArrayEquals((float[]) null, (float[]) null);
		assertArrayEquals((double[]) null, (double[]) null);
		assertArrayEquals((Object[]) null, (Object[]) null);
	}

	@Test
	void assertArrayEqualsBooleanArrays() {
		assertArrayEquals(new boolean[] {}, new boolean[] {});
		assertArrayEquals(new boolean[] { true }, new boolean[] { true });
		assertArrayEquals(new boolean[] { false, true, false, false }, new boolean[] { false, true, false, false });
	}

	@Test
	void assertArrayEqualsBooleanArrayVsNull() {
		try {
			assertArrayEquals(null, new boolean[] { true, false });
			expectAssertionFailedError();
		}
		catch (AssertionFailedError ex) {
			assertMessageEquals(ex, "expected array was <null>");
		}

		try {
			assertArrayEquals(new boolean[] { true, false }, null);
			expectAssertionFailedError();
		}
		catch (AssertionFailedError ex) {
			assertMessageEquals(ex, "actual array was <null>");
		}
	}

	@Test
	void assertArrayEqualsBooleanArrayVsNullAndMessage() {
		try {
			assertArrayEquals(null, new boolean[] { true, false }, "message");
			expectAssertionFailedError();
		}
		catch (AssertionFailedError ex) {
			assertMessageStartsWith(ex, "message");
			assertMessageEndsWith(ex, "expected array was <null>");
		}

		try {
			assertArrayEquals(new boolean[] { true, false }, null, "message");
			expectAssertionFailedError();
		}
		catch (AssertionFailedError ex) {
			assertMessageStartsWith(ex, "message");
			assertMessageEndsWith(ex, "actual array was <null>");
		}
	}

	@Test
	void assertArrayEqualsBooleanArrayVsNullAndMessageSupplier() {
		try {
			assertArrayEquals(null, new boolean[] { true, false }, () -> "message");
			expectAssertionFailedError();
		}
		catch (AssertionFailedError ex) {
			assertMessageStartsWith(ex, "message");
			assertMessageEndsWith(ex, "expected array was <null>");
		}

		try {
			assertArrayEquals(new boolean[] { true, false }, null, () -> "message");
			expectAssertionFailedError();
		}
		catch (AssertionFailedError ex) {
			assertMessageStartsWith(ex, "message");
			assertMessageEndsWith(ex, "actual array was <null>");
		}
	}

	@Test
	void assertArrayEqualsBooleanArraysOfDifferentLength() {
		try {
			assertArrayEquals(new boolean[] { true, false }, new boolean[] { true, false, true });
			expectAssertionFailedError();
		}
		catch (AssertionFailedError ex) {
			assertMessageEquals(ex, "array lengths differ, expected: <2> but was: <3>");
		}
	}

	@Test
	void assertArrayEqualsBooleanArraysOfDifferentLengthAndMessage() {
		try {
			assertArrayEquals(new boolean[] { true, false, false }, new boolean[] { true }, "message");
			expectAssertionFailedError();
		}
		catch (AssertionFailedError ex) {
			assertMessageStartsWith(ex, "message");
			assertMessageEndsWith(ex, "array lengths differ, expected: <3> but was: <1>");
		}
	}

	@Test
	void assertArrayEqualsBooleanArraysOfDifferentLengthAndMessageSupplier() {
		try {
			assertArrayEquals(new boolean[] { true }, new boolean[] { true, false }, () -> "message");
			expectAssertionFailedError();
		}
		catch (AssertionFailedError ex) {
			assertMessageStartsWith(ex, "message");
			assertMessageEndsWith(ex, "array lengths differ, expected: <1> but was: <2>");
		}
	}

	@Test
	void assertArrayEqualsDifferentBooleanArrays() {
		try {
			assertArrayEquals(new boolean[] { true, false, false }, new boolean[] { true, false, true });
			expectAssertionFailedError();
		}
		catch (AssertionFailedError ex) {
			assertMessageEquals(ex, "array contents differ at index [2], expected: <false> but was: <true>");
		}
	}

	@Test
	void assertArrayEqualsDifferentBooleanArraysAndMessage() {
		try {
			assertArrayEquals(new boolean[] { true, true }, new boolean[] { false, true }, "message");
			expectAssertionFailedError();
		}
		catch (AssertionFailedError ex) {
			assertMessageStartsWith(ex, "message");
			assertMessageEndsWith(ex, "array contents differ at index [0], expected: <true> but was: <false>");
		}
	}

	@Test
	void assertArrayEqualsDifferentBooleanArraysAndMessageSupplier() {
		try {
			assertArrayEquals(new boolean[] { false, false, false }, new boolean[] { false, true, true },
				() -> "message");
			expectAssertionFailedError();
		}
		catch (AssertionFailedError ex) {
			assertMessageStartsWith(ex, "message");
			assertMessageEndsWith(ex, "array contents differ at index [1], expected: <false> but was: <true>");
		}
	}

	@Test
	void assertArrayEqualsCharArrays() {
		assertArrayEquals(new char[] {}, new char[] {});
		assertArrayEquals(new char[] { 'a' }, new char[] { 'a' });
		assertArrayEquals(new char[] { 'j', 'u', 'n', 'i', 't' }, new char[] { 'j', 'u', 'n', 'i', 't' });
	}

	@Test
	void assertArrayEqualsCharArrayVsNull() {
		try {
			assertArrayEquals(null, new char[] { 'a', 'z' });
			expectAssertionFailedError();
		}
		catch (AssertionFailedError ex) {
			assertMessageEquals(ex, "expected array was <null>");
		}

		try {
			assertArrayEquals(new char[] { 'a', 'z' }, null);
			expectAssertionFailedError();
		}
		catch (AssertionFailedError ex) {
			assertMessageEquals(ex, "actual array was <null>");
		}
	}

	@Test
	void assertArrayEqualsCharArrayVsNullAndMessage() {
		try {
			assertArrayEquals(null, new char[] { 'a', 'b', 'z' }, "message");
			expectAssertionFailedError();
		}
		catch (AssertionFailedError ex) {
			assertMessageStartsWith(ex, "message");
			assertMessageEndsWith(ex, "expected array was <null>");
		}

		try {
			assertArrayEquals(new char[] { 'a', 'b', 'z' }, null, "message");
			expectAssertionFailedError();
		}
		catch (AssertionFailedError ex) {
			assertMessageStartsWith(ex, "message");
			assertMessageEndsWith(ex, "actual array was <null>");
		}
	}

	@Test
	void assertArrayEqualsCharArrayVsNullAndMessageSupplier() {
		try {
			assertArrayEquals(null, new char[] { 'z', 'x', 'y' }, () -> "message");
			expectAssertionFailedError();
		}
		catch (AssertionFailedError ex) {
			assertMessageStartsWith(ex, "message");
			assertMessageEndsWith(ex, "expected array was <null>");
		}

		try {
			assertArrayEquals(new char[] { 'z', 'x', 'y' }, null, () -> "message");
			expectAssertionFailedError();
		}
		catch (AssertionFailedError ex) {
			assertMessageStartsWith(ex, "message");
			assertMessageEndsWith(ex, "actual array was <null>");
		}
	}

	@Test
	void assertArrayEqualsCharArraysOfDifferentLength() {
		try {
			assertArrayEquals(new char[] { 'q', 'w', 'e' }, new char[] { 'q', 'w' });
			expectAssertionFailedError();
		}
		catch (AssertionFailedError ex) {
			assertMessageEquals(ex, "array lengths differ, expected: <3> but was: <2>");
		}
	}

	@Test
	void assertArrayEqualsCharArraysOfDifferentLengthAndMessage() {
		try {
			assertArrayEquals(new char[] { 'a', 's', 'd' }, new char[] { 'd' }, "message");
			expectAssertionFailedError();
		}
		catch (AssertionFailedError ex) {
			assertMessageStartsWith(ex, "message");
			assertMessageEndsWith(ex, "array lengths differ, expected: <3> but was: <1>");
		}
	}

	@Test
	void assertArrayEqualsCharArraysOfDifferentLengthAndMessageSupplier() {
		try {
			assertArrayEquals(new char[] { 'q' }, new char[] { 't', 'u' }, () -> "message");
			expectAssertionFailedError();
		}
		catch (AssertionFailedError ex) {
			assertMessageStartsWith(ex, "message");
			assertMessageEndsWith(ex, "array lengths differ, expected: <1> but was: <2>");
		}
	}

	@Test
	void assertArrayEqualsDifferentCharArrays() {
		try {
			assertArrayEquals(new char[] { 'a', 'b', 'c' }, new char[] { 'a', 'b', 'a' });
			expectAssertionFailedError();
		}
		catch (AssertionFailedError ex) {
			assertMessageEquals(ex, "array contents differ at index [2], expected: <c> but was: <a>");
		}
	}

	@Test
	void assertArrayEqualsDifferentCharArraysAndMessage() {
		try {
			assertArrayEquals(new char[] { 'z', 'x', 'c', 'v' }, new char[] { 'x', 'x', 'c', 'v' }, "message");
			expectAssertionFailedError();
		}
		catch (AssertionFailedError ex) {
			assertMessageStartsWith(ex, "message");
			assertMessageEndsWith(ex, "array contents differ at index [0], expected: <z> but was: <x>");
		}
	}

	@Test
	void assertArrayEqualsDifferentCharArraysAndMessageSupplier() {
		try {
			assertArrayEquals(new char[] { 'r', 't', 'y' }, new char[] { 'r', 'y', 'u' }, () -> "message");
			expectAssertionFailedError();
		}
		catch (AssertionFailedError ex) {
			assertMessageStartsWith(ex, "message");
			assertMessageEndsWith(ex, "array contents differ at index [1], expected: <t> but was: <y>");
		}
	}

	@Test
	void assertArrayEqualsByteArrays() {
		assertArrayEquals(new byte[] {}, new byte[] {});
		assertArrayEquals(new byte[] { 42 }, new byte[] { 42 });
		assertArrayEquals(new byte[] { 1, 2, 3, 42 }, new byte[] { 1, 2, 3, 42 });
	}

	@Test
	void assertArrayEqualsByteArrayVsNull() {
		try {
			assertArrayEquals(null, new byte[] { 7, 8, 9 });
			expectAssertionFailedError();
		}
		catch (AssertionFailedError ex) {
			assertMessageEquals(ex, "expected array was <null>");
		}

		try {
			assertArrayEquals(new byte[] { 7, 8, 9 }, null);
			expectAssertionFailedError();
		}
		catch (AssertionFailedError ex) {
			assertMessageEquals(ex, "actual array was <null>");
		}
	}

	@Test
	void assertArrayEqualsByteArrayVsNullAndMessage() {
		try {
			assertArrayEquals(null, new byte[] { 9, 8, 7 }, "message");
			expectAssertionFailedError();
		}
		catch (AssertionFailedError ex) {
			assertMessageStartsWith(ex, "message");
			assertMessageEndsWith(ex, "expected array was <null>");
		}

		try {
			assertArrayEquals(new byte[] { 9, 8, 7 }, null, "message");
			expectAssertionFailedError();
		}
		catch (AssertionFailedError ex) {
			assertMessageStartsWith(ex, "message");
			assertMessageEndsWith(ex, "actual array was <null>");
		}
	}

	@Test
	void assertArrayEqualsByteArrayVsNullAndMessageSupplier() {
		try {
			assertArrayEquals(null, new byte[] { 10, 20, 30 }, () -> "message");
			expectAssertionFailedError();
		}
		catch (AssertionFailedError ex) {
			assertMessageStartsWith(ex, "message");
			assertMessageEndsWith(ex, "expected array was <null>");
		}

		try {
			assertArrayEquals(new byte[] { 10, 20, 30 }, null, () -> "message");
			expectAssertionFailedError();
		}
		catch (AssertionFailedError ex) {
			assertMessageStartsWith(ex, "message");
			assertMessageEndsWith(ex, "actual array was <null>");
		}
	}

	@Test
	void assertArrayEqualsByteArraysOfDifferentLength() {
		try {
			assertArrayEquals(new byte[] { 1, 2, 100 }, new byte[] { 1, 2, 100, 101 });
			expectAssertionFailedError();
		}
		catch (AssertionFailedError ex) {
			assertMessageEquals(ex, "array lengths differ, expected: <3> but was: <4>");
		}
	}

	@Test
	void assertArrayEqualsByteArraysOfDifferentLengthAndMessage() {
		try {
			assertArrayEquals(new byte[] { 1, 2 }, new byte[] { 1, 2, 3, 4, 5, 6, 7, 8, 9 }, "message");
			expectAssertionFailedError();
		}
		catch (AssertionFailedError ex) {
			assertMessageStartsWith(ex, "message");
			assertMessageEndsWith(ex, "array lengths differ, expected: <2> but was: <9>");
		}
	}

	@Test
	void assertArrayEqualsByteArraysOfDifferentLengthAndMessageSupplier() {
		try {
			assertArrayEquals(new byte[] { 88, 99 }, new byte[] { 99, 88, 77 }, () -> "message");
			expectAssertionFailedError();
		}
		catch (AssertionFailedError ex) {
			assertMessageStartsWith(ex, "message");
			assertMessageEndsWith(ex, "array lengths differ, expected: <2> but was: <3>");
		}
	}

	@Test
	void assertArrayEqualsDifferentByteArrays() {
		try {
			assertArrayEquals(new byte[] { 12, 13, 12, 13 }, new byte[] { 12, 13, 12, 14 });
			expectAssertionFailedError();
		}
		catch (AssertionFailedError ex) {
			assertMessageEquals(ex, "array contents differ at index [3], expected: <13> but was: <14>");
		}
	}

	@Test
	void assertArrayEqualsDifferentByteArraysAndMessage() {
		try {
			assertArrayEquals(new byte[] { 1, 2, 3, 4, 5 }, new byte[] { 1, 2, 3, 5, 5 }, "message");
			expectAssertionFailedError();
		}
		catch (AssertionFailedError ex) {
			assertMessageStartsWith(ex, "message");
			assertMessageEndsWith(ex, "array contents differ at index [3], expected: <4> but was: <5>");
		}
	}

	@Test
	void assertArrayEqualsDifferentByteArraysAndMessageSupplier() {
		try {
			assertArrayEquals(new byte[] { 127, 126, -128, +127 }, new byte[] { 127, 126, -128, -127 },
				() -> "message");
			expectAssertionFailedError();
		}
		catch (AssertionFailedError ex) {
			assertMessageStartsWith(ex, "message");
			assertMessageEndsWith(ex, "array contents differ at index [3], expected: <127> but was: <-127>");
		}
	}

	@Test
	void assertArrayEqualsShortArrays() {
		assertArrayEquals(new short[] {}, new short[] {});
		assertArrayEquals(new short[] { 999 }, new short[] { 999 });
		assertArrayEquals(new short[] { 111, 222, 333, 444, 999 }, new short[] { 111, 222, 333, 444, 999 });
	}

	@Test
	void assertArrayEqualsShortArrayVsNull() {
		try {
			assertArrayEquals(null, new short[] { 5, 10, 12 });
			expectAssertionFailedError();
		}
		catch (AssertionFailedError ex) {
			assertMessageEquals(ex, "expected array was <null>");
		}

		try {
			assertArrayEquals(new short[] { 5, 10, 12 }, null);
			expectAssertionFailedError();
		}
		catch (AssertionFailedError ex) {
			assertMessageEquals(ex, "actual array was <null>");
		}
	}

	@Test
	void assertArrayEqualsShortArrayVsNullAndMessage() {
		try {
			assertArrayEquals(null, new short[] { 128, 129, 130 }, "message");
			expectAssertionFailedError();
		}
		catch (AssertionFailedError ex) {
			assertMessageStartsWith(ex, "message");
			assertMessageEndsWith(ex, "expected array was <null>");
		}

		try {
			assertArrayEquals(new short[] { -129, -130, -131 }, null, "message");
			expectAssertionFailedError();
		}
		catch (AssertionFailedError ex) {
			assertMessageStartsWith(ex, "message");
			assertMessageEndsWith(ex, "actual array was <null>");
		}
	}

	@Test
	void assertArrayEqualsShortArrayVsNullAndMessageSupplier() {
		try {
			assertArrayEquals(null, new short[] { 1, 2, 3, 4 }, () -> "message");
			expectAssertionFailedError();
		}
		catch (AssertionFailedError ex) {
			assertMessageStartsWith(ex, "message");
			assertMessageEndsWith(ex, "expected array was <null>");
		}

		try {
			assertArrayEquals(new short[] { -2000, 1, 2, 3, 4 }, null, () -> "message");
			expectAssertionFailedError();
		}
		catch (AssertionFailedError ex) {
			assertMessageStartsWith(ex, "message");
			assertMessageEndsWith(ex, "actual array was <null>");
		}
	}

	@Test
	void assertArrayEqualsShortArraysOfDifferentLength() {
		try {
			assertArrayEquals(new short[] { 1, 2, 3, 4, 5, 6 }, new short[] { 1, 2, 3 });
			expectAssertionFailedError();
		}
		catch (AssertionFailedError ex) {
			assertMessageEquals(ex, "array lengths differ, expected: <6> but was: <3>");
		}
	}

	@Test
	void assertArrayEqualsShortArraysOfDifferentLengthAndMessage() {
		try {
			assertArrayEquals(new short[] { 1, 2, 3, 10_000 }, new short[] { 10_000, 1, 2, 3, 4, 5, 6, 7 }, "message");
			expectAssertionFailedError();
		}
		catch (AssertionFailedError ex) {
			assertMessageStartsWith(ex, "message");
			assertMessageEndsWith(ex, "array lengths differ, expected: <4> but was: <8>");
		}
	}

	@Test
	void assertArrayEqualsShortArraysOfDifferentLengthAndMessageSupplier() {
		try {
			assertArrayEquals(new short[] { 150, 151 }, new short[] { 150, 151, 152 }, () -> "message");
			expectAssertionFailedError();
		}
		catch (AssertionFailedError ex) {
			assertMessageStartsWith(ex, "message");
			assertMessageEndsWith(ex, "array lengths differ, expected: <2> but was: <3>");
		}
	}

	@Test
	void assertArrayEqualsDifferentShortArrays() {
		try {
			assertArrayEquals(new short[] { 10, 100, 1000, 10000 }, new short[] { 1, 10, 100, 1000 });
			expectAssertionFailedError();
		}
		catch (AssertionFailedError ex) {
			assertMessageEquals(ex, "array contents differ at index [0], expected: <10> but was: <1>");
		}
	}

	@Test
	void assertArrayEqualsDifferentShortArraysAndMessage() {
		try {
			assertArrayEquals(new short[] { 1, 2, 100, -200 }, new short[] { 1, 2, 100, -500 }, "message");
			expectAssertionFailedError();
		}
		catch (AssertionFailedError ex) {
			assertMessageStartsWith(ex, "message");
			assertMessageEndsWith(ex, "array contents differ at index [3], expected: <-200> but was: <-500>");
		}
	}

	@Test
	void assertArrayEqualsDifferentShortArraysAndMessageSupplier() {
		try {
			assertArrayEquals(new short[] { 1000, 2000, +3000, 42 }, new short[] { 1000, 2000, -3000, 42 },
				() -> "message");
			expectAssertionFailedError();
		}
		catch (AssertionFailedError ex) {
			assertMessageStartsWith(ex, "message");
			assertMessageEndsWith(ex, "array contents differ at index [2], expected: <3000> but was: <-3000>");
		}
	}

	@Test
	void assertArrayEqualsIntArrays() {
		assertArrayEquals(new int[] {}, new int[] {});
		assertArrayEquals(new int[] { Integer.MAX_VALUE }, new int[] { Integer.MAX_VALUE });
		assertArrayEquals(new int[] { 1, 2, 3, 4, 5, 99_999 }, new int[] { 1, 2, 3, 4, 5, 99_999 });
	}

	@Test
	void assertArrayEqualsIntArrayVsNull() {
		try {
			assertArrayEquals(null, new int[] { Integer.MIN_VALUE, 2, 10 });
			expectAssertionFailedError();
		}
		catch (AssertionFailedError ex) {
			assertMessageEquals(ex, "expected array was <null>");
		}

		try {
			assertArrayEquals(new int[] { Integer.MIN_VALUE, 2, 10 }, null);
			expectAssertionFailedError();
		}
		catch (AssertionFailedError ex) {
			assertMessageEquals(ex, "actual array was <null>");
		}
	}

	@Test
	void assertArrayEqualsIntArrayVsNullAndMessage() {
		try {
			assertArrayEquals(null, new int[] { 99_999, 88_888, 1 }, "message");
			expectAssertionFailedError();
		}
		catch (AssertionFailedError ex) {
			assertMessageStartsWith(ex, "message");
			assertMessageEndsWith(ex, "expected array was <null>");
		}

		try {
			assertArrayEquals(new int[] { 99_999, 77_7777, 2 }, null, "message");
			expectAssertionFailedError();
		}
		catch (AssertionFailedError ex) {
			assertMessageStartsWith(ex, "message");
			assertMessageEndsWith(ex, "actual array was <null>");
		}
	}

	@Test
	void assertArrayEqualsIntArrayVsNullAndMessageSupplier() {
		try {
			assertArrayEquals(null, new int[] { 1, 10, 100, 1000, 10000, 100000 }, () -> "message");
			expectAssertionFailedError();
		}
		catch (AssertionFailedError ex) {
			assertMessageStartsWith(ex, "message");
			assertMessageEndsWith(ex, "expected array was <null>");
		}

		try {
			assertArrayEquals(new int[] { 100000, 10000, 1000, 100, 10, 1 }, null, () -> "message");
			expectAssertionFailedError();
		}
		catch (AssertionFailedError ex) {
			assertMessageStartsWith(ex, "message");
			assertMessageEndsWith(ex, "actual array was <null>");
		}
	}

	@Test
	void assertArrayEqualsIntArraysOfDifferentLength() {
		try {
			assertArrayEquals(new int[] { 1, 2, 3, Integer.MIN_VALUE, 4 }, new int[] { 1, Integer.MAX_VALUE, 2 });
			expectAssertionFailedError();
		}
		catch (AssertionFailedError ex) {
			assertMessageEquals(ex, "array lengths differ, expected: <5> but was: <3>");
		}
	}

	@Test
	void assertArrayEqualsIntArraysOfDifferentLengthAndMessage() {
		try {
			assertArrayEquals(new int[] { 100_000, 200_000, 1, 2 }, new int[] { 1, 2, 3 }, "message");
			expectAssertionFailedError();
		}
		catch (AssertionFailedError ex) {
			assertMessageStartsWith(ex, "message");
			assertMessageEndsWith(ex, "array lengths differ, expected: <4> but was: <3>");
		}
	}

	@Test
	void assertArrayEqualsIntArraysOfDifferentLengthAndMessageSupplier() {
		try {
			assertArrayEquals(new int[] { Integer.MAX_VALUE, Integer.MIN_VALUE }, new int[] { 1, 2, 3 },
				() -> "message");
			expectAssertionFailedError();
		}
		catch (AssertionFailedError ex) {
			assertMessageStartsWith(ex, "message");
			assertMessageEndsWith(ex, "array lengths differ, expected: <2> but was: <3>");
		}
	}

	@Test
	void assertArrayEqualsDifferentIntArrays() {
		try {
			assertArrayEquals(new int[] { Integer.MIN_VALUE, 1, 2, 10 }, new int[] { Integer.MIN_VALUE, 1, 10, 10 });
			expectAssertionFailedError();
		}
		catch (AssertionFailedError ex) {
			assertMessageEquals(ex, "array contents differ at index [2], expected: <2> but was: <10>");
		}
	}

	@Test
	void assertArrayEqualsDifferentIntArraysAndMessage() {
		try {
			assertArrayEquals(new int[] { 9, 10, 100, 100_000, 7 }, new int[] { 9, 10, 100, 100_000, 200_000 },
				"message");
			expectAssertionFailedError();
		}
		catch (AssertionFailedError ex) {
			assertMessageStartsWith(ex, "message");
			assertMessageEndsWith(ex, "array contents differ at index [4], expected: <7> but was: <200000>");
		}
	}

	@Test
	void assertArrayEqualsDifferentIntArraysAndMessageSupplier() {
		try {
			assertArrayEquals(new int[] { 1, Integer.MIN_VALUE, 2 }, new int[] { 1, Integer.MAX_VALUE, 2 },
				() -> "message");
			expectAssertionFailedError();
		}
		catch (AssertionFailedError ex) {
			assertMessageStartsWith(ex, "message");
			assertMessageEndsWith(ex,
				"array contents differ at index [1], expected: <-2147483648> but was: <2147483647>");
		}
	}

	@Test
	void assertArrayEqualsLongArrays() {
		assertArrayEquals(new long[] {}, new long[] {});
		assertArrayEquals(new long[] { Long.MAX_VALUE }, new long[] { Long.MAX_VALUE });
		assertArrayEquals(new long[] { Long.MIN_VALUE, 10, 20, 30 }, new long[] { Long.MIN_VALUE, 10, 20, 30 });
	}

	@Test
	void assertArrayEqualsLongArrayVsNull() {
		try {
			assertArrayEquals(null, new long[] { Long.MAX_VALUE, 2, 10 });
			expectAssertionFailedError();
		}
		catch (AssertionFailedError ex) {
			assertMessageEquals(ex, "expected array was <null>");
		}

		try {
			assertArrayEquals(new long[] { Long.MAX_VALUE, 2, 10 }, null);
			expectAssertionFailedError();
		}
		catch (AssertionFailedError ex) {
			assertMessageEquals(ex, "actual array was <null>");
		}
	}

	@Test
	void assertArrayEqualsLongArrayVsNullAndMessage() {
		try {
			assertArrayEquals(null, new long[] { 42, 4242, 424242, 4242424242L }, "message");
			expectAssertionFailedError();
		}
		catch (AssertionFailedError ex) {
			assertMessageStartsWith(ex, "message");
			assertMessageEndsWith(ex, "expected array was <null>");
		}

		try {
			assertArrayEquals(new long[] { 4242424242L, 424242, 4242, 42 }, null, "message");
			expectAssertionFailedError();
		}
		catch (AssertionFailedError ex) {
			assertMessageStartsWith(ex, "message");
			assertMessageEndsWith(ex, "actual array was <null>");
		}
	}

	@Test
	void assertArrayEqualsLongArrayVsNullAndMessageSupplier() {
		try {
			assertArrayEquals(null, new long[] { 12345678910L, 10, 9, 8 }, () -> "message");
			expectAssertionFailedError();
		}
		catch (AssertionFailedError ex) {
			assertMessageStartsWith(ex, "message");
			assertMessageEndsWith(ex, "expected array was <null>");
		}

		try {
			assertArrayEquals(new long[] { 8, 9, 10, 12345678910L }, null, () -> "message");
			expectAssertionFailedError();
		}
		catch (AssertionFailedError ex) {
			assertMessageStartsWith(ex, "message");
			assertMessageEndsWith(ex, "actual array was <null>");
		}
	}

	@Test
	void assertArrayEqualsLongArraysOfDifferentLength() {
		try {
			assertArrayEquals(new long[] { 1, 2, 3, Long.MIN_VALUE, 4 }, new long[] { 1, Long.MAX_VALUE, 2 });
			expectAssertionFailedError();
		}
		catch (AssertionFailedError ex) {
			assertMessageEquals(ex, "array lengths differ, expected: <5> but was: <3>");
		}
	}

	@Test
	void assertArrayEqualsLongArraysOfDifferentLengthAndMessage() {
		try {
			assertArrayEquals(new long[] { 100_000L, 200_000L, 1L, 2L }, new long[] { 1L, 2L, 3L }, "message");
			expectAssertionFailedError();
		}
		catch (AssertionFailedError ex) {
			assertMessageStartsWith(ex, "message");
			assertMessageEndsWith(ex, "array lengths differ, expected: <4> but was: <3>");
		}
	}

	@Test
	void assertArrayEqualsLongArraysOfDifferentLengthAndMessageSupplier() {
		try {
			assertArrayEquals(new long[] { Long.MAX_VALUE, Long.MIN_VALUE }, new long[] { 1L, 2L, 42L },
				() -> "message");
			expectAssertionFailedError();
		}
		catch (AssertionFailedError ex) {
			assertMessageStartsWith(ex, "message");
			assertMessageEndsWith(ex, "array lengths differ, expected: <2> but was: <3>");
		}
	}

	@Test
	void assertArrayEqualsDifferentLongArrays() {
		try {
			assertArrayEquals(new long[] { Long.MIN_VALUE, 17, 18L, 19 }, new long[] { Long.MIN_VALUE, 17, 18, 20 });
			expectAssertionFailedError();
		}
		catch (AssertionFailedError ex) {
			assertMessageEquals(ex, "array contents differ at index [3], expected: <19> but was: <20>");
		}
	}

	@Test
	void assertArrayEqualsDifferentLongArraysAndMessage() {
		try {
			assertArrayEquals(new long[] { 6, 5, 4, 3, 2, Long.MIN_VALUE }, new long[] { 6, 5, 4, 3, 2, 1 }, "message");
			expectAssertionFailedError();
		}
		catch (AssertionFailedError ex) {
			assertMessageStartsWith(ex, "message");
			assertMessageEndsWith(ex,
				"array contents differ at index [5], expected: <-9223372036854775808> but was: <1>");
		}
	}

	@Test
	void assertArrayEqualsDifferentLongArraysAndMessageSupplier() {
		try {
			assertArrayEquals(new long[] { 42, -9999L, 2 }, new long[] { 42L, +9999L, 2L }, () -> "message");
			expectAssertionFailedError();
		}
		catch (AssertionFailedError ex) {
			assertMessageStartsWith(ex, "message");
			assertMessageEndsWith(ex, "array contents differ at index [1], expected: <-9999> but was: <9999>");
		}
	}

	@Test
	void assertArrayEqualsFloatArrays() {
		assertArrayEquals(new float[] {}, new float[] {});
		assertArrayEquals(new float[] { Float.MAX_VALUE }, new float[] { Float.MAX_VALUE });
		assertArrayEquals(new float[] { Float.MIN_VALUE, 5F, 5.5F, 1.00F },
			new float[] { Float.MIN_VALUE, 5F, 5.5F, 1.00F });

		assertArrayEquals(new float[] { Float.NaN }, new float[] { Float.NaN });
		assertArrayEquals(new float[] { 10.18F, Float.NaN, 42.9F }, new float[] { 10.18F, Float.NaN, 42.9F });
	}

	@Test
	void assertArrayEqualsFloatArrayVsNull() {
		try {
			assertArrayEquals(null, new float[] { Float.MAX_VALUE, 4.2F, 9.0F });
			expectAssertionFailedError();
		}
		catch (AssertionFailedError ex) {
			assertMessageEquals(ex, "expected array was <null>");
		}

		try {
			assertArrayEquals(new float[] { Float.MIN_VALUE, 2.3F, 10.10F }, null);
			expectAssertionFailedError();
		}
		catch (AssertionFailedError ex) {
			assertMessageEquals(ex, "actual array was <null>");
		}
	}

	@Test
	void assertArrayEqualsFloatArrayVsNullAndMessage() {
		try {
			assertArrayEquals(null, new float[] { 42.42F, 42.4242F, 19.20F }, "message");
			expectAssertionFailedError();
		}
		catch (AssertionFailedError ex) {
			assertMessageStartsWith(ex, "message");
			assertMessageEndsWith(ex, "expected array was <null>");
		}

		try {
			assertArrayEquals(new float[] { 11.101F, 12.101F, 99.9F }, null, "message");
			expectAssertionFailedError();
		}
		catch (AssertionFailedError ex) {
			assertMessageStartsWith(ex, "message");
			assertMessageEndsWith(ex, "actual array was <null>");
		}
	}

	@Test
	void assertArrayEqualsFloatArrayVsNullAndMessageSupplier() {
		try {
			assertArrayEquals(null, new float[] { 5F, 6F, 7.77F, 8.88F }, () -> "message");
			expectAssertionFailedError();
		}
		catch (AssertionFailedError ex) {
			assertMessageStartsWith(ex, "message");
			assertMessageEndsWith(ex, "expected array was <null>");
		}

		try {
			assertArrayEquals(new float[] { 1F, 1.1F, 1.11F, 1.111F }, null, () -> "message");
			expectAssertionFailedError();
		}
		catch (AssertionFailedError ex) {
			assertMessageStartsWith(ex, "message");
			assertMessageEndsWith(ex, "actual array was <null>");
		}
	}

	@Test
	void assertArrayEqualsFloatArraysOfDifferentLength() {
		try {
			assertArrayEquals(new float[] { Float.MIN_VALUE, 1F, 2F, 3F }, new float[] { Float.MAX_VALUE, 7.1F });
			expectAssertionFailedError();
		}
		catch (AssertionFailedError ex) {
			assertMessageEquals(ex, "array lengths differ, expected: <4> but was: <2>");
		}
	}

	@Test
	void assertArrayEqualsFloatArraysOfDifferentLengthAndMessage() {
		try {
			assertArrayEquals(new float[] { 19.1F, 12.77F, 18.F }, new float[] { .9F, .8F, 5.123F, .10F }, "message");
			expectAssertionFailedError();
		}
		catch (AssertionFailedError ex) {
			assertMessageStartsWith(ex, "message");
			assertMessageEndsWith(ex, "array lengths differ, expected: <3> but was: <4>");
		}
	}

	@Test
	void assertArrayEqualsFloatArraysOfDifferentLengthAndMessageSupplier() {
		try {
			assertArrayEquals(new float[] { 1.1F, 1.2F, 1.3F }, new float[] { 1F, 2F, 3F, 4F, 5F, 6F },
				() -> "message");
			expectAssertionFailedError();
		}
		catch (AssertionFailedError ex) {
			assertMessageStartsWith(ex, "message");
			assertMessageEndsWith(ex, "array lengths differ, expected: <3> but was: <6>");
		}
	}

	@Test
	void assertArrayEqualsDifferentFloatArrays() {
		try {
			assertArrayEquals(new float[] { 5.5F, 6.5F, 7.5F, 8.5F }, new float[] { 5.5F, 6.5F, 7.4F, 8.5F });
			expectAssertionFailedError();
		}
		catch (AssertionFailedError ex) {
			assertMessageEquals(ex, "array contents differ at index [2], expected: <7.5> but was: <7.4>");
		}

		try {
			assertArrayEquals(new float[] { 1.0F, 2.0F, 3.0F, Float.NaN }, new float[] { 1.0F, 2.0F, 3.0F, 4.0F });
			expectAssertionFailedError();
		}
		catch (AssertionFailedError ex) {
			assertMessageEquals(ex, "array contents differ at index [3], expected: <NaN> but was: <4.0>");
		}
	}

	@Test
	void assertArrayEqualsDifferentFloatArraysAndMessage() {
		try {
			assertArrayEquals(new float[] { 1.9F, 0.5F, 0.4F, 0.3F }, new float[] { 1.9F, 0.5F, 0.4F, -0.333F },
				"message");
			expectAssertionFailedError();
		}
		catch (AssertionFailedError ex) {
			assertMessageStartsWith(ex, "message");
			assertMessageEndsWith(ex, "array contents differ at index [3], expected: <0.3> but was: <-0.333>");
		}
	}

	@Test
	void assertArrayEqualsDifferentFloatArraysAndMessageSupplier() {
		try {
			assertArrayEquals(new float[] { 0.3F, 0.9F, 8F }, new float[] { 0.3F, Float.MIN_VALUE, 8F },
				() -> "message");
			expectAssertionFailedError();
		}
		catch (AssertionFailedError ex) {
			assertMessageStartsWith(ex, "message");
			assertMessageEndsWith(ex, "array contents differ at index [1], expected: <0.9> but was: <1.4E-45>");
		}
	}

	@Test
	void assertArrayEqualsDeltaFloatArrays() {
		assertArrayEquals(new float[] {}, new float[] {}, 0.001F);
		assertArrayEquals(new float[] { Float.MAX_VALUE }, new float[] { Float.MAX_VALUE }, 0.0001F);
		assertArrayEquals(new float[] { Float.MIN_VALUE, 2.111F, 2.521F, 1.01F },
			new float[] { Float.MIN_VALUE, 2.119F, 2.523F, 1.01001F }, 0.01F);

		assertArrayEquals(new float[] { Float.NaN }, new float[] { Float.NaN }, 0.1F);
		assertArrayEquals(new float[] { 10.18F, Float.NaN, 42.9F }, new float[] { 10.98F, Float.NaN, 43.9F }, 1F);
	}

	@Test
	void assertArrayEqualsDeltaFloatArraysThrowsForIllegalDelta() {
		try {
			assertArrayEquals(new float[] {}, new float[] {}, -0.5F);
			expectAssertionFailedError();
		}
		catch (AssertionFailedError ex) {
			assertMessageEquals(ex, "positive delta expected but was: <-0.5>");
		}
		try {
			assertArrayEquals(new float[] {}, new float[] {}, Float.NaN);
			expectAssertionFailedError();
		}
		catch (AssertionFailedError ex) {
			assertMessageEquals(ex, "positive delta expected but was: <NaN>");
		}

		try {
			assertArrayEquals(new float[] { 12.9F, 7F, 13F }, new float[] { 12.9F, 7F, 13F }, -0.5F);
			expectAssertionFailedError();
		}
		catch (AssertionFailedError ex) {
			assertMessageEquals(ex, "positive delta expected but was: <-0.5>");
		}
		try {
			assertArrayEquals(new float[] { 1.11F, 1.11F, 9F }, new float[] { 1.11F, 1.11F, 9F, 10F }, Float.NaN);
			expectAssertionFailedError();
		}
		catch (AssertionFailedError ex) {
			assertMessageEquals(ex, "positive delta expected but was: <NaN>");
		}
	}

	@Test
	void assertArrayEqualsDeltaFloatArrayVsNull() {
		try {
			assertArrayEquals(null, new float[] { Float.MAX_VALUE, 4.2F, 9.0F }, 0.001F);
			expectAssertionFailedError();
		}
		catch (AssertionFailedError ex) {
			assertMessageEquals(ex, "expected array was <null>");
		}

		try {
			assertArrayEquals(new float[] { Float.MIN_VALUE, 2.3F, 10.10F }, null, 0.01F);
			expectAssertionFailedError();
		}
		catch (AssertionFailedError ex) {
			assertMessageEquals(ex, "actual array was <null>");
		}
	}

	@Test
	void assertArrayEqualsDeltaFloatArrayVsNullAndMessage() {
		try {
			assertArrayEquals(null, new float[] { 42.42F, 42.4242F, 19.20F }, 0.0001F, "message");
			expectAssertionFailedError();
		}
		catch (AssertionFailedError ex) {
			assertMessageStartsWith(ex, "message");
			assertMessageEndsWith(ex, "expected array was <null>");
		}

		try {
			assertArrayEquals(new float[] { 11.101F, 12.101F, 99.9F }, null, 0.01F, "message");
			expectAssertionFailedError();
		}
		catch (AssertionFailedError ex) {
			assertMessageStartsWith(ex, "message");
			assertMessageEndsWith(ex, "actual array was <null>");
		}
	}

	@Test
	void assertArrayEqualsDeltaFloatArrayVsNullAndMessageSupplier() {
		try {
			assertArrayEquals(null, new float[] { 5F, 6F, 7.77F, 8.88F }, 0.1F, () -> "message");
			expectAssertionFailedError();
		}
		catch (AssertionFailedError ex) {
			assertMessageStartsWith(ex, "message");
			assertMessageEndsWith(ex, "expected array was <null>");
		}

		try {
			assertArrayEquals(new float[] { 1F, 1.1F, 1.11F, 1.111F }, null, 0.1F, () -> "message");
			expectAssertionFailedError();
		}
		catch (AssertionFailedError ex) {
			assertMessageStartsWith(ex, "message");
			assertMessageEndsWith(ex, "actual array was <null>");
		}
	}

	@Test
	void assertArrayEqualsDeltaFloatArraysOfDifferentLength() {
		try {
			assertArrayEquals(new float[] { Float.MIN_VALUE, 1F, 2F, 3F }, new float[] { Float.MAX_VALUE, 7.1F }, 0.1F);
			expectAssertionFailedError();
		}
		catch (AssertionFailedError ex) {
			assertMessageEquals(ex, "array lengths differ, expected: <4> but was: <2>");
		}
	}

	@Test
	void assertArrayEqualsDeltaFloatArraysOfDifferentLengthAndMessage() {
		try {
			assertArrayEquals(new float[] { 19.1F, 12.77F }, new float[] { .9F, .8F, 5.123F }, 0.1F, "message");
			expectAssertionFailedError();
		}
		catch (AssertionFailedError ex) {
			assertMessageStartsWith(ex, "message");
			assertMessageEndsWith(ex, "array lengths differ, expected: <2> but was: <3>");
		}
	}

	@Test
	void assertArrayEqualsDeltaFloatArraysOfDifferentLengthAndMessageSupplier() {
		try {
			assertArrayEquals(new float[] { 1.1F, 1.2F, 1.3F }, new float[] { 1F, 2F, 3F, 4F }, 0.1F, () -> "message");
			expectAssertionFailedError();
		}
		catch (AssertionFailedError ex) {
			assertMessageStartsWith(ex, "message");
			assertMessageEndsWith(ex, "array lengths differ, expected: <3> but was: <4>");
		}
	}

	@Test
	void assertArrayEqualsDeltaDifferentFloatArrays() {
		try {
			assertArrayEquals(new float[] { 5.6F, 3.2F, 9.1F, 0.5F }, new float[] { 5.55F, 3.3F, 9.201F, 0.51F }, 0.1F);
			expectAssertionFailedError();
		}
		catch (AssertionFailedError ex) {
			assertMessageEquals(ex, "array contents differ at index [2], expected: <9.1> but was: <9.201>");
		}

		try {
			assertArrayEquals(new float[] { 1.0F, 2.0F, 3.0F, Float.NaN }, new float[] { 1.5F, 1.5F, 2.9F, 4.0F },
				0.5F);
			expectAssertionFailedError();
		}
		catch (AssertionFailedError ex) {
			assertMessageEquals(ex, "array contents differ at index [3], expected: <NaN> but was: <4.0>");
		}
	}

	@Test
	void assertArrayEqualsDeltaDifferentFloatArraysAndMessage() {
		try {
			assertArrayEquals(new float[] { 1.91F, 0.5F, .4F, 0.3F }, new float[] { 2F, 0.509F, .499F, -0.333F }, 0.1F,
				"message");
			expectAssertionFailedError();
		}
		catch (AssertionFailedError ex) {
			assertMessageStartsWith(ex, "message");
			assertMessageEndsWith(ex, "array contents differ at index [3], expected: <0.3> but was: <-0.333>");
		}
	}

	@Test
	void assertArrayEqualsDeltaDifferentFloatArraysAndMessageSupplier() {
		try {
			assertArrayEquals(new float[] { 0.3F, 0.9F, 8F }, new float[] { 0.6F, Float.MIN_VALUE, 8.4F }, 0.5F,
				() -> "message");
			expectAssertionFailedError();
		}
		catch (AssertionFailedError ex) {
			assertMessageStartsWith(ex, "message");
			assertMessageEndsWith(ex, "array contents differ at index [1], expected: <0.9> but was: <1.4E-45>");
		}
	}

	@Test
	void assertArrayEqualsDoubleArrays() {
		assertArrayEquals(new double[] {}, new double[] {});
		assertArrayEquals(new double[] { Double.MAX_VALUE }, new double[] { Double.MAX_VALUE });
		assertArrayEquals(new double[] { Double.MIN_VALUE, 2.1, 5.5, 1.0 },
			new double[] { Double.MIN_VALUE, 2.1, 5.5, 1.0 });

		assertArrayEquals(new double[] { Double.NaN }, new double[] { Double.NaN });
		assertArrayEquals(new double[] { 1.2, 10.8, Double.NaN, 42.9 }, new double[] { 1.2, 10.8, Double.NaN, 42.9 });
	}

	@Test
	void assertArrayEqualsDoubleArrayVsNull() {
		try {
			assertArrayEquals(null, new double[] { Double.MAX_VALUE, 17.4, 98.7654321 });
			expectAssertionFailedError();
		}
		catch (AssertionFailedError ex) {
			assertMessageEquals(ex, "expected array was <null>");
		}

		try {
			assertArrayEquals(new double[] { Double.MIN_VALUE, 93.0, 92.000001 }, null);
			expectAssertionFailedError();
		}
		catch (AssertionFailedError ex) {
			assertMessageEquals(ex, "actual array was <null>");
		}
	}

	@Test
	void assertArrayEqualsDoubleArrayVsNullAndMessage() {
		try {
			assertArrayEquals(null, new double[] { 33.3, 34.9, 20.1, 11.0011 }, "message");
			expectAssertionFailedError();
		}
		catch (AssertionFailedError ex) {
			assertMessageStartsWith(ex, "message");
			assertMessageEndsWith(ex, "expected array was <null>");
		}

		try {
			assertArrayEquals(new double[] { 44.4, 20.19, 11.3, 0.11 }, null, "message");
			expectAssertionFailedError();
		}
		catch (AssertionFailedError ex) {
			assertMessageStartsWith(ex, "message");
			assertMessageEndsWith(ex, "actual array was <null>");
		}
	}

	@Test
	void assertArrayEqualsDoubleArrayVsNullAndMessageSupplier() {
		try {
			assertArrayEquals(null, new double[] { 1.2, 1.3, 1.4, 2.2002 }, () -> "message");
			expectAssertionFailedError();
		}
		catch (AssertionFailedError ex) {
			assertMessageStartsWith(ex, "message");
			assertMessageEndsWith(ex, "expected array was <null>");
		}

		try {
			assertArrayEquals(new double[] { 13.13, 43.33, 100 }, null, () -> "message");
			expectAssertionFailedError();
		}
		catch (AssertionFailedError ex) {
			assertMessageStartsWith(ex, "message");
			assertMessageEndsWith(ex, "actual array was <null>");
		}
	}

	@Test
	void assertArrayEqualsDoubleArraysOfDifferentLength() {
		try {
			assertArrayEquals(new double[] { Double.MIN_VALUE, 1.0, 2.0, 3.0 },
				new double[] { Double.MAX_VALUE, 1.1, 1.0 });
			expectAssertionFailedError();
		}
		catch (AssertionFailedError ex) {
			assertMessageEquals(ex, "array lengths differ, expected: <4> but was: <3>");
		}
	}

	@Test
	void assertArrayEqualsDoubleArraysOfDifferentLengthAndMessage() {
		try {
			assertArrayEquals(new double[] { 11.1, 99.1, 2 }, new double[] { .9, .1, .0, .1, .3 }, "message");
			expectAssertionFailedError();
		}
		catch (AssertionFailedError ex) {
			assertMessageStartsWith(ex, "message");
			assertMessageEndsWith(ex, "array lengths differ, expected: <3> but was: <5>");
		}
	}

	@Test
	void assertArrayEqualsDoubleArraysOfDifferentLengthAndMessageSupplier() {
		try {
			assertArrayEquals(new double[] { 1.15D, 2.2, 2.3 }, new double[] { 1.15D, 1.15D }, () -> "message");
			expectAssertionFailedError();
		}
		catch (AssertionFailedError ex) {
			assertMessageStartsWith(ex, "message");
			assertMessageEndsWith(ex, "array lengths differ, expected: <3> but was: <2>");
		}
	}

	@Test
	void assertArrayEqualsDifferentDoubleArrays() {
		try {
			assertArrayEquals(new double[] { 1.17, 1.19, 1.21, 5 }, new double[] { 1.17, 1.00019, 1.21, 5 });
			expectAssertionFailedError();
		}
		catch (AssertionFailedError ex) {
			assertMessageEquals(ex, "array contents differ at index [1], expected: <1.19> but was: <1.00019>");
		}

		try {
			assertArrayEquals(new double[] { 0.1, 0.2, 0.3, 0.4, 0.5 },
				new double[] { 0.1, 0.2, 0.3, 0.4, Double.NaN });
			expectAssertionFailedError();
		}
		catch (AssertionFailedError ex) {
			assertMessageEquals(ex, "array contents differ at index [4], expected: <0.5> but was: <NaN>");
		}
	}

	@Test
	void assertArrayEqualsDifferentDoubleArraysAndMessage() {
		try {
			assertArrayEquals(new double[] { 1.01, 9.031, .123, 4.23 }, new double[] { 1.01, 9.099, .123, 4.23 },
				"message");
			expectAssertionFailedError();
		}
		catch (AssertionFailedError ex) {
			assertMessageStartsWith(ex, "message");
			assertMessageEndsWith(ex, "array contents differ at index [1], expected: <9.031> but was: <9.099>");
		}
	}

	@Test
	void assertArrayEqualsDifferentDoubleArraysAndMessageSupplier() {
		try {
			assertArrayEquals(new double[] { 0.7, .1, 8 }, new double[] { 0.7, Double.MIN_VALUE, 8 }, () -> "message");
			expectAssertionFailedError();
		}
		catch (AssertionFailedError ex) {
			assertMessageStartsWith(ex, "message");
			assertMessageEndsWith(ex, "array contents differ at index [1], expected: <0.1> but was: <4.9E-324>");
		}
	}

	@Test
	void assertArrayEqualsDeltaDoubleArrays() {
		assertArrayEquals(new double[] {}, new double[] {}, 0.5);
		assertArrayEquals(new double[] { Double.MAX_VALUE, 0.1 }, new double[] { Double.MAX_VALUE, 0.2 }, 0.2);
		assertArrayEquals(new double[] { Double.MIN_VALUE, 3.1, 1.3, 2.7 },
			new double[] { Double.MIN_VALUE, 3.4, 1.7, 2.4 }, 0.5);

		assertArrayEquals(new double[] { Double.NaN }, new double[] { Double.NaN }, 0.01);
		assertArrayEquals(new double[] { 1.2, 1.8, Double.NaN, 4.9 }, new double[] { 1.25, 1.7, Double.NaN, 4.8 }, 0.2);
	}

	@Test
	void assertArrayEqualsDeltaDoubleArraysThrowsForIllegalDelta() {
		try {
			assertArrayEquals(new double[] {}, new double[] {}, -0.5F);
			expectAssertionFailedError();
		}
		catch (AssertionFailedError ex) {
			assertMessageEquals(ex, "positive delta expected but was: <-0.5>");
		}
		try {
			assertArrayEquals(new double[] {}, new double[] {}, Float.NaN);
			expectAssertionFailedError();
		}
		catch (AssertionFailedError ex) {
			assertMessageEquals(ex, "positive delta expected but was: <NaN>");
		}

		try {
			assertArrayEquals(new double[] { 1.2, 1.3, 10 }, new double[] { 1.2, 1.3, 10 }, -0.5F);
			expectAssertionFailedError();
		}
		catch (AssertionFailedError ex) {
			assertMessageEquals(ex, "positive delta expected but was: <-0.5>");
		}
		try {
			assertArrayEquals(new double[] { 0.1, 10 }, new double[] { 0.1, 10, 11 }, Float.NaN);
			expectAssertionFailedError();
		}
		catch (AssertionFailedError ex) {
			assertMessageEquals(ex, "positive delta expected but was: <NaN>");
		}
	}

	@Test
	void assertArrayEqualsDeltaDoubleArrayVsNull() {
		try {
			assertArrayEquals(null, new double[] { Double.MAX_VALUE, 11.1, 12.12 }, 0.5);
			expectAssertionFailedError();
		}
		catch (AssertionFailedError ex) {
			assertMessageEquals(ex, "expected array was <null>");
		}

		try {
			assertArrayEquals(new double[] { Double.MIN_VALUE, 90, 91.9 }, null, 0.1);
			expectAssertionFailedError();
		}
		catch (AssertionFailedError ex) {
			assertMessageEquals(ex, "actual array was <null>");
		}
	}

	@Test
	void assertArrayEqualsDeltaDoubleArrayVsNullAndMessage() {
		try {
			assertArrayEquals(null, new double[] { 33.3, 34.9, 20.1, 11.0011 }, 0.1, "message");
			expectAssertionFailedError();
		}
		catch (AssertionFailedError ex) {
			assertMessageStartsWith(ex, "message");
			assertMessageEndsWith(ex, "expected array was <null>");
		}

		try {
			assertArrayEquals(new double[] { 44.4, 20.19, 11.3, 0.11 }, null, 0.5, "message");
			expectAssertionFailedError();
		}
		catch (AssertionFailedError ex) {
			assertMessageStartsWith(ex, "message");
			assertMessageEndsWith(ex, "actual array was <null>");
		}
	}

	@Test
	void assertArrayEqualsDeltaDoubleArrayVsNullAndMessageSupplier() {
		try {
			assertArrayEquals(null, new double[] { 1.2, 1.3, 1.4, 2.2002 }, 1, () -> "message");
			expectAssertionFailedError();
		}
		catch (AssertionFailedError ex) {
			assertMessageStartsWith(ex, "message");
			assertMessageEndsWith(ex, "expected array was <null>");
		}

		try {
			assertArrayEquals(new double[] { 13.13, 43.33, 100 }, null, 1.5, () -> "message");
			expectAssertionFailedError();
		}
		catch (AssertionFailedError ex) {
			assertMessageStartsWith(ex, "message");
			assertMessageEndsWith(ex, "actual array was <null>");
		}
	}

	@Test
	void assertArrayEqualsDeltaDoubleArraysOfDifferentLength() {
		try {
			assertArrayEquals(new double[] { Double.MIN_VALUE, 2.0, 3.0, 4.0 },
				new double[] { Double.MAX_VALUE, 2.1, 3.1 }, 0.001);
			expectAssertionFailedError();
		}
		catch (AssertionFailedError ex) {
			assertMessageEquals(ex, "array lengths differ, expected: <4> but was: <3>");
		}
	}

	@Test
	void assertArrayEqualsDeltaDoubleArraysOfDifferentLengthAndMessage() {
		try {
			assertArrayEquals(new double[] { 1.1, 99.1, 3.1 }, new double[] { .9, .1, .0, .1, .3 }, 0.1, "message");
			expectAssertionFailedError();
		}
		catch (AssertionFailedError ex) {
			assertMessageStartsWith(ex, "message");
			assertMessageEndsWith(ex, "array lengths differ, expected: <3> but was: <5>");
		}
	}

	@Test
	void assertArrayEqualsDeltaDoubleArraysOfDifferentLengthAndMessageSupplier() {
		try {
			assertArrayEquals(new double[] { 1.77D, 2.1, 3 }, new double[] { 8.8, 0.11 }, 1, () -> "message");
			expectAssertionFailedError();
		}
		catch (AssertionFailedError ex) {
			assertMessageStartsWith(ex, "message");
			assertMessageEndsWith(ex, "array lengths differ, expected: <3> but was: <2>");
		}
	}

	@Test
	void assertArrayEqualsDeltaDifferentDoubleArrays() {
		try {
			assertArrayEquals(new double[] { 1.12, 2.92, 1.201 }, new double[] { 1.1201, 2.94, 1.201 }, 0.01);
			expectAssertionFailedError();
		}
		catch (AssertionFailedError ex) {
			assertMessageEquals(ex, "array contents differ at index [1], expected: <2.92> but was: <2.94>");
		}

		try {
			assertArrayEquals(new double[] { 0.6, 0.12, 19.9, 5.5 }, new double[] { 1.0, 0.42, 20, Double.NaN }, 0.5);
			expectAssertionFailedError();
		}
		catch (AssertionFailedError ex) {
			assertMessageEquals(ex, "array contents differ at index [3], expected: <5.5> but was: <NaN>");
		}
	}

	@Test
	void assertArrayEqualsDeltaDifferentDoubleArraysAndMessage() {
		try {
			assertArrayEquals(new double[] { 1.01, 9.031, .123, 4.23 }, new double[] { 1.1, 9.231, .13, 4.3 }, 0.1,
				"message");
			expectAssertionFailedError();
		}
		catch (AssertionFailedError ex) {
			assertMessageStartsWith(ex, "message");
			assertMessageEndsWith(ex, "array contents differ at index [1], expected: <9.031> but was: <9.231>");
		}
	}

	@Test
	void assertArrayEqualsDeltaDifferentDoubleArraysAndMessageSupplier() {
		try {
			assertArrayEquals(new double[] { 0.7, 0.3001, 8 }, new double[] { 0.7, 0.4002, 8 }, 0.1, () -> "message");
			expectAssertionFailedError();
		}
		catch (AssertionFailedError ex) {
			assertMessageStartsWith(ex, "message");
			assertMessageEndsWith(ex, "array contents differ at index [1], expected: <0.3001> but was: <0.4002>");
		}
	}

	@Test
	void assertArrayEqualsObjectArrays() {
		Object[] array = { "a", 'b', 1, 2 };
		assertArrayEquals(array, array);

		assertArrayEquals(new Object[] {}, new Object[] {});
		assertArrayEquals(new Object[] { "abc" }, new Object[] { "abc" });
		assertArrayEquals(new Object[] { "abc", 1, 2L, 3D }, new Object[] { "abc", 1, 2L, 3D });

		assertArrayEquals(new Object[] { new Object[] { new Object[] {} } },
			new Object[] { new Object[] { new Object[] {} } });

		assertArrayEquals(
			new Object[] { null, new Object[] { null, new Object[] { null, null } }, null, new Object[] { null } },
			new Object[] { null, new Object[] { null, new Object[] { null, null } }, null, new Object[] { null } });

		assertArrayEquals(new Object[] { "a", new Object[] { new Object[] { "b", new Object[] { "c", "d" } } }, "e" },
			new Object[] { "a", new Object[] { new Object[] { "b", new Object[] { "c", "d" } } }, "e" });

		assertArrayEquals(
			new Object[] { new Object[] { 1 }, new Object[] { 2 },
					new Object[] { new Object[] { 3, new Object[] { 4 } } } },
			new Object[] { new Object[] { 1 }, new Object[] { 2 },
					new Object[] { new Object[] { 3, new Object[] { 4 } } } });

		assertArrayEquals(
			new Object[] { new Object[] {
					new Object[] { new Object[] { new Object[] { new Object[] { new Object[] { "abc" } } } } } } },
			new Object[] { new Object[] {
					new Object[] { new Object[] { new Object[] { new Object[] { new Object[] { "abc" } } } } } } });

		assertArrayEquals(
			new Object[] { null, new Object[] { null, Double.NaN, new Object[] { Float.NaN, null, new Object[] {} } } },
			new Object[] { null,
					new Object[] { null, Double.NaN, new Object[] { Float.NaN, null, new Object[] {} } } });

		assertArrayEquals(
			new Object[] { new String("a"), Integer.valueOf(1), new Object[] { Double.parseDouble("1.1"), "b" } },
			new Object[] { new String("a"), Integer.valueOf(1), new Object[] { Double.parseDouble("1.1"), "b" } });

		assertArrayEquals(
			new Object[] { 1, 2,
					new Object[] { 3, new int[] { 4, 5 }, new long[] { 6 },
							new Object[] { new Object[] { new int[] { 7 } } } },
					new int[] { 8 }, new Object[] { new long[] { 9 } } },
			new Object[] { 1, 2,
					new Object[] { 3, new int[] { 4, 5 }, new long[] { 6 },
							new Object[] { new Object[] { new int[] { 7 } } } },
					new int[] { 8 }, new Object[] { new long[] { 9 } } });

		assertArrayEquals(
			new Object[] { "a", new char[] { 'b', 'c' }, new int[] { 'd' },
					new Object[] { new Object[] { new String[] { "ef" }, new Object[] { new String[] { "ghi" } } } } },
			new Object[] { "a", new char[] { 'b', 'c' }, new int[] { 'd' },
					new Object[] { new Object[] { new String[] { "ef" }, new Object[] { new String[] { "ghi" } } } } });
	}

	@Test
	void assertArrayEqualsObjectArrayVsNull() {
		try {
			assertArrayEquals(null, new Object[] { "a", "b", 1, new Object() });
			expectAssertionFailedError();
		}
		catch (AssertionFailedError ex) {
			assertMessageEquals(ex, "expected array was <null>");
		}

		try {
			assertArrayEquals(new Object[] { 'a', 1, new Object(), 10L }, null);
			expectAssertionFailedError();
		}
		catch (AssertionFailedError ex) {
			assertMessageEquals(ex, "actual array was <null>");
		}
	}

	@Test
	void assertArrayEqualsNestedObjectArrayVsNull() {
		try {
			assertArrayEquals(//
				new Object[] { new Object[] {}, 1, "2", new Object[] { '3', new Object[] { null } } }, //
				new Object[] { new Object[] {}, 1, "2", new Object[] { '3', new Object[] { new Object[] { "4" } } } });
			expectAssertionFailedError();
		}
		catch (AssertionFailedError ex) {
			assertMessageEquals(ex, "expected array was <null> at index [3][1][0]");
		}

		try {
			assertArrayEquals(
				new Object[] { 1, 2, new Object[] { 3, new Object[] { "4", new Object[] { 5, new Object[] { 6 } } } },
						"7" },
				new Object[] { 1, 2, new Object[] { 3, new Object[] { "4", new Object[] { 5, null } } }, "7" });
			expectAssertionFailedError();
		}
		catch (AssertionFailedError ex) {
			assertMessageEquals(ex, "actual array was <null> at index [2][1][1][1]");
		}
	}

	@Test
	void assertArrayEqualsObjectArrayVsNullAndMessage() {
		try {
			assertArrayEquals(null, new Object[] { 'a', "b", 10, 20D }, "message");
			expectAssertionFailedError();
		}
		catch (AssertionFailedError ex) {
			assertMessageStartsWith(ex, "message");
			assertMessageEndsWith(ex, "expected array was <null>");
		}

		try {
			assertArrayEquals(new Object[] { "hello", 42 }, null, "message");
			expectAssertionFailedError();
		}
		catch (AssertionFailedError ex) {
			assertMessageStartsWith(ex, "message");
			assertMessageEndsWith(ex, "actual array was <null>");
		}
	}

	@Test
	void assertArrayEqualsNestedObjectArrayVsNullAndMessage() {
		try {
			assertArrayEquals(new Object[] { 1, new Object[] { 2, 3, new Object[] { 4, 5, new Object[] { null } } } },
				new Object[] { 1, new Object[] { 2, 3, new Object[] { 4, 5, new Object[] { new Object[] { 6 } } } } },
				"message");
			expectAssertionFailedError();
		}
		catch (AssertionFailedError ex) {
			assertMessageStartsWith(ex, "message");
			assertMessageEndsWith(ex, "expected array was <null> at index [1][2][2][0]");
		}

		try {
			assertArrayEquals(
				new Object[] { 1, new Object[] { 2, new Object[] { 3, new Object[] { new Object[] { 4 } } } } },
				new Object[] { 1, new Object[] { 2, new Object[] { 3, new Object[] { null } } } }, "message");
			expectAssertionFailedError();
		}
		catch (AssertionFailedError ex) {
			assertMessageStartsWith(ex, "message");
			assertMessageEndsWith(ex, "actual array was <null> at index [1][1][1][0]");
		}
	}

	@Test
	void assertArrayEqualsObjectArrayVsNullAndMessageSupplier() {
		try {
			assertArrayEquals(null, new Object[] { 42, "42", new float[] { 42F }, 42D }, () -> "message");
			expectAssertionFailedError();
		}
		catch (AssertionFailedError ex) {
			assertMessageStartsWith(ex, "message");
			assertMessageEndsWith(ex, "expected array was <null>");
		}

		try {
			assertArrayEquals(new Object[] { new Object[] { "a" }, new Object() }, null, () -> "message");
			expectAssertionFailedError();
		}
		catch (AssertionFailedError ex) {
			assertMessageStartsWith(ex, "message");
			assertMessageEndsWith(ex, "actual array was <null>");
		}
	}

	@Test
	void assertArrayEqualsNestedObjectArrayVsNullAndMessageSupplier() {
		try {
			assertArrayEquals(new Object[] { "1", "2", "3", new Object[] { "4", new Object[] { null } } },
				new Object[] { "1", "2", "3", new Object[] { "4", new Object[] { new int[] { 5 } } } },
				() -> "message");
			expectAssertionFailedError();
		}
		catch (AssertionFailedError ex) {
			assertMessageStartsWith(ex, "message");
			assertMessageEndsWith(ex, "expected array was <null> at index [3][1][0]");
		}

		try {
			assertArrayEquals(
				new Object[] { 1, 2, new Object[] { "3", new Object[] { '4', new Object[] { 5, 6, new long[] {} } } } },
				new Object[] { 1, 2, new Object[] { "3", new Object[] { '4', new Object[] { 5, 6, null } } } },
				() -> "message");
			expectAssertionFailedError();
		}
		catch (AssertionFailedError ex) {
			assertMessageStartsWith(ex, "message");
			assertMessageEndsWith(ex, "actual array was <null> at index [2][1][1][2]");
		}
	}

	@Test
	void assertArrayEqualsObjectArraysOfDifferentLength() {
		try {
			assertArrayEquals(new Object[] { 'a', "b", 'c' }, new Object[] { 'a', "b", 'c', 1 });
			expectAssertionFailedError();
		}
		catch (AssertionFailedError ex) {
			assertMessageEquals(ex, "array lengths differ, expected: <3> but was: <4>");
		}
	}

	@Test
	void assertArrayEqualsNestedObjectArraysOfDifferentLength() {
		try {
			assertArrayEquals(
				new Object[] { "a", new Object[] { "b", new Object[] { "c", "d", new Object[] { "e", 1, 2, 3 } } } },
				new Object[] { "a",
						new Object[] { "b", new Object[] { "c", "d", new Object[] { "e", 1, 2, 3, 4, 5 } } } });
			expectAssertionFailedError();
		}
		catch (AssertionFailedError ex) {
			assertMessageEquals(ex, "array lengths differ at index [1][1][2], expected: <4> but was: <6>");
		}

		try {
			assertArrayEquals(
				new Object[] { new Object[] {
						new Object[] { new Object[] { new Object[] { new Object[] { new char[] { 'a' } } } } } } },
				new Object[] { new Object[] { new Object[] {
						new Object[] { new Object[] { new Object[] { new char[] { 'a', 'b' } } } } } } });
			expectAssertionFailedError();
		}
		catch (AssertionFailedError ex) {
			assertMessageEquals(ex, "array lengths differ at index [0][0][0][0][0][0], expected: <1> but was: <2>");
		}
	}

	@Test
	void assertArrayEqualsObjectArraysOfDifferentLengthAndMessage() {
		try {
			assertArrayEquals(new Object[] { 'a', 1 }, new Object[] { 'a', 1, new Object() }, "message");
			expectAssertionFailedError();
		}
		catch (AssertionFailedError ex) {
			assertMessageStartsWith(ex, "message");
			assertMessageEndsWith(ex, "array lengths differ, expected: <2> but was: <3>");
		}
	}

	@Test
	void assertArrayEqualsNestedObjectArraysOfDifferentLengthAndMessage() {
		try {
			assertArrayEquals(//
				new Object[] { 'a', 1, new Object[] { 2, 3 } }, //
				new Object[] { 'a', 1, new Object[] { 2, 3, 4, 5 } }, //
				"message");
			expectAssertionFailedError();
		}
		catch (AssertionFailedError ex) {
			assertMessageStartsWith(ex, "message");
			assertMessageEndsWith(ex, "array lengths differ at index [2], expected: <2> but was: <4>");
		}
	}

	@Test
	void assertArrayEqualsObjectArraysOfDifferentLengthAndMessageSupplier() {
		try {
			assertArrayEquals(new Object[] { "a", "b", "c" }, new Object[] { "a", "b", "c", "d", "e", "f" },
				() -> "message");
			expectAssertionFailedError();
		}
		catch (AssertionFailedError ex) {
			assertMessageStartsWith(ex, "message");
			assertMessageEndsWith(ex, "array lengths differ, expected: <3> but was: <6>");
		}
	}

	@Test
	void assertArrayEqualsNestedObjectArraysOfDifferentLengthAndMessageSupplier() {
		try {
			assertArrayEquals(//
				new Object[] { "a", new Object[] { 1, 2, 3, new double[] { 4.0, 5.1, 6.1 }, 7 } }, //
				new Object[] { "a", new Object[] { 1, 2, 3, new double[] { 4.0, 5.1, 6.1, 7.0 }, 8 } }, //
				() -> "message");
			expectAssertionFailedError();
		}
		catch (AssertionFailedError ex) {
			assertMessageStartsWith(ex, "message");
			assertMessageEndsWith(ex, "array lengths differ at index [1][3], expected: <3> but was: <4>");
		}
	}

	@Test
	void assertArrayEqualsDifferentObjectArrays() {
		try {
			assertArrayEquals(new Object[] { 1L, "2", '3', 4, 5D }, new Object[] { 1L, "2", '9', 4, 5D });
			expectAssertionFailedError();
		}
		catch (AssertionFailedError ex) {
			assertMessageEquals(ex, "array contents differ at index [2], expected: <3> but was: <9>");
		}

		try {
			assertArrayEquals(new Object[] { "a", 10, 11, 12, Double.NaN }, new Object[] { "a", 10, 11, 12, 13.55D });
			expectAssertionFailedError();
		}
		catch (AssertionFailedError ex) {
			assertMessageEquals(ex, "array contents differ at index [4], expected: <NaN> but was: <13.55>");
		}
	}

	@Test
	void assertArrayEqualsDifferentNestedObjectArrays() {
		try {
			assertArrayEquals(
				new Object[] { 1, 2, new Object[] { 3, new Object[] { 4, new boolean[] { false, true } } } },
				new Object[] { 1, 2, new Object[] { 3, new Object[] { 4, new boolean[] { true, false } } } });
			expectAssertionFailedError();
		}
		catch (AssertionFailedError ex) {
			assertMessageEquals(ex, "array contents differ at index [2][1][1][0], expected: <false> but was: <true>");
		}

		Object[] differentElement = new Object[] {};
		try {
			assertArrayEquals(new Object[] { 1, 2, 3, new Object[] { new Object[] { 4, new Object[] { 5 } } } },
				new Object[] { 1, 2, 3, new Object[] { new Object[] { 4, new Object[] { differentElement } } } });
			expectAssertionFailedError();
		}
		catch (AssertionFailedError ex) {
			assertMessageEquals(ex,
				"array contents differ at index [3][0][1][0], expected: <5> but was: <" + differentElement + ">");
		}
	}

	@Test
	void assertArrayEqualsDifferentObjectArraysAndMessage() {
		try {
			assertArrayEquals(new Object[] { 1.1D, 2L, "3" }, new Object[] { 1D, 2L, "3" }, "message");
			expectAssertionFailedError();
		}
		catch (AssertionFailedError ex) {
			assertMessageStartsWith(ex, "message");
			assertMessageEndsWith(ex, "array contents differ at index [0], expected: <1.1> but was: <1.0>");
		}
	}

	@Test
	void assertArrayEqualsDifferentNestedObjectArraysAndMessage() {
		try {
			assertArrayEquals(new Object[] { 9, 8, '6', new Object[] { 5, 4, "3", new Object[] { "2", '1' } } },
				new Object[] { 9, 8, '6', new Object[] { 5, 4, "3", new Object[] { "99", '1' } } }, "message");
			expectAssertionFailedError();
		}
		catch (AssertionFailedError ex) {
			assertMessageStartsWith(ex, "message");
			assertMessageEndsWith(ex, "array contents differ at index [3][3][0], expected: <2> but was: <99>");
		}

		try {
			assertArrayEquals(new Object[] { 9, 8, '6', new Object[] { 5, 4, "3", new String[] { "2", "1" } } },
				new Object[] { 9, 8, '6', new Object[] { 5, 4, "3", new String[] { "99", "1" } } }, "message");
			expectAssertionFailedError();
		}
		catch (AssertionFailedError ex) {
			assertMessageStartsWith(ex, "message");
			assertMessageEndsWith(ex, "array contents differ at index [3][3][0], expected: <2> but was: <99>");
		}
	}

	@Test
	void assertArrayEqualsDifferentObjectArraysAndMessageSupplier() {
		try {
			assertArrayEquals(new Object[] { "one", 1L, Double.MIN_VALUE, "abc" },
				new Object[] { "one", 1L, 42.42, "abc" }, () -> "message");
			expectAssertionFailedError();
		}
		catch (AssertionFailedError ex) {
			assertMessageStartsWith(ex, "message");
			assertMessageEndsWith(ex, "array contents differ at index [2], expected: <4.9E-324> but was: <42.42>");
		}
	}

	@Test
	void assertArrayEqualsDifferentNestedObjectArraysAndMessageSupplier() {
		try {
			assertArrayEquals(
				new Object[] { "one", 1L, new Object[] { "a", 'b', new Object[] { 1, new Object[] { 2, 3 } } }, "abc" },
				new Object[] { "one", 1L, new Object[] { "a", 'b', new Object[] { 1, new Object[] { 2, 4 } } }, "abc" },
				() -> "message");
			expectAssertionFailedError();
		}
		catch (AssertionFailedError ex) {
			assertMessageStartsWith(ex, "message");
			assertMessageEndsWith(ex, "array contents differ at index [2][2][1][1], expected: <3> but was: <4>");
		}

		try {
			assertArrayEquals(
				new Object[] { "j", new String[] { "a" }, new int[] { 42 }, "ab", new Object[] { 1, new int[] { 3 } } },
				new Object[] { "j", new String[] { "a" }, new int[] { 42 }, "ab", new Object[] { 1, new int[] { 5 } } },
				() -> "message");
			expectAssertionFailedError();
		}
		catch (AssertionFailedError ex) {
			assertMessageStartsWith(ex, "message");
			assertMessageEndsWith(ex, "array contents differ at index [4][1][0], expected: <3> but was: <5>");
		}
	}

	// --- assertNotEquals -------------------------------------------------

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

	// --- assertSame ----------------------------------------------------

	@Test
	void assertSameWithTwoNulls() {
		assertSame(null, null);
	}

	@Test
	void assertSameWithSameObject() {
		Object foo = new Object();
		assertSame(foo, foo);
	}

	@Test
	void assertSameWithObjectVsNull() {
		Object expected = new Object();
		try {
			assertSame(expected, null);
			expectAssertionFailedError();
		}
		catch (AssertionFailedError ex) {
			assertMessageContains(ex, "expected: <java.lang.Object@");
			assertMessageContains(ex, "but was: <null>");
			assertExpectedAndActualValues(ex, expected, null);
		}
	}

	@Test
	void assertSameWithNullVsObject() {
		Object actual = new Object();
		try {
			assertSame(null, actual);
			expectAssertionFailedError();
		}
		catch (AssertionFailedError ex) {
			assertMessageContains(ex, "expected: <null>");
			assertMessageContains(ex, "but was: <java.lang.Object@");
			assertExpectedAndActualValues(ex, null, actual);
		}
	}

	@Test
	void assertSameWithDifferentObjects() {
		Object expected = new Object();
		Object actual = new Object();
		try {
			assertSame(expected, actual);
			expectAssertionFailedError();
		}
		catch (AssertionFailedError ex) {
			assertMessageContains(ex, "expected: <java.lang.Object@");
			assertMessageContains(ex, "but was: <java.lang.Object@");
			assertExpectedAndActualValues(ex, expected, actual);
		}
	}

	@Test
	void assertSameWithEquivalentStringsAndMessageSupplier() {
		String expected = new String("foo");
		String actual = new String("foo");
		try {
			assertSame(expected, actual, () -> "test");
			expectAssertionFailedError();
		}
		catch (AssertionFailedError ex) {
			assertMessageStartsWith(ex, "test");
			assertMessageContains(ex, "expected: java.lang.String@");
			assertMessageContains(ex, "but was: java.lang.String@");
			assertExpectedAndActualValues(ex, expected, actual);
		}
	}

	// --- assertNotSame -------------------------------------------------

	@Test
	void assertNotSameWithDifferentObjects() {
		assertNotSame(new Object(), new Object());
	}

	@Test
	void assertNotSameWithObjectVsNull() {
		assertNotSame(new Object(), null);
	}

	@Test
	void assertNotSameWithNullVsObject() {
		assertNotSame(null, new Object());
	}

	@Test
	void assertNotSameWithTwoNulls() {
		try {
			assertNotSame(null, null);
			expectAssertionFailedError();
		}
		catch (AssertionFailedError ex) {
			assertMessageEquals(ex, "expected: not same but was: <null>");
		}
	}

	@Test
	void assertNotSameWithSameObjectAndMessage() {
		try {
			Object foo = new Object();
			assertNotSame(foo, foo, "test");
			expectAssertionFailedError();
		}
		catch (AssertionFailedError ex) {
			assertMessageStartsWith(ex, "test");
			assertMessageContains(ex, "expected: not same but was: <java.lang.Object@");
		}
	}

	@Test
	void assertNotSameWithSameObjectAndMessageSupplier() {
		try {
			Object foo = new Object();
			assertNotSame(foo, foo, () -> "test");
			expectAssertionFailedError();
		}
		catch (AssertionFailedError ex) {
			assertMessageStartsWith(ex, "test");
			assertMessageContains(ex, "expected: not same but was: <java.lang.Object@");
		}
	}

	// --- assertAll -----------------------------------------------------

	@Test
	void assertAllWithNullInExecutableArray() {
		try {
			// @formatter:off
			assertAll(
				() -> {},
				(Executable) null
			);
			// @formatter:on
		}
		catch (PreconditionViolationException ex) {
			assertMessageEquals(ex, "executables must not be null");
		}
	}

	@Test
	void assertAllWithNullExecutableArray() {
		try {
			assertAll((Executable[]) null);
		}
		catch (PreconditionViolationException ex) {
			assertMessageEquals(ex, "executables must not be null");
		}
	}

	@Test
	void assertAllWithNullExecutableStream() {
		try {
			assertAll((Stream<Executable>) null);
		}
		catch (PreconditionViolationException ex) {
			assertMessageEquals(ex, "executables must not be null");
		}
	}

	@Test
	void assertAllWithExecutablesThatDoNotThrowExceptions() {
		// @formatter:off
		assertAll(
			() -> assertTrue(true),
			() -> assertFalse(false),
			() -> assertTrue(true)
		);
		// @formatter:on
	}

	@Test
	void assertAllWithExecutablesThatThrowAssertionErrors() {
		// @formatter:off
		MultipleFailuresError multipleFailuresError = expectThrows(MultipleFailuresError.class, () ->
			assertAll(
				() -> assertFalse(true),
				() -> assertFalse(true)
			)
		);
		// @formatter:on

		assertTrue(multipleFailuresError != null);
		List<AssertionError> failures = multipleFailuresError.getFailures();
		assertTrue(failures.size() == 2);
		assertTrue(failures.get(0).getClass().equals(AssertionFailedError.class));
	}

	@Test
	void assertAllWithStreamOfExecutablesThatThrowAssertionErrors() {
		// @formatter:off
		MultipleFailuresError multipleFailuresError = expectThrows(MultipleFailuresError.class, () ->
			assertAll(Stream.of(() -> assertFalse(true), () -> assertFalse(true)))
		);
		// @formatter:on

		assertTrue(multipleFailuresError != null);
		List<AssertionError> failures = multipleFailuresError.getFailures();
		assertTrue(failures.size() == 2);
		assertTrue(failures.get(0).getClass().equals(AssertionFailedError.class));
	}

	@Test
	void assertAllWithExecutableThatThrowsThrowable() {
		assertThrows(EnigmaThrowable.class, () -> assertAll(() -> {
			throw new EnigmaThrowable();
		}));
	}

	@Test
	void assertAllWithExecutableThatThrowsCheckedException() {
		assertThrows(IOException.class, () -> assertAll(() -> {
			throw new IOException();
		}));
	}

	@Test
	void assertAllWithExecutableThatThrowsRuntimeException() {
		assertThrows(IllegalStateException.class, () -> assertAll(() -> {
			throw new IllegalStateException();
		}));
	}

	@Test
	void assertAllWithExecutableThatThrowsError() {
		assertThrows(StackOverflowError.class, () -> assertAll(this::recurseIndefinitely));
	}

	// --- assertThrows --------------------------------------------------

	@Test
	void assertThrowsThrowable() {
		assertThrows(EnigmaThrowable.class, () -> {
			throw new EnigmaThrowable();
		});
	}

	@Test
	void assertThrowsCheckedException() {
		assertThrows(IOException.class, () -> {
			throw new IOException();
		});
	}

	@Test
	void assertThrowsRuntimeException() {
		assertThrows(IllegalStateException.class, () -> {
			throw new IllegalStateException();
		});
	}

	@Test
	void assertThrowsError() {
		assertThrows(StackOverflowError.class, this::recurseIndefinitely);
	}

	// --- expectThrows --------------------------------------------------

	@Test
	void expectThrowsThrowable() {
		EnigmaThrowable enigmaThrowable = expectThrows(EnigmaThrowable.class, () -> {
			throw new EnigmaThrowable();
		});
		assertNotNull(enigmaThrowable);
	}

	@Test
	void expectThrowsCheckedException() {
		IOException exception = expectThrows(IOException.class, () -> {
			throw new IOException();
		});
		assertNotNull(exception);
	}

	@Test
	void expectThrowsRuntimeException() {
		IllegalStateException illegalStateException = expectThrows(IllegalStateException.class, () -> {
			throw new IllegalStateException();
		});
		assertNotNull(illegalStateException);
	}

	@Test
	void expectThrowsError() {
		StackOverflowError stackOverflowError = expectThrows(StackOverflowError.class, this::recurseIndefinitely);
		assertNotNull(stackOverflowError);
	}

	@Test
	void expectThrowsWithExecutableThatDoesNotThrowAnException() {
		try {
			expectThrows(IllegalStateException.class, () -> {
			});
			expectAssertionFailedError();
		}
		catch (AssertionFailedError ex) {
			assertMessageContains(ex, "Expected java.lang.IllegalStateException to be thrown");
		}
	}

	@Test
	void expectThrowsWithExecutableThatThrowsAnUnexpectedException() {
		try {
			expectThrows(IllegalStateException.class, () -> {
				throw new NumberFormatException();
			});
			expectAssertionFailedError();
		}
		catch (AssertionFailedError ex) {
			assertMessageContains(ex, "Unexpected exception type thrown");
			assertMessageContains(ex, "expected: <java.lang.IllegalStateException>");
			assertMessageContains(ex, "but was: <java.lang.NumberFormatException>");
		}
	}

	// --- assertTimeout -------------------------------------------------

	private static ThreadLocal<AtomicBoolean> changed = ThreadLocal.withInitial(() -> new AtomicBoolean(false));

	@Test
	void assertTimeoutForExecutableThatCompletesBeforeTheTimeout() {
		changed.get().set(false);

		assertTimeout(ofMillis(500), () -> {
			changed.get().set(true);
		});

		assertTrue(changed.get().get(), "should have executed in the same thread");
	}

	@Test
	void assertTimeoutForExecutableThatThrowsAnException() {
		RuntimeException exception = expectThrows(RuntimeException.class, () -> assertTimeout(ofMillis(500), () -> {
			throw new RuntimeException("not this time");
		}));
		assertMessageEquals(exception, "not this time");
	}

	@Test
	void assertTimeoutForExecutableThatThrowsAnAssertionFailedError() {
		AssertionFailedError exception = expectThrows(AssertionFailedError.class,
			() -> assertTimeout(ofMillis(500), () -> fail("enigma")));
		assertMessageEquals(exception, "enigma");
	}

	@Test
	void assertTimeoutForExecutableThatCompletesAfterTheTimeout() {
		AssertionFailedError error = expectThrows(AssertionFailedError.class,
			() -> assertTimeout(ofMillis(50), () -> Thread.sleep(100)));
		assertMessageStartsWith(error, "execution exceeded timeout of 50 ms by");
	}

	@Test
	void assertTimeoutWithMessageForExecutableThatCompletesAfterTheTimeout() {
		AssertionFailedError error = expectThrows(AssertionFailedError.class,
			() -> assertTimeout(ofMillis(50), () -> Thread.sleep(100), "Tempus Fugit"));
		assertMessageStartsWith(error, "Tempus Fugit ==> execution exceeded timeout of 50 ms by");
	}

	@Test
	void assertTimeoutWithMessageSupplierForExecutableThatCompletesAfterTheTimeout() {
		AssertionFailedError error = expectThrows(AssertionFailedError.class,
			() -> assertTimeout(ofMillis(50), () -> Thread.sleep(100), () -> "Tempus" + " " + "Fugit"));
		assertMessageStartsWith(error, "Tempus Fugit ==> execution exceeded timeout of 50 ms by");
	}

	@Test
	void assertTimeoutPreemptivelyForExecutableThatCompletesBeforeTheTimeout() {
		changed.get().set(false);

		assertTimeoutPreemptively(ofMillis(500), () -> {
			changed.get().set(true);
		});

		assertFalse(changed.get().get(), "should have executed in a different thread");
	}

	@Test
	void assertTimeoutPreemptivelyForExecutableThatThrowsAnException() {
		RuntimeException exception = expectThrows(RuntimeException.class,
			() -> assertTimeoutPreemptively(ofMillis(500), () -> {
				throw new RuntimeException("not this time");
			}));
		assertMessageEquals(exception, "not this time");
	}

	@Test
	void assertTimeoutPreemptivelyForExecutableThatThrowsAnAssertionFailedError() {
		AssertionFailedError exception = expectThrows(AssertionFailedError.class,
			() -> assertTimeoutPreemptively(ofMillis(500), () -> fail("enigma")));
		assertMessageEquals(exception, "enigma");
	}

	@Test
	void assertTimeoutPreemptivelyForExecutableThatCompletesAfterTheTimeout() {
		AssertionFailedError error = expectThrows(AssertionFailedError.class,
			() -> assertTimeoutPreemptively(ofMillis(50), () -> Thread.sleep(100)));
		assertMessageEquals(error, "execution timed out after 50 ms");
	}

	@Test
	void assertTimeoutPreemptivelyWithMessageForExecutableThatCompletesAfterTheTimeout() {
		AssertionFailedError error = expectThrows(AssertionFailedError.class,
			() -> assertTimeoutPreemptively(ofMillis(50), () -> Thread.sleep(100), "Tempus Fugit"));
		assertMessageEquals(error, "Tempus Fugit ==> execution timed out after 50 ms");
	}

	@Test
	void assertTimeoutPreemptivelyWithMessageSupplierForExecutableThatCompletesAfterTheTimeout() {
		AssertionFailedError error = expectThrows(AssertionFailedError.class,
			() -> assertTimeoutPreemptively(ofMillis(50), () -> Thread.sleep(100), () -> "Tempus" + " " + "Fugit"));
		assertMessageEquals(error, "Tempus Fugit ==> execution timed out after 50 ms");
	}

	// -------------------------------------------------------------------

	private void recurseIndefinitely() {
		// simulate infinite recursion
		throw new StackOverflowError();
	}

	private static void expectAssertionFailedError() {
		throw new AssertionError("Should have thrown an " + AssertionFailedError.class.getName());
	}

	private static void assertMessageEquals(Throwable ex, String msg) throws AssertionError {
		if (!msg.equals(ex.getMessage())) {
			throw new AssertionError("Exception message should be [" + msg + "], but was [" + ex.getMessage() + "].");
		}
	}

	private static void assertMessageStartsWith(Throwable ex, String msg) throws AssertionError {
		if (!ex.getMessage().startsWith(msg)) {
			throw new AssertionError(
				"Exception message should start with [" + msg + "], but was [" + ex.getMessage() + "].");
		}
	}

	private static void assertMessageEndsWith(Throwable ex, String msg) throws AssertionError {
		if (!ex.getMessage().endsWith(msg)) {
			throw new AssertionError(
				"Exception message should end with [" + msg + "], but was [" + ex.getMessage() + "].");
		}
	}

	private static void assertMessageContains(Throwable ex, String msg) throws AssertionError {
		if (!ex.getMessage().contains(msg)) {
			throw new AssertionError(
				"Exception message should contain [" + msg + "], but was [" + ex.getMessage() + "].");
		}
	}

	private static void assertExpectedAndActualValues(AssertionFailedError ex, Object expected, Object actual)
			throws AssertionError {
		if (!wrapsEqualValue(ex.getExpected(), expected)) {
			throw new AssertionError("Expected value in AssertionFailedError should equal ["
					+ ValueWrapper.create(expected) + "], but was [" + ex.getExpected() + "].");
		}
		if (!wrapsEqualValue(ex.getActual(), actual)) {
			throw new AssertionError("Actual value in AssertionFailedError should equal [" + ValueWrapper.create(actual)
					+ "], but was [" + ex.getActual() + "].");
		}
	}

	private static boolean wrapsEqualValue(ValueWrapper wrapper, Object value) {
		if (value == null || value instanceof Serializable) {
			return Objects.equals(value, wrapper.getValue());
		}
		return wrapper.getIdentityHashCode() == System.identityHashCode(value)
				&& Objects.equals(wrapper.getStringRepresentation(), String.valueOf(value))
				&& Objects.equals(wrapper.getType(), value.getClass());
	}

	@SuppressWarnings("serial")
	private static class EnigmaThrowable extends Throwable {
	}

	private static class EqualsThrowsExceptionClass {

		@Override
		public boolean equals(Object obj) {
			throw new NumberFormatException();
		}
	}

}
