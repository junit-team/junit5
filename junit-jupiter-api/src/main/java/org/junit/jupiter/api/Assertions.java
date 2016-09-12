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
import static org.junit.platform.commons.meta.API.Usage.Experimental;
import static org.junit.platform.commons.meta.API.Usage.Maintained;
import static org.junit.platform.commons.util.ReflectionUtils.isArray;

import java.time.Duration;
import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.Deque;
import java.util.Objects;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.function.BooleanSupplier;
import java.util.function.Supplier;
import java.util.stream.Stream;

import org.junit.jupiter.api.function.Executable;
import org.junit.platform.commons.meta.API;
import org.junit.platform.commons.util.ExceptionUtils;
import org.junit.platform.commons.util.Preconditions;
import org.junit.platform.commons.util.StringUtils;
import org.opentest4j.AssertionFailedError;
import org.opentest4j.MultipleFailuresError;

/**
 * {@code Assertions} is a collection of utility methods that support asserting
 * conditions in tests.
 *
 * <p>Unless otherwise noted, a <em>failed</em> assertion will throw an
 * {@link AssertionFailedError} or a subclass thereof.
 *
 * @since 5.0
 * @see AssertionFailedError
 * @see Assumptions
 */
@API(Maintained)
public final class Assertions {

	///CLOVER:OFF
	private Assertions() {
		/* no-op */
	}
	///CLOVER:ON

	// --- fail ----------------------------------------------------------------

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

	// --- assertTrue ----------------------------------------------------------

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

	// --- assertFalse ---------------------------------------------------------

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

	// --- assertNull ----------------------------------------------------------

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

	// --- assertNotNull -------------------------------------------------------

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

