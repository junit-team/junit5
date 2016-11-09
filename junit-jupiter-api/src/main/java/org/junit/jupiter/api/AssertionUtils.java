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

import static java.util.stream.Collectors.joining;

import java.util.Deque;
import java.util.function.Supplier;

import org.junit.platform.commons.util.StringUtils;
import org.opentest4j.AssertionFailedError;

class AssertionUtils {

	///CLOVER:OFF
	private AssertionUtils() {
		/* no-op */
	}
	///CLOVER:ON

	static void fail(String message) {
		fail(() -> message);
	}

	static void fail(Supplier<String> messageSupplier) {
		throw new AssertionFailedError(nullSafeGet(messageSupplier));
	}

	static void fail(String message, Object expected, Object actual) {
		throw new AssertionFailedError(message, expected, actual);
	}

	static String buildPrefix(String message) {
		return (StringUtils.isNotBlank(message) ? message + " ==> " : "");
	}

	static String nullSafeGet(Supplier<String> messageSupplier) {
		return (messageSupplier != null ? messageSupplier.get() : null);
	}

	static String formatValues(Object expected, Object actual) {
		String expectedString = String.valueOf(expected);
		String actualString = String.valueOf(actual);
		if (expectedString.equals(actualString)) {
			return "expected: " + formatClassAndValue(expected, expectedString) + " but was: "
					+ formatClassAndValue(actual, actualString);
		}
		else {
			return "expected: <" + expectedString + "> but was: <" + actualString + ">";
		}
	}

	static String formatIndexes(Deque<Integer> indexes) {
		if (indexes == null || indexes.isEmpty()) {
			return "";
		}
		String indexesString = indexes.stream().map(Object::toString).collect(joining("][", "[", "]"));
		return " at index " + indexesString;
	}

	static boolean floatsAreEqual(float value1, float value2, float delta) {
		assertValidDelta(delta);
		return floatsAreEqual(value1, value2) || Math.abs(value1 - value2) <= delta;
	}

	static void assertValidDelta(float delta) {
		if (Float.isNaN(delta) || delta <= 0.0) {
			failIllegalDelta(String.valueOf(delta));
		}
	}

	static void assertValidDelta(double delta) {
		if (Double.isNaN(delta) || delta <= 0.0) {
			failIllegalDelta(String.valueOf(delta));
		}
	}

	static boolean floatsAreEqual(float value1, float value2) {
		return Float.floatToIntBits(value1) == Float.floatToIntBits(value2);
	}

	static boolean doublesAreEqual(double value1, double value2, double delta) {
		assertValidDelta(delta);
		return doublesAreEqual(value1, value2) || Math.abs(value1 - value2) <= delta;
	}

	static boolean doublesAreEqual(double value1, double value2) {
		return Double.doubleToLongBits(value1) == Double.doubleToLongBits(value2);
	}

	static boolean objectsAreEqual(Object obj1, Object obj2) {
		if (obj1 == null) {
			return (obj2 == null);
		}
		else {
			return obj1.equals(obj2);
		}
	}

	static String format(Object expected, Object actual, String message) {
		return buildPrefix(message) + formatValues(expected, actual);
	}

	private static String formatClassAndValue(Object value, String valueString) {
		String className = (value == null ? "null" : value.getClass().getName());
		String hash = (value == null ? "" : "@" + Integer.toHexString(System.identityHashCode(value)));
		return className + hash + "<" + valueString + ">";
	}

	private static void failIllegalDelta(String delta) {
		Assertions.fail("positive delta expected but was: <" + delta + ">");
	}

}
