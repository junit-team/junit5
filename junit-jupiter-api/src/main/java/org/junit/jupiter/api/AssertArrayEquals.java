/*
 * Copyright 2015-2024 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * https://www.eclipse.org/legal/epl-v20.html
 */

package org.junit.jupiter.api;

import static org.junit.jupiter.api.AssertionFailureBuilder.assertionFailure;
import static org.junit.jupiter.api.AssertionUtils.formatIndexes;
import static org.junit.platform.commons.util.ReflectionUtils.isArray;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Objects;
import java.util.function.Supplier;

/**
 * {@code AssertArrayEquals} is a collection of utility methods that support asserting
 * array equality in tests.
 *
 * @since 5.0
 */
class AssertArrayEquals {

	private AssertArrayEquals() {
		/* no-op */
	}

	static void assertArrayEquals(boolean[] expected, boolean[] actual) {
		assertArrayEquals(expected, actual, (String) null);
	}

	static void assertArrayEquals(boolean[] expected, boolean[] actual, String message) {
		assertArrayEquals(expected, actual, null, message);
	}

	static void assertArrayEquals(boolean[] expected, boolean[] actual, Supplier<String> messageSupplier) {
		assertArrayEquals(expected, actual, null, messageSupplier);
	}

	static void assertArrayEquals(char[] expected, char[] actual, String message) {
		assertArrayEquals(expected, actual, null, message);
	}

	static void assertArrayEquals(char[] expected, char[] actual) {
		assertArrayEquals(expected, actual, (String) null);
	}

	static void assertArrayEquals(char[] expected, char[] actual, Supplier<String> messageSupplier) {
		assertArrayEquals(expected, actual, null, messageSupplier);
	}

	static void assertArrayEquals(byte[] expected, byte[] actual) {
		assertArrayEquals(expected, actual, (String) null);
	}

	static void assertArrayEquals(byte[] expected, byte[] actual, String message) {
		assertArrayEquals(expected, actual, null, message);
	}

	static void assertArrayEquals(byte[] expected, byte[] actual, Supplier<String> messageSupplier) {
		assertArrayEquals(expected, actual, null, messageSupplier);
	}

	static void assertArrayEquals(short[] expected, short[] actual) {
		assertArrayEquals(expected, actual, (String) null);
	}

	static void assertArrayEquals(short[] expected, short[] actual, String message) {
		assertArrayEquals(expected, actual, null, message);
	}

	static void assertArrayEquals(short[] expected, short[] actual, Supplier<String> messageSupplier) {
		assertArrayEquals(expected, actual, null, messageSupplier);
	}

	static void assertArrayEquals(int[] expected, int[] actual) {
		assertArrayEquals(expected, actual, (String) null);
	}

	static void assertArrayEquals(int[] expected, int[] actual, String message) {
		assertArrayEquals(expected, actual, null, message);
	}

	static void assertArrayEquals(int[] expected, int[] actual, Supplier<String> messageSupplier) {
		assertArrayEquals(expected, actual, null, messageSupplier);
	}

	static void assertArrayEquals(long[] expected, long[] actual) {
		assertArrayEquals(expected, actual, (String) null);
	}

	static void assertArrayEquals(long[] expected, long[] actual, String message) {
		assertArrayEquals(expected, actual, null, message);
	}

	static void assertArrayEquals(long[] expected, long[] actual, Supplier<String> messageSupplier) {
		assertArrayEquals(expected, actual, null, messageSupplier);
	}

	static void assertArrayEquals(float[] expected, float[] actual) {
		assertArrayEquals(expected, actual, (String) null);
	}

	static void assertArrayEquals(float[] expected, float[] actual, String message) {
		assertArrayEquals(expected, actual, null, message);
	}

	static void assertArrayEquals(float[] expected, float[] actual, Supplier<String> messageSupplier) {
		assertArrayEquals(expected, actual, null, messageSupplier);
	}

	static void assertArrayEquals(float[] expected, float[] actual, float delta) {
		assertArrayEquals(expected, actual, delta, (String) null);
	}

	static void assertArrayEquals(float[] expected, float[] actual, float delta, String message) {
		assertArrayEquals(expected, actual, delta, null, message);
	}

