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

import static org.junit.jupiter.api.AssertionsUtil.buildPrefix;
import static org.junit.jupiter.api.AssertionsUtil.formatIndexes;
import static org.junit.jupiter.api.AssertionsUtil.formatValues;
import static org.junit.jupiter.api.AssertionsUtil.nullSafeGet;
import static org.junit.jupiter.api.Fail.fail;
import static org.junit.platform.commons.util.ReflectionUtils.isArray;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Objects;
import java.util.function.Supplier;

public class AssertArrayEquals {

	public static void assertArrayEquals(boolean[] expected, boolean[] actual) {
		assertArrayEquals(expected, actual, () -> null);
	}

	public static void assertArrayEquals(boolean[] expected, boolean[] actual, String message) {
		assertArrayEquals(expected, actual, () -> message);
	}

	public static void assertArrayEquals(boolean[] expected, boolean[] actual, Supplier<String> messageSupplier) {
		assertArrayEquals(expected, actual, null, messageSupplier);
	}

	public static void assertArrayEquals(char[] expected, char[] actual, String message) {
		assertArrayEquals(expected, actual, () -> message);
	}

	public static void assertArrayEquals(char[] expected, char[] actual) {
		assertArrayEquals(expected, actual, () -> null);
	}

	public static void assertArrayEquals(char[] expected, char[] actual, Supplier<String> messageSupplier) {
		assertArrayEquals(expected, actual, null, messageSupplier);
	}

	public static void assertArrayEquals(byte[] expected, byte[] actual) {
		assertArrayEquals(expected, actual, () -> null);
	}

	public static void assertArrayEquals(byte[] expected, byte[] actual, String message) {
		assertArrayEquals(expected, actual, () -> message);
	}

	public static void assertArrayEquals(byte[] expected, byte[] actual, Supplier<String> messageSupplier) {
		assertArrayEquals(expected, actual, null, messageSupplier);
	}

	public static void assertArrayEquals(short[] expected, short[] actual) {
		assertArrayEquals(expected, actual, () -> null);
	}

	public static void assertArrayEquals(short[] expected, short[] actual, String message) {
		assertArrayEquals(expected, actual, () -> message);
	}

	public static void assertArrayEquals(short[] expected, short[] actual, Supplier<String> messageSupplier) {
		assertArrayEquals(expected, actual, null, messageSupplier);
	}

	public static void assertArrayEquals(int[] expected, int[] actual) {
		assertArrayEquals(expected, actual, () -> null);
	}

	public static void assertArrayEquals(int[] expected, int[] actual, String message) {
		assertArrayEquals(expected, actual, () -> message);
	}

	public static void assertArrayEquals(int[] expected, int[] actual, Supplier<String> messageSupplier) {
		assertArrayEquals(expected, actual, null, messageSupplier);
	}

	public static void assertArrayEquals(long[] expected, long[] actual) {
		assertArrayEquals(expected, actual, () -> null);
	}

	public static void assertArrayEquals(long[] expected, long[] actual, String message) {
		assertArrayEquals(expected, actual, () -> message);
	}

	public static void assertArrayEquals(long[] expected, long[] actual, Supplier<String> messageSupplier) {
		assertArrayEquals(expected, actual, null, messageSupplier);
	}

	public static void assertArrayEquals(float[] expected, float[] actual) {
		assertArrayEquals(expected, actual, () -> null);
	}

	public static void assertArrayEquals(float[] expected, float[] actual, String message) {
		assertArrayEquals(expected, actual, () -> message);
	}

	public static void assertArrayEquals(float[] expected, float[] actual, Supplier<String> messageSupplier) {
		assertArrayEquals(expected, actual, null, messageSupplier);
	}

	public static void assertArrayEquals(float[] expected, float[] actual, float delta) {
		assertArrayEquals(expected, actual, delta, () -> null);
	}

	public static void assertArrayEquals(float[] expected, float[] actual, float delta, String message) {
		assertArrayEquals(expected, actual, delta, () -> message);
	}

	public static void assertArrayEquals(float[] expected, float[] actual, float delta,
			Supplier<String> messageSupplier) {
		assertArrayEquals(expected, actual, delta, null, messageSupplier);
	}

