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

import java.io.Serializable;
import java.util.Objects;

import org.opentest4j.AssertionFailedError;
import org.opentest4j.ValueWrapper;

class AssertionTestUtils {

	private AssertionTestUtils() {
		/* no-op */
	}

	static void expectAssertionFailedError() {
		throw new AssertionError("Should have thrown an " + AssertionFailedError.class.getName());
	}

	static void assertEmptyMessage(Throwable ex) throws AssertionError {
		if (!ex.getMessage().isEmpty()) {
			throw new AssertionError("Exception message should be empty, but was [" + ex.getMessage() + "].");
		}
	}

	static void assertMessageEquals(Throwable ex, String msg) throws AssertionError {
		if (!msg.equals(ex.getMessage())) {
			throw new AssertionError("Exception message should be [" + msg + "], but was [" + ex.getMessage() + "].");
		}
	}

	static void assertMessageMatches(Throwable ex, String regex) throws AssertionError {
		if (!ex.getMessage().matches(regex)) {
			throw new AssertionError("Exception message should match regular expression [" + regex + "], but was ["
					+ ex.getMessage() + "].");
		}
	}

	static void assertMessageStartsWith(Throwable ex, String msg) throws AssertionError {
		if (!ex.getMessage().startsWith(msg)) {
			throw new AssertionError(
				"Exception message should start with [" + msg + "], but was [" + ex.getMessage() + "].");
		}
	}

	static void assertMessageEndsWith(Throwable ex, String msg) throws AssertionError {
		if (!ex.getMessage().endsWith(msg)) {
			throw new AssertionError(
				"Exception message should end with [" + msg + "], but was [" + ex.getMessage() + "].");
		}
	}

	static void assertMessageContains(Throwable ex, String msg) throws AssertionError {
		if (!ex.getMessage().contains(msg)) {
			throw new AssertionError(
				"Exception message should contain [" + msg + "], but was [" + ex.getMessage() + "].");
		}
	}

	static void assertExpectedAndActualValues(AssertionFailedError ex, Object expected, Object actual)
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

	static boolean wrapsEqualValue(ValueWrapper wrapper, Object value) {
		if (value == null || value instanceof Serializable) {
			return Objects.equals(value, wrapper.getValue());
		}
		return wrapper.getIdentityHashCode() == System.identityHashCode(value)
				&& Objects.equals(wrapper.getStringRepresentation(), String.valueOf(value))
				&& Objects.equals(wrapper.getType(), value.getClass());
	}

	static void recurseIndefinitely() {
		// simulate infinite recursion
		throw new StackOverflowError();
	}

	static void runOutOfMemory() {
		// simulate running out of memory
		throw new OutOfMemoryError("boom");
	}

}
