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

import static org.junit.gen5.commons.meta.API.Usage.Experimental;
import static org.junit.gen5.commons.meta.API.Usage.Maintained;

import java.util.Objects;
import java.util.function.BooleanSupplier;
import java.util.function.Supplier;

import org.junit.gen5.commons.meta.API;
import org.junit.gen5.commons.util.ExceptionUtils;
import org.junit.gen5.commons.util.StringUtils;
import org.opentest4j.AssertionFailedError;
import org.opentest4j.MultipleFailuresError;

/**
 * {@code Assertions} is a collection of utility methods that support
 * asserting conditions in tests. A <em>failed</em> assertion will
 * always throw {@link AssertionFailedError} or a subclass thereof.
 *
 * @since 5.0
 * @see AssertionFailedError
 * @see Assumptions
 */
@API(Maintained)
public final class Assertions {

	private Assertions() {
		/* no-op */
	}

	// --- fail ----------------------------------------------------------

	/**
	 * <em>Fails</em> a test with the given failure {@code message}.
	 */
	public static void fail(String message) {
		throw new AssertionFailedError(message);
	}

	/**
	 * <em>Fails</em> a test with the failure message retrieved from the
	 * given {@code messageSupplier}.
	 */
	public static void fail(Supplier<String> messageSupplier) {
		fail(nullSafeGet(messageSupplier));
	}

	// --- assertTrue ----------------------------------------------------

	/**
	 * <em>Asserts</em> that the supplied {@code condition} is {@code true}.
	 */
	public static void assertTrue(boolean condition) {
		assertTrue(() -> condition, () -> null);
	}

	/**
	 * <em>Asserts</em> that the supplied {@code condition} is {@code true}.
	 * <p>If necessary, the failure message will be retrieved lazily from the supplied {@code messageSupplier}.
	 */
	public static void assertTrue(boolean condition, Supplier<String> messageSupplier) {
		assertTrue(() -> condition, messageSupplier);
	}

	/**
	 * <em>Asserts</em> that the boolean condition supplied by {@code booleanSupplier} is {@code true}.
	 */
	public static void assertTrue(BooleanSupplier booleanSupplier) {
		assertTrue(booleanSupplier, () -> null);
	}

	/**
	 * <em>Asserts</em> that the boolean condition supplied by {@code booleanSupplier} is {@code true}.
	 * <p>Fails with the supplied failure {@code message}.
	 */
	public static void assertTrue(BooleanSupplier booleanSupplier, String message) {
		assertTrue(booleanSupplier, () -> message);
	}

	/**
	 * <em>Asserts</em> that the supplied {@code condition} is {@code true}.
	 * <p>Fails with the supplied failure {@code message}.
	 */
	public static void assertTrue(boolean condition, String message) {
		assertTrue(() -> condition, () -> message);
	}

	/**
	 * <em>Asserts</em> that the boolean condition supplied by {@code booleanSupplier} is {@code true}.
	 * <p>If necessary, the failure message will be retrieved lazily from the supplied {@code messageSupplier}.
	 */
	public static void assertTrue(BooleanSupplier booleanSupplier, Supplier<String> messageSupplier) {
		if (!booleanSupplier.getAsBoolean()) {
			fail(messageSupplier);
		}
	}

	// --- assertFalse ---------------------------------------------------

	/**
	 * <em>Asserts</em> that the supplied {@code condition} is not {@code true}.
	 */
	public static void assertFalse(boolean condition) {
		assertFalse(() -> condition, () -> null);
	}

	/**
	 * <em>Asserts</em> that the supplied {@code condition} is not {@code true}.
	 * <p>Fails with the supplied failure {@code message}.
	 */
	public static void assertFalse(boolean condition, String message) {
		assertFalse(() -> condition, () -> message);
	}

	/**
	 * <em>Asserts</em> that the supplied {@code condition} is not {@code true}.
	 * <p>If necessary, the failure message will be retrieved lazily from the supplied {@code messageSupplier}.
	 */
	public static void assertFalse(boolean condition, Supplier<String> messageSupplier) {
		assertFalse(() -> condition, messageSupplier);
	}