	public static void assertArrayEquals(double[] expected, double[] actual) {
		assertArrayEquals(expected, actual, () -> null);
	}

	public static void assertArrayEquals(double[] expected, double[] actual, String message) {
		assertArrayEquals(expected, actual, () -> message);
	}

	public static void assertArrayEquals(double[] expected, double[] actual, Supplier<String> messageSupplier) {
		assertArrayEquals(expected, actual, null, messageSupplier);
	}

	public static void assertArrayEquals(double[] expected, double[] actual, double delta) {
		assertArrayEquals(expected, actual, delta, () -> null);
	}

	public static void assertArrayEquals(double[] expected, double[] actual, double delta, String message) {
		assertArrayEquals(expected, actual, delta, () -> message);
	}

	public static void assertArrayEquals(double[] expected, double[] actual, double delta,
			Supplier<String> messageSupplier) {
		assertArrayEquals(expected, actual, delta, null, messageSupplier);
	}

	public static void assertArrayEquals(Object[] expected, Object[] actual) {
		assertArrayEquals(expected, actual, () -> null);
	}

	public static void assertArrayEquals(Object[] expected, Object[] actual, String message) {
		assertArrayEquals(expected, actual, () -> message);
	}

	public static void assertArrayEquals(Object[] expected, Object[] actual, Supplier<String> messageSupplier) {
		assertArrayEquals(expected, actual, new ArrayDeque<>(), messageSupplier);
	}

	private static void assertArrayEquals(boolean[] expected, boolean[] actual, Deque<Integer> indexes,
			Supplier<String> messageSupplier) {

		if (expected == actual) {
			return;
		}
		assertArraysNotNull(expected, actual, indexes, messageSupplier);
		assertArraysHaveSameLength(expected.length, actual.length, indexes, messageSupplier);

		for (int i = 0; i < expected.length; i++) {
			if (expected[i] != actual[i]) {
				failArraysNotEqual(expected[i], actual[i], nullSafeIndexes(indexes, i), messageSupplier);
			}
		}
	}

	private static void assertArrayEquals(char[] expected, char[] actual, Deque<Integer> indexes,
			Supplier<String> messageSupplier) {

		if (expected == actual) {
			return;
		}
		assertArraysNotNull(expected, actual, indexes, messageSupplier);
		assertArraysHaveSameLength(expected.length, actual.length, indexes, messageSupplier);

		for (int i = 0; i < expected.length; i++) {
			if (expected[i] != actual[i]) {
				failArraysNotEqual(expected[i], actual[i], nullSafeIndexes(indexes, i), messageSupplier);
			}
		}
	}

	private static void assertArrayEquals(byte[] expected, byte[] actual, Deque<Integer> indexes,
			Supplier<String> messageSupplier) {

		if (expected == actual) {
			return;
		}
		assertArraysNotNull(expected, actual, indexes, messageSupplier);
		assertArraysHaveSameLength(expected.length, actual.length, indexes, messageSupplier);

		for (int i = 0; i < expected.length; i++) {
			if (expected[i] != actual[i]) {
				failArraysNotEqual(expected[i], actual[i], nullSafeIndexes(indexes, i), messageSupplier);
			}
		}
	}

	private static void assertArrayEquals(short[] expected, short[] actual, Deque<Integer> indexes,
			Supplier<String> messageSupplier) {

		if (expected == actual) {
			return;
		}
		assertArraysNotNull(expected, actual, indexes, messageSupplier);
		assertArraysHaveSameLength(expected.length, actual.length, indexes, messageSupplier);

		for (int i = 0; i < expected.length; i++) {
			if (expected[i] != actual[i]) {
				failArraysNotEqual(expected[i], actual[i], nullSafeIndexes(indexes, i), messageSupplier);
			}
		}
	}

