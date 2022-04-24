/*
 * Copyright 2015-2022 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * https://www.eclipse.org/legal/epl-v20.html
 */

package org.junit.jupiter.api;

import static org.junit.jupiter.api.AssertionTestUtils.assertMessageEquals;
import static org.junit.jupiter.api.AssertionTestUtils.expectAssertionFailedError;
import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assumptions.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.FutureTask;

import org.junit.jupiter.api.function.Executable;
import org.junit.jupiter.api.function.ThrowingSupplier;
import org.opentest4j.AssertionFailedError;
import org.opentest4j.TestAbortedException;

/**
 * Unit tests for JUnit Jupiter {@link Assumptions}.
 *
 * @since 5.0
 */
class AssumptionsTests {

	private static final Executable nix = () -> {
	};

	private static final ThrowingSupplier<String> something = () -> "enigma";

	// --- assumeTrue ----------------------------------------------------

	@Test
	void assumeTrueWithBooleanTrue() {
		String foo = null;
		try {
			assumeTrue(true);
			assumeTrue(true, "message");
			assumeTrue(true, () -> "message");
			foo = "foo";
		}
		finally {
			assertNotNull(foo);
		}
	}

	@Test
	void assumeTrueWithBooleanSupplierTrue() {
		String foo = null;
		try {
			assumeTrue(() -> true);
			assumeTrue(() -> true, "message");
			assumeTrue(() -> true, () -> "message");
			foo = "foo";
		}
		finally {
			assertNotNull(foo);
		}
	}

	@Test
	void assumeTrueWithBooleanFalse() {
		assertAssumptionFailure("assumption is not true", () -> assumeTrue(false));
	}

	@Test
	void assumeTrueWithBooleanSupplierFalse() {
		assertAssumptionFailure("assumption is not true", () -> assumeTrue(() -> false));
	}

	@Test
	void assumeTrueWithBooleanFalseAndStringMessage() {
		assertAssumptionFailure("test", () -> assumeTrue(false, "test"));
	}

	@Test
	void assumeTrueWithBooleanFalseAndNullStringMessage() {
		assertAssumptionFailure(null, () -> assumeTrue(false, (String) null));
	}

	@Test
	void assumeTrueWithBooleanSupplierFalseAndStringMessage() {
		assertAssumptionFailure("test", () -> assumeTrue(() -> false, "test"));
	}

	@Test
	void assumeTrueWithBooleanSupplierFalseAndMessageSupplier() {
		assertAssumptionFailure("test", () -> assumeTrue(() -> false, () -> "test"));
	}

	@Test
	void assumeTrueWithBooleanFalseAndMessageSupplier() {
		assertAssumptionFailure("test", () -> assumeTrue(false, () -> "test"));
	}

	// --- assumeFalse ----------------------------------------------------

	@Test
	void assumeFalseWithBooleanFalse() {
		String foo = null;
		try {
			assumeFalse(false);
			assumeFalse(false, "message");
			assumeFalse(false, () -> "message");
			foo = "foo";
		}
		finally {
			assertNotNull(foo);
		}
	}

	@Test
	void assumeFalseWithBooleanSupplierFalse() {
		String foo = null;
		try {
			assumeFalse(() -> false);
			assumeFalse(() -> false, "message");
			assumeFalse(() -> false, () -> "message");
			foo = "foo";
		}
		finally {
			assertNotNull(foo);
		}
	}

	@Test
	void assumeFalseWithBooleanTrue() {
		assertAssumptionFailure("assumption is not false", () -> assumeFalse(true));
	}

	@Test
	void assumeFalseWithBooleanSupplierTrue() {
		assertAssumptionFailure("assumption is not false", () -> assumeFalse(() -> true));
	}

	@Test
	void assumeFalseWithBooleanTrueAndStringMessage() {
		assertAssumptionFailure("test", () -> assumeFalse(true, "test"));
	}

	@Test
	void assumeFalseWithBooleanSupplierTrueAndMessage() {
		assertAssumptionFailure("test", () -> assumeFalse(() -> true, "test"));
	}

	@Test
	void assumeFalseWithBooleanSupplierTrueAndMessageSupplier() {
		assertAssumptionFailure("test", () -> assumeFalse(() -> true, () -> "test"));
	}

	@Test
	void assumeFalseWithBooleanTrueAndMessageSupplier() {
		assertAssumptionFailure("test", () -> assumeFalse(true, () -> "test"));
	}

	// --- assumingThat --------------------------------------------------

	@Test
	void assumingThatWithBooleanTrue() {
		List<String> list = new ArrayList<>();
		assumingThat(true, () -> list.add("test"));
		assertEquals(1, list.size());
		assertEquals("test", list.get(0));
	}

	@Test
	void assumingThatWithBooleanSupplierTrue() {
		List<String> list = new ArrayList<>();
		assumingThat(() -> true, () -> list.add("test"));
		assertEquals(1, list.size());
		assertEquals("test", list.get(0));
	}