	static void assertArrayEquals(float[] expected, float[] actual, float delta, Supplier<String> messageSupplier) {
		assertArrayEquals(expected, actual, delta, null, messageSupplier);
	}

	static void assertArrayEquals(double[] expected, double[] actual) {
		assertArrayEquals(expected, actual, (String) null);
	}

	static void assertArrayEquals(double[] expected, double[] actual, String message) {
		assertArrayEquals(expected, actual, null, message);
	}

	static void assertArrayEquals(double[] expected, double[] actual, Supplier<String> messageSupplier) {
		assertArrayEquals(expected, actual, null, messageSupplier);
	}

	static void assertArrayEquals(double[] expected, double[] actual, double delta) {
		assertArrayEquals(expected, actual, delta, (String) null);
	}

	static void assertArrayEquals(double[] expected, double[] actual, double delta, String message) {
		assertArrayEquals(expected, actual, delta, null, message);
	}

	static void assertArrayEquals(double[] expected, double[] actual, double delta, Supplier<String> messageSupplier) {
		assertArrayEquals(expected, actual, delta, null, messageSupplier);
	}

	static void assertArrayEquals(Object[] expected, Object[] actual) {
		assertArrayEquals(expected, actual, (String) null);
	}

	static void assertArrayEquals(Object[] expected, Object[] actual, String message) {
		assertArrayEquals(expected, actual, new ArrayDeque<>(), message);
	}

	static void assertArrayEquals(Object[] expected, Object[] actual, Supplier<String> messageSupplier) {
		assertArrayEquals(expected, actual, new ArrayDeque<>(), messageSupplier);
	}

	private static void assertArrayEquals(boolean[] expected, boolean[] actual, Deque<Integer> indexes,
			Object messageOrSupplier) {

		if (expected == actual) {
			return;
		}
		assertArraysNotNull(expected, actual, indexes, messageOrSupplier);
		assertArraysHaveSameLength(expected.length, actual.length, indexes, messageOrSupplier);

		for (int i = 0; i < expected.length; i++) {
			if (expected[i] != actual[i]) {
				failArraysNotEqual(expected[i], actual[i], nullSafeIndexes(indexes, i), messageOrSupplier);
			}
		}
	}

	private static void assertArrayEquals(char[] expected, char[] actual, Deque<Integer> indexes,
			Object messageOrSupplier) {

		if (expected == actual) {
			return;
		}
		assertArraysNotNull(expected, actual, indexes, messageOrSupplier);
		assertArraysHaveSameLength(expected.length, actual.length, indexes, messageOrSupplier);

		for (int i = 0; i < expected.length; i++) {
			if (expected[i] != actual[i]) {
				failArraysNotEqual(expected[i], actual[i], nullSafeIndexes(indexes, i), messageOrSupplier);
			}
		}
	}

	private static void assertArrayEquals(byte[] expected, byte[] actual, Deque<Integer> indexes,
			Object messageOrSupplier) {

		if (expected == actual) {
			return;
		}
		assertArraysNotNull(expected, actual, indexes, messageOrSupplier);
		assertArraysHaveSameLength(expected.length, actual.length, indexes, messageOrSupplier);

		for (int i = 0; i < expected.length; i++) {
			if (expected[i] != actual[i]) {
				failArraysNotEqual(expected[i], actual[i], nullSafeIndexes(indexes, i), messageOrSupplier);
			}
		}
	}

	private static void assertArrayEquals(short[] expected, short[] actual, Deque<Integer> indexes,
			Object messageOrSupplier) {

		if (expected == actual) {
			return;
		}
		assertArraysNotNull(expected, actual, indexes, messageOrSupplier);
		assertArraysHaveSameLength(expected.length, actual.length, indexes, messageOrSupplier);

		for (int i = 0; i < expected.length; i++) {
			if (expected[i] != actual[i]) {
				failArraysNotEqual(expected[i], actual[i], nullSafeIndexes(indexes, i), messageOrSupplier);
			}
		}
	}

