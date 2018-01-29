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
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

import java.io.IOException;
import java.net.URLClassLoader;

import org.junit.jupiter.api.function.Executable;
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
	void assertThrowsThrowable() {
		EnigmaThrowable enigmaThrowable = assertThrows(EnigmaThrowable.class, () -> {
			throw new EnigmaThrowable();
		});
		assertNotNull(enigmaThrowable);
	}

	@Test
	void assertThrowsThrowableWithMessage() {
		EnigmaThrowable enigmaThrowable = assertThrows(EnigmaThrowable.class, () -> {
			throw new EnigmaThrowable();
		}, "message");
		assertNotNull(enigmaThrowable);
	}

	@Test
	void assertThrowsThrowableWithMessageSupplier() {
		EnigmaThrowable enigmaThrowable = assertThrows(EnigmaThrowable.class, () -> {
			throw new EnigmaThrowable();
		}, () -> "message");
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
			assertThrows(IllegalStateException.class, nix);
			expectAssertionFailedError();
		}
		catch (AssertionFailedError ex) {
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
			assertThrows(IllegalStateException.class, () -> {
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
			assertThrows(IllegalStateException.class, () -> {
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
			assertThrows(IllegalStateException.class, () -> {
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
			assertThrows(IllegalStateException.class, () -> {
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
			assertThrows(IllegalStateException.class, () -> {
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
				assertThrows(enigmaThrowableClass, () -> {
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

	@SuppressWarnings("serial")
	private static class LocalException extends RuntimeException {
	}

	private static class EnigmaClassLoader extends URLClassLoader {

		EnigmaClassLoader() {
			super(getUrlClassLoader().getURLs());
		}

		private static URLClassLoader getUrlClassLoader() {
			ClassLoader systemClassLoader = getSystemClassLoader();
			assumeTrue(systemClassLoader instanceof URLClassLoader,
				"aborting test since system ClassLoader is not a URLClassLoader");
			return (URLClassLoader) systemClassLoader;
		}

		@Override
		public Class<?> loadClass(String name) throws ClassNotFoundException {
			return (EnigmaThrowable.class.getName().equals(name) ? findClass(name) : super.loadClass(name));
		}

	}

}
