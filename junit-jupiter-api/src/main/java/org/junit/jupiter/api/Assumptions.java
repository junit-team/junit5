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

import static org.junit.platform.commons.meta.API.Usage.Experimental;
import static org.junit.platform.commons.meta.API.Usage.Maintained;

import java.util.function.BooleanSupplier;
import java.util.function.Supplier;

import org.junit.jupiter.api.function.Executable;
import org.junit.platform.commons.meta.API;
import org.junit.platform.commons.util.ExceptionUtils;
import org.junit.platform.commons.util.StringUtils;
import org.opentest4j.TestAbortedException;

/**
 * {@code Assumptions} is a collection of utility methods that support
 * conditional test execution based on <em>assumptions</em>.
 *
 * <p>In direct contrast to failed {@linkplain Assertions assertions},
 * failed assumptions do not result in a test <em>failure</em>; rather,
 * a failed assumption results in a test being <em>aborted</em>.
 *
 * <p>Assumptions are typically used whenever it does not make sense to
 * continue execution of a given test method &mdash; for example, if the
 * test depends on something that does not exist in the current runtime
 * environment.
 *
 * @since 5.0
 * @see TestAbortedException
 * @see Assertions
 */
@API(Maintained)
public final class Assumptions {

	private Assumptions() {
		/* no-op */
	}

	// --- assumeTrue ----------------------------------------------------

	/**
	 * Validate the given assumption.
	 *
	 * @param assumption the assumption to validate
	 * @throws TestAbortedException if the assumption is not {@code true}
	 */
	public static void assumeTrue(boolean assumption) throws TestAbortedException {
		assumeTrue(() -> assumption);
	}

	/**
	 * Validate the given assumption.
	 *
	 * @param assumptionSupplier the supplier of the assumption to validate
	 * @throws TestAbortedException if the assumption is not {@code true}
	 */
	public static void assumeTrue(BooleanSupplier assumptionSupplier) throws TestAbortedException {
		assumeTrue(assumptionSupplier, () -> "assumption is not true");
	}

	/**
	 * Validate the given assumption.
	 *
	 * @param assumptionSupplier the supplier of the assumption to validate
	 * @param message the message to be included in the {@code TestAbortedException}
	 * if the assumption is invalid
	 * @throws TestAbortedException if the assumption is not {@code true}
	 */
	public static void assumeTrue(BooleanSupplier assumptionSupplier, String message) throws TestAbortedException {
		assumeTrue(assumptionSupplier, () -> message);
	}

	/**
	 * Validate the given assumption.
	 *
	 * @param assumption the assumption to validate
	 * @param messageSupplier the supplier of the message to be included in
	 * the {@code TestAbortedException} if the assumption is invalid
	 * @throws TestAbortedException if the assumption is not {@code true}
	 */
	public static void assumeTrue(boolean assumption, Supplier<String> messageSupplier) throws TestAbortedException {
		assumeTrue(() -> assumption, messageSupplier);
	}

	/**
	 * Validate the given assumption.
	 *
	 * @param assumption the assumption to validate
	 * @param message the message to be included in the {@code TestAbortedException}
	 * if the assumption is invalid
	 * @throws TestAbortedException if the assumption is not {@code true}
	 */
	public static void assumeTrue(boolean assumption, String message) throws TestAbortedException {
		assumeTrue(() -> assumption, () -> message);
	}

	/**
	 * Validate the given assumption.
	 *
	 * @param assumptionSupplier the supplier of the assumption to validate
	 * @param messageSupplier the supplier of the message to be included in
	 * the {@code TestAbortedException} if the assumption is invalid
	 * @throws TestAbortedException if the assumption is not {@code true}
	 */
	public static void assumeTrue(BooleanSupplier assumptionSupplier, Supplier<String> messageSupplier)
			throws TestAbortedException {

		if (!assumptionSupplier.getAsBoolean()) {
			throwTestAbortedException(messageSupplier.get());
		}
	}

	// --- assumeFalse ----------------------------------------------------

	/**
	 * Validate the given assumption.
	 *
	 * @param assumption the assumption to validate
	 * @throws TestAbortedException if the assumption is not {@code false}
	 */
	public static void assumeFalse(boolean assumption) throws TestAbortedException {
		assumeFalse(() -> assumption);
	}

