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

import static org.junit.gen5.api.Assertions.assertAll;
import static org.junit.gen5.api.Assertions.assertFalse;
import static org.junit.gen5.api.Assertions.assertThrows;
import static org.junit.gen5.api.Assertions.assertTrue;
import static org.junit.gen5.api.Assertions.expectThrows;
import static org.junit.gen5.api.Assertions.fail;

import java.io.IOException;
import java.util.List;
import java.util.function.Supplier;

import org.opentest4j.AssertionFailedError;
import org.opentest4j.MultipleFailuresError;

/**
 * Unit tests for {@link Assertions}.
 *
 * @since 5.0
 */
public class AssertionsTests {

	@Test
	void failWithString() {
		try {
			fail("test");
			expectAssertionFailedError();
		}
		catch (AssertionFailedError ex) {
			assertMessageEquals(ex, "test");
		}
	}

	@Test
	void failWithMessageSupplier() {
		try {
			fail(() -> "test");
			expectAssertionFailedError();
		}
		catch (AssertionFailedError ex) {
			assertMessageEquals(ex, "test");
		}
	}

	@Test
	void failWithNullString() {
		try {
			fail((String) null);
			expectAssertionFailedError();
		}
		catch (AssertionFailedError ex) {
			assertMessageIsNull(ex);
		}
	}

	@Test
	void failWithNullMessageSupplier() {
		try {
			fail((Supplier<String>) null);
			expectAssertionFailedError();
		}
		catch (AssertionFailedError ex) {
			assertMessageIsNull(ex);
		}
	}

	@Test
	void assertTrueWithBooleanTrue() {
		assertTrue(true);
	}

	@Test
	void assertTrueWithBooleanSupplierTrue() {
		assertTrue(() -> true);
	}

	@Test
	void assertTrueWithBooleanTrueAndString() {
		assertTrue(true, "test");
	}

	@Test
	void assertTrueWithBooleanFalse() {
		try {
			assertTrue(false);
			expectAssertionFailedError();
		}
		catch (AssertionFailedError ex) {
			assertMessageIsNull(ex);
		}
	}

	@Test
	void assertTrueWithBooleanFalseAndString() {
		try {
			assertTrue(false, "test");
			expectAssertionFailedError();
		}
		catch (AssertionFailedError ex) {
			assertMessageEquals(ex, "test");
		}
	}

	@Test
	void assertTrueWithBooleanSupplierFalseAndMessageSupplier() {
		try {
			assertTrue(() -> false, () -> "test");
			expectAssertionFailedError();
		}
		catch (AssertionFailedError ex) {
			assertMessageEquals(ex, "test");
		}
	}

	@Test
	void assertFalseWithBooleanFalse() {
		assertFalse(false);
	}

	@Test
	void assertFalseWithBooleanTrueAndString() {
		try {
			assertFalse(true, "test");
			expectAssertionFailedError();
		}
		catch (AssertionFailedError ex) {
			assertMessageEquals(ex, "test");
		}
	}

	@Test
	void assertAllWithExecutableThatThrowsAssertionError() {
		MultipleFailuresError multipleFailuresError = expectThrows(MultipleFailuresError.class,
			() -> assertAll(() -> assertFalse(true)));
		assertTrue(multipleFailuresError != null);
		List<AssertionError> failures = multipleFailuresError.getFailures();
		assertTrue(failures.size() == 1);
		assertTrue(failures.get(0).getClass().equals(AssertionFailedError.class));
	}

	@Test
	void assertAllWithExecutableThatThrowsThrowable() {
		assertThrows(EnigmaThrowable.class, () -> assertAll(() -> {
			throw new EnigmaThrowable();
		}));
	}

	@Test
	void assertAllWithExecutableThatThrowsCheckedException() {
		assertThrows(IOException.class, () -> assertAll(() -> {
			throw new IOException();
		}));
	}

	@Test
	void assertAllWithExecutableThatThrowsRuntimeException() {
		assertThrows(IllegalStateException.class, () -> assertAll(() -> {
			throw new IllegalStateException();
		}));
	}

	@Test
	void assertAllWithExecutableThatThrowsError() {
		assertThrows(StackOverflowError.class, () -> assertAll(this::recurseIndefinitely));
	}

	@Test
	void assertThrowsThrowable() {
		assertThrows(EnigmaThrowable.class, () -> {
			throw new EnigmaThrowable();
		});
	}

	@Test
	void assertThrowsCheckedException() {
		assertThrows(IOException.class, () -> {
			throw new IOException();
		});
	}

	@Test
	void assertThrowsRuntimeException() {
		assertThrows(IllegalStateException.class, () -> {
			throw new IllegalStateException();
		});
	}

	@Test
	void assertThrowsError() {
		assertThrows(StackOverflowError.class, this::recurseIndefinitely);
	}

	private void recurseIndefinitely() {
		// simulate infinite recursion
		throw new StackOverflowError();
	}

	// -------------------------------------------------------------------

	private static void expectAssertionFailedError() {
		throw new AssertionError("Should have thrown an " + AssertionFailedError.class.getName());
	}

	private static void assertMessageIsNull(AssertionFailedError ex) throws AssertionError {
		if (ex.getMessage() != null) {
			throw new AssertionError("Message in AssertionFailedError should be null");
		}
	}

	private static void assertMessageEquals(AssertionFailedError ex, String msg) throws AssertionError {
		if (!msg.equals(ex.getMessage())) {
			throw new AssertionError(
				"Message in AssertionFailedError should be '" + msg + "', but was '" + ex.getMessage() + "'.");
		}
	}

	@SuppressWarnings("serial")
	private static class EnigmaThrowable extends Throwable {
	}
}
