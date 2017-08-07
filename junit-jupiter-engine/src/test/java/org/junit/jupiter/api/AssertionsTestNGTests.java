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

import static org.junit.jupiter.api.AssertionTestUtils.assertExpectedAndActualValues;
import static org.junit.jupiter.api.AssertionTestUtils.assertMessageContains;
import static org.junit.jupiter.api.AssertionTestUtils.expectAssertionFailedError;
import static org.junit.jupiter.api.AssertionUtils.formatLengths;
import static org.junit.jupiter.api.AssertionUtils.formatValues;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.AssertionsTestNG.assertArrayEquals;
import static org.junit.jupiter.api.AssertionsTestNG.assertEquals;
import static org.junit.jupiter.api.AssertionsTestNG.assertIterableEquals;
import static org.junit.jupiter.api.AssertionsTestNG.assertLinesMatch;
import static org.junit.jupiter.api.AssertionsTestNG.assertSame;
import static org.junit.jupiter.api.IterableFactory.listOf;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import org.opentest4j.AssertionFailedError;

class AssertionsTestNGTests {
	private final static String MESSAGE = "ignored";
	private final static Supplier<String> MESSAGE_SUPPLIER = () -> "ignored";

	@Test
	void assertSameAssertMethods() {
		Class<Assertions> assertions = Assertions.class;
		Class<AssertionsTestNG> assertionsTestNG = AssertionsTestNG.class;

		List<Method> unsupportedAssertions = Arrays.stream(assertions.getMethods()).filter(
			junitAssert -> !methodInClass(junitAssert, assertionsTestNG)).collect(Collectors.toList());

		assertTrue(unsupportedAssertions.isEmpty(),
			"Found unsupported assertions:\n" + formatMethods(unsupportedAssertions));
	}

	@Test
	void assertNoExtraMethods() {
		Class<Assertions> assertions = Assertions.class;
		Class<AssertionsTestNG> assertionsTestNG = AssertionsTestNG.class;

		List<Method> extraAssertions = Arrays.stream(assertionsTestNG.getMethods()).filter(
			testngAssert -> !methodInClass(testngAssert, assertions)).collect(Collectors.toList());

		assertTrue(extraAssertions.isEmpty(), "Found extra assertions:\n" + formatMethods(extraAssertions));
	}

	private static boolean methodInClass(Method method, Class<?> clazz) {
		try {
			clazz.getMethod(method.getName(), method.getParameterTypes());
			return true;
		}
		catch (NoSuchMethodException e) {
			return false;
		}
	}

	@Test
	void testAssertEqualsShort() {
		short actual = 2179;
		short expected = 2181;

		try {
			assertEquals(actual, expected);
			expectAssertionFailedError();
		}
		catch (AssertionFailedError ex) {
			assertExpectedAndActualValues(ex, expected, actual);
		}

		try {
			assertEquals(actual, expected, MESSAGE);
			expectAssertionFailedError();
		}
		catch (AssertionFailedError ex) {
			assertExpectedAndActualValues(ex, expected, actual);
		}

		try {
			assertEquals(actual, expected, MESSAGE_SUPPLIER);
			expectAssertionFailedError();
		}
		catch (AssertionFailedError ex) {
			assertExpectedAndActualValues(ex, expected, actual);
		}
	}

	@Test
	void testAssertEqualsByte() {
		byte actual = 83;
		byte expected = 79;

		try {
			assertEquals(actual, expected);
			expectAssertionFailedError();
		}
		catch (AssertionFailedError ex) {
			assertExpectedAndActualValues(ex, expected, actual);
		}

		try {
			assertEquals(actual, expected, MESSAGE);
			expectAssertionFailedError();
		}
		catch (AssertionFailedError ex) {
			assertExpectedAndActualValues(ex, expected, actual);
		}

		try {
			assertEquals(actual, expected, MESSAGE_SUPPLIER);
			expectAssertionFailedError();
		}
		catch (AssertionFailedError ex) {
			assertExpectedAndActualValues(ex, expected, actual);
		}
	}

	@Test
	void testAssertEqualsInt() {
		int actual = 2128675309;
		int expected = 2127365000;

		try {
			assertEquals(actual, expected);
			expectAssertionFailedError();
		}
		catch (AssertionFailedError ex) {
			assertExpectedAndActualValues(ex, expected, actual);
		}

		try {
			assertEquals(actual, expected, MESSAGE);
			expectAssertionFailedError();
		}
		catch (AssertionFailedError ex) {
			assertExpectedAndActualValues(ex, expected, actual);
		}

		try {
			assertEquals(actual, expected, MESSAGE_SUPPLIER);
			expectAssertionFailedError();
		}
		catch (AssertionFailedError ex) {
			assertExpectedAndActualValues(ex, expected, actual);
		}
	}

