/*
 * Copyright 2015 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.junit.gen5.api;

import static org.junit.gen5.api.Assertions.assertFalse;
import static org.junit.gen5.api.Assertions.assertTrue;
import static org.junit.gen5.api.Assertions.fail;

import java.util.function.Supplier;

import org.opentest4j.AssertionFailedError;

/**
 * Unit tests for {@link Assertions}.
 *
 * @since 5.0
 */
// TODO Fix DependencyTests so that JUnit5 Runner can be used.
// @RunWith(JUnit5.class)
public class AssertionsTests {

	@org.junit.Test
	public void failWithString() {
		try {
			fail("test");
			expectAssertionFailedError();
		}
		catch (AssertionFailedError ex) {
			assertMessageEquals(ex, "test");
		}
	}

	@org.junit.Test
	public void failWithMessageSupplier() {
		try {
			fail(() -> "test");
			expectAssertionFailedError();
		}
		catch (AssertionFailedError ex) {
			assertMessageEquals(ex, "test");
		}
	}

	@org.junit.Test
	public void failWithNullString() {
		try {
			fail((String) null);
			expectAssertionFailedError();
		}
		catch (AssertionFailedError ex) {
			assertMessageIsNull(ex);
		}
	}

	@org.junit.Test
	public void failWithNullMessageSupplier() {
		try {
			fail((Supplier<String>) null);
			expectAssertionFailedError();
		}
		catch (AssertionFailedError ex) {
			assertMessageIsNull(ex);
		}
	}

	@org.junit.Test
	public void assertTrueWithBooleanTrue() {
		assertTrue(true);
	}

	@org.junit.Test
	public void assertTrueWithBooleanSupplierTrue() {
		assertTrue(() -> true);
	}

	@org.junit.Test
	public void assertTrueWithBooleanTrueAndString() {
		assertTrue(true, "test");
	}

	@org.junit.Test
	public void assertTrueWithBooleanFalse() {
		try {
			assertTrue(false);
			expectAssertionFailedError();
		}
		catch (AssertionFailedError ex) {
			assertMessageIsNull(ex);
		}
	}

	@org.junit.Test
	public void assertTrueWithBooleanFalseAndString() {
		try {
			assertTrue(false, "test");
			expectAssertionFailedError();
		}
		catch (AssertionFailedError ex) {
			assertMessageEquals(ex, "test");
		}
	}

	@org.junit.Test
	public void assertTrueWithBooleanSupplierFalseAndMessageSupplier() {
		try {
			assertTrue(() -> false, () -> "test");
			expectAssertionFailedError();
		}
		catch (AssertionFailedError ex) {
			assertMessageEquals(ex, "test");
		}
	}

	@org.junit.Test
	public void assertFalseWithBooleanFalse() {
		assertFalse(false);
	}

	@org.junit.Test
	public void assertFalseWithBooleanTrueAndString() {
		try {
			assertFalse(true, "test");
			expectAssertionFailedError();
		}
		catch (AssertionFailedError ex) {
			assertMessageEquals(ex, "test");
		}
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

}
