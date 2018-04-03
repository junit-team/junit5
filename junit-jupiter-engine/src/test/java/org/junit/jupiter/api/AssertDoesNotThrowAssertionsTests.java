/*
 * Copyright 2015-2018 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v20.html
 */

package org.junit.jupiter.api;

import static org.junit.jupiter.api.AssertionTestUtils.assertMessageEquals;
import static org.junit.jupiter.api.AssertionTestUtils.expectAssertionFailedError;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

import java.io.IOException;

import org.junit.jupiter.api.function.Executable;
import org.opentest4j.AssertionFailedError;

/**
 * Unit tests for JUnit Jupiter {@link Assertions}.
 *
 * @since 5.2
 */
class AssertDoesNotThrowAssertionsTests {

	private static final Executable nix = () -> {
	};

	@Test
	void assertDoesNotThrowAnything() {
		assertDoesNotThrow(nix);
	}

	@Test
	void assertDoesNotThrowAnythingWithMessage() {
		assertDoesNotThrow(nix, "message");
	}

	@Test
	void assertDoesNotThrowAnythingWithMessageSupplier() {
		assertDoesNotThrow(nix, () -> "message");
	}

	@Test
	void assertDoesNotThrowWithExecutableThatThrowsACheckedException() {
		try {
			assertDoesNotThrow(() -> {
				throw new IOException();
			});
			expectAssertionFailedError();
		}
		catch (AssertionFailedError ex) {
			assertMessageEquals(ex, "Unexpected exception thrown: " + IOException.class.getName());
		}
	}

	@Test
	void assertDoesNotThrowWithExecutableThatThrowsARuntimeException() {
		try {
			assertDoesNotThrow(() -> {
				throw new IllegalStateException();
			});
			expectAssertionFailedError();
		}
		catch (AssertionFailedError ex) {
			assertMessageEquals(ex, "Unexpected exception thrown: " + IllegalStateException.class.getName());
		}
	}

	@Test
	void assertDoesNotThrowWithExecutableThatThrowsAnError() {
		try {
			assertDoesNotThrow(AssertionTestUtils::recurseIndefinitely);
			expectAssertionFailedError();
		}
		catch (AssertionFailedError ex) {
			assertMessageEquals(ex, "Unexpected exception thrown: " + StackOverflowError.class.getName());
		}
	}

	@Test
	void assertDoesNotThrowWithExecutableThatThrowsAnExceptionWithMessageString() {
		try {
			assertDoesNotThrow(() -> {
				throw new IllegalStateException();
			}, "Custom message");
			expectAssertionFailedError();
		}
		catch (AssertionFailedError ex) {
			assertMessageEquals(ex,
				"Custom message ==> Unexpected exception thrown: " + IllegalStateException.class.getName());
		}
	}

	@Test
	void assertDoesNotThrowWithExecutableThatThrowsAnExceptionWithMessageSupplier() {
		try {
			assertDoesNotThrow(() -> {
				throw new IllegalStateException();
			}, () -> "Custom message");
			expectAssertionFailedError();
		}
		catch (AssertionFailedError ex) {
			assertMessageEquals(ex,
				"Custom message ==> Unexpected exception thrown: " + IllegalStateException.class.getName());
		}
	}

}