	/**
	 * <em>Asserts</em> that the boolean condition supplied by {@code booleanSupplier} is not {@code true}.
	 */
	public static void assertFalse(BooleanSupplier booleanSupplier) {
		assertFalse(booleanSupplier, () -> null);
	}

	/**
	 * <em>Asserts</em> that the boolean condition supplied by {@code booleanSupplier} is not {@code true}.
	 * <p>Fails with the supplied failure {@code message}.
	 */
	public static void assertFalse(BooleanSupplier booleanSupplier, String message) {
		assertFalse(booleanSupplier, () -> message);
	}

	/**
	 * <em>Asserts</em> that the boolean condition supplied by {@code booleanSupplier} is not {@code true}.
	 * <p>If necessary, the failure message will be retrieved lazily from the supplied {@code messageSupplier}.
	 */
	public static void assertFalse(BooleanSupplier booleanSupplier, Supplier<String> messageSupplier) {
		if (booleanSupplier.getAsBoolean()) {
			fail(messageSupplier);
		}
	}

	// --- assertNull ----------------------------------------------------

	/**
	 * <em>Asserts</em> that {@code actual} is {@code null}.
	 */
	public static void assertNull(Object actual) {
		assertNull(actual, () -> null);
	}

	/**
	 * <em>Asserts</em> that {@code actual} is {@code null}.
	 * <p>Fails with the supplied failure {@code message}.
	 */
	public static void assertNull(Object actual, String message) {
		assertNull(actual, () -> message);
	}

	/**
	 * <em>Asserts</em> that {@code actual} is {@code null}.
	 * <p>If necessary, the failure message will be retrieved lazily from the supplied {@code messageSupplier}.
	 */
	public static void assertNull(Object actual, Supplier<String> messageSupplier) {
		if (actual != null) {
			failNotNull(actual, nullSafeGet(messageSupplier));
		}
	}

	// --- assertNotNull -------------------------------------------------

	/**
	 * <em>Asserts</em> that {@code actual} is not {@code null}.
	 */
	public static void assertNotNull(Object actual) {
		assertNotNull(actual, () -> null);
	}

	/**
	 * <em>Asserts</em> that {@code actual} is not {@code null}.
	 * <p>Fails with the supplied failure {@code message}.
	 */
	public static void assertNotNull(Object actual, String message) {
		assertNotNull(actual, () -> message);
	}

	/**
	 * <em>Asserts</em> that {@code actual} is not {@code null}.
	 * <p>If necessary, the failure message will be retrieved lazily from the supplied {@code messageSupplier}.
	 */
	public static void assertNotNull(Object actual, Supplier<String> messageSupplier) {
		if (actual == null) {
			failNull(nullSafeGet(messageSupplier));
		}
	}

	// --- assertEquals -------------------------------------------------

	/**
	 * <em>Asserts</em> that {@code expected} and {@code actual} are equal.
	 */
	public static void assertEquals(short expected, short actual) {
		assertEquals(expected, actual, () -> null);
	}

	/**
	 * <em>Asserts</em> that {@code expected} and {@code actual} are equal.
	 */
	public static void assertEquals(short expected, short actual, String message) {
		assertEquals(expected, actual, () -> message);
	}

	/**
	 * <em>Asserts</em> that {@code expected} and {@code actual} are equal.
	 * <p>If necessary, the failure message will be retrieved lazily from the supplied {@code messageSupplier}.
	 */
	public static void assertEquals(short expected, short actual, Supplier<String> messageSupplier) {
		if (expected != actual) {
			failNotEqual(expected, actual, nullSafeGet(messageSupplier));
		}
	}

	/**
	 * <em>Asserts</em> that {@code expected} and {@code actual} are equal.
	 */
	public static void assertEquals(byte expected, byte actual) {
		assertEquals(expected, actual, () -> null);
	}

	/**
	 * <em>Asserts</em> that {@code expected} and {@code actual} are equal.
	 */
	public static void assertEquals(byte expected, byte actual, String message) {
		assertEquals(expected, actual, () -> message);
	}