	@Test
	void testAssertEqualsLong() {
		long actual = 6178675309L;
		long expected = 6175365400L;

		try {
			assertEquals(actual, expected);
			expectAssertionFailedError();
		}
		catch (AssertionFailedError ex) {
			assertExpectedAndActualValues(ex, expected, actual);
		}

		try {
			assertEquals(actual, expected, MESSAGE);
			expectAssertionFailedError();
		}
		catch (AssertionFailedError ex) {
			assertExpectedAndActualValues(ex, expected, actual);
		}

		try {
			assertEquals(actual, expected, MESSAGE_SUPPLIER);
			expectAssertionFailedError();
		}
		catch (AssertionFailedError ex) {
			assertExpectedAndActualValues(ex, expected, actual);
		}
	}

	@Test
	void testAssertEqualsChar() {
		char actual = 'A';
		char expected = 'T';

		try {
			assertEquals(actual, expected);
			expectAssertionFailedError();
		}
		catch (AssertionFailedError ex) {
			assertExpectedAndActualValues(ex, expected, actual);
		}

		try {
			assertEquals(actual, expected, MESSAGE);
			expectAssertionFailedError();
		}
		catch (AssertionFailedError ex) {
			assertExpectedAndActualValues(ex, expected, actual);
		}

		try {
			assertEquals(actual, expected, MESSAGE_SUPPLIER);
			expectAssertionFailedError();
		}
		catch (AssertionFailedError ex) {
			assertExpectedAndActualValues(ex, expected, actual);
		}
	}

	@Test
	void testAssertEqualsFloat() {
		float actual = 2179.0F;
		float expected = 2181.0F;

		try {
			assertEquals(actual, expected);
			expectAssertionFailedError();
		}
		catch (AssertionFailedError ex) {
			assertExpectedAndActualValues(ex, expected, actual);
		}

		try {
			assertEquals(actual, expected, MESSAGE);
			expectAssertionFailedError();
		}
		catch (AssertionFailedError ex) {
			assertExpectedAndActualValues(ex, expected, actual);
		}

		try {
			assertEquals(actual, expected, MESSAGE_SUPPLIER);
			expectAssertionFailedError();
		}
		catch (AssertionFailedError ex) {
			assertExpectedAndActualValues(ex, expected, actual);
		}
	}

	@Test
	void testAssertEqualsFloatDelta() {
		float actual = 2179.0F;
		float expected = 2181.0F;
		float delta = 0.1F;

		try {
			assertEquals(actual, expected, delta);
			expectAssertionFailedError();
		}
		catch (AssertionFailedError ex) {
			assertExpectedAndActualValues(ex, expected, actual);
		}

		try {
			assertEquals(actual, expected, delta, MESSAGE);
			expectAssertionFailedError();
		}
		catch (AssertionFailedError ex) {
			assertExpectedAndActualValues(ex, expected, actual);
		}

		try {
			assertEquals(actual, expected, delta, MESSAGE_SUPPLIER);
			expectAssertionFailedError();
		}
		catch (AssertionFailedError ex) {
			assertExpectedAndActualValues(ex, expected, actual);
		}
	}

	@Test
	void testAssertEqualsDouble() {
		double actual = 2179.0;
		double expected = 2181.0;

		try {
			assertEquals(actual, expected);
			expectAssertionFailedError();
		}
		catch (AssertionFailedError ex) {
			assertExpectedAndActualValues(ex, expected, actual);
		}

		try {
			assertEquals(actual, expected, MESSAGE);
			expectAssertionFailedError();
		}
		catch (AssertionFailedError ex) {
			assertExpectedAndActualValues(ex, expected, actual);
		}

		try {
			assertEquals(actual, expected, MESSAGE_SUPPLIER);
			expectAssertionFailedError();
		}
		catch (AssertionFailedError ex) {
			assertExpectedAndActualValues(ex, expected, actual);
		}
	}

