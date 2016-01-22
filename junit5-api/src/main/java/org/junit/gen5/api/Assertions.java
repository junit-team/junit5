/*
 * Copyright 2015-2016 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.junit.gen5.api;

import java.util.Objects;
import java.util.function.BooleanSupplier;
import java.util.function.Supplier;

import org.junit.gen5.commons.util.ExceptionUtils;
import org.junit.gen5.commons.util.StringUtils;
import org.opentest4j.AssertionFailedError;
import org.opentest4j.MultipleFailuresError;

/**
 * @since 5.0
 */
public final class Assertions {

	private Assertions() {
		/* no-op */
	}

	// --- fail ----------------------------------------------------------

	public static void fail(String message) {
		throw new AssertionFailedError(message);
	}

	public static void fail(Supplier<String> messageSupplier) {
		fail(nullSafeGet(messageSupplier));
	}

	// --- assertTrue ----------------------------------------------------

	public static void assertTrue(boolean condition) {
		assertTrue(() -> condition, () -> null);
	}

	public static void assertTrue(boolean condition, Supplier<String> messageSupplier) {
		assertTrue(() -> condition, messageSupplier);
	}

	public static void assertTrue(BooleanSupplier booleanSupplier) {
		assertTrue(booleanSupplier, () -> null);
	}

	public static void assertTrue(BooleanSupplier booleanSupplier, String message) {
		assertTrue(booleanSupplier, () -> message);
	}

	public static void assertTrue(boolean condition, String message) {
		assertTrue(() -> condition, () -> message);
	}

	public static void assertTrue(BooleanSupplier booleanSupplier, Supplier<String> messageSupplier) {
		if (!booleanSupplier.getAsBoolean()) {
			fail(messageSupplier);
		}
	}

	// --- assertFalse ---------------------------------------------------

	public static void assertFalse(boolean condition) {
		assertFalse(() -> condition, () -> null);
	}

	public static void assertFalse(boolean condition, String message) {
		assertFalse(() -> condition, () -> message);
	}

	public static void assertFalse(boolean condition, Supplier<String> messageSupplier) {
		assertFalse(() -> condition, messageSupplier);
	}

	public static void assertFalse(BooleanSupplier booleanSupplier) {
		assertFalse(booleanSupplier, () -> null);
	}

	public static void assertFalse(BooleanSupplier booleanSupplier, String message) {
		assertFalse(booleanSupplier, () -> message);
	}

	public static void assertFalse(BooleanSupplier booleanSupplier, Supplier<String> messageSupplier) {
		if (booleanSupplier.getAsBoolean()) {
			fail(messageSupplier);
		}
	}

	// --- assertNull ----------------------------------------------------

	public static void assertNull(Object actual) {
		assertNull(actual, () -> null);
	}

	public static void assertNull(Object actual, String message) {
		assertNull(actual, () -> message);
	}

	public static void assertNull(Object actual, Supplier<String> messageSupplier) {
		if (actual != null) {
			failNotNull(actual, nullSafeGet(messageSupplier));
		}
	}

	// --- assertNotNull -------------------------------------------------

	public static void assertNotNull(Object actual) {
		assertNotNull(actual, () -> null);
	}

	public static void assertNotNull(Object actual, String message) {
		assertNotNull(actual, () -> message);
	}

	public static void assertNotNull(Object actual, Supplier<String> messageSupplier) {
		if (actual == null) {
			failNull(nullSafeGet(messageSupplier));
		}
	}

	// --- assertEquals -------------------------------------------------

	public static void assertEquals(Object expected, Object actual) {
		assertEquals(expected, actual, () -> null);
	}

	public static void assertEquals(Object expected, Object actual, String message) {
		assertEquals(expected, actual, () -> message);
	}

	public static void assertEquals(Object expected, Object actual, Supplier<String> messageSupplier) {
		if (!Objects.equals(expected, actual)) {
			failNotEqual(expected, actual, nullSafeGet(messageSupplier));
		}
	}

	// --- assertNotEquals -------------------------------------------------

	public static void assertNotEquals(Object unexpected, Object actual) {
		assertNotEquals(unexpected, actual, () -> null);
	}

	public static void assertNotEquals(Object unexpected, Object actual, String message) {
		assertNotEquals(unexpected, actual, () -> message);
	}

	public static void assertNotEquals(Object unexpected, Object actual, Supplier<String> messageSupplier) {
		if (Objects.equals(unexpected, actual)) {
			failEqual(actual, nullSafeGet(messageSupplier));
		}
	}

