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

import static org.junit.gen5.api.Assertions.assertEquals;
import static org.junit.gen5.api.Assertions.assertTrue;
import static org.junit.gen5.api.Assumptions.assumeFalse;
import static org.junit.gen5.api.Assumptions.assumeTrue;
import static org.junit.gen5.api.Assumptions.assumingThat;

import java.util.ArrayList;
import java.util.List;

import org.opentest4j.TestAbortedException;

/**
 * Unit tests for JUnit 5 {@link Assumptions}.
 *
 * @since 5.0
 */
public class AssumptionsTests {

	// --- assumeTrue ----------------------------------------------------

	@Test
	void assumeTrueWithBooleanTrue() {
		assumeTrue(true);
	}

	@Test
	void assumeTrueWithBooleanSupplierTrue() {
		assumeTrue(() -> true);
	}

	@Test
	void assumeTrueWithBooleanTrueAndStringMessage() {
		assumeTrue(true, "true");
	}

	@Test
	void assumeTrueWithBooleanSupplierTrueAndMessageSupplier() {
		assumeTrue(() -> true, () -> "true");
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
	void assumeTrueWithBooleanSupplierFalseAndMessageSupplier() {
		assertAssumptionFailure("test", () -> assumeTrue(() -> false, () -> "test"));
	}

	// --- assumeFalse ----------------------------------------------------

	@Test
	void assumeFalseWithBooleanFalse() {
		assumeFalse(false);
	}

	@Test
	void assumeFalseWithBooleanSupplierFalse() {
		assumeFalse(() -> false);
	}

	@Test
	void assumeFalseWithBooleanFalseAndStringMessage() {
		assumeFalse(false, "false");
	}

	@Test
	void assumeFalseWithBooleanSupplierFalseAndMessageSupplier() {
		assumeFalse(() -> false, () -> "false");
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
	void assumeFalseWithBooleanSupplierTrueAndMessageSupplier() {
		assertAssumptionFailure("test", () -> assumeFalse(() -> true, () -> "test"));
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

	// -------------------------------------------------------------------

	private static void assertAssumptionFailure(String msg, Executable executable) {
		try {
			executable.execute();
			expectTestAbortedException();
		}
		catch (Throwable ex) {
			assertTrue(ex instanceof TestAbortedException);
			assertMessageEquals((TestAbortedException) ex, "Assumption failed: " + msg);
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

}