	/**
	 * Validate the given assumption.
	 *
	 * @param assumptionSupplier the supplier of the assumption to validate
	 * @throws TestAbortedException if the assumption is not {@code false}
	 */
	public static void assumeFalse(BooleanSupplier assumptionSupplier) throws TestAbortedException {
		assumeFalse(assumptionSupplier, () -> "assumption is not false");
	}

	/**
	 * Validate the given assumption.
	 *
	 * @param assumptionSupplier the supplier of the assumption to validate
	 * @param message the message to be included in the {@code TestAbortedException}
	 * if the assumption is invalid
	 * @throws TestAbortedException if the assumption is not {@code false}
	 */
	public static void assumeFalse(BooleanSupplier assumptionSupplier, String message) throws TestAbortedException {
		assumeFalse(assumptionSupplier, () -> message);
	}

	/**
	 * Validate the given assumption.
	 *
	 * @param assumption the assumption to validate
	 * @param messageSupplier the supplier of the message to be included in
	 * the {@code TestAbortedException} if the assumption is invalid
	 * @throws TestAbortedException if the assumption is not {@code false}
	 */
	public static void assumeFalse(boolean assumption, Supplier<String> messageSupplier) throws TestAbortedException {
		assumeFalse(() -> assumption, messageSupplier);
	}

	/**
	 * Validate the given assumption.
	 *
	 * @param assumption the assumption to validate
	 * @param message the message to be included in the {@code TestAbortedException}
	 * if the assumption is invalid
	 * @throws TestAbortedException if the assumption is not {@code false}
	 */
	public static void assumeFalse(boolean assumption, String message) throws TestAbortedException {
		assumeFalse(() -> assumption, () -> message);
	}

	/**
	 * Validate the given assumption.
	 *
	 * @param assumptionSupplier the supplier of the assumption to validate
	 * @param messageSupplier the supplier of the message to be included in
	 * the {@code TestAbortedException} if the assumption is invalid
	 * @throws TestAbortedException if the assumption is not {@code false}
	 */
	public static void assumeFalse(BooleanSupplier assumptionSupplier, Supplier<String> messageSupplier)
			throws TestAbortedException {

		if (assumptionSupplier.getAsBoolean()) {
			throwTestAbortedException(messageSupplier.get());
		}
	}

	// --- assumingThat --------------------------------------------------

	/**
	 * Execute the supplied {@link Executable}, but only if the supplied
	 * assumption is valid.
	 *
	 * <p>If the assumption is invalid, this method does nothing.
	 *
	 * <p>If the {@code executable} throws an exception, it will be rethrown
	 * <em>as is</em> but  {@link ExceptionUtils#throwAsUncheckedException masked}
	 * as an unchecked exception.
	 *
	 * @param assumptionSupplier the supplier of the assumption to validate
	 * @param executable the block of code to execute if the assumption is valid
	 * @see #assumingThat(boolean, Executable)
	 */
	@API(Experimental)
	public static void assumingThat(BooleanSupplier assumptionSupplier, Executable executable) {
		assumingThat(assumptionSupplier.getAsBoolean(), executable);
	}

	/**
	 * Execute the supplied {@link Executable}, but only if the supplied
	 * assumption is valid.
	 *
	 * <p>If the assumption is invalid, this method does nothing.
	 *
	 * <p>If the {@code executable} throws an exception, it will be rethrown
	 * <em>as is</em> but  {@link ExceptionUtils#throwAsUncheckedException masked}
	 * as an unchecked exception.
	 *
	 * @param assumption the assumption to validate
	 * @param executable the block of code to execute if the assumption is valid
	 * @see #assumingThat(BooleanSupplier, Executable)
	 */
	@API(Experimental)
	public static void assumingThat(boolean assumption, Executable executable) {
		if (assumption) {
			try {
				executable.execute();
			}
			catch (Throwable t) {
				ExceptionUtils.throwAsUncheckedException(t);
			}
		}
	}

	private static void throwTestAbortedException(String message) {
		throw new TestAbortedException(
			StringUtils.isNotBlank(message) ? ("Assumption failed: " + message) : "Assumption failed");
	}

}