	// --- assertSame ----------------------------------------------------

	public static void assertSame(Object expected, Object actual) {
		assertSame(expected, actual, () -> null);
	}

	public static void assertSame(Object expected, Object actual, String message) {
		assertSame(expected, actual, () -> message);
	}

	public static void assertSame(Object expected, Object actual, Supplier<String> messageSupplier) {
		if (expected != actual) {
			failNotSame(expected, actual, nullSafeGet(messageSupplier));
		}
	}

	// --- assertNotSame -------------------------------------------------

	public static void assertNotSame(Object unexpected, Object actual) {
		assertNotSame(unexpected, actual, () -> null);
	}

	public static void assertNotSame(Object unexpected, Object actual, String message) {
		assertNotSame(unexpected, actual, () -> message);
	}

	public static void assertNotSame(Object unexpected, Object actual, Supplier<String> messageSupplier) {
		if (unexpected == actual) {
			failSame(actual, nullSafeGet(messageSupplier));
		}
	}

	// --- assertAll -----------------------------------------------------

	public static void assertAll(Executable... asserts) throws MultipleFailuresError {
		assertAll(null, asserts);
	}

	public static void assertAll(String heading, Executable... asserts) throws MultipleFailuresError {
		MultipleFailuresError multipleFailuresError = new MultipleFailuresError(heading);
		for (Executable executable : asserts) {
			try {
				executable.execute();
			}
			catch (AssertionError assertionError) {
				multipleFailuresError.addFailure(assertionError);
			}
			catch (Throwable t) {
				throw ExceptionUtils.throwAsUncheckedException(t);
			}
		}
		if (multipleFailuresError.hasFailures()) {
			throw multipleFailuresError;
		}
	}

	// --- assert exceptions ---------------------------------------------

	public static void assertThrows(Class<? extends Throwable> expected, Executable executable) {
		expectThrows(expected, executable);
	}

	@SuppressWarnings("unchecked")
	public static <T extends Throwable> T expectThrows(Class<T> expectedType, Executable executable) {
		try {
			executable.execute();
		}
		catch (Throwable actualException) {
			if (expectedType.isInstance(actualException)) {
				return (T) actualException;
			}
			else {
				String message = Assertions.format(expectedType.getName(), actualException.getClass().getName(),
					"Unexpected exception type thrown");
				throw new AssertionFailedError(message, actualException);
			}
		}
		throw new AssertionFailedError(
			String.format("Expected %s to be thrown, but nothing was thrown.", expectedType.getName()));
	}

	// -------------------------------------------------------------------

	private static void failEqual(Object actual, String message) {
		fail(buildPrefix(message) + "expected: not equal but was: <" + actual + ">");
	}

	private static void failNull(String message) {
		fail(buildPrefix(message) + "expected: not <null>");
	}

	private static void failNotNull(Object actual, String message) {
		fail(buildPrefix(message) + "expected: <null> but was: <" + actual + ">");
	}

	private static void failSame(Object actual, String message) {
		fail(buildPrefix(message) + "expected: not same but was: <" + actual + ">");
	}

	private static void failNotSame(Object expected, Object actual, String message) {
		fail(format(expected, actual, message));
	}

	private static void failNotEqual(Object expected, Object actual, String message) {
		fail(format(expected, actual, message));
	}

	private static String format(Object expected, Object actual, String message) {
		String prefix = buildPrefix(message);
		String expectedString = String.valueOf(expected);
		String actualString = String.valueOf(actual);
		if (expectedString.equals(actualString)) {
			return prefix + "expected: " + formatClassAndValue(expected, expectedString) + " but was: "
					+ formatClassAndValue(actual, actualString);
		}
		else {
			return prefix + "expected: <" + expectedString + "> but was: <" + actualString + ">";
		}
	}

	private static String formatClassAndValue(Object value, String valueString) {
		String className = (value == null ? "null" : value.getClass().getName());
		String hash = (value == null ? "" : "@" + Integer.toHexString(System.identityHashCode(value)));
		return className + hash + "<" + valueString + ">";
	}

	private static String buildPrefix(String message) {
		return (StringUtils.isNotBlank(message) ? message + " ==> " : "");
	}

	private static String nullSafeGet(Supplier<String> messageSupplier) {
		return (messageSupplier != null ? messageSupplier.get() : null);
	}

}