	@Test
	void testAssertEqualsDoubleDelta() {
		double actual = 2179.0;
		double expected = 2181.0;
		double delta = 0.1;

		try {
			assertEquals(actual, expected, delta);
			expectAssertionFailedError();
		}
		catch (AssertionFailedError ex) {
			assertExpectedAndActualValues(ex, expected, actual);
		}

		try {
			assertEquals(actual, expected, delta, MESSAGE);
			expectAssertionFailedError();
		}
		catch (AssertionFailedError ex) {
			assertExpectedAndActualValues(ex, expected, actual);
		}

		try {
			assertEquals(actual, expected, delta, MESSAGE_SUPPLIER);
			expectAssertionFailedError();
		}
		catch (AssertionFailedError ex) {
			assertExpectedAndActualValues(ex, expected, actual);
		}
	}

	@Test
	void testAssertEqualsObject() {
		String actual = "A man, a plan, a canal, Panama!";
		String expected = "!amanaP ,lanac a ,nalp a ,nam A";

		try {
			assertEquals(actual, expected);
			expectAssertionFailedError();
		}
		catch (AssertionFailedError ex) {
			assertExpectedAndActualValues(ex, expected, actual);
		}

		try {
			assertEquals(actual, expected, MESSAGE);
			expectAssertionFailedError();
		}
		catch (AssertionFailedError ex) {
			assertExpectedAndActualValues(ex, expected, actual);
		}

		try {
			assertEquals(actual, expected, MESSAGE_SUPPLIER);
			expectAssertionFailedError();
		}
		catch (AssertionFailedError ex) {
			assertExpectedAndActualValues(ex, expected, actual);
		}
	}

	@Test
	void testAssertArrayEqualsBoolean() {
		boolean[] actual = { true, false };
		boolean[] expected = { true, true };
		int index = 1;

		try {
			assertArrayEquals(actual, expected);
			expectAssertionFailedError();
		}
		catch (AssertionFailedError ex) {
			assertElementMessage(ex, expected[index], actual[index]);
		}

		try {
			assertArrayEquals(actual, expected, MESSAGE);
			expectAssertionFailedError();
		}
		catch (AssertionFailedError ex) {
			assertElementMessage(ex, expected[index], actual[index]);
		}

		try {
			assertArrayEquals(actual, expected, MESSAGE_SUPPLIER);
			expectAssertionFailedError();
		}
		catch (AssertionFailedError ex) {
			assertElementMessage(ex, expected[index], actual[index]);
		}
	}

	@Test
	void testAssertArrayEqualsChar() {
		char[] actual = { 'A', 'T' };
		char[] expected = { 'L', 'T' };
		int index = 0;

		try {
			assertArrayEquals(actual, expected);
			expectAssertionFailedError();
		}
		catch (AssertionFailedError ex) {
			assertElementMessage(ex, expected[index], actual[index]);
		}

		try {
			assertArrayEquals(actual, expected, MESSAGE);
			expectAssertionFailedError();
		}
		catch (AssertionFailedError ex) {
			assertElementMessage(ex, expected[index], actual[index]);
		}

		try {
			assertArrayEquals(actual, expected, MESSAGE_SUPPLIER);
			expectAssertionFailedError();
		}
		catch (AssertionFailedError ex) {
			assertElementMessage(ex, expected[index], actual[index]);
		}
	}

	@Test
	void testAssertArrayEqualsByte() {
		byte[] actual = { 21, 79 };
		byte[] expected = { 21, 81 };
		int index = 1;

		try {
			assertArrayEquals(actual, expected);
			expectAssertionFailedError();
		}
		catch (AssertionFailedError ex) {
			assertElementMessage(ex, expected[index], actual[index]);
		}

		try {
			assertArrayEquals(actual, expected, MESSAGE);
			expectAssertionFailedError();
		}
		catch (AssertionFailedError ex) {
			assertElementMessage(ex, expected[index], actual[index]);
		}

		try {
			assertArrayEquals(actual, expected, MESSAGE_SUPPLIER);
			expectAssertionFailedError();
		}
		catch (AssertionFailedError ex) {
			assertElementMessage(ex, expected[index], actual[index]);
		}
	}

	@Test
	void testAssertArrayEqualsShort() {
		short[] actual = { 2179, 2650 };
		short[] expected = { 2181, 2650 };
		int index = 0;

		try {
			assertArrayEquals(actual, expected);
			expectAssertionFailedError();
		}
		catch (AssertionFailedError ex) {
			assertElementMessage(ex, expected[index], actual[index]);
		}

		try {
			assertArrayEquals(actual, expected, MESSAGE);
			expectAssertionFailedError();
		}
		catch (AssertionFailedError ex) {
			assertElementMessage(ex, expected[index], actual[index]);
		}

		try {
			assertArrayEquals(actual, expected, MESSAGE_SUPPLIER);
			expectAssertionFailedError();
		}
		catch (AssertionFailedError ex) {
			assertElementMessage(ex, expected[index], actual[index]);
		}
	}

