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
import org.opentest4j.AssertionFailedError;

/**
 * {@code AssertThrows} is a collection of utility methods that support asserting
 * an exception of an expected type is thrown.
 *
 * @since 5.0
 */
class AssertThrows {

	///CLOVER:OFF
	private AssertThrows() {
		/* no-op */
	}
	///CLOVER:ON

	static <T extends Throwable> T assertThrows(Class<T> expectedType, ThrowingSupplier<?> supplier) {
		return assertThrows(expectedType, supplier::get, (Object) null);
	}

	static <T extends Throwable> T assertThrows(Class<T> expectedType, Executable executable) {
		return assertThrows(expectedType, asSupplier(executable), (Object) null);
	}

	static <T extends Throwable> T assertThrows(Class<T> expectedType, ThrowingSupplier<?> supplier, String message) {
		return assertThrows(expectedType, supplier::get, (Object) message);
	}

	static <T extends Throwable> T assertThrows(Class<T> expectedType, Executable executable, String message) {
		return assertThrows(expectedType, asSupplier(executable), (Object) message);
	}

	static <T extends Throwable> T assertThrows(Class<T> expectedType, ThrowingSupplier<?> supplier,
			Supplier<String> messageSupplier) {
		return assertThrows(expectedType, supplier::get, (Object) messageSupplier);
	}

	static <T extends Throwable> T assertThrows(Class<T> expectedType, Executable executable,
			Supplier<String> messageSupplier) {
		return assertThrows(expectedType, asSupplier(executable), (Object) messageSupplier);
	}

	@SuppressWarnings("unchecked")
	private static <T extends Throwable> T assertThrows(Class<T> expectedType, ThrowingResultSupplier<?> supplier,
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

		String message = buildPrefix(nullSafeGet(messageOrSupplier))
				+ String.format("Expected %s to be thrown, but nothing was thrown", getCanonicalName(expectedType))
				+ (supplier.formatResult() ? String.format(" (returned %s).", result) : ".");
		throw new AssertionFailedError(message);
	}

	private interface ThrowingResultSupplier<T> extends ThrowingSupplier<T> {
		/**
		 * Returns true if the result should be included in the failure message in the case where the supplier
		 * returns a result instead of throwing the expected exception.
		 */
		default boolean formatResult() {
			return true;
		}
	}

	private static ThrowingResultSupplier<Void> asSupplier(Executable executable) {
		return new ThrowingResultSupplier<Void>() {
			@Override
			public Void get() throws Throwable {
				executable.execute();
				return null;
			}

			@Override
			public boolean formatResult() {
				return false;
			}
		};
	}

}