	/**
	 * <em>Asserts</em> that {@code expected} and {@code actual} are equal.
	 * <p>If necessary, the failure message will be retrieved lazily from the supplied {@code messageSupplier}.
	 */
	public static void assertEquals(byte expected, byte actual, Supplier<String> messageSupplier) {
		if (expected != actual) {
			failNotEqual(expected, actual, nullSafeGet(messageSupplier));
		}
	}

	/**
	 * <em>Asserts</em> that {@code expected} and {@code actual} are equal.
	 */
	public static void assertEquals(int expected, int actual) {
		assertEquals(expected, actual, () -> null);
	}

	/**
	 * <em>Asserts</em> that {@code expected} and {@code actual} are equal.
	 */
	public static void assertEquals(int expected, int actual, String message) {
		assertEquals(expected, actual, () -> message);
	}

	/**
	 * <em>Asserts</em> that {@code expected} and {@code actual} are equal.
	 * <p>If necessary, the failure message will be retrieved lazily from the supplied {@code messageSupplier}.
	 */
	public static void assertEquals(int expected, int actual, Supplier<String> messageSupplier) {
		if (expected != actual) {
			failNotEqual(expected, actual, nullSafeGet(messageSupplier));
		}
	}

	/**
	 * <em>Asserts</em> that {@code expected} and {@code actual} are equal.
	 */
	public static void assertEquals(long expected, long actual) {
		assertEquals(expected, actual, () -> null);
	}

	/**
	 * <em>Asserts</em> that {@code expected} and {@code actual} are equal.
	 */
	public static void assertEquals(long expected, long actual, String message) {
		assertEquals(expected, actual, () -> message);
	}

	/**
	 * <em>Asserts</em> that {@code expected} and {@code actual} are equal.
	 * <p>If necessary, the failure message will be retrieved lazily from the supplied {@code messageSupplier}.
	 */
	public static void assertEquals(long expected, long actual, Supplier<String> messageSupplier) {
		if (expected != actual) {
			failNotEqual(expected, actual, nullSafeGet(messageSupplier));
		}
	}

	/**
	 * <em>Asserts</em> that {@code expected} and {@code actual} are equal.
	 */
	public static void assertEquals(char expected, char actual) {
		assertEquals(expected, actual, () -> null);
	}

	/**
	 * <em>Asserts</em> that {@code expected} and {@code actual} are equal.
	 */
	public static void assertEquals(char expected, char actual, String message) {
		assertEquals(expected, actual, () -> message);
	}

	/**
	 * <em>Asserts</em> that {@code expected} and {@code actual} are equal.
	 * <p>If necessary, the failure message will be retrieved lazily from the supplied {@code messageSupplier}.
	 */
	public static void assertEquals(char expected, char actual, Supplier<String> messageSupplier) {
		if (expected != actual) {
			failNotEqual(expected, actual, nullSafeGet(messageSupplier));
		}
	}

	/**
	 * <em>Asserts</em> that {@code expected} and {@code actual} are equal.
	 * <p>Equality imposed by this method is consistent with {@link Float#equals(Object)} and
	 * {@link Float#compare(float, float)}.</p>
	 */
	public static void assertEquals(float expected, float actual) {
		assertEquals(expected, actual, () -> null);
	}

	/**
	 * <em>Asserts</em> that {@code expected} and {@code actual} are equal.
	 * <p>Equality imposed by this method is consistent with {@link Float#equals(Object)} and
	 * {@link Float#compare(float, float)}.</p>
	 */
	public static void assertEquals(float expected, float actual, String message) {
		assertEquals(expected, actual, () -> message);
	}

	/**
	 * <em>Asserts</em> that {@code expected} and {@code actual} are equal.
	 * <p>Equality imposed by this method is consistent with {@link Float#equals(Object)} and
	 * {@link Float#compare(float, float)}.</p>
	 * <p>If necessary, the failure message will be retrieved lazily from the supplied {@code messageSupplier}.
	 */
	public static void assertEquals(float expected, float actual, Supplier<String> messageSupplier) {
		if (!floatsEqual(expected, actual)) {
			failNotEqual(expected, actual, nullSafeGet(messageSupplier));
		}
	}

