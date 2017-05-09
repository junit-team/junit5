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

import static org.junit.platform.commons.meta.API.Usage.Experimental;
import static org.junit.platform.commons.meta.API.Usage.Maintained;

import java.time.Duration;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.function.BooleanSupplier;
import java.util.function.Supplier;
import java.util.stream.Stream;

import org.junit.jupiter.api.function.Executable;
import org.junit.jupiter.api.function.ThrowingSupplier;
import org.junit.platform.commons.meta.API;
import org.opentest4j.MultipleFailuresError;

/**
 * {@code Assertions} is a collection of utility methods that support asserting
 * conditions in tests.
 *
 * <p>Unless otherwise noted, a <em>failed</em> assertion will throw an
 * {@link org.opentest4j.AssertionFailedError} or a subclass thereof.
 *
 * @since 5.0
 * @see org.opentest4j.AssertionFailedError
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
	 *
	 * <p>The generic return type {@code V} allows this method to be used directly
	 * as an expression value. As an {@link org.opentest4j.AssertionFailedError}
	 * is created and thrown before the final return statement, this method never
	 * returns a value to its caller.
	 */
	public static <V> V fail(String message) {
		AssertionUtils.fail(message);
		return null; // appeasing the compiler: this line will never be executed.
	}

	/**
	 * <em>Fails</em> a test with the given failure {@code message} as well
	 * as the underlying {@code cause}.
	 *
	 * <p>The generic return type {@code V} allows this method to be used directly
	 * as an expression value. As an {@link org.opentest4j.AssertionFailedError}
	 * is created and thrown before the final return statement, this method never
	 * returns a value to its caller.
	 */
	public static <V> V fail(String message, Throwable cause) {
		AssertionUtils.fail(message, cause);
		return null; // appeasing the compiler: this line will never be executed.
	}

	/**
	 * <em>Fails</em> a test with the given underlying {@code cause}.
	 *
	 * <p>The generic return type {@code V} allows this method to be used directly
	 * as an expression value. As an {@link org.opentest4j.AssertionFailedError}
	 * is created and thrown before the final return statement, this method never
	 * returns a value to its caller.
	 */
	public static <V> V fail(Throwable cause) {
		AssertionUtils.fail(cause);
		return null; // appeasing the compiler: this line will never be executed.
	}

	/**
	 * <em>Fails</em> a test with the failure message retrieved from the
	 * given {@code messageSupplier}.
	 *
	 * <p>The generic return type {@code V} allows this method to be used directly
	 * as an expression value. As an {@link org.opentest4j.AssertionFailedError}
	 * is created and thrown before the final return statement, this method never
	 * returns a value to its caller.
	 */
	public static <V> V fail(Supplier<String> messageSupplier) {
		AssertionUtils.fail(messageSupplier);
		return null; // appeasing the compiler: this line will never be executed.
	}

	// --- assertTrue ----------------------------------------------------------

	/**
	 * <em>Asserts</em> that the supplied {@code condition} is {@code true}.
	 */
	public static void assertTrue(boolean condition) {
		AssertTrue.assertTrue(condition);
	}

	/**
	 * <em>Asserts</em> that the supplied {@code condition} is {@code true}.
	 * <p>If necessary, the failure message will be retrieved lazily from the supplied {@code messageSupplier}.
	 */
	public static void assertTrue(boolean condition, Supplier<String> messageSupplier) {
		AssertTrue.assertTrue(condition, messageSupplier);
	}

	/**
	 * <em>Asserts</em> that the boolean condition supplied by {@code booleanSupplier} is {@code true}.
	 */
	public static void assertTrue(BooleanSupplier booleanSupplier) {
		AssertTrue.assertTrue(booleanSupplier);
	}

	/**
	 * <em>Asserts</em> that the boolean condition supplied by {@code booleanSupplier} is {@code true}.
	 * <p>Fails with the supplied failure {@code message}.
	 */
	public static void assertTrue(BooleanSupplier booleanSupplier, String message) {
		AssertTrue.assertTrue(booleanSupplier, message);
	}

	/**
	 * <em>Asserts</em> that the supplied {@code condition} is {@code true}.
	 * <p>Fails with the supplied failure {@code message}.
	 */
	public static void assertTrue(boolean condition, String message) {
		AssertTrue.assertTrue(condition, message);
	}

	/**
	 * <em>Asserts</em> that the boolean condition supplied by {@code booleanSupplier} is {@code true}.
	 * <p>If necessary, the failure message will be retrieved lazily from the supplied {@code messageSupplier}.
	 */
	public static void assertTrue(BooleanSupplier booleanSupplier, Supplier<String> messageSupplier) {
		AssertTrue.assertTrue(booleanSupplier, messageSupplier);
	}

	// --- assertFalse ---------------------------------------------------------

	/**
	 * <em>Asserts</em> that the supplied {@code condition} is not {@code true}.
	 */
	public static void assertFalse(boolean condition) {
		AssertFalse.assertFalse(condition);
	}

	/**
	 * <em>Asserts</em> that the supplied {@code condition} is not {@code true}.
	 * <p>Fails with the supplied failure {@code message}.
	 */
	public static void assertFalse(boolean condition, String message) {
		AssertFalse.assertFalse(condition, message);
	}

	/**
	 * <em>Asserts</em> that the supplied {@code condition} is not {@code true}.
	 * <p>If necessary, the failure message will be retrieved lazily from the supplied {@code messageSupplier}.
	 */
	public static void assertFalse(boolean condition, Supplier<String> messageSupplier) {
		AssertFalse.assertFalse(condition, messageSupplier);
	}

	/**
	 * <em>Asserts</em> that the boolean condition supplied by {@code booleanSupplier} is not {@code true}.
	 */
	public static void assertFalse(BooleanSupplier booleanSupplier) {
		AssertFalse.assertFalse(booleanSupplier);
	}

	/**
	 * <em>Asserts</em> that the boolean condition supplied by {@code booleanSupplier} is not {@code true}.
	 * <p>Fails with the supplied failure {@code message}.
	 */
	public static void assertFalse(BooleanSupplier booleanSupplier, String message) {
		AssertFalse.assertFalse(booleanSupplier, message);
	}

	/**
	 * <em>Asserts</em> that the boolean condition supplied by {@code booleanSupplier} is not {@code true}.
	 * <p>If necessary, the failure message will be retrieved lazily from the supplied {@code messageSupplier}.
	 */
	public static void assertFalse(BooleanSupplier booleanSupplier, Supplier<String> messageSupplier) {
		AssertFalse.assertFalse(booleanSupplier, messageSupplier);
	}

	// --- assertNull ----------------------------------------------------------

	/**
	 * <em>Asserts</em> that {@code actual} is {@code null}.
	 */
	public static void assertNull(Object actual) {
		AssertNull.assertNull(actual);
	}

	/**
	 * <em>Asserts</em> that {@code actual} is {@code null}.
	 * <p>Fails with the supplied failure {@code message}.
	 */
	public static void assertNull(Object actual, String message) {
		AssertNull.assertNull(actual, message);
	}

	/**
	 * <em>Asserts</em> that {@code actual} is {@code null}.
	 * <p>If necessary, the failure message will be retrieved lazily from the supplied {@code messageSupplier}.
	 */
	public static void assertNull(Object actual, Supplier<String> messageSupplier) {
		AssertNull.assertNull(actual, messageSupplier);
	}

	// --- assertNotNull -------------------------------------------------------

	/**
	 * <em>Asserts</em> that {@code actual} is not {@code null}.
	 */
	public static void assertNotNull(Object actual) {
		AssertNotNull.assertNotNull(actual);
	}

	/**
	 * <em>Asserts</em> that {@code actual} is not {@code null}.
	 * <p>Fails with the supplied failure {@code message}.
	 */
	public static void assertNotNull(Object actual, String message) {
		AssertNotNull.assertNotNull(actual, message);
	}

	/**
	 * <em>Asserts</em> that {@code actual} is not {@code null}.
	 * <p>If necessary, the failure message will be retrieved lazily from the supplied {@code messageSupplier}.
	 */
	public static void assertNotNull(Object actual, Supplier<String> messageSupplier) {
		AssertNotNull.assertNotNull(actual, messageSupplier);
	}

	// --- assertEquals --------------------------------------------------------

	/**
	 * <em>Asserts</em> that {@code expected} and {@code actual} are equal.
	 */
	public static void assertEquals(short expected, short actual) {
		AssertEquals.assertEquals(expected, actual);
	}

	/**
	 * <em>Asserts</em> that {@code expected} and {@code actual} are equal.
	 */
	public static void assertEquals(short expected, short actual, String message) {
		AssertEquals.assertEquals(expected, actual, message);
	}

	/**
	 * <em>Asserts</em> that {@code expected} and {@code actual} are equal.
	 * <p>If necessary, the failure message will be retrieved lazily from the supplied {@code messageSupplier}.
	 */
	public static void assertEquals(short expected, short actual, Supplier<String> messageSupplier) {
		AssertEquals.assertEquals(expected, actual, messageSupplier);
	}

	/**
	 * <em>Asserts</em> that {@code expected} and {@code actual} are equal.
	 */
	public static void assertEquals(byte expected, byte actual) {
		AssertEquals.assertEquals(expected, actual);
	}

	/**
	 * <em>Asserts</em> that {@code expected} and {@code actual} are equal.
	 */
	public static void assertEquals(byte expected, byte actual, String message) {
		AssertEquals.assertEquals(expected, actual, message);
	}

	/**
	 * <em>Asserts</em> that {@code expected} and {@code actual} are equal.
	 * <p>If necessary, the failure message will be retrieved lazily from the supplied {@code messageSupplier}.
	 */
	public static void assertEquals(byte expected, byte actual, Supplier<String> messageSupplier) {
		AssertEquals.assertEquals(expected, actual, messageSupplier);
	}

	/**
	 * <em>Asserts</em> that {@code expected} and {@code actual} are equal.
	 */
	public static void assertEquals(int expected, int actual) {
		AssertEquals.assertEquals(expected, actual);
	}

	/**
	 * <em>Asserts</em> that {@code expected} and {@code actual} are equal.
	 */
	public static void assertEquals(int expected, int actual, String message) {
		AssertEquals.assertEquals(expected, actual, message);
	}

	/**
	 * <em>Asserts</em> that {@code expected} and {@code actual} are equal.
	 * <p>If necessary, the failure message will be retrieved lazily from the supplied {@code messageSupplier}.
	 */
	public static void assertEquals(int expected, int actual, Supplier<String> messageSupplier) {
		AssertEquals.assertEquals(expected, actual, messageSupplier);
	}

	/**
	 * <em>Asserts</em> that {@code expected} and {@code actual} are equal.
	 */
	public static void assertEquals(long expected, long actual) {
		AssertEquals.assertEquals(expected, actual);
	}

	/**
	 * <em>Asserts</em> that {@code expected} and {@code actual} are equal.
	 */
	public static void assertEquals(long expected, long actual, String message) {
		AssertEquals.assertEquals(expected, actual, message);
	}

	/**
	 * <em>Asserts</em> that {@code expected} and {@code actual} are equal.
	 * <p>If necessary, the failure message will be retrieved lazily from the supplied {@code messageSupplier}.
	 */
	public static void assertEquals(long expected, long actual, Supplier<String> messageSupplier) {
		AssertEquals.assertEquals(expected, actual, messageSupplier);
	}

	/**
	 * <em>Asserts</em> that {@code expected} and {@code actual} are equal.
	 */
	public static void assertEquals(char expected, char actual) {
		AssertEquals.assertEquals(expected, actual);
	}

	/**
	 * <em>Asserts</em> that {@code expected} and {@code actual} are equal.
	 */
	public static void assertEquals(char expected, char actual, String message) {
		AssertEquals.assertEquals(expected, actual, message);
	}

	/**
	 * <em>Asserts</em> that {@code expected} and {@code actual} are equal.
	 * <p>If necessary, the failure message will be retrieved lazily from the supplied {@code messageSupplier}.
	 */
	public static void assertEquals(char expected, char actual, Supplier<String> messageSupplier) {
		AssertEquals.assertEquals(expected, actual, messageSupplier);
	}

	/**
	 * <em>Asserts</em> that {@code expected} and {@code actual} are equal.
	 * <p>Equality imposed by this method is consistent with {@link Float#equals(Object)} and
	 * {@link Float#compare(float, float)}.</p>
	 */
	public static void assertEquals(float expected, float actual) {
		AssertEquals.assertEquals(expected, actual);
	}

	/**
	 * <em>Asserts</em> that {@code expected} and {@code actual} are equal.
	 * <p>Equality imposed by this method is consistent with {@link Float#equals(Object)} and
	 * {@link Float#compare(float, float)}.</p>
	 */
	public static void assertEquals(float expected, float actual, String message) {
		AssertEquals.assertEquals(expected, actual, message);
	}

	/**
	 * <em>Asserts</em> that {@code expected} and {@code actual} are equal.
	 * <p>Equality imposed by this method is consistent with {@link Float#equals(Object)} and
	 * {@link Float#compare(float, float)}.</p>
	 * <p>If necessary, the failure message will be retrieved lazily from the supplied {@code messageSupplier}.
	 */
	public static void assertEquals(float expected, float actual, Supplier<String> messageSupplier) {
		AssertEquals.assertEquals(expected, actual, messageSupplier);
	}

	/**
	 * <em>Asserts</em> that {@code expected} and {@code actual} are equal within the given {@code delta}.
	 * <p>Equality imposed by this method is consistent with {@link Float#equals(Object)} and
	 * {@link Float#compare(float, float)}.</p>
	 */
	public static void assertEquals(float expected, float actual, float delta) {
		AssertEquals.assertEquals(expected, actual, delta);
	}

	/**
	 * <em>Asserts</em> that {@code expected} and {@code actual} are equal within the given {@code delta}.
	 * <p>Equality imposed by this method is consistent with {@link Float#equals(Object)} and
	 * {@link Float#compare(float, float)}.</p>
	 */
	public static void assertEquals(float expected, float actual, float delta, String message) {
		AssertEquals.assertEquals(expected, actual, delta, message);
	}

	/**
	 * <em>Asserts</em> that {@code expected} and {@code actual} are equal within the given {@code delta}.
	 * <p>Equality imposed by this method is consistent with {@link Float#equals(Object)} and
	 * {@link Float#compare(float, float)}.</p>
	 * <p>If necessary, the failure message will be retrieved lazily from the supplied {@code messageSupplier}.
	 */
	public static void assertEquals(float expected, float actual, float delta, Supplier<String> messageSupplier) {
		AssertEquals.assertEquals(expected, actual, delta, messageSupplier);
	}

	/**
	 * <em>Asserts</em> that {@code expected} and {@code actual} are equal.
	 * <p>Equality imposed by this method is consistent with {@link Double#equals(Object)} and
	 * {@link Double#compare(double, double)}.</p>
	 */
	public static void assertEquals(double expected, double actual) {
		AssertEquals.assertEquals(expected, actual);
	}

	/**
	 * <em>Asserts</em> that {@code expected} and {@code actual} are equal.
	 * <p>Equality imposed by this method is consistent with {@link Double#equals(Object)} and
	 * {@link Double#compare(double, double)}.</p>
	 */
	public static void assertEquals(double expected, double actual, String message) {
		AssertEquals.assertEquals(expected, actual, message);
	}

	/**
	 * <em>Asserts</em> that {@code expected} and {@code actual} are equal.
	 * <p>Equality imposed by this method is consistent with {@link Double#equals(Object)} and
	 * {@link Double#compare(double, double)}.</p>
	 * <p>If necessary, the failure message will be retrieved lazily from the supplied {@code messageSupplier}.
	 */
	public static void assertEquals(double expected, double actual, Supplier<String> messageSupplier) {
		AssertEquals.assertEquals(expected, actual, messageSupplier);
	}

	/**
	 * <em>Asserts</em> that {@code expected} and {@code actual} are equal within the given {@code delta}.
	 * <p>Equality imposed by this method is consistent with {@link Double#equals(Object)} and
	 * {@link Double#compare(double, double)}.</p>
	 */
	public static void assertEquals(double expected, double actual, double delta) {
		AssertEquals.assertEquals(expected, actual, delta);
	}

	/**
	 * <em>Asserts</em> that {@code expected} and {@code actual} are equal within the given {@code delta}.
	 * <p>Equality imposed by this method is consistent with {@link Double#equals(Object)} and
	 * {@link Double#compare(double, double)}.</p>
	 */
	public static void assertEquals(double expected, double actual, double delta, String message) {
		AssertEquals.assertEquals(expected, actual, delta, message);
	}

	/**
	 * <em>Asserts</em> that {@code expected} and {@code actual} are equal within the given {@code delta}.
	 * <p>Equality imposed by this method is consistent with {@link Double#equals(Object)} and
	 * {@link Double#compare(double, double)}.</p>
	 * <p>If necessary, the failure message will be retrieved lazily from the supplied {@code messageSupplier}.
	 */
	public static void assertEquals(double expected, double actual, double delta, Supplier<String> messageSupplier) {
		AssertEquals.assertEquals(expected, actual, delta, messageSupplier);
	}

	/**
	 * <em>Asserts</em> that {@code expected} and {@code actual} are equal.
	 * <p>If both are {@code null}, they are considered equal.
	 *
	 * @see Object#equals(Object)
	 */
	public static void assertEquals(Object expected, Object actual) {
		AssertEquals.assertEquals(expected, actual);
	}

	/**
	 * <em>Asserts</em> that {@code expected} and {@code actual} are equal.
	 * <p>If both are {@code null}, they are considered equal.
	 * <p>Fails with the supplied failure {@code message}.
	 *
	 * @see Object#equals(Object)
	 */
	public static void assertEquals(Object expected, Object actual, String message) {
		AssertEquals.assertEquals(expected, actual, message);
	}

	/**
	 * <em>Asserts</em> that {@code expected} and {@code actual} are equal.
	 * <p>If both are {@code null}, they are considered equal.
	 * <p>If necessary, the failure message will be retrieved lazily from the supplied {@code messageSupplier}.
	 *
	 * @see Object#equals(Object)
	 */
	public static void assertEquals(Object expected, Object actual, Supplier<String> messageSupplier) {
		AssertEquals.assertEquals(expected, actual, messageSupplier);
	}

	// --- assertArrayEquals ---------------------------------------------------

	/**
	 * <em>Asserts</em> that {@code expected} and {@code actual} boolean arrays are equal.
	 * <p>If both are {@code null}, they are considered equal.
	 */
	public static void assertArrayEquals(boolean[] expected, boolean[] actual) {
		AssertArrayEquals.assertArrayEquals(expected, actual);
	}

	/**
	 * <em>Asserts</em> that {@code expected} and {@code actual} boolean arrays are equal.
	 * <p>If both are {@code null}, they are considered equal.
	 * <p>Fails with the supplied failure {@code message}.
	 */
	public static void assertArrayEquals(boolean[] expected, boolean[] actual, String message) {
		AssertArrayEquals.assertArrayEquals(expected, actual, message);
	}

	/**
	 * <em>Asserts</em> that {@code expected} and {@code actual} boolean arrays are equal.
	 * <p>If both are {@code null}, they are considered equal.
	 * <p>If necessary, the failure message will be retrieved lazily from the supplied {@code messageSupplier}.
	 */
	public static void assertArrayEquals(boolean[] expected, boolean[] actual, Supplier<String> messageSupplier) {
		AssertArrayEquals.assertArrayEquals(expected, actual, messageSupplier);
	}

	/**
	 * <em>Asserts</em> that {@code expected} and {@code actual} char arrays are equal.
	 * <p>If both are {@code null}, they are considered equal.
	 */
	public static void assertArrayEquals(char[] expected, char[] actual) {
		AssertArrayEquals.assertArrayEquals(expected, actual);
	}

	/**
	 * <em>Asserts</em> that {@code expected} and {@code actual} char arrays are equal.
	 * <p>If both are {@code null}, they are considered equal.
	 * <p>Fails with the supplied failure {@code message}.
	 */
	public static void assertArrayEquals(char[] expected, char[] actual, String message) {
		AssertArrayEquals.assertArrayEquals(expected, actual, message);
	}

	/**
	 * <em>Asserts</em> that {@code expected} and {@code actual} char arrays are equal.
	 * <p>If both are {@code null}, they are considered equal.
	 * <p>If necessary, the failure message will be retrieved lazily from the supplied {@code messageSupplier}.
	 */
	public static void assertArrayEquals(char[] expected, char[] actual, Supplier<String> messageSupplier) {
		AssertArrayEquals.assertArrayEquals(expected, actual, messageSupplier);
	}

	/**
	 * <em>Asserts</em> that {@code expected} and {@code actual} byte arrays are equal.
	 * <p>If both are {@code null}, they are considered equal.
	 */
	public static void assertArrayEquals(byte[] expected, byte[] actual) {
		AssertArrayEquals.assertArrayEquals(expected, actual);
	}

	/**
	 * <em>Asserts</em> that {@code expected} and {@code actual} byte arrays are equal.
	 * <p>If both are {@code null}, they are considered equal.
	 * <p>Fails with the supplied failure {@code message}.
	 */
	public static void assertArrayEquals(byte[] expected, byte[] actual, String message) {
		AssertArrayEquals.assertArrayEquals(expected, actual, message);
	}

	/**
	 * <em>Asserts</em> that {@code expected} and {@code actual} byte arrays are equal.
	 * <p>If both are {@code null}, they are considered equal.
	 * <p>If necessary, the failure message will be retrieved lazily from the supplied {@code messageSupplier}.
	 */
	public static void assertArrayEquals(byte[] expected, byte[] actual, Supplier<String> messageSupplier) {
		AssertArrayEquals.assertArrayEquals(expected, actual, messageSupplier);
	}

	/**
	 * <em>Asserts</em> that {@code expected} and {@code actual} short arrays are equal.
	 * <p>If both are {@code null}, they are considered equal.
	 */
	public static void assertArrayEquals(short[] expected, short[] actual) {
		AssertArrayEquals.assertArrayEquals(expected, actual);
	}

	/**
	 * <em>Asserts</em> that {@code expected} and {@code actual} short arrays are equal.
	 * <p>If both are {@code null}, they are considered equal.
	 * <p>Fails with the supplied failure {@code message}.
	 */
	public static void assertArrayEquals(short[] expected, short[] actual, String message) {
		AssertArrayEquals.assertArrayEquals(expected, actual, message);
	}

	/**
	 * <em>Asserts</em> that {@code expected} and {@code actual} short arrays are equal.
	 * <p>If both are {@code null}, they are considered equal.
	 * <p>If necessary, the failure message will be retrieved lazily from the supplied {@code messageSupplier}.
	 */
	public static void assertArrayEquals(short[] expected, short[] actual, Supplier<String> messageSupplier) {
		AssertArrayEquals.assertArrayEquals(expected, actual, messageSupplier);
	}

	/**
	 * <em>Asserts</em> that {@code expected} and {@code actual} int arrays are equal.
	 * <p>If both are {@code null}, they are considered equal.
	 */
	public static void assertArrayEquals(int[] expected, int[] actual) {
		AssertArrayEquals.assertArrayEquals(expected, actual);
	}

	/**
	 * <em>Asserts</em> that {@code expected} and {@code actual} int arrays are equal.
	 * <p>If both are {@code null}, they are considered equal.
	 * <p>Fails with the supplied failure {@code message}.
	 */
	public static void assertArrayEquals(int[] expected, int[] actual, String message) {
		AssertArrayEquals.assertArrayEquals(expected, actual, message);
	}

	/**
	 * <em>Asserts</em> that {@code expected} and {@code actual} int arrays are equal.
	 * <p>If both are {@code null}, they are considered equal.
	 * <p>If necessary, the failure message will be retrieved lazily from the supplied {@code messageSupplier}.
	 */
	public static void assertArrayEquals(int[] expected, int[] actual, Supplier<String> messageSupplier) {
		AssertArrayEquals.assertArrayEquals(expected, actual, messageSupplier);
	}

	/**
	 * <em>Asserts</em> that {@code expected} and {@code actual} long arrays are equal.
	 * <p>If both are {@code null}, they are considered equal.
	 */
	public static void assertArrayEquals(long[] expected, long[] actual) {
		AssertArrayEquals.assertArrayEquals(expected, actual);
	}

	/**
	 * <em>Asserts</em> that {@code expected} and {@code actual} long arrays are equal.
	 * <p>If both are {@code null}, they are considered equal.
	 * <p>Fails with the supplied failure {@code message}.
	 */
	public static void assertArrayEquals(long[] expected, long[] actual, String message) {
		AssertArrayEquals.assertArrayEquals(expected, actual, message);
	}

	/**
	 * <em>Asserts</em> that {@code expected} and {@code actual} long arrays are equal.
	 * <p>If both are {@code null}, they are considered equal.
	 * <p>If necessary, the failure message will be retrieved lazily from the supplied {@code messageSupplier}.
	 */
	public static void assertArrayEquals(long[] expected, long[] actual, Supplier<String> messageSupplier) {
		AssertArrayEquals.assertArrayEquals(expected, actual, messageSupplier);
	}

	/**
	 * <em>Asserts</em> that {@code expected} and {@code actual} float arrays are equal.
	 * <p>Equality imposed by this method is consistent with {@link Float#equals(Object)} and
	 * {@link Float#compare(float, float)}.</p>
	 */
	public static void assertArrayEquals(float[] expected, float[] actual) {
		AssertArrayEquals.assertArrayEquals(expected, actual);
	}

	/**
	 * <em>Asserts</em> that {@code expected} and {@code actual} float arrays are equal.
	 * <p>Equality imposed by this method is consistent with {@link Float#equals(Object)} and
	 * {@link Float#compare(float, float)}.</p>
	 * <p>Fails with the supplied failure {@code message}.
	 */
	public static void assertArrayEquals(float[] expected, float[] actual, String message) {
		AssertArrayEquals.assertArrayEquals(expected, actual, message);
	}

	/**
	 * <em>Asserts</em> that {@code expected} and {@code actual} float arrays are equal.
	 * <p>Equality imposed by this method is consistent with {@link Float#equals(Object)} and
	 * {@link Float#compare(float, float)}.</p>
	 * <p>If necessary, the failure message will be retrieved lazily from the supplied {@code messageSupplier}.
	 */
	public static void assertArrayEquals(float[] expected, float[] actual, Supplier<String> messageSupplier) {
		AssertArrayEquals.assertArrayEquals(expected, actual, messageSupplier);
	}

	/**
	 * <em>Asserts</em> that {@code expected} and {@code actual} float arrays are equal within the given {@code delta}.
	 * <p>Equality imposed by this method is consistent with {@link Float#equals(Object)} and
	 * {@link Float#compare(float, float)}.</p>
	 */
	public static void assertArrayEquals(float[] expected, float[] actual, float delta) {
		AssertArrayEquals.assertArrayEquals(expected, actual, delta);
	}

	/**
	 * <em>Asserts</em> that {@code expected} and {@code actual} float arrays are equal within the given {@code delta}.
	 * <p>Equality imposed by this method is consistent with {@link Float#equals(Object)} and
	 * {@link Float#compare(float, float)}.</p>
	 * <p>Fails with the supplied failure {@code message}.
	 */
	public static void assertArrayEquals(float[] expected, float[] actual, float delta, String message) {
		AssertArrayEquals.assertArrayEquals(expected, actual, delta, message);
	}

	/**
	 * <em>Asserts</em> that {@code expected} and {@code actual} float arrays are equal within the given {@code delta}.
	 * <p>Equality imposed by this method is consistent with {@link Float#equals(Object)} and
	 * {@link Float#compare(float, float)}.</p>
	 * <p>If necessary, the failure message will be retrieved lazily from the supplied {@code messageSupplier}.
	 */
	public static void assertArrayEquals(float[] expected, float[] actual, float delta,
			Supplier<String> messageSupplier) {
		AssertArrayEquals.assertArrayEquals(expected, actual, delta, messageSupplier);
	}

	/**
	 * <em>Asserts</em> that {@code expected} and {@code actual} double arrays are equal.
	 * <p>Equality imposed by this method is consistent with {@link Double#equals(Object)} and
	 * {@link Double#compare(double, double)}.</p>
	 */
	public static void assertArrayEquals(double[] expected, double[] actual) {
		AssertArrayEquals.assertArrayEquals(expected, actual);
	}

	/**
	 * <em>Asserts</em> that {@code expected} and {@code actual} double arrays are equal.
	 * <p>Equality imposed by this method is consistent with {@link Double#equals(Object)} and
	 * {@link Double#compare(double, double)}.</p>
	 * <p>Fails with the supplied failure {@code message}.
	 */
	public static void assertArrayEquals(double[] expected, double[] actual, String message) {
		AssertArrayEquals.assertArrayEquals(expected, actual, message);
	}

	/**
	 * <em>Asserts</em> that {@code expected} and {@code actual} double arrays are equal.
	 * <p>Equality imposed by this method is consistent with {@link Double#equals(Object)} and
	 * {@link Double#compare(double, double)}.</p>
	 * <p>If necessary, the failure message will be retrieved lazily from the supplied {@code messageSupplier}.
	 */
	public static void assertArrayEquals(double[] expected, double[] actual, Supplier<String> messageSupplier) {
		AssertArrayEquals.assertArrayEquals(expected, actual, messageSupplier);
	}

	/**
	 * <em>Asserts</em> that {@code expected} and {@code actual} double arrays are equal within the given {@code delta}.
	 * <p>Equality imposed by this method is consistent with {@link Double#equals(Object)} and
	 * {@link Double#compare(double, double)}.</p>
	 */
	public static void assertArrayEquals(double[] expected, double[] actual, double delta) {
		AssertArrayEquals.assertArrayEquals(expected, actual, delta);
	}

	/**
	 * <em>Asserts</em> that {@code expected} and {@code actual} double arrays are equal within the given {@code delta}.
	 * <p>Equality imposed by this method is consistent with {@link Double#equals(Object)} and
	 * {@link Double#compare(double, double)}.</p>
	 * <p>Fails with the supplied failure {@code message}.
	 */
	public static void assertArrayEquals(double[] expected, double[] actual, double delta, String message) {
		AssertArrayEquals.assertArrayEquals(expected, actual, delta, message);
	}

	/**
	 * <em>Asserts</em> that {@code expected} and {@code actual} double arrays are equal within the given {@code delta}.
	 * <p>Equality imposed by this method is consistent with {@link Double#equals(Object)} and
	 * {@link Double#compare(double, double)}.</p>
	 * <p>If necessary, the failure message will be retrieved lazily from the supplied {@code messageSupplier}.
	 */
	public static void assertArrayEquals(double[] expected, double[] actual, double delta,
			Supplier<String> messageSupplier) {
		AssertArrayEquals.assertArrayEquals(expected, actual, delta, messageSupplier);
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
		AssertArrayEquals.assertArrayEquals(expected, actual);
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
		AssertArrayEquals.assertArrayEquals(expected, actual, message);
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
		AssertArrayEquals.assertArrayEquals(expected, actual, messageSupplier);
	}

	// --- assertIterableEquals --------------------------------------------

	/**
	 * <em>Asserts</em> that {@code expected} and {@code actual} iterables are deeply equal.
	 * <p>Similarly to the check for deep equality in {@link #assertArrayEquals(Object[], Object[])},
	 * if two iterables are encountered (including {@code expected} and {@code actual}) then their
	 * iterators must return equal elements in the same order as each other. <strong>Note:</strong>
	 * this means that the iterables <em>do not</em> need to be of the same type. Example: <pre>{@code
	 * import static java.util.Arrays.asList;
	 *  . . .
	 * Iterable<Integer> i0 = new ArrayList<>(asList(1, 2, 3));
	 * Iterable<Integer> i1 = new LinkedList<>(asList(1, 2, 3));
	 * assertIterableEquals(i0, i1); // Passes
	 * }</pre>
	 * <p>If both {@code expected} and {@code actual} are {@code null}, they are considered equal.
	 *
	 * @see Objects#equals(Object, Object)
	 * @see Arrays#deepEquals(Object[], Object[])
	 * @see #assertArrayEquals(Object[], Object[])
	 */
	public static void assertIterableEquals(Iterable<?> expected, Iterable<?> actual) {
		AssertIterableEquals.assertIterableEquals(expected, actual);
	}

	/**
	 * <em>Asserts</em> that {@code expected} and {@code actual} iterables are deeply equal.
	 * <p>Similarly to the check for deep equality in
	 * {@link #assertArrayEquals(Object[], Object[], String)}, if two iterables are encountered
	 * (including {@code expected} and {@code actual}) then their iterators must return equal
	 * elements in the same order as each other. <strong>Note:</strong> this means that the iterables
	 * <em>do not</em> need to be of the same type. Example: <pre>{@code
	 * import static java.util.Arrays.asList;
	 *  . . .
	 * Iterable<Integer> i0 = new ArrayList<>(asList(1, 2, 3));
	 * Iterable<Integer> i1 = new LinkedList<>(asList(1, 2, 3));
	 * assertIterableEquals(i0, i1); // Passes
	 * }</pre>
	 * <p>If both {@code expected} and {@code actual} are {@code null}, they are considered equal.
	 * <p>Fails with the supplied failure {@code message}.
	 *
	 * @see Objects#equals(Object, Object)
	 * @see Arrays#deepEquals(Object[], Object[])
	 * @see #assertArrayEquals(Object[], Object[], String)
	 */
	public static void assertIterableEquals(Iterable<?> expected, Iterable<?> actual, String message) {
		AssertIterableEquals.assertIterableEquals(expected, actual, message);
	}

	/**
	 * <em>Asserts</em> that {@code expected} and {@code actual} iterables are deeply equal.
	 * <p>Similarly to the check for deep equality in
	 * {@link #assertArrayEquals(Object[], Object[], Supplier)}, if two iterables are encountered
	 * (including {@code expected} and {@code actual}) then their iterators must return equal
	 * elements in the same order as each other. <strong>Note:</strong> this means that the iterables
	 * <em>do not</em> need to be of the same type. Example: <pre>{@code
	 * import static java.util.Arrays.asList;
	 *  . . .
	 * Iterable<Integer> i0 = new ArrayList<>(asList(1, 2, 3));
	 * Iterable<Integer> i1 = new LinkedList<>(asList(1, 2, 3));
	 * assertIterableEquals(i0, i1); // Passes
	 * }</pre>
	 * <p>If both {@code expected} and {@code actual} are {@code null}, they are considered equal.
	 * <p>If necessary, the failure message will be retrieved lazily from the supplied {@code messageSupplier}.
	 *
	 * @see Objects#equals(Object, Object)
	 * @see Arrays#deepEquals(Object[], Object[])
	 * @see #assertArrayEquals(Object[], Object[], Supplier)
	 */
	public static void assertIterableEquals(Iterable<?> expected, Iterable<?> actual,
			Supplier<String> messageSupplier) {
		AssertIterableEquals.assertIterableEquals(expected, actual, messageSupplier);
	}

	// --- assertLinesMatch ----------------------------------------------------

	/**
	 * <em>Asserts</em> that {@code expected} list of {@linkplain String}s matches {@code actual}
	 * list.
	 *
	 * <p>This method differs from other assertions that effectively only check {@link String#equals(Object)},
	 * in that it uses the following staged matching algorithm:
	 *
	 * <p>For each pair of expected and actual lines do
	 * <ol>
	 *   <li>check if {@code expected.equals(actual)} - if yes, continue with next pair</li>
	 *   <li>otherwise treat {@code expected} as a regular expression and check via
	 *   {@link String#matches(String)} - if yes, continue with next pair</li>
	 *   <li>otherwise check if {@code expected} line is a fast-forward marker, if yes apply
	 *   fast-forward actual lines accordingly (see below) and goto 1.</li>
	 * </ol>
	 *
	 * <p>A valid fast-forward marker is an expected line that starts and ends with the literal
	 * {@code >>} and contains at least 4 characters. Examples:
	 * <ul>
	 *   <li>{@code >>>>}<br>{@code >> stacktrace >>}<br>{@code >> single line, non Integer.parse()-able comment >>}
	 *   <br>Skip arbitrary number of actual lines, until first matching subsequent expected line is found. Any
	 *   character between the fast-forward literals are discarded.</li>
	 *   <li>{@code ">> 21 >>"}
	 *   <br>Skip strictly 21 lines. If they can't be skipped for any reason, an assertion error is raised.</li>
	 * </ul>
	 *
	 * <p>Example showing all three kinds of expected line formats:
	 * <pre>{@code
	 * │  │  │     caught: AssertionFailedError: single line fail message
	 * >> S T A C K T R A C E >>
	 * │  │  │   duration: [\d]+ ms
	 * │  │  │     status: ✘ FAILED
	 * │  └─ test() finished after [\d]+ ms\.
	 * └─ JUnit Jupiter finished after [\d]+ ms\.
	 * Test plan execution finished. Number of all tests: 1
	 *
	 * Test run finished after [\d]+ ms
	 * }</pre>
	 */
	public static void assertLinesMatch(List<String> expectedLines, List<String> actualLines) {
		AssertLinesMatch.assertLinesMatch(expectedLines, actualLines);
	}

	// --- assertNotEquals -----------------------------------------------------

	/**
	 * <em>Asserts</em> that {@code expected} and {@code actual} are not equal.
	 * <p>Fails if both are {@code null}.
	 *
	 * @see Object#equals(Object)
	 */
	public static void assertNotEquals(Object unexpected, Object actual) {
		AssertNotEquals.assertNotEquals(unexpected, actual);
	}

	/**
	 * <em>Asserts</em> that {@code expected} and {@code actual} are not equal.
	 * <p>Fails if both are {@code null}.
	 * <p>Fails with the supplied failure {@code message}.
	 *
	 * @see Object#equals(Object)
	 */
	public static void assertNotEquals(Object unexpected, Object actual, String message) {
		AssertNotEquals.assertNotEquals(unexpected, actual, message);
	}

	/**
	 * <em>Asserts</em> that {@code expected} and {@code actual} are not equal.
	 * <p>Fails if both are {@code null}.
	 * <p>If necessary, the failure message will be retrieved lazily from the supplied {@code messageSupplier}.
	 *
	 * @see Object#equals(Object)
	 */
	public static void assertNotEquals(Object unexpected, Object actual, Supplier<String> messageSupplier) {
		AssertNotEquals.assertNotEquals(unexpected, actual, messageSupplier);
	}

	// --- assertSame ----------------------------------------------------------

	/**
	 * <em>Asserts</em> that {@code expected} and {@code actual} refer to the same object.
	 */
	public static void assertSame(Object expected, Object actual) {
		AssertSame.assertSame(expected, actual);
	}

	/**
	 * <em>Asserts</em> that {@code expected} and {@code actual} refer to the same object.
	 * <p>Fails with the supplied failure {@code message}.
	 */
	public static void assertSame(Object expected, Object actual, String message) {
		AssertSame.assertSame(expected, actual, message);
	}

	/**
	 * <em>Asserts</em> that {@code expected} and {@code actual} refer to the same object.
	 * <p>If necessary, the failure message will be retrieved lazily from the supplied {@code messageSupplier}.
	 */
	public static void assertSame(Object expected, Object actual, Supplier<String> messageSupplier) {
		AssertSame.assertSame(expected, actual, messageSupplier);
	}

	// --- assertNotSame -------------------------------------------------------

	/**
	 * <em>Asserts</em> that {@code expected} and {@code actual} do not refer to the same object.
	 */
	public static void assertNotSame(Object unexpected, Object actual) {
		AssertNotSame.assertNotSame(unexpected, actual);
	}

	/**
	 * <em>Asserts</em> that {@code expected} and {@code actual} do not refer to the same object.
	 * <p>Fails with the supplied failure {@code message}.
	 */
	public static void assertNotSame(Object unexpected, Object actual, String message) {
		AssertNotSame.assertNotSame(unexpected, actual, message);
	}

	/**
	 * <em>Asserts</em> that {@code expected} and {@code actual} do not refer to the same object.
	 * <p>If necessary, the failure message will be retrieved lazily from the supplied {@code messageSupplier}.
	 */
	public static void assertNotSame(Object unexpected, Object actual, Supplier<String> messageSupplier) {
		AssertNotSame.assertNotSame(unexpected, actual, messageSupplier);
	}

	// --- assertAll -----------------------------------------------------------

	/**
	 * <em>Asserts</em> that <em>all</em> supplied {@code executables} do not throw
	 * exceptions.
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
		AssertAll.assertAll(executables);
	}

	/**
	 * <em>Asserts</em> that <em>all</em> supplied {@code executables} do not throw
	 * exceptions.
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
		AssertAll.assertAll(executables);
	}

	/**
	 * <em>Asserts</em> that <em>all</em> supplied {@code executables} do not throw
	 * exceptions.
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
		AssertAll.assertAll(heading, executables);
	}

	/**
	 * <em>Asserts</em> that <em>all</em> supplied {@code executables} do not throw
	 * exceptions.
	 *
	 * <p>If any supplied {@link Executable} throws an exception (i.e., a {@link Throwable}
	 * or any subclass thereof), all remaining {@code executables} will still be executed,
	 * and all exceptions will be aggregated and reported in a {@link MultipleFailuresError}.
	 * However, if an {@code executable} throws a <em>blacklisted</em> exception &mdash; for
	 * example, an {@link OutOfMemoryError} &mdash; execution will halt immediately, and the
	 * blacklisted exception will be rethrown <em>as is</em> but <em>masked</em> as an
	 * unchecked exception.
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
		AssertAll.assertAll(heading, executables);
	}

	// --- assert exceptions ---------------------------------------------------

	/**
	 * <em>Asserts</em> that execution of the supplied {@code executable} throws
	 * an exception of the {@code expectedType} and returns the exception.
	 *
	 * <p>If no exception is thrown, or if an exception of a different type is thrown,
	 * this method will fail.
	 *
	 * <p>If you do not want to perform additional checks on the exception instance,
	 * simply ignore the return value.
	 */
	public static <T extends Throwable> T assertThrows(Class<T> expectedType, Executable executable) {
		return AssertThrows.assertThrows(expectedType, executable);
	}

	// --- assertTimeout -------------------------------------------------------

	// --- executable ---

	/**
	 * <em>Asserts</em> that execution of the supplied {@code executable}
	 * completes before the given {@code timeout} is exceeded.
	 *
	 * <p>Note: the {@code executable} will be executed in the same thread as that
	 * of the calling code. Consequently, execution of the {@code executable} will
	 * not be preemptively aborted if the timeout is exceeded.
	 *
	 * @see #assertTimeout(Duration, Executable, String)
	 * @see #assertTimeout(Duration, Executable, Supplier)
	 * @see #assertTimeout(Duration, ThrowingSupplier)
	 * @see #assertTimeout(Duration, ThrowingSupplier, String)
	 * @see #assertTimeout(Duration, ThrowingSupplier, Supplier)
	 * @see #assertTimeoutPreemptively(Duration, Executable)
	 */
	public static void assertTimeout(Duration timeout, Executable executable) {
		AssertTimeout.assertTimeout(timeout, executable);
	}

	/**
	 * <em>Asserts</em> that execution of the supplied {@code executable}
	 * completes before the given {@code timeout} is exceeded.
	 *
	 * <p>Note: the {@code executable} will be executed in the same thread as that
	 * of the calling code. Consequently, execution of the {@code executable} will
	 * not be preemptively aborted if the timeout is exceeded.
	 *
	 * <p>Fails with the supplied failure {@code message}.
	 *
	 * @see #assertTimeout(Duration, Executable)
	 * @see #assertTimeout(Duration, Executable, Supplier)
	 * @see #assertTimeout(Duration, ThrowingSupplier)
	 * @see #assertTimeout(Duration, ThrowingSupplier, String)
	 * @see #assertTimeout(Duration, ThrowingSupplier, Supplier)
	 * @see #assertTimeoutPreemptively(Duration, Executable, String)
	 */
	public static void assertTimeout(Duration timeout, Executable executable, String message) {
		AssertTimeout.assertTimeout(timeout, executable, message);
	}

	/**
	 * <em>Asserts</em> that execution of the supplied {@code executable}
	 * completes before the given {@code timeout} is exceeded.
	 *
	 * <p>Note: the {@code executable} will be executed in the same thread as that
	 * of the calling code. Consequently, execution of the {@code executable} will
	 * not be preemptively aborted if the timeout is exceeded.
	 *
	 * <p>If necessary, the failure message will be retrieved lazily from the
	 * supplied {@code messageSupplier}.
	 *
	 * @see #assertTimeout(Duration, Executable)
	 * @see #assertTimeout(Duration, Executable, String)
	 * @see #assertTimeout(Duration, ThrowingSupplier)
	 * @see #assertTimeout(Duration, ThrowingSupplier, String)
	 * @see #assertTimeout(Duration, ThrowingSupplier, Supplier)
	 * @see #assertTimeoutPreemptively(Duration, Executable, Supplier)
	 */
	public static void assertTimeout(Duration timeout, Executable executable, Supplier<String> messageSupplier) {
		AssertTimeout.assertTimeout(timeout, executable, messageSupplier);
	}

	// --- supplier ---

	/**
	 * <em>Asserts</em> that execution of the supplied {@code supplier}
	 * completes before the given {@code timeout} is exceeded.
	 *
	 * <p>If the assertion passes then the {@code supplier}'s result is returned.
	 *
	 * <p>Note: the {@code supplier} will be executed in the same thread as that
	 * of the calling code. Consequently, execution of the {@code supplier} will
	 * not be preemptively aborted if the timeout is exceeded.
	 *
	 * @see #assertTimeout(Duration, Executable)
	 * @see #assertTimeout(Duration, Executable, String)
	 * @see #assertTimeout(Duration, Executable, Supplier)
	 * @see #assertTimeout(Duration, ThrowingSupplier, String)
	 * @see #assertTimeout(Duration, ThrowingSupplier, Supplier)
	 * @see #assertTimeoutPreemptively(Duration, Executable)
	 */
	public static <T> T assertTimeout(Duration timeout, ThrowingSupplier<T> supplier) {
		return AssertTimeout.assertTimeout(timeout, supplier);
	}

	/**
	 * <em>Asserts</em> that execution of the supplied {@code supplier}
	 * completes before the given {@code timeout} is exceeded.
	 *
	 * <p>If the assertion passes then the {@code supplier}'s result is returned.
	 *
	 * <p>Note: the {@code supplier} will be executed in the same thread as that
	 * of the calling code. Consequently, execution of the {@code supplier} will
	 * not be preemptively aborted if the timeout is exceeded.
	 *
	 * <p>Fails with the supplied failure {@code message}.
	 *
	 * @see #assertTimeout(Duration, Executable)
	 * @see #assertTimeout(Duration, Executable, String)
	 * @see #assertTimeout(Duration, Executable, Supplier)
	 * @see #assertTimeout(Duration, ThrowingSupplier)
	 * @see #assertTimeout(Duration, ThrowingSupplier, Supplier)
	 * @see #assertTimeoutPreemptively(Duration, Executable, String)
	 */
	public static <T> T assertTimeout(Duration timeout, ThrowingSupplier<T> supplier, String message) {
		return AssertTimeout.assertTimeout(timeout, supplier, message);
	}

	/**
	 * <em>Asserts</em> that execution of the supplied {@code supplier}
	 * completes before the given {@code timeout} is exceeded.
	 *
	 * <p>If the assertion passes then the {@code supplier}'s result is returned.
	 *
	 * <p>Note: the {@code supplier} will be executed in the same thread as that
	 * of the calling code. Consequently, execution of the {@code supplier} will
	 * not be preemptively aborted if the timeout is exceeded.
	 *
	 * <p>If necessary, the failure message will be retrieved lazily from the
	 * supplied {@code messageSupplier}.
	 *
	 * @see #assertTimeout(Duration, Executable)
	 * @see #assertTimeout(Duration, Executable, String)
	 * @see #assertTimeout(Duration, Executable, Supplier)
	 * @see #assertTimeout(Duration, ThrowingSupplier)
	 * @see #assertTimeout(Duration, ThrowingSupplier, String)
	 * @see #assertTimeoutPreemptively(Duration, Executable, Supplier)
	 */
	public static <T> T assertTimeout(Duration timeout, ThrowingSupplier<T> supplier,
			Supplier<String> messageSupplier) {
		return AssertTimeout.assertTimeout(timeout, supplier, messageSupplier);
	}

	// --- executable - preemptively ---

	/**
	 * <em>Asserts</em> that execution of the supplied {@code executable}
	 * completes before the given {@code timeout} is exceeded.
	 *
	 * <p>Note: the {@code executable} will be executed in a different thread than
	 * that of the calling code. Furthermore, execution of the {@code executable} will
	 * be preemptively aborted if the timeout is exceeded.
	 *
	 * @see #assertTimeoutPreemptively(Duration, Executable, String)
	 * @see #assertTimeoutPreemptively(Duration, Executable, Supplier)
	 * @see #assertTimeoutPreemptively(Duration, ThrowingSupplier)
	 * @see #assertTimeoutPreemptively(Duration, ThrowingSupplier, String)
	 * @see #assertTimeoutPreemptively(Duration, ThrowingSupplier, Supplier)
	 * @see #assertTimeout(Duration, Executable)
	 */
	public static void assertTimeoutPreemptively(Duration timeout, Executable executable) {
		AssertTimeout.assertTimeoutPreemptively(timeout, executable);
	}

	/**
	 * <em>Asserts</em> that execution of the supplied {@code executable}
	 * completes before the given {@code timeout} is exceeded.
	 *
	 * <p>Note: the {@code executable} will be executed in a different thread than
	 * that of the calling code. Furthermore, execution of the {@code executable} will
	 * be preemptively aborted if the timeout is exceeded.
	 *
	 * <p>Fails with the supplied failure {@code message}.
	 *
	 * @see #assertTimeoutPreemptively(Duration, Executable)
	 * @see #assertTimeoutPreemptively(Duration, Executable, Supplier)
	 * @see #assertTimeoutPreemptively(Duration, ThrowingSupplier)
	 * @see #assertTimeoutPreemptively(Duration, ThrowingSupplier, String)
	 * @see #assertTimeoutPreemptively(Duration, ThrowingSupplier, Supplier)
	 * @see #assertTimeout(Duration, Executable, String)
	 */
	public static void assertTimeoutPreemptively(Duration timeout, Executable executable, String message) {
		AssertTimeout.assertTimeoutPreemptively(timeout, executable, message);
	}

	/**
	 * <em>Asserts</em> that execution of the supplied {@code executable}
	 * completes before the given {@code timeout} is exceeded.
	 *
	 * <p>Note: the {@code executable} will be executed in a different thread than
	 * that of the calling code. Furthermore, execution of the {@code executable} will
	 * be preemptively aborted if the timeout is exceeded.
	 *
	 * <p>If necessary, the failure message will be retrieved lazily from the
	 * supplied {@code messageSupplier}.
	 *
	 * @see #assertTimeoutPreemptively(Duration, Executable)
	 * @see #assertTimeoutPreemptively(Duration, Executable, String)
	 * @see #assertTimeoutPreemptively(Duration, ThrowingSupplier)
	 * @see #assertTimeoutPreemptively(Duration, ThrowingSupplier, String)
	 * @see #assertTimeoutPreemptively(Duration, ThrowingSupplier, Supplier)
	 * @see #assertTimeout(Duration, Executable, Supplier)
	 */
	public static void assertTimeoutPreemptively(Duration timeout, Executable executable,
			Supplier<String> messageSupplier) {
		AssertTimeout.assertTimeoutPreemptively(timeout, executable, messageSupplier);
	}

	// --- supplier - preemptively ---

	/**
	 * <em>Asserts</em> that execution of the supplied {@code supplier}
	 * completes before the given {@code timeout} is exceeded.
	 *
	 * <p>If the assertion passes then the {@code supplier}'s result is returned.
	 *
	 * <p>Note: the {@code supplier} will be executed in a different thread than
	 * that of the calling code. Furthermore, execution of the {@code supplier} will
	 * be preemptively aborted if the timeout is exceeded.
	 *
	 * @see #assertTimeoutPreemptively(Duration, Executable)
	 * @see #assertTimeoutPreemptively(Duration, Executable, String)
	 * @see #assertTimeoutPreemptively(Duration, Executable, Supplier)
	 * @see #assertTimeoutPreemptively(Duration, ThrowingSupplier, String)
	 * @see #assertTimeoutPreemptively(Duration, ThrowingSupplier, Supplier)
	 * @see #assertTimeout(Duration, Executable)
	 */
	public static <T> T assertTimeoutPreemptively(Duration timeout, ThrowingSupplier<T> supplier) {
		return AssertTimeout.assertTimeoutPreemptively(timeout, supplier);
	}

	/**
	 * <em>Asserts</em> that execution of the supplied {@code supplier}
	 * completes before the given {@code timeout} is exceeded.
	 *
	 * <p>If the assertion passes then the {@code supplier}'s result is returned.
	 *
	 * <p>Note: the {@code supplier} will be executed in a different thread than
	 * that of the calling code. Furthermore, execution of the {@code supplier} will
	 * be preemptively aborted if the timeout is exceeded.
	 *
	 * <p>Fails with the supplied failure {@code message}.
	 *
	 * @see #assertTimeoutPreemptively(Duration, Executable)
	 * @see #assertTimeoutPreemptively(Duration, Executable, String)
	 * @see #assertTimeoutPreemptively(Duration, Executable, Supplier)
	 * @see #assertTimeoutPreemptively(Duration, ThrowingSupplier)
	 * @see #assertTimeoutPreemptively(Duration, ThrowingSupplier, Supplier)
	 * @see #assertTimeout(Duration, Executable, String)
	 */
	public static <T> T assertTimeoutPreemptively(Duration timeout, ThrowingSupplier<T> supplier, String message) {
		return AssertTimeout.assertTimeoutPreemptively(timeout, supplier, message);
	}

	/**
	 * <em>Asserts</em> that execution of the supplied {@code supplier}
	 * completes before the given {@code timeout} is exceeded.
	 *
	 * <p>If the assertion passes then the {@code supplier}'s result is returned.
	 *
	 * <p>Note: the {@code supplier} will be executed in a different thread than
	 * that of the calling code. Furthermore, execution of the {@code supplier} will
	 * be preemptively aborted if the timeout is exceeded.
	 *
	 * <p>If necessary, the failure message will be retrieved lazily from the
	 * supplied {@code messageSupplier}.
	 *
	 * @see #assertTimeoutPreemptively(Duration, Executable)
	 * @see #assertTimeoutPreemptively(Duration, Executable, String)
	 * @see #assertTimeoutPreemptively(Duration, Executable, Supplier)
	 * @see #assertTimeoutPreemptively(Duration, ThrowingSupplier)
	 * @see #assertTimeoutPreemptively(Duration, ThrowingSupplier, String)
	 * @see #assertTimeout(Duration, Executable, Supplier)
	 */
	public static <T> T assertTimeoutPreemptively(Duration timeout, ThrowingSupplier<T> supplier,
			Supplier<String> messageSupplier) {
		return AssertTimeout.assertTimeoutPreemptively(timeout, supplier, messageSupplier);
	}

}
