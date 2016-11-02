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

import static org.junit.jupiter.api.AssertionTestUtils.assertMessageContains;
import static org.junit.jupiter.api.AssertionTestUtils.expectAssertionFailedError;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.IOException;

import org.opentest4j.AssertionFailedError;

/**
 * Unit tests for JUnit Jupiter {@link Assertions}.
 *
 * @since 5.0
 */
public class AssertionsAssertThrowsTests {

	@Test
	void assertThrowsThrowable() {
		EnigmaThrowable enigmaThrowable = assertThrows(EnigmaThrowable.class, () -> {
			throw new EnigmaThrowable();
		});
		assertNotNull(enigmaThrowable);
	}

	@Test
	void assertThrowsCheckedException() {
		IOException exception = assertThrows(IOException.class, () -> {
			throw new IOException();
		});
		assertNotNull(exception);
	}

	@Test
	void assertThrowsRuntimeException() {
		IllegalStateException illegalStateException = assertThrows(IllegalStateException.class, () -> {
			throw new IllegalStateException();
		});
		assertNotNull(illegalStateException);
	}

	@Test
	void assertThrowsError() {
		StackOverflowError stackOverflowError = assertThrows(StackOverflowError.class,
			AssertionTestUtils::recurseIndefinitely);
		assertNotNull(stackOverflowError);
	}

	@Test
	void assertThrowsWithExecutableThatDoesNotThrowAnException() {
		try {
			assertThrows(IllegalStateException.class, () -> {
			});
			expectAssertionFailedError();
		}
		catch (AssertionFailedError ex) {
			assertMessageContains(ex, "Expected java.lang.IllegalStateException to be thrown");
		}
	}

	@Test
	void assertThrowsWithExecutableThatThrowsAnUnexpectedException() {
		try {
			assertThrows(IllegalStateException.class, () -> {
				throw new NumberFormatException();
			});
			expectAssertionFailedError();
		}
		catch (AssertionFailedError ex) {
			assertMessageContains(ex, "Unexpected exception type thrown");
			assertMessageContains(ex, "expected: <java.lang.IllegalStateException>");
			assertMessageContains(ex, "but was: <java.lang.NumberFormatException>");
		}
	}

	@SuppressWarnings("serial")
	private static class EnigmaThrowable extends Throwable {
	}

}
