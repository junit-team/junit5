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

import java.util.function.Supplier;

import org.junit.gen5.commons.util.StringUtils;
import org.opentestalliance.TestAbortedException;

/**
 * Collection of utility methods for conditional test execution based on
 * <em>assumptions</em>.
 *
 * @author Sam Brannen
 * @since 5.0
 * @see TestAbortedException
 */
public final class Assumptions {

	private Assumptions() {
		/* no-op */
	}

	public static void assumeTrue(boolean condition) {
		if (!condition) {
			throwTestAbortedException("condition is not true");
		}
	}

	public static void assumeTrue(boolean condition, String message) {
		if (!condition) {
			throwTestAbortedException(message);
		}
	}

	public static void assumeTrue(boolean condition, Supplier<String> messageSupplier) {
		if (!condition) {
			throwTestAbortedException(messageSupplier.get());
		}
	}

	public static void assumeFalse(boolean condition) {
		if (condition) {
			throwTestAbortedException("condition is not false");
		}
	}

	public static void assumeFalse(boolean condition, String message) {
		if (condition) {
			throwTestAbortedException(message);
		}
	}

	public static void assumeFalse(boolean condition, Supplier<String> messageSupplier) {
		if (condition) {
			throwTestAbortedException(messageSupplier.get());
		}
	}

	public static void assumingThat(boolean condition, Executable executable) {
		if (condition) {
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