	/**
	 * <em>Asserts</em> that {@code expected} and {@code actual} are equal within the given {@code delta}.
	 * <p>Equality imposed by this method is consistent with {@link Float#equals(Object)} and
	 * {@link Float#compare(float, float)}.</p>
	 */
	public static void assertEquals(float expected, float actual, float delta) {
		assertEquals(expected, actual, delta, () -> null);
	}

	/**
	 * <em>Asserts</em> that {@code expected} and {@code actual} are equal within the given {@code delta}.
	 * <p>Equality imposed by this method is consistent with {@link Float#equals(Object)} and
	 * {@link Float#compare(float, float)}.</p>
	 */
	public static void assertEquals(float expected, float actual, float delta, String message) {
		assertEquals(expected, actual, delta, () -> message);
	}

	/**
	 * <em>Asserts</em> that {@code expected} and {@code actual} are equal within the given {@code delta}.
	 * <p>Equality imposed by this method is consistent with {@link Float#equals(Object)} and
	 * {@link Float#compare(float, float)}.</p>
	 * <p>If necessary, the failure message will be retrieved lazily from the supplied {@code messageSupplier}.
	 */
	public static void assertEquals(float expected, float actual, float delta, Supplier<String> messageSupplier) {
		if (floatsDifferent(expected, actual, delta)) {
			failNotEqual(expected, actual, nullSafeGet(messageSupplier));
		}
	}

	/**
	 * <em>Asserts</em> that {@code expected} and {@code actual} are equal.
	 * <p>Equality imposed by this method is consistent with {@link Double#equals(Object)} and
	 * {@link Double#compare(double, double)}.</p>
	 */
	public static void assertEquals(double expected, double actual) {
		assertEquals(expected, actual, () -> null);
	}

	/**
	 * <em>Asserts</em> that {@code expected} and {@code actual} are equal.
	 * <p>Equality imposed by this method is consistent with {@link Double#equals(Object)} and
	 * {@link Double#compare(double, double)}.</p>
	 */
	public static void assertEquals(double expected, double actual, String message) {
		assertEquals(expected, actual, () -> message);
	}

	/**
	 * <em>Asserts</em> that {@code expected} and {@code actual} are equal.
	 * <p>Equality imposed by this method is consistent with {@link Double#equals(Object)} and
	 * {@link Double#compare(double, double)}.</p>
	 * <p>If necessary, the failure message will be retrieved lazily from the supplied {@code messageSupplier}.
	 */
	public static void assertEquals(double expected, double actual, Supplier<String> messageSupplier) {
		if (!doublesEqual(expected, actual)) {
			failNotEqual(expected, actual, nullSafeGet(messageSupplier));
		}
	}

	/**
	 * <em>Asserts</em> that {@code expected} and {@code actual} are equal within the given {@code delta}.
	 * <p>Equality imposed by this method is consistent with {@link Double#equals(Object)} and
	 * {@link Double#compare(double, double)}.</p>
	 */
	public static void assertEquals(double expected, double actual, double delta) {
		assertEquals(expected, actual, delta, () -> null);
	}

	/**
	 * <em>Asserts</em> that {@code expected} and {@code actual} are equal within the given {@code delta}.
	 * <p>Equality imposed by this method is consistent with {@link Double#equals(Object)} and
	 * {@link Double#compare(double, double)}.</p>
	 */
	public static void assertEquals(double expected, double actual, double delta, String message) {
		assertEquals(expected, actual, delta, () -> message);
	}

	/**
	 * <em>Asserts</em> that {@code expected} and {@code actual} are equal within the given {@code delta}.
	 * <p>Equality imposed by this method is consistent with {@link Double#equals(Object)} and
	 * {@link Double#compare(double, double)}.</p>
	 * <p>If necessary, the failure message will be retrieved lazily from the supplied {@code messageSupplier}.
	 */
	public static void assertEquals(double expected, double actual, double delta, Supplier<String> messageSupplier) {
		if (doublesDifferent(expected, actual, delta)) {
			failNotEqual(expected, actual, nullSafeGet(messageSupplier));
		}
	}

