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

import static org.junit.jupiter.api.AssertionUtils.buildPrefix;
import static org.junit.jupiter.api.AssertionUtils.format;
import static org.junit.jupiter.api.AssertionUtils.getCanonicalName;
import static org.junit.jupiter.api.AssertionUtils.nullSafeGet;

import java.util.function.Supplier;

import org.junit.jupiter.api.function.Executable;
import org.junit.jupiter.api.function.ThrowingSupplier;
import org.junit.platform.commons.util.StringUtils;
import org.opentest4j.AssertionFailedError;

/**
 * {@code AssertThrows} is a collection of utility methods that support asserting
 * an exception of an expected type is thrown.
 *
 * @since 5.0
 */
class AssertThrows {

	private AssertThrows() {
		/* no-op */
	}

	static <T extends Throwable> T assertThrows(Class<T> expectedType, Executable executable) {
		return assertThrows(expectedType, asSupplier(executable), (Object) null);
	}

	static <T extends Throwable> T assertThrows(Class<T> expectedType, Executable executable, String message) {
		return assertThrows(expectedType, asSupplier(executable), (Object) message);
	}

	static <T extends Throwable> T assertThrows(Class<T> expectedType, Executable executable,
			Supplier<String> messageSupplier) {

		return assertThrows(expectedType, asSupplier(executable), (Object) messageSupplier);
	}

	/**
	 * @since 5.3
	 */
	static <T extends Throwable> T assertThrows(Class<T> expectedType, ThrowingSupplier<?> supplier) {
		return assertThrows(expectedType, supplier::get, (Object) null);
	}

	/**
	 * @since 5.3
	 */
	static <T extends Throwable> T assertThrows(Class<T> expectedType, ThrowingSupplier<?> supplier, String message) {
		return assertThrows(expectedType, supplier::get, (Object) message);
	}

	/**
	 * @since 5.3
	 */
	static <T extends Throwable> T assertThrows(Class<T> expectedType, ThrowingSupplier<?> supplier,
			Supplier<String> messageSupplier) {

		return assertThrows(expectedType, supplier::get, (Object) messageSupplier);
	}

	@SuppressWarnings("unchecked")
	private static <T extends Throwable> T assertThrows(Class<T> expectedType, ResultAwareThrowingSupplier<?> supplier,
			Object messageOrSupplier) {

		Object result;
		try {
			result = supplier.get();
		}
		catch (Throwable actualException) {
			if (expectedType.isInstance(actualException)) {
				return (T) actualException;
			}
			else {
				String message = buildPrefix(nullSafeGet(messageOrSupplier))
						+ format(expectedType, actualException.getClass(), "Unexpected exception type thrown");
				throw new AssertionFailedError(message, actualException);
			}
		}

		String includedResult = supplier.includeResult()
				? String.format(" (returned %s).", StringUtils.nullSafeToString(result))
				: ".";
		String message = buildPrefix(nullSafeGet(messageOrSupplier))
				+ String.format("Expected %s to be thrown, but nothing was thrown", getCanonicalName(expectedType))
				+ includedResult;
		throw new AssertionFailedError(message);
	}

	private interface ResultAwareThrowingSupplier<T> extends ThrowingSupplier<T> {

		/**
		 * Determine if the result of invoking {@link #get()} should be included
		 * in the assertion failure message if this supplier returns an actual
		 * result instead of throwing an exception.
		 *
		 * @return {@code true} by default; can be overridden in concrete implementations
		 */
		default boolean includeResult() {
			return true;
		}
	}

	private static ResultAwareThrowingSupplier<Void> asSupplier(Executable executable) {
		return new ResultAwareThrowingSupplierAdapter(executable);
	}

	/**
	 * Adapts an {@link Executable} to the {@link ResultAwareThrowingSupplier} API.
	 */
	private static class ResultAwareThrowingSupplierAdapter implements ResultAwareThrowingSupplier<Void> {

		private final Executable executable;

		ResultAwareThrowingSupplierAdapter(Executable executable) {
			this.executable = executable;
		}

		@Override
		public Void get() throws Throwable {
			executable.execute();
			return null;
		}

		@Override
		public boolean includeResult() {
			return false;
		}
	}

}
