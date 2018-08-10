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

import static org.junit.jupiter.api.AssertionTestUtils.assertMessageContains;
import static org.junit.jupiter.api.AssertionTestUtils.assertMessageEquals;
import static org.junit.jupiter.api.AssertionTestUtils.assertMessageStartsWith;
import static org.junit.jupiter.api.AssertionTestUtils.expectAssertionFailedError;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;

import org.junit.jupiter.api.function.Executable;
import org.junit.jupiter.api.function.ThrowingSupplier;
import org.opentest4j.AssertionFailedError;

/**
 * Unit tests for JUnit Jupiter {@link Assertions}.
 *
 * @since 5.0
 */
class AssertThrowsAssertionsTests {

	private static final Executable nix = () -> {
	};

	@Test
	void assertThrowsWithFutureMethodReference() {
		FutureTask<String> future = new FutureTask<>(() -> {
			throw new RuntimeException("boom");
		});
		future.run();

		ExecutionException exception;

		// Current compiler's type inference
		// For rationale, see https://github.com/junit-team/junit5/issues/1414
		exception = assertThrows(ExecutionException.class, future::get);
		assertEquals("boom", exception.getCause().getMessage());

		// Explicitly as an Executable
		exception = assertThrows(ExecutionException.class, (Executable) future::get);
		assertEquals("boom", exception.getCause().getMessage());

		// Explicitly as a ThrowingSupplier
		exception = assertThrows(ExecutionException.class, (ThrowingSupplier<?>) future::get);
		assertEquals("boom", exception.getCause().getMessage());
	}

	@Test
	void assertThrowsWithExecutableThatThrowsThrowable() {
		EnigmaThrowable enigmaThrowable = assertThrows(EnigmaThrowable.class, (Executable) () -> {
			throw new EnigmaThrowable();
		});
		assertNotNull(enigmaThrowable);
	}

	@Test
	void assertThrowsWithExecutableThatThrowsThrowableWithMessage() {
		EnigmaThrowable enigmaThrowable = assertThrows(EnigmaThrowable.class, (Executable) () -> {
			throw new EnigmaThrowable();
		}, "message");
		assertNotNull(enigmaThrowable);
	}

	@Test
	void assertThrowsWithExecutableThatThrowsThrowableWithMessageSupplier() {
		EnigmaThrowable enigmaThrowable = assertThrows(EnigmaThrowable.class, (Executable) () -> {
			throw new EnigmaThrowable();
		}, () -> "message");
		assertNotNull(enigmaThrowable);
	}

	@Test
	void assertThrowsWithExecutableThatThrowsCheckedException() {
		IOException exception = assertThrows(IOException.class, (Executable) () -> {
			throw new IOException();
		});
		assertNotNull(exception);
	}

	@Test
	void assertThrowsWithExecutableThatThrowsRuntimeException() {
		IllegalStateException illegalStateException = assertThrows(IllegalStateException.class, (Executable) () -> {
			throw new IllegalStateException();
		});
		assertNotNull(illegalStateException);
	}

	@Test
	void assertThrowsWithExecutableThatThrowsError() {
		StackOverflowError stackOverflowError = assertThrows(StackOverflowError.class,
			AssertionTestUtils::recurseIndefinitely);
		assertNotNull(stackOverflowError);
	}

	@Test
	void assertThrowsWithExecutableThatDoesNotThrowAnException() {
		try {
			assertThrows(IllegalStateException.class, nix);
			expectAssertionFailedError();
		}
		catch (AssertionFailedError ex) {
			// the following also implicitly verifies that the failure message does not contain "(returned null)".
			assertMessageEquals(ex, "Expected java.lang.IllegalStateException to be thrown, but nothing was thrown.");
		}
	}