	private static void assertArrayEquals(int[] expected, int[] actual, Deque<Integer> indexes,
			Object messageOrSupplier) {

		if (expected == actual) {
			return;
		}
		assertArraysNotNull(expected, actual, indexes, messageOrSupplier);
		assertArraysHaveSameLength(expected.length, actual.length, indexes, messageOrSupplier);

		for (int i = 0; i < expected.length; i++) {
			if (expected[i] != actual[i]) {
				failArraysNotEqual(expected[i], actual[i], nullSafeIndexes(indexes, i), messageOrSupplier);
			}
		}
	}

	private static void assertArrayEquals(long[] expected, long[] actual, Deque<Integer> indexes,
			Object messageOrSupplier) {

		if (expected == actual) {
			return;
		}
		assertArraysNotNull(expected, actual, indexes, messageOrSupplier);
		assertArraysHaveSameLength(expected.length, actual.length, indexes, messageOrSupplier);

		for (int i = 0; i < expected.length; i++) {
			if (expected[i] != actual[i]) {
				failArraysNotEqual(expected[i], actual[i], nullSafeIndexes(indexes, i), messageOrSupplier);
			}
		}
	}

	private static void assertArrayEquals(float[] expected, float[] actual, Deque<Integer> indexes,
			Object messageOrSupplier) {

		if (expected == actual) {
			return;
		}
		assertArraysNotNull(expected, actual, indexes, messageOrSupplier);
		assertArraysHaveSameLength(expected.length, actual.length, indexes, messageOrSupplier);

		for (int i = 0; i < expected.length; i++) {
			if (!AssertionUtils.floatsAreEqual(expected[i], actual[i])) {
				failArraysNotEqual(expected[i], actual[i], nullSafeIndexes(indexes, i), messageOrSupplier);
			}
		}
	}

	private static void assertArrayEquals(float[] expected, float[] actual, float delta, Deque<Integer> indexes,
			Object messageOrSupplier) {

		AssertionUtils.assertValidDelta(delta);
		if (expected == actual) {
			return;
		}
		assertArraysNotNull(expected, actual, indexes, messageOrSupplier);
		assertArraysHaveSameLength(expected.length, actual.length, indexes, messageOrSupplier);

		for (int i = 0; i < expected.length; i++) {
			if (!AssertionUtils.floatsAreEqual(expected[i], actual[i], delta)) {
				failArraysNotEqual(expected[i], actual[i], nullSafeIndexes(indexes, i), messageOrSupplier);
			}
		}
	}

	private static void assertArrayEquals(double[] expected, double[] actual, Deque<Integer> indexes,
			Object messageOrSupplier) {

		if (expected == actual) {
			return;
		}
		assertArraysNotNull(expected, actual, indexes, messageOrSupplier);
		assertArraysHaveSameLength(expected.length, actual.length, indexes, messageOrSupplier);

		for (int i = 0; i < expected.length; i++) {
			if (!AssertionUtils.doublesAreEqual(expected[i], actual[i])) {
				failArraysNotEqual(expected[i], actual[i], nullSafeIndexes(indexes, i), messageOrSupplier);
			}
		}
	}

	private static void assertArrayEquals(double[] expected, double[] actual, double delta, Deque<Integer> indexes,
			Object messageOrSupplier) {

		AssertionUtils.assertValidDelta(delta);
		if (expected == actual) {
			return;
		}
		assertArraysNotNull(expected, actual, indexes, messageOrSupplier);
		assertArraysHaveSameLength(expected.length, actual.length, indexes, messageOrSupplier);

		for (int i = 0; i < expected.length; i++) {
			if (!AssertionUtils.doublesAreEqual(expected[i], actual[i], delta)) {
				failArraysNotEqual(expected[i], actual[i], nullSafeIndexes(indexes, i), messageOrSupplier);
			}
		}
	}

	private static void assertArrayEquals(Object[] expected, Object[] actual, Deque<Integer> indexes,
			Object messageOrSupplier) {

		if (expected == actual) {
			return;
		}
		assertArraysNotNull(expected, actual, indexes, messageOrSupplier);
		assertArraysHaveSameLength(expected.length, actual.length, indexes, messageOrSupplier);

		for (int i = 0; i < expected.length; i++) {
			Object expectedElement = expected[i];
			Object actualElement = actual[i];

			if (expectedElement == actualElement) {
				continue;
			}

			indexes.addLast(i);
			assertArrayElementsEqual(expectedElement, actualElement, indexes, messageOrSupplier);
			indexes.removeLast();
		}
	}