	@Test
	void testAssertArrayEqualsInt() {
		int[] actual = { 2128675309, 2127365000 };
		int[] expected = { 2128675309, 2125357710 };
		int index = 1;

		try {
			assertArrayEquals(actual, expected);
			expectAssertionFailedError();
		}
		catch (AssertionFailedError ex) {
			assertElementMessage(ex, expected[index], actual[index]);
		}

		try {
			assertArrayEquals(actual, expected, MESSAGE);
			expectAssertionFailedError();
		}
		catch (AssertionFailedError ex) {
			assertElementMessage(ex, expected[index], actual[index]);
		}

		try {
			assertArrayEquals(actual, expected, MESSAGE_SUPPLIER);
			expectAssertionFailedError();
		}
		catch (AssertionFailedError ex) {
			assertElementMessage(ex, expected[index], actual[index]);
		}
	}

	@Test
	void testAssertArrayEqualsLong() {
		long[] actual = { 6175365400L, 6178675309L };
		long[] expected = { 6172679300L, 6178675309L };
		int index = 0;

		try {
			assertArrayEquals(actual, expected);
			expectAssertionFailedError();
		}
		catch (AssertionFailedError ex) {
			assertElementMessage(ex, expected[index], actual[index]);
		}

		try {
			assertArrayEquals(actual, expected, MESSAGE);
			expectAssertionFailedError();
		}
		catch (AssertionFailedError ex) {
			assertElementMessage(ex, expected[index], actual[index]);
		}

		try {
			assertArrayEquals(actual, expected, MESSAGE_SUPPLIER);
			expectAssertionFailedError();
		}
		catch (AssertionFailedError ex) {
			assertElementMessage(ex, expected[index], actual[index]);
		}
	}

	@Test
	void testAssertArrayEqualsFloat() {
		float[] actual = { 2179.0F, 2650.0F };
		float[] expected = { 2181.0F, 2650.0F };
		int index = 0;

		try {
			assertArrayEquals(actual, expected);
			expectAssertionFailedError();
		}
		catch (AssertionFailedError ex) {
			assertElementMessage(ex, expected[index], actual[index]);
		}

		try {
			assertArrayEquals(actual, expected, MESSAGE);
			expectAssertionFailedError();
		}
		catch (AssertionFailedError ex) {
			assertElementMessage(ex, expected[index], actual[index]);
		}

		try {
			assertArrayEquals(actual, expected, MESSAGE_SUPPLIER);
			expectAssertionFailedError();
		}
		catch (AssertionFailedError ex) {
			assertElementMessage(ex, expected[index], actual[index]);
		}
	}

	@Test
	void testAssertArrayEqualsFloatDelta() {
		float[] actual = { 2179.0F, 2650.0F };
		float[] expected = { 2181.0F, 2650.0F };
		int index = 0;

		try {
			assertArrayEquals(actual, expected, 0.1F);
			expectAssertionFailedError();
		}
		catch (AssertionFailedError ex) {
			assertElementMessage(ex, expected[index], actual[index]);
		}

		try {
			assertArrayEquals(actual, expected, 0.1F, MESSAGE);
			expectAssertionFailedError();
		}
		catch (AssertionFailedError ex) {
			assertElementMessage(ex, expected[index], actual[index]);
		}

		try {
			assertArrayEquals(actual, expected, 0.1F, MESSAGE_SUPPLIER);
			expectAssertionFailedError();
		}
		catch (AssertionFailedError ex) {
			assertElementMessage(ex, expected[index], actual[index]);
		}
	}

	@Test
	void testAssertArrayEqualsDouble() {
		double[] actual = { 2179.0, 2650.0 };
		double[] expected = { 2181.0, 2650.0 };
		int index = 0;

		try {
			assertArrayEquals(actual, expected);
			expectAssertionFailedError();
		}
		catch (AssertionFailedError ex) {
			assertElementMessage(ex, expected[index], actual[index]);
		}

		try {
			assertArrayEquals(actual, expected, MESSAGE);
			expectAssertionFailedError();
		}
		catch (AssertionFailedError ex) {
			assertElementMessage(ex, expected[index], actual[index]);
		}

		try {
			assertArrayEquals(actual, expected, MESSAGE_SUPPLIER);
			expectAssertionFailedError();
		}
		catch (AssertionFailedError ex) {
			assertElementMessage(ex, expected[index], actual[index]);
		}
	}