	@Test
	void assertThrowsWithExecutableThatDoesNotThrowAnExceptionWithMessageString() {
		try {
			assertThrows(IOException.class, nix, "Custom message");
			expectAssertionFailedError();
		}
		catch (AssertionError ex) {
			assertMessageEquals(ex,
				"Custom message ==> Expected java.io.IOException to be thrown, but nothing was thrown.");
		}
	}

	@Test
	void assertThrowsWithExecutableThatDoesNotThrowAnExceptionWithMessageSupplier() {
		try {
			assertThrows(IOException.class, nix, () -> "Custom message");
			expectAssertionFailedError();
		}
		catch (AssertionError ex) {
			assertMessageEquals(ex,
				"Custom message ==> Expected java.io.IOException to be thrown, but nothing was thrown.");
		}
	}

	@Test
	void assertThrowsWithExecutableThatThrowsAnUnexpectedException() {
		try {
			assertThrows(IllegalStateException.class, (Executable) () -> {
				throw new NumberFormatException();
			});
			expectAssertionFailedError();
		}
		catch (AssertionFailedError ex) {
			assertMessageStartsWith(ex, "Unexpected exception type thrown ==> ");
			assertMessageContains(ex, "expected: <java.lang.IllegalStateException>");
			assertMessageContains(ex, "but was: <java.lang.NumberFormatException>");
		}
	}

	@Test
	void assertThrowsWithExecutableThatThrowsAnUnexpectedExceptionWithMessageString() {
		try {
			assertThrows(IllegalStateException.class, (Executable) () -> {
				throw new NumberFormatException();
			}, "Custom message");
			expectAssertionFailedError();
		}
		catch (AssertionFailedError ex) {
			// Should look something like this:
			// Custom message ==> Unexpected exception type thrown ==> expected: <java.lang.IllegalStateException> but was: <java.lang.NumberFormatException>
			assertMessageStartsWith(ex, "Custom message ==> ");
			assertMessageContains(ex, "Unexpected exception type thrown ==> ");
			assertMessageContains(ex, "expected: <java.lang.IllegalStateException>");
			assertMessageContains(ex, "but was: <java.lang.NumberFormatException>");
		}
	}

	@Test
	void assertThrowsWithExecutableThatThrowsAnUnexpectedExceptionWithMessageSupplier() {
		try {
			assertThrows(IllegalStateException.class, (Executable) () -> {
				throw new NumberFormatException();
			}, () -> "Custom message");
			expectAssertionFailedError();
		}
		catch (AssertionFailedError ex) {
			// Should look something like this:
			// Custom message ==> Unexpected exception type thrown ==> expected: <java.lang.IllegalStateException> but was: <java.lang.NumberFormatException>
			assertMessageStartsWith(ex, "Custom message ==> ");
			assertMessageContains(ex, "Unexpected exception type thrown ==> ");
			assertMessageContains(ex, "expected: <java.lang.IllegalStateException>");
			assertMessageContains(ex, "but was: <java.lang.NumberFormatException>");
		}
	}

	@Test
	@SuppressWarnings("serial")
	void assertThrowsWithExecutableThatThrowsInstanceOfAnonymousInnerClassAsUnexpectedException() {
		try {
			assertThrows(IllegalStateException.class, (Executable) () -> {
				throw new NumberFormatException() {
				};
			});
			expectAssertionFailedError();
		}
		catch (AssertionFailedError ex) {
			assertMessageStartsWith(ex, "Unexpected exception type thrown ==> ");
			assertMessageContains(ex, "expected: <java.lang.IllegalStateException>");
			// As of the time of this writing, the class name of the above anonymous inner
			// class is org.junit.jupiter.api.AssertionsAssertThrowsTests$2; however, hard
			// coding "$2" is fragile. So we just check for the presence of the "$"
			// appended to this class's name.
			assertMessageContains(ex, "but was: <" + getClass().getName() + "$");
		}
	}