	/**
	 * <em>Asserts</em> that {@code expected} and {@code actual} are equal.
	 * <p>If both are {@code null}, they are considered equal.
	 *
	 * @see Objects#equals(Object, Object)
	 */
	public static void assertEquals(Object expected, Object actual) {
		assertEquals(expected, actual, () -> null);
	}

	/**
	 * <em>Asserts</em> that {@code expected} and {@code actual} are equal.
	 * <p>If both are {@code null}, they are considered equal.
	 * <p>Fails with the supplied failure {@code message}.
	 *
	 * @see Objects#equals(Object, Object)
	 */
	public static void assertEquals(Object expected, Object actual, String message) {
		assertEquals(expected, actual, () -> message);
	}

	/**
	 * <em>Asserts</em> that {@code expected} and {@code actual} are equal.
	 * <p>If both are {@code null}, they are considered equal.
	 * <p>If necessary, the failure message will be retrieved lazily from the supplied {@code messageSupplier}.
	 *
	 * @see Objects#equals(Object, Object)
	 */
	public static void assertEquals(Object expected, Object actual, Supplier<String> messageSupplier) {
		if (!Objects.equals(expected, actual)) {
			failNotEqual(expected, actual, nullSafeGet(messageSupplier));
		}
	}

	// --- assertNotEquals -------------------------------------------------

	/**
	 * <em>Asserts</em> that {@code expected} and {@code actual} are not equal.
	 * <p>Fails if both are {@code null}.
	 *
	 * @see Objects#equals(Object, Object)
	 */
	public static void assertNotEquals(Object unexpected, Object actual) {
		assertNotEquals(unexpected, actual, () -> null);
	}

	/**
	 * <em>Asserts</em> that {@code expected} and {@code actual} are not equal.
	 * <p>Fails if both are {@code null}.
	 * <p>Fails with the supplied failure {@code message}.
	 *
	 * @see Objects#equals(Object, Object)
	 */
	public static void assertNotEquals(Object unexpected, Object actual, String message) {
		assertNotEquals(unexpected, actual, () -> message);
	}

	/**
	 * <em>Asserts</em> that {@code expected} and {@code actual} are not equal.
	 * <p>Fails if both are {@code null}.
	 * <p>If necessary, the failure message will be retrieved lazily from the supplied {@code messageSupplier}.
	 *
	 * @see Objects#equals(Object, Object)
	 */
	public static void assertNotEquals(Object unexpected, Object actual, Supplier<String> messageSupplier) {
		if (Objects.equals(unexpected, actual)) {
			failEqual(actual, nullSafeGet(messageSupplier));
		}
	}

	// --- assertSame ----------------------------------------------------

	/**
	 * <em>Asserts</em> that {@code expected} and {@code actual} refer to the same object.
	 */
	public static void assertSame(Object expected, Object actual) {
		assertSame(expected, actual, () -> null);
	}

	/**
	 * <em>Asserts</em> that {@code expected} and {@code actual} refer to the same object.
	 * <p>Fails with the supplied failure {@code message}.
	 */
	public static void assertSame(Object expected, Object actual, String message) {
		assertSame(expected, actual, () -> message);
	}

	/**
	 * <em>Asserts</em> that {@code expected} and {@code actual} refer to the same object.
	 * <p>If necessary, the failure message will be retrieved lazily from the supplied {@code messageSupplier}.
	 */
	public static void assertSame(Object expected, Object actual, Supplier<String> messageSupplier) {
		if (expected != actual) {
			failNotSame(expected, actual, nullSafeGet(messageSupplier));
		}
	}

	// --- assertNotSame -------------------------------------------------

	/**
	 * <em>Asserts</em> that {@code expected} and {@code actual} do not refer to the same object.
	 */
	public static void assertNotSame(Object unexpected, Object actual) {
		assertNotSame(unexpected, actual, () -> null);
	}

	/**
	 * <em>Asserts</em> that {@code expected} and {@code actual} do not refer to the same object.
	 * <p>Fails with the supplied failure {@code message}.
	 */
	public static void assertNotSame(Object unexpected, Object actual, String message) {
		assertNotSame(unexpected, actual, () -> message);
	}