	@Test
	void assumingThatWithBooleanFalse() {
		List<String> list = new ArrayList<>();
		assumingThat(false, () -> list.add("test"));
		assertEquals(0, list.size());
	}

	@Test
	void assumingThatWithBooleanSupplierFalse() {
		List<String> list = new ArrayList<>();
		assumingThat(() -> false, () -> list.add("test"));
		assertEquals(0, list.size());
	}

	@Test
	void assumingThatWithFailingExecutable() {
		assertThrows(EnigmaThrowable.class, () -> assumingThat(true, () -> {
			throw new EnigmaThrowable();
		}));
	}

	// --- assummeDoesNotThrow --------------------------------------------------
	@Test
	void assumeDoesNotThrowWithMethodReferenceForNonVoidReturnType() {
		FutureTask<String> future = new FutureTask<>(() -> {
			return "foo";
		});
		future.run();

		String result;

		// Current compiler's type inference: does NOT compile since the compiler
		// cannot figure out which overloaded variant of assumeDoesNotThrow() to
		// invoke (i.e., Executable vs. ThrowingSupplier).
		//
		// result = assumeDoesNotThrow(future::get);

		// Explicitly as an Executable
		assumeDoesNotThrow((Executable) future::get);

		// Explicitly as a ThrowingSupplier
		result = assumeDoesNotThrow((ThrowingSupplier<String>) future::get);
		assertEquals("foo", result);
	}

	@Test
	void assumeDoesNotThrowWithMethodReferenceForVoidReturnType() {
		var foo = new Foo();

		// Note: the following does not compile since the compiler cannot properly
		// perform type inference for a method reference for an overloaded method
		// that has a void return type such as Foo.overloaded(...), IFF the
		// compiler is simultaneously trying to pick which overloaded variant
		// of assumeDoesNotThrow() to invoke.
		//
		// assumeDoesNotThrow(foo::overloaded);

		// Current compiler's type inference
		assumeDoesNotThrow(foo::normalMethod);
		// Explicitly as an Executable
		assumeDoesNotThrow((Executable) foo::normalMethod);
		assumeDoesNotThrow((Executable) foo::overloaded);
	}

	// --- executable ----------------------------------------------------------

	@Test
	void assumeDoesNotThrowAnythingWithExecutable() {
		assumeDoesNotThrow(nix);
	}

	@Test
	void assumeDoesNotThrowAnythingWithExecutableAndMessage() {
		assumeDoesNotThrow(nix, "message");
	}

	@Test
	void assumeDoesNotThrowAnythingWithExecutableAndMessageSupplier() {
		assumeDoesNotThrow(nix, () -> "message");
	}

	@Test
	void assumeDoesNotThrowWithExecutableThatThrowsACheckedException() {
		try {
			assumeDoesNotThrow((Executable) () -> {
				throw new IOException();
			});
			expectTestAbortedException();
		}
		catch (TestAbortedException ex) {
			assertMessageEquals(ex, "Assumption failed");
		}
	}

	@Test
	void assumeDoesNotThrowWithExecutableThatThrowsACheckedExceptionWithMessage() {
		String message = "Checked exception message";
		try {
			assumeDoesNotThrow((Executable) () -> {
				throw new IOException(message);
			});
			expectTestAbortedException();
		}
		catch (TestAbortedException ex) {
			assertMessageEquals(ex, "Assumption failed");
		}
	}

	@Test
	void assumeDoesNotThrowWithExecutableThatThrowsARuntimeException() {
		try {
			assumeDoesNotThrow((Executable) () -> {
				throw new IllegalStateException();
			});
			expectTestAbortedException();
		}
		catch (TestAbortedException ex) {
			assertMessageEquals(ex, "Assumption failed");
		}
	}

	@Test
	void assumeDoesNotThrowWithExecutableThatThrowsARuntimeExceptionWithMessage() {
		String message = "Runtime exception message";
		try {
			assumeDoesNotThrow((Executable) () -> {
				throw new IllegalStateException(message);
			});
			expectTestAbortedException();
		}
		catch (TestAbortedException ex) {
			assertMessageEquals(ex, "Assumption failed");
		}
	}

	@Test
	void assumeDoesNotThrowWithExecutableThatThrowsAnError() {
		try {
			assumeDoesNotThrow((Executable) AssertionTestUtils::recurseIndefinitely);
			expectTestAbortedException();
		}
		catch (TestAbortedException ex) {
			assertMessageEquals(ex, "Assumption failed");
		}
	}

	@Test
	void assumeDoesNotThrowWithExecutableThatThrowsAnExceptionWithMessageString() {
		try {
			assumeDoesNotThrow((Executable) () -> {
				throw new IllegalStateException();
			}, "Custom message");
			expectTestAbortedException();
		}
		catch (TestAbortedException ex) {
			assertMessageEquals(ex,
					"Assumption failed: Custom message");
		}
	}