	@Test
	void assertThrowsWithExecutableThatThrowsInstanceOfStaticNestedClassAsUnexpectedException() {
		try {
			assertThrows(IllegalStateException.class, (Executable) () -> {
				throw new LocalException();
			});
			expectAssertionFailedError();
		}
		catch (AssertionFailedError ex) {
			assertMessageStartsWith(ex, "Unexpected exception type thrown ==> ");
			assertMessageContains(ex, "expected: <java.lang.IllegalStateException>");
			// The following verifies that the canonical name is used (i.e., "." instead of "$").
			assertMessageContains(ex, "but was: <" + LocalException.class.getName().replace("$", ".") + ">");
		}
	}

	@Test
	@SuppressWarnings("unchecked")
	void assertThrowsWithExecutableThatThrowsSameExceptionTypeFromDifferentClassLoader() throws Exception {
		try (EnigmaClassLoader enigmaClassLoader = new EnigmaClassLoader()) {

			// Load expected exception type from different class loader
			Class<? extends Throwable> enigmaThrowableClass = (Class<? extends Throwable>) enigmaClassLoader.loadClass(
				EnigmaThrowable.class.getName());

			try {
				assertThrows(enigmaThrowableClass, (Executable) () -> {
					throw new EnigmaThrowable();
				});
				expectAssertionFailedError();
			}
			catch (AssertionFailedError ex) {
				// Example Output:
				//
				// Unexpected exception type thrown ==>
				// expected: <org.junit.jupiter.api.EnigmaThrowable@5d3411d>
				// but was: <org.junit.jupiter.api.EnigmaThrowable@2471cca7>

				assertMessageStartsWith(ex, "Unexpected exception type thrown ==> ");
				// The presence of the "@" sign is sufficient to indicate that the hash was
				// generated to disambiguate between the two identical class names.
				assertMessageContains(ex, "expected: <org.junit.jupiter.api.EnigmaThrowable@");
				assertMessageContains(ex, "but was: <org.junit.jupiter.api.EnigmaThrowable@");
			}
		}
	}

	@Test
	void assertThrowsWithThrowingSupplierThatReturns() {
		try {
			assertThrows(EnigmaThrowable.class, () -> 42);
			expectAssertionFailedError();
		}
		catch (AssertionFailedError ex) {
			assertMessageContains(ex, "(returned 42)");
		}
	}

	@Test
	void assertThrowsWithThrowingSupplierThatReturnsNull() {
		try {
			assertThrows(EnigmaThrowable.class, () -> null);
			expectAssertionFailedError();
		}
		catch (AssertionFailedError ex) {
			assertMessageContains(ex, "(returned null)");
		}
	}

	@Test
	void assertThrowsWithThrowingSupplierThatReturnsAndWithCustomMessage() {
		try {
			assertThrows(EnigmaThrowable.class, () -> 42, "custom message");
			expectAssertionFailedError();
		}
		catch (AssertionFailedError ex) {
			assertMessageContains(ex, "(returned 42)");
			assertMessageContains(ex, "custom message");
		}
	}

	@Test
	void assertThrowsWithThrowingSupplierThatReturnsAndWithCustomMessageSupplier() {
		try {
			assertThrows(EnigmaThrowable.class, () -> 42, () -> "custom message");
			expectAssertionFailedError();
		}
		catch (AssertionFailedError ex) {
			assertMessageContains(ex, "(returned 42)");
			assertMessageContains(ex, "custom message");
		}
	}

	// -------------------------------------------------------------------------

	@SuppressWarnings("serial")
	private static class LocalException extends RuntimeException {
	}

	private static class EnigmaClassLoader extends URLClassLoader {

		EnigmaClassLoader() {
			super(new URL[] { EnigmaClassLoader.class.getProtectionDomain().getCodeSource().getLocation() },
				getSystemClassLoader());
		}

		@Override
		public Class<?> loadClass(String name) throws ClassNotFoundException {
			return (EnigmaThrowable.class.getName().equals(name) ? findClass(name) : super.loadClass(name));
		}

	}

}
