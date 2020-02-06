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

import static java.util.stream.Collectors.joining;

import java.util.Deque;
import java.util.function.Supplier;

import org.junit.platform.commons.util.BlacklistedExceptions;
import org.junit.platform.commons.util.StringUtils;
import org.opentest4j.AssertionFailedError;

/**
 * {@code AssertionUtils} is a collection of utility methods that are common to
 * all assertion implementations.
 *
 * @since 5.0
 */
class AssertionUtils {

	private AssertionUtils() {
		/* no-op */
	}

	static void fail() {
		throw new AssertionFailedError();
	}

	static void fail(String message) {
		throw new AssertionFailedError(message);
	}

	static void fail(String message, Throwable cause) {
		throw new AssertionFailedError(message, cause);
	}

	static void fail(Throwable cause) {
		throw new AssertionFailedError(null, cause);
	}

	static void fail(Supplier<String> messageSupplier) {
		throw new AssertionFailedError(nullSafeGet(messageSupplier));
	}

	static void fail(String message, Object expected, Object actual) {
		throw new AssertionFailedError(message, expected, actual);
	}

	/**
	 * Typically used for {@code assertEquals()}.
	 */
	static void failNotEqual(Object expected, Object actual, String message) {
		fail(format(expected, actual, message), expected, actual);
	}

	/**
	 * Typically used for {@code assertEquals()}.
	 */
	static void failNotEqual(Object expected, Object actual, Supplier<String> messageSupplier) {
		fail(format(expected, actual, nullSafeGet(messageSupplier)), expected, actual);
	}

	static String nullSafeGet(Supplier<String> messageSupplier) {
		return (messageSupplier != null ? messageSupplier.get() : null);
	}

	/**
	 * Alternative to {@link #nullSafeGet(Supplier)} that is used to avoid
	 * wrapping a String in a lambda expression.
	 *
	 * @param messageOrSupplier an object that is either a {@code String} or
	 * {@code Supplier<String>}
	 */
	static String nullSafeGet(Object messageOrSupplier) {
		if (messageOrSupplier instanceof String) {
			return (String) messageOrSupplier;
		}
		if (messageOrSupplier instanceof Supplier) {
			Object message = ((Supplier<?>) messageOrSupplier).get();
			if (message != null) {
				return message.toString();
			}
		}
		return null;
	}

	static String buildPrefix(String message) {
		return (StringUtils.isNotBlank(message) ? message + " ==> " : "");
	}

	static String getCanonicalName(Class<?> clazz) {
		try {
			String canonicalName = clazz.getCanonicalName();
			return (canonicalName != null ? canonicalName : clazz.getName());
		}
		catch (Throwable t) {
			BlacklistedExceptions.rethrowIfBlacklisted(t);
			return clazz.getName();
		}
	}

	static String format(Object expected, Object actual, String message) {
		return buildPrefix(message) + formatValues(expected, actual);
	}

	static String formatValues(Object expected, Object actual) {
		String expectedString = toString(expected);
		String actualString = toString(actual);
		if (expectedString.equals(actualString)) {
			return String.format("expected: %s but was: %s", formatClassAndValue(expected, expectedString),
				formatClassAndValue(actual, actualString));
		}
		return String.format("expected: <%s> but was: <%s>", expectedString, actualString);
	}

	private static String formatClassAndValue(Object value, String valueString) {
		String classAndHash = getClassName(value) + toHash(value);
		// if it's a class, there's no need to repeat the class name contained in the valueString.
		return (value instanceof Class ? "<" + classAndHash + ">" : classAndHash + "<" + valueString + ">");
	}

	private static String toString(Object obj) {
		if (obj instanceof Class) {
			return getCanonicalName((Class<?>) obj);
		}
		return StringUtils.nullSafeToString(obj);
	}

	private static String toHash(Object obj) {
		return (obj == null ? "" : "@" + Integer.toHexString(System.identityHashCode(obj)));
	}

	private static String getClassName(Object obj) {
		return (obj == null ? "null"
				: obj instanceof Class ? getCanonicalName((Class<?>) obj) : obj.getClass().getName());
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
		if (Float.isNaN(delta) || delta < 0.0) {
			failIllegalDelta(String.valueOf(delta));
		}
	}

	static void assertValidDelta(double delta) {
		if (Double.isNaN(delta) || delta < 0.0) {
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
		return obj1.equals(obj2);
	}

	private static void failIllegalDelta(String delta) {
		fail("positive delta expected but was: <" + delta + ">");
	}

}