	/**
	 * <em>Asserts</em> that {@code expected} and {@code actual} do not refer to the same object.
	 * <p>If necessary, the failure message will be retrieved lazily from the supplied {@code messageSupplier}.
	 */
	public static void assertNotSame(Object unexpected, Object actual, Supplier<String> messageSupplier) {
		if (unexpected == actual) {
			failSame(actual, nullSafeGet(messageSupplier));
		}
	}

	// --- assertAll -----------------------------------------------------

	/**
	 * <em>Asserts</em> that <em>all</em> supplied {@code executables} do not throw an
	 * {@link AssertionError}.
	 *
	 * <p>If any supplied {@link Executable} throws an {@code AssertionError}, all remaining
	 * {@code executables} will still be executed, and all failures will be aggregated
	 * and reported in a {@link MultipleFailuresError}. However, if an {@code executable}
	 * throws an exception that is not an {@code AssertionError}, execution will halt
	 * immediately, and the exception will be rethrown <em>as is</em> but
	 * {@link ExceptionUtils#throwAsUncheckedException masked} as an unchecked exception.
	 *
	 * @see #assertAll(String, Executable...)
	 */
	@API(Experimental)
	public static void assertAll(Executable... executables) throws MultipleFailuresError {
		assertAll(null, executables);
	}

	/**
	 * <em>Asserts</em> that <em>all</em> supplied {@code executables} do not throw an
	 * {@link AssertionError}.
	 *
	 * <p>If any supplied {@link Executable} throws an {@code AssertionError}, all remaining
	 * {@code executables} will still be executed, and all failures will be aggregated
	 * and reported in a {@link MultipleFailuresError}. However, if an {@code executable}
	 * throws an exception that is not an {@code AssertionError}, execution will halt
	 * immediately, and the exception will be rethrown <em>as is</em> but
	 * {@link ExceptionUtils#throwAsUncheckedException masked} as an unchecked exception.
	 *
	 * <p>The supplied {@code heading} will be included in the message string for the
	 * {@link MultipleFailuresError}.
	 */
	@API(Experimental)
	public static void assertAll(String heading, Executable... executables) throws MultipleFailuresError {
		MultipleFailuresError multipleFailuresError = new MultipleFailuresError(heading);
		for (Executable executable : executables) {
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

	/**
	 * <em>Asserts</em> that execution of the supplied {@code executable} throws
	 * an exception of the {@code expectedType}.
	 *
	 * <p>If no exception is thrown, or if an exception of a different type is thrown,
	 * this method will fail.
	 *
	 * <p>Use {@link #assertThrows} if you do not want to perform additional checks on
	 * the exception instance. Otherwise use {@link #expectThrows}.
	 */
	public static void assertThrows(Class<? extends Throwable> expectedType, Executable executable) {
		expectThrows(expectedType, executable);
	}

	/**
	 * <em>Asserts</em> that execution of the supplied {@code executable} throws
	 * an exception of the {@code expectedType}, and returns the exception.
	 *
	 * <p>If no exception is thrown or if an exception of a different type is thrown,
	 * this method will fail.
	 *
	 * <p>Use {@link #expectThrows} if you want to perform additional checks on the exception instance.
	 * Otherwise use {@link #assertThrows}.</p>
	 */
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

	private static boolean floatsDifferent(float value1, float value2, float delta) {
		if (floatsEqual(value1, value2)) {
			return false;
		}
		if (Math.abs(value1 - value2) <= delta) {
			return false;
		}
		return true;
	}

	private static boolean doublesDifferent(double value1, double value2, double delta) {
		if (doublesEqual(value1, value2)) {
			return false;
		}
		if (Math.abs(value1 - value2) <= delta) {
			return false;
		}
		return true;
	}

	private static boolean floatsEqual(float value1, float value2) {
		return Float.floatToIntBits(value1) == Float.floatToIntBits(value2);
	}

	private static boolean doublesEqual(double value1, double value2) {
		return Double.doubleToLongBits(value1) == Double.doubleToLongBits(value2);
	}

}