	@Test
	void assumeDoesNotThrowWithExecutableThatThrowsAnExceptionWithMessageWithMessageString() {
		String message = "Runtime exception message";
		try {
			assumeDoesNotThrow((Executable) () -> {
				throw new IllegalStateException(message);
			}, "Custom message");
			expectTestAbortedException();
		}
		catch (TestAbortedException ex) {
			assertMessageEquals(ex, "Assumption failed: Custom message");
		}
	}

	@Test
	void assumeDoesNotThrowWithExecutableThatThrowsAnExceptionWithMessageSupplier() {
		try {
			assumeDoesNotThrow((Executable) () -> {
				throw new IllegalStateException();
			}, () -> "Custom message");
			expectTestAbortedException();
		}
		catch (TestAbortedException ex) {
			assertMessageEquals(ex, "Assumption failed: Custom message");
		}
	}

	@Test
	void assumeDoesNotThrowWithExecutableThatThrowsAnExceptionWithMessageWithMessageSupplier() {
		String message = "Runtime exception message";
		try {
			assumeDoesNotThrow((Executable) () -> {
				throw new IllegalStateException(message);
			}, () -> "Custom message");
			expectTestAbortedException();
		}
		catch (TestAbortedException ex) {
			assertMessageEquals(ex, "Assumption failed: Custom message");
		}
	}




	// --- supplier ------------------------------------------------------------
	@Test
	void assumeDoesNotThrowAnythingWithSupplier() {
		assertEquals("enigma", assumeDoesNotThrow(something));
	}

	@Test
	void assumeDoesNotThrowAnythingWithSupplierAndMessage() {
		assertEquals("enigma", assumeDoesNotThrow(something, "message"));
	}

	@Test
	void assumeDoesNotThrowAnythingWithSupplierAndMessageSupplier() {
		assertEquals("enigma", assumeDoesNotThrow(something, () -> "message"));
	}

	@Test
	void assumeDoesNotThrowWithSupplierThatThrowsACheckedException() {
		try {
			assumeDoesNotThrow((ThrowingSupplier<?>) () -> {
				throw new IOException();
			});
			expectTestAbortedException();
		}
		catch (TestAbortedException ex) {
			assertMessageEquals(ex, "Assumption failed");
		}
	}

	@Test
	void assumeDoesNotThrowWithSupplierThatThrowsARuntimeException() {
		try {
			assumeDoesNotThrow((ThrowingSupplier<?>) () -> {
				throw new IllegalStateException();
			});
			expectTestAbortedException();
		}
		catch (TestAbortedException ex) {
			assertMessageEquals(ex, "Assumption failed");
		}
	}

	@Test
	void assumeDoesNotThrowWithSupplierThatThrowsAnError() {
		try {
			assumeDoesNotThrow((ThrowingSupplier<?>) () -> {
				throw new StackOverflowError();
			});
			expectTestAbortedException();
		}
		catch (TestAbortedException ex) {
			assertMessageEquals(ex, "Assumption failed");
		}
	}

	@Test
	void assumeDoesNotThrowWithSupplierThatThrowsAnExceptionWithMessageString() {
		try {
			assumeDoesNotThrow((ThrowingSupplier<?>) () -> {
				throw new IllegalStateException();
			}, "Custom message");
			expectTestAbortedException();
		}
		catch (TestAbortedException ex) {
			assertMessageEquals(ex, "Assumption failed: Custom message");
		}
	}

	@Test
	void assumeDoesNotThrowWithSupplierThatThrowsAnExceptionWithMessageSupplier() {
		try {
			assumeDoesNotThrow((ThrowingSupplier<?>) () -> {
				throw new IllegalStateException();
			}, () -> "Custom message");
			expectTestAbortedException();
		}
		catch (TestAbortedException ex) {
			assertMessageEquals(ex, "Assumption failed: Custom message");
		}
	}

	// -------------------------------------------------------------------

	private static void assertAssumptionFailure(String msg, Executable executable) {
		try {
			executable.execute();
			expectTestAbortedException();
		}
		catch (Throwable ex) {
			assertTrue(ex instanceof TestAbortedException);
			assertMessageEquals((TestAbortedException) ex,
					msg == null ? "Assumption failed" : "Assumption failed: " + msg);
		}
	}

	private static void expectTestAbortedException() {
		throw new AssertionError("Should have thrown a " + TestAbortedException.class.getName());
	}

	private static void assertMessageEquals(TestAbortedException ex, String msg) throws AssertionError {
		if (!msg.equals(ex.getMessage())) {
			throw new AssertionError(
					"Message in TestAbortedException should be [" + msg + "], but was [" + ex.getMessage() + "].");
		}
	}

	// -------------------------------------------------------------------------

	private static class Foo {

		void normalMethod() {
		}

		void overloaded() {
		}

		@SuppressWarnings("unused")
		void overloaded(int i) {
		}

	}

}