	@Test
	void testAssertArrayEqualsDoubleDelta() {
		double[] actual = { 2179.0, 2650.0 };
		double[] expected = { 2181.0, 2650.0 };
		int index = 0;

		try {
			assertArrayEquals(actual, expected, 0.1F);
			expectAssertionFailedError();
		}
		catch (AssertionFailedError ex) {
			assertElementMessage(ex, expected[index], actual[index]);
		}

		try {
			assertArrayEquals(actual, expected, 0.1F, MESSAGE);
			expectAssertionFailedError();
		}
		catch (AssertionFailedError ex) {
			assertElementMessage(ex, expected[index], actual[index]);
		}

		try {
			assertArrayEquals(actual, expected, 0.1F, MESSAGE_SUPPLIER);
			expectAssertionFailedError();
		}
		catch (AssertionFailedError ex) {
			assertElementMessage(ex, expected[index], actual[index]);
		}
	}

	@Test
	void testAssertArrayEqualsObject() {
		Object[] actual = { 'A', 'T' };
		Object[] expected = { 'P', 'C', 'T' };

		try {
			assertArrayEquals(actual, expected);
			expectAssertionFailedError();
		}
		catch (AssertionFailedError ex) {
			assertArraySizeMessage(ex, expected, actual);
		}

		try {
			assertArrayEquals(actual, expected, MESSAGE);
			expectAssertionFailedError();
		}
		catch (AssertionFailedError ex) {
			assertArraySizeMessage(ex, expected, actual);
		}

		try {
			assertArrayEquals(actual, expected, MESSAGE_SUPPLIER);
			expectAssertionFailedError();
		}
		catch (AssertionFailedError ex) {
			assertArraySizeMessage(ex, expected, actual);
		}
	}

	@Test
	void testAssertIterableEquals() {
		Iterable<Object> actual = listOf("foo", 'b', 1, 0.0);
		Iterable<Object> expected = listOf("foo", 'b', 2, 0.3);

		try {
			assertIterableEquals(actual, expected);
			expectAssertionFailedError();
		}
		catch (AssertionFailedError ex) {
			assertElementMessage(ex, 2, 1);
		}

		try {
			assertIterableEquals(actual, expected, MESSAGE);
			expectAssertionFailedError();
		}
		catch (AssertionFailedError ex) {
			assertElementMessage(ex, 2, 1);
		}

		try {
			assertIterableEquals(actual, expected, MESSAGE_SUPPLIER);
			expectAssertionFailedError();
		}
		catch (AssertionFailedError ex) {
			assertElementMessage(ex, 2, 1);
		}
	}

	@Test
	void testAssertLinesMatch() {
		List<String> actual = Arrays.asList("A", "man", "a", "plan", "a", "canal", "Van Halen");
		List<String> expected = Arrays.asList("A", "man", "a", "plan", "a", "canal", "Panama");

		try {
			assertLinesMatch(actual, expected);
			expectAssertionFailedError();
		}
		catch (AssertionFailedError ex) {
			assertExpectedAndActualValues(ex, expected.stream().collect(Collectors.joining("\n")),
				actual.stream().collect(Collectors.joining("\n")));
		}
	}

	@Test
	void testAssertSame() {
		String actual = "A man, a plan, a canal, Panama!";
		String expected = "!amanaP ,lanac a ,nalp a ,nam A";

		try {
			assertSame(actual, expected);
			expectAssertionFailedError();
		}
		catch (AssertionFailedError ex) {
			assertExpectedAndActualValues(ex, expected, actual);
		}

		try {
			assertSame(actual, expected, MESSAGE);
			expectAssertionFailedError();
		}
		catch (AssertionFailedError ex) {
			assertExpectedAndActualValues(ex, expected, actual);
		}

		try {
			assertSame(actual, expected, MESSAGE_SUPPLIER);
			expectAssertionFailedError();
		}
		catch (AssertionFailedError ex) {
			assertExpectedAndActualValues(ex, expected, actual);
		}
	}

	/*
	 * This is nowhere near general enough to go in AssertionTestUtils, but we need the sanity check
	 * here to make sure we really have our actual and expected values in the right places.
	 */
	private static void assertElementMessage(AssertionFailedError ex, Object expected, Object actual) {
		assertMessageContains(ex, formatValues(expected, actual));
	}

	private static void assertArraySizeMessage(AssertionFailedError ex, Object[] expected, Object[] actual) {
		assertMessageContains(ex, formatLengths(expected.length, actual.length));
	}

	private static String formatMethods(List<Method> methods) {
		return methods.stream().map(Method::toString).collect(Collectors.joining("\n"));
	}
}