	private static void assertArrayEquals(int[] expected, int[] actual, Deque<Integer> indexes,
			Supplier<String> messageSupplier) {

		if (expected == actual) {
			return;
		}
		assertArraysNotNull(expected, actual, indexes, messageSupplier);
		assertArraysHaveSameLength(expected.length, actual.length, indexes, messageSupplier);

		for (int i = 0; i < expected.length; i++) {
			if (expected[i] != actual[i]) {
				failArraysNotEqual(expected[i], actual[i], nullSafeIndexes(indexes, i), messageSupplier);
			}
		}
	}

	private static void assertArrayEquals(long[] expected, long[] actual, Deque<Integer> indexes,
			Supplier<String> messageSupplier) {

		if (expected == actual) {
			return;
		}
		assertArraysNotNull(expected, actual, indexes, messageSupplier);
		assertArraysHaveSameLength(expected.length, actual.length, indexes, messageSupplier);

		for (int i = 0; i < expected.length; i++) {
			if (expected[i] != actual[i]) {
				failArraysNotEqual(expected[i], actual[i], nullSafeIndexes(indexes, i), messageSupplier);
			}
		}
	}

	private static void assertArrayEquals(float[] expected, float[] actual, Deque<Integer> indexes,
			Supplier<String> messageSupplier) {

		if (expected == actual) {
			return;
		}
		assertArraysNotNull(expected, actual, indexes, messageSupplier);
		assertArraysHaveSameLength(expected.length, actual.length, indexes, messageSupplier);

		for (int i = 0; i < expected.length; i++) {
			if (!AssertionsUtil.floatsAreEqual(expected[i], actual[i])) {
				failArraysNotEqual(expected[i], actual[i], nullSafeIndexes(indexes, i), messageSupplier);
			}
		}
	}

	private static void assertArrayEquals(float[] expected, float[] actual, float delta, Deque<Integer> indexes,
			Supplier<String> messageSupplier) {

		AssertionsUtil.assertValidDelta(delta);
		if (expected == actual) {
			return;
		}
		assertArraysNotNull(expected, actual, indexes, messageSupplier);
		assertArraysHaveSameLength(expected.length, actual.length, indexes, messageSupplier);

		for (int i = 0; i < expected.length; i++) {
			if (!AssertionsUtil.floatsAreEqual(expected[i], actual[i], delta)) {
				failArraysNotEqual(expected[i], actual[i], nullSafeIndexes(indexes, i), messageSupplier);
			}
		}
	}

	private static void assertArrayEquals(double[] expected, double[] actual, Deque<Integer> indexes,
			Supplier<String> messageSupplier) {

		if (expected == actual) {
			return;
		}
		assertArraysNotNull(expected, actual, indexes, messageSupplier);
		assertArraysHaveSameLength(expected.length, actual.length, indexes, messageSupplier);

		for (int i = 0; i < expected.length; i++) {
			if (!AssertionsUtil.doublesAreEqual(expected[i], actual[i])) {
				failArraysNotEqual(expected[i], actual[i], nullSafeIndexes(indexes, i), messageSupplier);
			}
		}
	}

	private static void assertArrayEquals(double[] expected, double[] actual, double delta, Deque<Integer> indexes,
			Supplier<String> messageSupplier) {

		AssertionsUtil.assertValidDelta(delta);
		if (expected == actual) {
			return;
		}
		assertArraysNotNull(expected, actual, indexes, messageSupplier);
		assertArraysHaveSameLength(expected.length, actual.length, indexes, messageSupplier);

		for (int i = 0; i < expected.length; i++) {
			if (!AssertionsUtil.doublesAreEqual(expected[i], actual[i], delta)) {
				failArraysNotEqual(expected[i], actual[i], nullSafeIndexes(indexes, i), messageSupplier);
			}
		}
	}

	private static void assertArrayEquals(Object[] expected, Object[] actual, Deque<Integer> indexes,
			Supplier<String> messageSupplier) {

		if (expected == actual) {
			return;
		}
		assertArraysNotNull(expected, actual, indexes, messageSupplier);
		assertArraysHaveSameLength(expected.length, actual.length, indexes, messageSupplier);

		for (int i = 0; i < expected.length; i++) {
			Object expectedElement = expected[i];
			Object actualElement = actual[i];

			if (expectedElement == actualElement) {
				continue;
			}

			indexes.addLast(i);
			assertArrayElementsEqual(expectedElement, actualElement, indexes, messageSupplier);
			indexes.removeLast();
		}
	}