	private static void assertArrayElementsEqual(Object expected, Object actual, Deque<Integer> indexes,
			Object messageOrSupplier) {

		if (expected instanceof Object[] && actual instanceof Object[]) {
			assertArrayEquals((Object[]) expected, (Object[]) actual, indexes, messageOrSupplier);
		}
		else if (expected instanceof byte[] && actual instanceof byte[]) {
			assertArrayEquals((byte[]) expected, (byte[]) actual, indexes, messageOrSupplier);
		}
		else if (expected instanceof short[] && actual instanceof short[]) {
			assertArrayEquals((short[]) expected, (short[]) actual, indexes, messageOrSupplier);
		}
		else if (expected instanceof int[] && actual instanceof int[]) {
			assertArrayEquals((int[]) expected, (int[]) actual, indexes, messageOrSupplier);
		}
		else if (expected instanceof long[] && actual instanceof long[]) {
			assertArrayEquals((long[]) expected, (long[]) actual, indexes, messageOrSupplier);
		}
		else if (expected instanceof char[] && actual instanceof char[]) {
			assertArrayEquals((char[]) expected, (char[]) actual, indexes, messageOrSupplier);
		}
		else if (expected instanceof float[] && actual instanceof float[]) {
			assertArrayEquals((float[]) expected, (float[]) actual, indexes, messageOrSupplier);
		}
		else if (expected instanceof double[] && actual instanceof double[]) {
			assertArrayEquals((double[]) expected, (double[]) actual, indexes, messageOrSupplier);
		}
		else if (expected instanceof boolean[] && actual instanceof boolean[]) {
			assertArrayEquals((boolean[]) expected, (boolean[]) actual, indexes, messageOrSupplier);
		}
		else if (!Objects.equals(expected, actual)) {
			if (expected == null && isArray(actual)) {
				failExpectedArrayIsNull(indexes, messageOrSupplier);
			}
			else if (isArray(expected) && actual == null) {
				failActualArrayIsNull(indexes, messageOrSupplier);
			}
			else {
				failArraysNotEqual(expected, actual, indexes, messageOrSupplier);
			}
		}
	}

	private static void assertArraysNotNull(Object expected, Object actual, Deque<Integer> indexes,
			Object messageOrSupplier) {

		if (expected == null) {
			failExpectedArrayIsNull(indexes, messageOrSupplier);
		}
		if (actual == null) {
			failActualArrayIsNull(indexes, messageOrSupplier);
		}
	}

	private static void failExpectedArrayIsNull(Deque<Integer> indexes, Object messageOrSupplier) {
		assertionFailure() //
				.message(messageOrSupplier) //
				.reason("expected array was <null>" + formatIndexes(indexes)) //
				.buildAndThrow();
	}

	private static void failActualArrayIsNull(Deque<Integer> indexes, Object messageOrSupplier) {
		assertionFailure() //
				.message(messageOrSupplier) //
				.reason("actual array was <null>" + formatIndexes(indexes)) //
				.buildAndThrow();
	}

	private static void assertArraysHaveSameLength(int expected, int actual, Deque<Integer> indexes,
			Object messageOrSupplier) {

		if (expected != actual) {
			assertionFailure() //
					.message(messageOrSupplier) //
					.reason("array lengths differ" + formatIndexes(indexes)) //
					.expected(expected) //
					.actual(actual) //
					.buildAndThrow();
		}
	}

	private static void failArraysNotEqual(Object expected, Object actual, Deque<Integer> indexes,
			Object messageOrSupplier) {

		assertionFailure() //
				.message(messageOrSupplier) //
				.reason("array contents differ" + formatIndexes(indexes)) //
				.expected(expected) //
				.actual(actual) //
				.buildAndThrow();
	}

	private static Deque<Integer> nullSafeIndexes(Deque<Integer> indexes, int newIndex) {
		Deque<Integer> result = (indexes != null ? indexes : new ArrayDeque<>());
		result.addLast(newIndex);
		return result;
	}

}
