/*
 * Copyright 2015-2017 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v20.html
 */

package org.junit.jupiter.api;

import static org.junit.jupiter.api.AssertionTestUtils.assertMessageEquals;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.util.List;
import java.util.stream.Stream;

import org.junit.jupiter.api.function.Executable;
import org.junit.platform.commons.util.PreconditionViolationException;
import org.opentest4j.AssertionFailedError;
import org.opentest4j.MultipleFailuresError;

/**
 * Unit tests for JUnit Jupiter {@link Assertions}.
 *
 * @since 5.0
 */
class AssertAllAssertionsTests {

	@Test
	void assertAllWithNullInExecutableArray() {
		try {
			// @formatter:off
			assertAll(
				() -> {},
				(Executable) null
			);
			// @formatter:on
		}
		catch (PreconditionViolationException ex) {
			assertMessageEquals(ex, "individual executables must not be null");
		}
	}

	@Test
	void assertAllWithNullExecutableArray() {
		try {
			assertAll((Executable[]) null);
		}
		catch (PreconditionViolationException ex) {
			assertMessageEquals(ex, "executables array must not be null or empty");
		}
	}

	@Test
	void assertAllWithNullExecutableStream() {
		try {
			assertAll((Stream<Executable>) null);
		}
		catch (PreconditionViolationException ex) {
			assertMessageEquals(ex, "executables must not be null");
		}
	}

	@Test
	void assertAllWithExecutablesThatDoNotThrowExceptions() {
		// @formatter:off
		assertAll(
			() -> assertTrue(true),
			() -> assertFalse(false),
			() -> assertTrue(true)
		);
		// @formatter:on
	}

	@Test
	void assertAllWithExecutablesThatThrowAssertionErrors() {
		// @formatter:off
		MultipleFailuresError multipleFailuresError = assertThrows(MultipleFailuresError.class, () ->
			assertAll(
				() -> assertFalse(true),
				() -> assertFalse(true)
			)
		);
		// @formatter:on

		assertExpectedExceptionTypes(multipleFailuresError, AssertionFailedError.class, AssertionFailedError.class);
	}

	@Test
	void assertAllWithStreamOfExecutablesThatThrowAssertionErrors() {
		// @formatter:off
		MultipleFailuresError multipleFailuresError = assertThrows(MultipleFailuresError.class, () ->
			assertAll(Stream.of(() -> assertFalse(true), () -> assertFalse(true)))
		);
		// @formatter:on

		assertExpectedExceptionTypes(multipleFailuresError, AssertionFailedError.class, AssertionFailedError.class);
	}

	@Test
	void assertAllWithExecutableThatThrowsThrowable() {
		MultipleFailuresError multipleFailuresError = assertThrows(MultipleFailuresError.class, () -> assertAll(() -> {
			throw new EnigmaThrowable();
		}));

		assertExpectedExceptionTypes(multipleFailuresError, EnigmaThrowable.class);
	}

	@Test
	void assertAllWithExecutableThatThrowsCheckedException() {
		MultipleFailuresError multipleFailuresError = assertThrows(MultipleFailuresError.class, () -> assertAll(() -> {
			throw new IOException();
		}));

		assertExpectedExceptionTypes(multipleFailuresError, IOException.class);
	}

	@Test
	void assertAllWithExecutableThatThrowsRuntimeException() {
		MultipleFailuresError multipleFailuresError = assertThrows(MultipleFailuresError.class, () -> assertAll(() -> {
			throw new IllegalStateException();
		}));

		assertExpectedExceptionTypes(multipleFailuresError, IllegalStateException.class);
	}

	@Test
	void assertAllWithExecutableThatThrowsError() {
		MultipleFailuresError multipleFailuresError = assertThrows(MultipleFailuresError.class,
			() -> assertAll(AssertionTestUtils::recurseIndefinitely));

		assertExpectedExceptionTypes(multipleFailuresError, StackOverflowError.class);
	}

	@Test
	void assertAllWithExecutableThatThrowsBlacklistedException() {
		OutOfMemoryError outOfMemoryError = assertThrows(OutOfMemoryError.class,
			() -> assertAll(AssertionTestUtils::runOutOfMemory));

		assertEquals("boom", outOfMemoryError.getMessage());
	}

	@SafeVarargs
	private static void assertExpectedExceptionTypes(MultipleFailuresError multipleFailuresError,
			Class<? extends Throwable>... exceptionTypes) {

		assertNotNull(multipleFailuresError, "MultipleFailuresError");
		List<Throwable> failures = multipleFailuresError.getFailures();
		assertEquals(exceptionTypes.length, failures.size(), "number of failures");

		for (int i = 0; i < exceptionTypes.length; i++) {
			assertEquals(exceptionTypes[i], failures.get(i).getClass(), "exception type");
		}
	}

	@SuppressWarnings("serial")
	private static class EnigmaThrowable extends Throwable {
	}

}