	private static void assertArrayElementsEqual(Object expected, Object actual, Deque<Integer> indexes,
			Supplier<String> messageSupplier) {

		if (expected instanceof Object[] && actual instanceof Object[]) {
			assertArrayEquals((Object[]) expected, (Object[]) actual, indexes, messageSupplier);
		}
		else if (expected instanceof byte[] && actual instanceof byte[]) {
			assertArrayEquals((byte[]) expected, (byte[]) actual, indexes, messageSupplier);
		}
		else if (expected instanceof short[] && actual instanceof short[]) {
			assertArrayEquals((short[]) expected, (short[]) actual, indexes, messageSupplier);
		}
		else if (expected instanceof int[] && actual instanceof int[]) {
			assertArrayEquals((int[]) expected, (int[]) actual, indexes, messageSupplier);
		}
		else if (expected instanceof long[] && actual instanceof long[]) {
			assertArrayEquals((long[]) expected, (long[]) actual, indexes, messageSupplier);
		}
		else if (expected instanceof char[] && actual instanceof char[]) {
			assertArrayEquals((char[]) expected, (char[]) actual, indexes, messageSupplier);
		}
		else if (expected instanceof float[] && actual instanceof float[]) {
			assertArrayEquals((float[]) expected, (float[]) actual, indexes, messageSupplier);
		}
		else if (expected instanceof double[] && actual instanceof double[]) {
			assertArrayEquals((double[]) expected, (double[]) actual, indexes, messageSupplier);
		}
		else if (expected instanceof boolean[] && actual instanceof boolean[]) {
			assertArrayEquals((boolean[]) expected, (boolean[]) actual, indexes, messageSupplier);
		}
		else if (!Objects.equals(expected, actual)) {
			if (expected == null && isArray(actual)) {
				failExpectedArrayIsNull(indexes, messageSupplier);
			}
			else if (isArray(expected) && actual == null) {
				failActualArrayIsNull(indexes, messageSupplier);
			}
			else {
				failArraysNotEqual(expected, actual, indexes, messageSupplier);
			}
		}
	}

	private static void assertArraysNotNull(Object expected, Object actual, Deque<Integer> indexes,
			Supplier<String> messageSupplier) {

		if (expected == null) {
			failExpectedArrayIsNull(indexes, messageSupplier);
		}
		if (actual == null) {
			failActualArrayIsNull(indexes, messageSupplier);
		}
	}

	private static void failExpectedArrayIsNull(Deque<Integer> indexes, Supplier<String> messageSupplier) {
		fail(buildPrefix(nullSafeGet(messageSupplier)) + "expected array was <null>" + formatIndexes(indexes));
	}

	private static void failActualArrayIsNull(Deque<Integer> indexes, Supplier<String> messageSupplier) {
		fail(buildPrefix(nullSafeGet(messageSupplier)) + "actual array was <null>" + formatIndexes(indexes));
	}

	private static void assertArraysHaveSameLength(int expected, int actual, Deque<Integer> indexes,
			Supplier<String> messageSupplier) {

		if (expected != actual) {
			String prefix = buildPrefix(nullSafeGet(messageSupplier));
			String message = "array lengths differ" + formatIndexes(indexes) + ", expected: <" + expected
					+ "> but was: <" + actual + ">";
			fail(prefix + message);
		}
	}

	private static void failArraysNotEqual(Object expected, Object actual, Deque<Integer> indexes,
			Supplier<String> messageSupplier) {

		String prefix = buildPrefix(nullSafeGet(messageSupplier));
		String message = "array contents differ" + formatIndexes(indexes) + ", " + formatValues(expected, actual);
		fail(prefix + message);
	}

	private static Deque<Integer> nullSafeIndexes(Deque<Integer> indexes, int newIndex) {
		Deque<Integer> result = (indexes != null ? indexes : new ArrayDeque<>());
		result.addLast(newIndex);
		return result;
	}

}