	// --- assertEquals --------------------------------------------------------

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
		if (!floatsAreEqual(expected, actual)) {
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
		if (!floatsAreEqual(expected, actual, delta)) {
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
		if (!doublesAreEqual(expected, actual)) {
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
		if (!doublesAreEqual(expected, actual, delta)) {
			failNotEqual(expected, actual, nullSafeGet(messageSupplier));
		}
	}

	/**
	 * <em>Asserts</em> that {@code expected} and {@code actual} are equal.
	 * <p>If both are {@code null}, they are considered equal.
	 *
	 * @see Object#equals(Object)
	 */
	public static void assertEquals(Object expected, Object actual) {
		assertEquals(expected, actual, () -> null);
	}

	/**
	 * <em>Asserts</em> that {@code expected} and {@code actual} are equal.
	 * <p>If both are {@code null}, they are considered equal.
	 * <p>Fails with the supplied failure {@code message}.
	 *
	 * @see Object#equals(Object)
	 */
	public static void assertEquals(Object expected, Object actual, String message) {
		assertEquals(expected, actual, () -> message);
	}

	/**
	 * <em>Asserts</em> that {@code expected} and {@code actual} are equal.
	 * <p>If both are {@code null}, they are considered equal.
	 * <p>If necessary, the failure message will be retrieved lazily from the supplied {@code messageSupplier}.
	 *
	 * @see Object#equals(Object)
	 */
	public static void assertEquals(Object expected, Object actual, Supplier<String> messageSupplier) {
		if (!objectsAreEqual(expected, actual)) {
			failNotEqual(expected, actual, nullSafeGet(messageSupplier));
		}
	}

	// --- assertArrayEquals ---------------------------------------------------

	/**
	 * <em>Asserts</em> that {@code expected} and {@code actual} boolean arrays are equal.
	 * <p>If both are {@code null}, they are considered equal.
	 */
	public static void assertArrayEquals(boolean[] expected, boolean[] actual) {
		assertArrayEquals(expected, actual, () -> null);
	}

	/**
	 * <em>Asserts</em> that {@code expected} and {@code actual} boolean arrays are equal.
	 * <p>If both are {@code null}, they are considered equal.
	 * <p>Fails with the supplied failure {@code message}.
	 */
	public static void assertArrayEquals(boolean[] expected, boolean[] actual, String message) {
		assertArrayEquals(expected, actual, () -> message);
	}

	/**
	 * <em>Asserts</em> that {@code expected} and {@code actual} boolean arrays are equal.
	 * <p>If both are {@code null}, they are considered equal.
	 * <p>If necessary, the failure message will be retrieved lazily from the supplied {@code messageSupplier}.
	 */
	public static void assertArrayEquals(boolean[] expected, boolean[] actual, Supplier<String> messageSupplier) {
		assertArrayEquals(expected, actual, null, messageSupplier);
	}

	/**
	 * <em>Asserts</em> that {@code expected} and {@code actual} char arrays are equal.
	 * <p>If both are {@code null}, they are considered equal.
	 */
	public static void assertArrayEquals(char[] expected, char[] actual) {
		assertArrayEquals(expected, actual, () -> null);
	}

	/**
	 * <em>Asserts</em> that {@code expected} and {@code actual} char arrays are equal.
	 * <p>If both are {@code null}, they are considered equal.
	 * <p>Fails with the supplied failure {@code message}.
	 */
	public static void assertArrayEquals(char[] expected, char[] actual, String message) {
		assertArrayEquals(expected, actual, () -> message);
	}

	/**
	 * <em>Asserts</em> that {@code expected} and {@code actual} char arrays are equal.
	 * <p>If both are {@code null}, they are considered equal.
	 * <p>If necessary, the failure message will be retrieved lazily from the supplied {@code messageSupplier}.
	 */
	public static void assertArrayEquals(char[] expected, char[] actual, Supplier<String> messageSupplier) {
		assertArrayEquals(expected, actual, null, messageSupplier);
	}

	/**
	 * <em>Asserts</em> that {@code expected} and {@code actual} byte arrays are equal.
	 * <p>If both are {@code null}, they are considered equal.
	 */
	public static void assertArrayEquals(byte[] expected, byte[] actual) {
		assertArrayEquals(expected, actual, () -> null);
	}

	/**
	 * <em>Asserts</em> that {@code expected} and {@code actual} byte arrays are equal.
	 * <p>If both are {@code null}, they are considered equal.
	 * <p>Fails with the supplied failure {@code message}.
	 */
	public static void assertArrayEquals(byte[] expected, byte[] actual, String message) {
		assertArrayEquals(expected, actual, () -> message);
	}

	/**
	 * <em>Asserts</em> that {@code expected} and {@code actual} byte arrays are equal.
	 * <p>If both are {@code null}, they are considered equal.
	 * <p>If necessary, the failure message will be retrieved lazily from the supplied {@code messageSupplier}.
	 */
	public static void assertArrayEquals(byte[] expected, byte[] actual, Supplier<String> messageSupplier) {
		assertArrayEquals(expected, actual, null, messageSupplier);
	}

	/**
	 * <em>Asserts</em> that {@code expected} and {@code actual} short arrays are equal.
	 * <p>If both are {@code null}, they are considered equal.
	 */
	public static void assertArrayEquals(short[] expected, short[] actual) {
		assertArrayEquals(expected, actual, () -> null);
	}

	/**
	 * <em>Asserts</em> that {@code expected} and {@code actual} short arrays are equal.
	 * <p>If both are {@code null}, they are considered equal.
	 * <p>Fails with the supplied failure {@code message}.
	 */
	public static void assertArrayEquals(short[] expected, short[] actual, String message) {
		assertArrayEquals(expected, actual, () -> message);
	}

	/**
	 * <em>Asserts</em> that {@code expected} and {@code actual} short arrays are equal.
	 * <p>If both are {@code null}, they are considered equal.
	 * <p>If necessary, the failure message will be retrieved lazily from the supplied {@code messageSupplier}.
	 */
	public static void assertArrayEquals(short[] expected, short[] actual, Supplier<String> messageSupplier) {
		assertArrayEquals(expected, actual, null, messageSupplier);
	}

	/**
	 * <em>Asserts</em> that {@code expected} and {@code actual} int arrays are equal.
	 * <p>If both are {@code null}, they are considered equal.
	 */
	public static void assertArrayEquals(int[] expected, int[] actual) {
		assertArrayEquals(expected, actual, () -> null);
	}

	/**
	 * <em>Asserts</em> that {@code expected} and {@code actual} int arrays are equal.
	 * <p>If both are {@code null}, they are considered equal.
	 * <p>Fails with the supplied failure {@code message}.
	 */
	public static void assertArrayEquals(int[] expected, int[] actual, String message) {
		assertArrayEquals(expected, actual, () -> message);
	}

	/**
	 * <em>Asserts</em> that {@code expected} and {@code actual} int arrays are equal.
	 * <p>If both are {@code null}, they are considered equal.
	 * <p>If necessary, the failure message will be retrieved lazily from the supplied {@code messageSupplier}.
	 */
	public static void assertArrayEquals(int[] expected, int[] actual, Supplier<String> messageSupplier) {
		assertArrayEquals(expected, actual, null, messageSupplier);
	}

	/**
	 * <em>Asserts</em> that {@code expected} and {@code actual} long arrays are equal.
	 * <p>If both are {@code null}, they are considered equal.
	 */
	public static void assertArrayEquals(long[] expected, long[] actual) {
		assertArrayEquals(expected, actual, () -> null);
	}

	/**
	 * <em>Asserts</em> that {@code expected} and {@code actual} long arrays are equal.
	 * <p>If both are {@code null}, they are considered equal.
	 * <p>Fails with the supplied failure {@code message}.
	 */
	public static void assertArrayEquals(long[] expected, long[] actual, String message) {
		assertArrayEquals(expected, actual, () -> message);
	}

	/**
	 * <em>Asserts</em> that {@code expected} and {@code actual} long arrays are equal.
	 * <p>If both are {@code null}, they are considered equal.
	 * <p>If necessary, the failure message will be retrieved lazily from the supplied {@code messageSupplier}.
	 */
	public static void assertArrayEquals(long[] expected, long[] actual, Supplier<String> messageSupplier) {
		assertArrayEquals(expected, actual, null, messageSupplier);
	}

	/**
	 * <em>Asserts</em> that {@code expected} and {@code actual} float arrays are equal.
	 * <p>Equality imposed by this method is consistent with {@link Float#equals(Object)} and
	 * {@link Float#compare(float, float)}.</p>
	 */
	public static void assertArrayEquals(float[] expected, float[] actual) {
		assertArrayEquals(expected, actual, () -> null);
	}

	/**
	 * <em>Asserts</em> that {@code expected} and {@code actual} float arrays are equal.
	 * <p>Equality imposed by this method is consistent with {@link Float#equals(Object)} and
	 * {@link Float#compare(float, float)}.</p>
	 * <p>Fails with the supplied failure {@code message}.
	 */
	public static void assertArrayEquals(float[] expected, float[] actual, String message) {
		assertArrayEquals(expected, actual, () -> message);
	}

	/**
	 * <em>Asserts</em> that {@code expected} and {@code actual} float arrays are equal.
	 * <p>Equality imposed by this method is consistent with {@link Float#equals(Object)} and
	 * {@link Float#compare(float, float)}.</p>
	 * <p>If necessary, the failure message will be retrieved lazily from the supplied {@code messageSupplier}.
	 */
	public static void assertArrayEquals(float[] expected, float[] actual, Supplier<String> messageSupplier) {
		assertArrayEquals(expected, actual, null, messageSupplier);
	}

	/**
	 * <em>Asserts</em> that {@code expected} and {@code actual} float arrays are equal within the given {@code delta}.
	 * <p>Equality imposed by this method is consistent with {@link Float#equals(Object)} and
	 * {@link Float#compare(float, float)}.</p>
	 */
	public static void assertArrayEquals(float[] expected, float[] actual, float delta) {
		assertArrayEquals(expected, actual, delta, () -> null);
	}

	/**
	 * <em>Asserts</em> that {@code expected} and {@code actual} float arrays are equal within the given {@code delta}.
	 * <p>Equality imposed by this method is consistent with {@link Float#equals(Object)} and
	 * {@link Float#compare(float, float)}.</p>
	 * <p>Fails with the supplied failure {@code message}.
	 */
	public static void assertArrayEquals(float[] expected, float[] actual, float delta, String message) {
		assertArrayEquals(expected, actual, delta, () -> message);
	}

	/**
	 * <em>Asserts</em> that {@code expected} and {@code actual} float arrays are equal within the given {@code delta}.
	 * <p>Equality imposed by this method is consistent with {@link Float#equals(Object)} and
	 * {@link Float#compare(float, float)}.</p>
	 * <p>If necessary, the failure message will be retrieved lazily from the supplied {@code messageSupplier}.
	 */
	public static void assertArrayEquals(float[] expected, float[] actual, float delta,
			Supplier<String> messageSupplier) {
		assertArrayEquals(expected, actual, delta, null, messageSupplier);
	}

	/**
	 * <em>Asserts</em> that {@code expected} and {@code actual} double arrays are equal.
	 * <p>Equality imposed by this method is consistent with {@link Double#equals(Object)} and
	 * {@link Double#compare(double, double)}.</p>
	 */
	public static void assertArrayEquals(double[] expected, double[] actual) {
		assertArrayEquals(expected, actual, () -> null);
	}

	/**
	 * <em>Asserts</em> that {@code expected} and {@code actual} double arrays are equal.
	 * <p>Equality imposed by this method is consistent with {@link Double#equals(Object)} and
	 * {@link Double#compare(double, double)}.</p>
	 * <p>Fails with the supplied failure {@code message}.
	 */
	public static void assertArrayEquals(double[] expected, double[] actual, String message) {
		assertArrayEquals(expected, actual, () -> message);
	}

	/**
	 * <em>Asserts</em> that {@code expected} and {@code actual} double arrays are equal.
	 * <p>Equality imposed by this method is consistent with {@link Double#equals(Object)} and
	 * {@link Double#compare(double, double)}.</p>
	 * <p>If necessary, the failure message will be retrieved lazily from the supplied {@code messageSupplier}.
	 */
	public static void assertArrayEquals(double[] expected, double[] actual, Supplier<String> messageSupplier) {
		assertArrayEquals(expected, actual, null, messageSupplier);
	}

	/**
	 * <em>Asserts</em> that {@code expected} and {@code actual} double arrays are equal within the given {@code delta}.
	 * <p>Equality imposed by this method is consistent with {@link Double#equals(Object)} and
	 * {@link Double#compare(double, double)}.</p>
	 */
	public static void assertArrayEquals(double[] expected, double[] actual, double delta) {
		assertArrayEquals(expected, actual, delta, () -> null);
	}

	/**
	 * <em>Asserts</em> that {@code expected} and {@code actual} double arrays are equal within the given {@code delta}.
	 * <p>Equality imposed by this method is consistent with {@link Double#equals(Object)} and
	 * {@link Double#compare(double, double)}.</p>
	 * <p>Fails with the supplied failure {@code message}.
	 */
	public static void assertArrayEquals(double[] expected, double[] actual, double delta, String message) {
		assertArrayEquals(expected, actual, delta, () -> message);
	}

	/**
	 * <em>Asserts</em> that {@code expected} and {@code actual} double arrays are equal within the given {@code delta}.
	 * <p>Equality imposed by this method is consistent with {@link Double#equals(Object)} and
	 * {@link Double#compare(double, double)}.</p>
	 * <p>If necessary, the failure message will be retrieved lazily from the supplied {@code messageSupplier}.
	 */
	public static void assertArrayEquals(double[] expected, double[] actual, double delta,
			Supplier<String> messageSupplier) {
		assertArrayEquals(expected, actual, delta, null, messageSupplier);
	}

	/**
	 * <em>Asserts</em> that {@code expected} and {@code actual} object arrays are deeply equal.
	 * <p>If both are {@code null}, they are considered equal.
	 * <p>Nested float arrays are checked as in {@link #assertEquals(float, float)}.
	 * <p>Nested double arrays are checked as in {@link #assertEquals(double, double)}.
	 *
	 * @see Objects#equals(Object, Object)
	 * @see Arrays#deepEquals(Object[], Object[])
	 */
	public static void assertArrayEquals(Object[] expected, Object[] actual) {
		assertArrayEquals(expected, actual, () -> null);
	}

	/**
	 * <em>Asserts</em> that {@code expected} and {@code actual} object arrays are deeply equal.
	 * <p>If both are {@code null}, they are considered equal.
	 * <p>Nested float arrays are checked as in {@link #assertEquals(float, float)}.
	 * <p>Nested double arrays are checked as in {@link #assertEquals(double, double)}.
	 * <p>Fails with the supplied failure {@code message}.
	 *
	 * @see Objects#equals(Object, Object)
	 * @see Arrays#deepEquals(Object[], Object[])
	 */
	public static void assertArrayEquals(Object[] expected, Object[] actual, String message) {
		assertArrayEquals(expected, actual, () -> message);
	}

	/**
	 * <em>Asserts</em> that {@code expected} and {@code actual} object arrays are deeply equal.
	 * <p>If both are {@code null}, they are considered equal.
	 * <p>Nested float arrays are checked as in {@link #assertEquals(float, float)}.
	 * <p>Nested double arrays are checked as in {@link #assertEquals(double, double)}.
	 * <p>If necessary, the failure message will be retrieved lazily from the supplied {@code messageSupplier}.
	 *
	 * @see Objects#equals(Object, Object)
	 * @see Arrays#deepEquals(Object[], Object[])
	 */
	public static void assertArrayEquals(Object[] expected, Object[] actual, Supplier<String> messageSupplier) {
		assertArrayEquals(expected, actual, new ArrayDeque<>(), messageSupplier);
	}

	// --- assertNotEquals -----------------------------------------------------

	/**
	 * <em>Asserts</em> that {@code expected} and {@code actual} are not equal.
	 * <p>Fails if both are {@code null}.
	 *
	 * @see Object#equals(Object)
	 */
	public static void assertNotEquals(Object unexpected, Object actual) {
		assertNotEquals(unexpected, actual, () -> null);
	}

	/**
	 * <em>Asserts</em> that {@code expected} and {@code actual} are not equal.
	 * <p>Fails if both are {@code null}.
	 * <p>Fails with the supplied failure {@code message}.
	 *
	 * @see Object#equals(Object)
	 */
	public static void assertNotEquals(Object unexpected, Object actual, String message) {
		assertNotEquals(unexpected, actual, () -> message);
	}

	/**
	 * <em>Asserts</em> that {@code expected} and {@code actual} are not equal.
	 * <p>Fails if both are {@code null}.
	 * <p>If necessary, the failure message will be retrieved lazily from the supplied {@code messageSupplier}.
	 *
	 * @see Object#equals(Object)
	 */
	public static void assertNotEquals(Object unexpected, Object actual, Supplier<String> messageSupplier) {
		if (objectsAreEqual(unexpected, actual)) {
			failEqual(actual, nullSafeGet(messageSupplier));
		}
	}

	// --- assertSame ----------------------------------------------------------

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

	// --- assertNotSame -------------------------------------------------------

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

	// --- assertAll -----------------------------------------------------------

	/**
	 * <em>Asserts</em> that <em>all</em> supplied {@code executables} do not throw an
	 * {@link AssertionError}.
	 *
	 * <p>See Javadoc for {@link #assertAll(String, Stream)} for an explanation of this
	 * method's exception handling semantics.
	 *
	 * @see #assertAll(String, Executable...)
	 * @see #assertAll(Stream)
	 * @see #assertAll(String, Stream)
	 */
	@API(Experimental)
	public static void assertAll(Executable... executables) throws MultipleFailuresError {
		assertAll(null, executables);
	}

	/**
	 * <em>Asserts</em> that <em>all</em> supplied {@code executables} do not throw an
	 * {@link AssertionError}.
	 *
	 * <p>See Javadoc for {@link #assertAll(String, Stream)} for an explanation of this
	 * method's exception handling semantics.
	 *
	 * @see #assertAll(Executable...)
	 * @see #assertAll(String, Executable...)
	 * @see #assertAll(String, Stream)
	 */
	@API(Experimental)
	public static void assertAll(Stream<Executable> executables) throws MultipleFailuresError {
		assertAll(null, executables);
	}

	/**
	 * <em>Asserts</em> that <em>all</em> supplied {@code executables} do not throw an
	 * {@link AssertionError}.
	 *
	 * <p>See Javadoc for {@link #assertAll(String, Stream)} for an explanation of this
	 * method's exception handling semantics.
	 *
	 * @see #assertAll(Executable...)
	 * @see #assertAll(Stream)
	 * @see #assertAll(String, Stream)
	 */
	@API(Experimental)
	public static void assertAll(String heading, Executable... executables) throws MultipleFailuresError {
		Preconditions.notNull(executables, "executables must not be null");
		assertAll(heading, Arrays.stream(executables));
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
	 *
	 * @see #assertAll(Executable...)
	 * @see #assertAll(String, Executable...)
	 * @see #assertAll(Stream)
	 */
	@API(Experimental)
	public static void assertAll(String heading, Stream<Executable> executables) throws MultipleFailuresError {
		Preconditions.notNull(executables, "executables must not be null");
		MultipleFailuresError multipleFailuresError = new MultipleFailuresError(heading);

		executables.forEach(executable -> {
			try {
				executable.execute();
			}
			catch (AssertionError assertionError) {
				multipleFailuresError.addFailure(assertionError);
			}
			catch (Throwable t) {
				ExceptionUtils.throwAsUncheckedException(t);
			}
		});

		if (multipleFailuresError.hasFailures()) {
			throw multipleFailuresError;
		}
	}

	// --- assert exceptions ---------------------------------------------------

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

	// --- assertTimeout -------------------------------------------------------

	/**
	 * <em>Asserts</em> that execution of the supplied {@code executable}
	 * completes before the given {@code timeout} is exceeded.
	 *
	 * <p>Note: the executable will be executed in the same thread as that
	 * of the calling code. Consequently, execution of the executable will
	 * not be preemptively aborted if the timeout is exceeded.
	 *
	 * @see #assertTimeout(Duration, Executable, String)
	 * @see #assertTimeout(Duration, Executable, Supplier)
	 * @see #assertTimeoutPreemptively(Duration, Executable)
	 */
	public static void assertTimeout(Duration timeout, Executable executable) {
		assertTimeout(timeout, executable, () -> null);
	}

	/**
	 * <em>Asserts</em> that execution of the supplied {@code executable}
	 * completes before the given {@code timeout} is exceeded.
	 *
	 * <p>Note: the executable will be executed in the same thread as that
	 * of the calling code. Consequently, execution of the executable will
	 * not be preemptively aborted if the timeout is exceeded.
	 *
	 * <p>Fails with the supplied failure {@code message}.
	 *
	 * @see #assertTimeout(Duration, Executable)
	 * @see #assertTimeout(Duration, Executable, Supplier)
	 * @see #assertTimeoutPreemptively(Duration, Executable, String)
	 */
	public static void assertTimeout(Duration timeout, Executable executable, String message) {
		assertTimeout(timeout, executable, () -> message);
	}

	/**
	 * <em>Asserts</em> that execution of the supplied {@code executable}
	 * completes before the given {@code timeout} is exceeded.
	 *
	 * <p>Note: the executable will be executed in the same thread as that
	 * of the calling code. Consequently, execution of the executable will
	 * not be preemptively aborted if the timeout is exceeded.
	 *
	 * <p>If necessary, the failure message will be retrieved lazily from the
	 * supplied {@code messageSupplier}.
	 *
	 * @see #assertTimeout(Duration, Executable)
	 * @see #assertTimeout(Duration, Executable, String)
	 * @see #assertTimeoutPreemptively(Duration, Executable, Supplier)
	 */
	public static void assertTimeout(Duration timeout, Executable executable, Supplier<String> messageSupplier) {
		long timeoutInMillis = timeout.toMillis();
		long start = System.currentTimeMillis();
		try {
			executable.execute();
		}
		catch (Throwable ex) {
			ExceptionUtils.throwAsUncheckedException(ex);
		}

		long timeElapsed = System.currentTimeMillis() - start;
		if (timeElapsed > timeoutInMillis) {
			fail(buildPrefix(nullSafeGet(messageSupplier)) + "execution exceeded timeout of " + timeoutInMillis
					+ " ms by " + (timeElapsed - timeoutInMillis) + " ms");
		}
	}

	/**
	 * <em>Asserts</em> that execution of the supplied {@code executable}
	 * completes before the given {@code timeout} is exceeded.
	 *
	 * <p>Note: the executable will be executed in a different thread than
	 * that of the calling code. Furthermore, execution of the executable will
	 * be preemptively aborted if the timeout is exceeded.
	 *
	 * @see #assertTimeoutPreemptively(Duration, Executable, String)
	 * @see #assertTimeoutPreemptively(Duration, Executable, Supplier)
	 * @see #assertTimeout(Duration, Executable)
	 */
	public static void assertTimeoutPreemptively(Duration timeout, Executable executable) {
		assertTimeoutPreemptively(timeout, executable, () -> null);
	}

	/**
	 * <em>Asserts</em> that execution of the supplied {@code executable}
	 * completes before the given {@code timeout} is exceeded.
	 *
	 * <p>Note: the executable will be executed in a different thread than
	 * that of the calling code. Furthermore, execution of the executable will
	 * be preemptively aborted if the timeout is exceeded.
	 *
	 * <p>Fails with the supplied failure {@code message}.
	 *
	 * @see #assertTimeoutPreemptively(Duration, Executable)
	 * @see #assertTimeoutPreemptively(Duration, Executable, Supplier)
	 * @see #assertTimeout(Duration, Executable, String)
	 */
	public static void assertTimeoutPreemptively(Duration timeout, Executable executable, String message) {
		assertTimeoutPreemptively(timeout, executable, () -> message);
	}

	/**
	 * <em>Asserts</em> that execution of the supplied {@code executable}
	 * completes before the given {@code timeout} is exceeded.
	 *
	 * <p>Note: the executable will be executed in a different thread than
	 * that of the calling code. Furthermore, execution of the executable will
	 * be preemptively aborted if the timeout is exceeded.
	 *
	 * <p>If necessary, the failure message will be retrieved lazily from the
	 * supplied {@code messageSupplier}.
	 *
	 * @see #assertTimeoutPreemptively(Duration, Executable)
	 * @see #assertTimeoutPreemptively(Duration, Executable, String)
	 * @see #assertTimeout(Duration, Executable, Supplier)
	 */
	public static void assertTimeoutPreemptively(Duration timeout, Executable executable,
			Supplier<String> messageSupplier) {

		ExecutorService executorService = Executors.newSingleThreadExecutor();
		try {
			Future<Throwable> future = executorService.submit(() -> {
				try {
					executable.execute();
				}
				catch (Throwable ex) {
					return ex;
				}
				return null;
			});

			long timeoutInMillis = timeout.toMillis();
			Throwable throwable = null;

			try {
				throwable = future.get(timeoutInMillis, TimeUnit.MILLISECONDS);
			}
			catch (TimeoutException ex) {
				fail(
					buildPrefix(nullSafeGet(messageSupplier)) + "execution timed out after " + timeoutInMillis + " ms");
			}
			catch (ExecutionException ex) {
				throwable = ex.getCause();
			}
			catch (Throwable ex) {
				throwable = ex;
			}

			if (throwable != null) {
				ExceptionUtils.throwAsUncheckedException(throwable);
			}
		}
		finally {
			executorService.shutdownNow();
		}
	}

	// --- assertArrayEquals helpers -------------------------------------------

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
			if (!floatsAreEqual(expected[i], actual[i])) {
				failArraysNotEqual(expected[i], actual[i], nullSafeIndexes(indexes, i), messageSupplier);
			}
		}
	}

	private static void assertArrayEquals(float[] expected, float[] actual, float delta, Deque<Integer> indexes,
			Supplier<String> messageSupplier) {

		assertValidDelta(delta);
		if (expected == actual) {
			return;
		}
		assertArraysNotNull(expected, actual, indexes, messageSupplier);
		assertArraysHaveSameLength(expected.length, actual.length, indexes, messageSupplier);

		for (int i = 0; i < expected.length; i++) {
			if (!floatsAreEqual(expected[i], actual[i], delta)) {
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
			if (!doublesAreEqual(expected[i], actual[i])) {
				failArraysNotEqual(expected[i], actual[i], nullSafeIndexes(indexes, i), messageSupplier);
			}
		}
	}

	private static void assertArrayEquals(double[] expected, double[] actual, double delta, Deque<Integer> indexes,
			Supplier<String> messageSupplier) {

		assertValidDelta(delta);
		if (expected == actual) {
			return;
		}
		assertArraysNotNull(expected, actual, indexes, messageSupplier);
		assertArraysHaveSameLength(expected.length, actual.length, indexes, messageSupplier);

		for (int i = 0; i < expected.length; i++) {
			if (!doublesAreEqual(expected[i], actual[i], delta)) {
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

	private static String formatIndexes(Deque<Integer> indexes) {
		if (indexes == null || indexes.isEmpty()) {
			return "";
		}
		String indexesString = indexes.stream().map(Object::toString).collect(joining("][", "[", "]"));
		return " at index " + indexesString;
	}

	// -------------------------------------------------------------------------

	private static void failEqual(Object actual, String message) {
		fail(buildPrefix(message) + "expected: not equal but was: <" + actual + ">");
	}

	private static void failNull(String message) {
		fail(buildPrefix(message) + "expected: not <null>");
	}

	private static void failNotNull(Object actual, String message) {
		fail(buildPrefix(message) + "expected: <null> but was: <" + actual + ">", null, actual);
	}

	private static void failSame(Object actual, String message) {
		fail(buildPrefix(message) + "expected: not same but was: <" + actual + ">");
	}

	private static void failNotSame(Object expected, Object actual, String message) {
		fail(format(expected, actual, message), expected, actual);
	}

	private static void failNotEqual(Object expected, Object actual, String message) {
		fail(format(expected, actual, message), expected, actual);
	}

	private static void fail(String message, Object expected, Object actual) {
		throw new AssertionFailedError(message, expected, actual);
	}

	private static String format(Object expected, Object actual, String message) {
		return buildPrefix(message) + formatValues(expected, actual);
	}

	private static String formatValues(Object expected, Object actual) {
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

	private static boolean objectsAreEqual(Object obj1, Object obj2) {
		if (obj1 == null) {
			return (obj2 == null);
		}
		else {
			return obj1.equals(obj2);
		}
	}

	private static boolean floatsAreEqual(float value1, float value2) {
		return Float.floatToIntBits(value1) == Float.floatToIntBits(value2);
	}

	private static boolean floatsAreEqual(float value1, float value2, float delta) {
		assertValidDelta(delta);
		return floatsAreEqual(value1, value2) || Math.abs(value1 - value2) <= delta;
	}

	private static void assertValidDelta(float delta) {
		if (Float.isNaN(delta) || delta <= 0.0) {
			failIllegalDelta(String.valueOf(delta));
		}
	}

	private static boolean doublesAreEqual(double value1, double value2) {
		return Double.doubleToLongBits(value1) == Double.doubleToLongBits(value2);
	}

	private static boolean doublesAreEqual(double value1, double value2, double delta) {
		assertValidDelta(delta);
		return doublesAreEqual(value1, value2) || Math.abs(value1 - value2) <= delta;
	}

	private static void assertValidDelta(double delta) {
		if (Double.isNaN(delta) || delta <= 0.0) {
			failIllegalDelta(String.valueOf(delta));
		}
	}

	private static void failIllegalDelta(String delta) {
		fail("positive delta expected but was: <" + delta + ">");
	}

}
