/*
 * Copyright 2015-2021 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * https://www.eclipse.org/legal/epl-v20.html
 */

package org.junit.jupiter.api;

import static org.junit.jupiter.api.AssertionTestUtils.assertMessageContains;
import static org.junit.jupiter.api.AssertionTestUtils.assertMessageStartsWith;
import static org.junit.jupiter.api.AssertionTestUtils.expectAssertionFailedError;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrowsExactly;

import java.util.function.Supplier;

import org.junit.jupiter.api.function.Executable;
import org.opentest4j.AssertionFailedError;

/**
 * Unit tests for JUnit Jupiter {@link Assertions}.
 *
 * @since 5.8
 */
class AssertThrowsExactlyAssertionsTests extends AbstractThrowsAssertionsTests {

	@Override
	<T extends Throwable> T assertThrows(Class<T> expectedType, Executable executable) {
		return Assertions.assertThrowsExactly(expectedType, executable);
	}

	@Override
	<T extends Throwable> T assertThrows(Class<T> expectedType, Executable executable, String message) {
		return Assertions.assertThrowsExactly(expectedType, executable, message);
	}

	@Override
	<T extends Throwable> T assertThrows(Class<T> expectedType, Executable executable,
			Supplier<String> messageSupplier) {
		return Assertions.assertThrowsExactly(expectedType, executable, messageSupplier);
	}

	@Test
	void assertThrowsExactlyTheSpecifiedExceptionClass() {
		var actual = assertThrowsExactly(EnigmaThrowable.class, (Executable) () -> {
			throw new EnigmaThrowable();
		});
		assertNotNull(actual);
	}

	@Test
	void assertThrowsExactlyWithTheExpectedChildException() {
		try {
			assertThrowsExactly(RuntimeException.class, (Executable) () -> {
				throw new Exception();
			});
			expectAssertionFailedError();
		}
		catch (AssertionFailedError ex) {
			assertMessageStartsWith(ex, "Unexpected exception type thrown ==> ");
			assertMessageContains(ex, "expected: <java.lang.RuntimeException>");
			assertMessageContains(ex, "but was: <java.lang.Exception>");
		}
	}

	@Test
	void assertThrowsExactlyWithTheExpectedParentException() {
		try {
			assertThrowsExactly(RuntimeException.class, (Executable) () -> {
				throw new NumberFormatException();
			});
			expectAssertionFailedError();
		}
		catch (AssertionFailedError ex) {
			assertMessageStartsWith(ex, "Unexpected exception type thrown ==> ");
			assertMessageContains(ex, "expected: <java.lang.RuntimeException>");
			assertMessageContains(ex, "but was: <java.lang.NumberFormatException>");
		}
	}
}
