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

import java.util.function.BooleanSupplier;
import java.util.function.Supplier;

import org.junit.gen5.commons.util.StringUtils;
import org.opentest4j.TestAbortedException;

/**
 * Collection of utility methods for conditional test execution based on
 * <em>assumptions</em>.
 *
 * @since 5.0
 * @see TestAbortedException
 */
public final class Assumptions {

	private Assumptions() {
		/* no-op */
	}

	public static void assumeTrue(boolean assumption) {
		assumeTrue(() -> assumption);
	}

	public static void assumeTrue(BooleanSupplier booleanSupplier) {
		assumeTrue(booleanSupplier, () -> "assumption is not true");
	}

	public static void assumeTrue(BooleanSupplier booleanSupplier, String message) {
		assumeTrue(booleanSupplier, () -> message);
	}

	public static void assumeTrue(boolean assumption, Supplier<String> messageSupplier) {
		assumeTrue(() -> assumption, messageSupplier);
	}

	public static void assumeTrue(boolean assumption, String message) {
		assumeTrue(() -> assumption, () -> message);
	}

	public static void assumeTrue(BooleanSupplier booleanSupplier, Supplier<String> messageSupplier) {
		if (!booleanSupplier.getAsBoolean()) {
			throwTestAbortedException(messageSupplier.get());
		}
	}

	public static void assumeFalse(boolean assumption) {
		assumeFalse(() -> assumption);
	}

	public static void assumeFalse(BooleanSupplier booleanSupplier) {
		assumeFalse(booleanSupplier, () -> "assumption is not false");
	}

	public static void assumeFalse(BooleanSupplier booleanSupplier, String message) {
		assumeFalse(booleanSupplier, () -> message);
	}

	public static void assumeFalse(boolean assumption, Supplier<String> messageSupplier) {
		assumeFalse(() -> assumption, messageSupplier);
	}

	public static void assumeFalse(boolean assumption, String message) {
		assumeFalse(() -> assumption, () -> message);
	}

	public static void assumeFalse(BooleanSupplier booleanSupplier, Supplier<String> messageSupplier) {
		if (booleanSupplier.getAsBoolean()) {
			throwTestAbortedException(messageSupplier.get());
		}
	}

	public static void assumingThat(BooleanSupplier booleanSupplier, Executable executable) {
		assumingThat(booleanSupplier.getAsBoolean(), executable);
	}

	public static void assumingThat(boolean assumption, Executable executable) {
		if (assumption) {
			try {
				executable.execute();
			}
			catch (Error | RuntimeException ex) {
				// rethrow
				throw ex;
			}
			catch (Throwable ex) {
				throw new RuntimeException("Wrapped checked exception thrown from Executable", ex);
			}
		}
	}

	private static void throwTestAbortedException(String message) {
		throw new TestAbortedException(
			StringUtils.isNotBlank(message) ? ("Assumption failed: " + message) : "Assumption failed");
	}

}
