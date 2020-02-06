/*
 * Copyright 2015-2020 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * https://www.eclipse.org/legal/epl-v20.html
 */

package org.junit.jupiter.api;

import static org.junit.jupiter.api.AssertionUtils.buildPrefix;
import static org.junit.jupiter.api.AssertionUtils.format;
import static org.junit.jupiter.api.AssertionUtils.getCanonicalName;
import static org.junit.jupiter.api.AssertionUtils.nullSafeGet;

import java.util.function.Supplier;

import org.junit.jupiter.api.function.Executable;
import org.junit.platform.commons.util.BlacklistedExceptions;
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
		return assertThrows(expectedType, executable, (Object) null);
	}

	static <T extends Throwable> T assertThrows(Class<T> expectedType, Executable executable, String message) {
		return assertThrows(expectedType, executable, (Object) message);
	}

	static <T extends Throwable> T assertThrows(Class<T> expectedType, Executable executable,
			Supplier<String> messageSupplier) {

		return assertThrows(expectedType, executable, (Object) messageSupplier);
	}

	@SuppressWarnings("unchecked")
	private static <T extends Throwable> T assertThrows(Class<T> expectedType, Executable executable,
			Object messageOrSupplier) {

		try {
			executable.execute();
		}
		catch (Throwable actualException) {
			if (expectedType.isInstance(actualException)) {
				return (T) actualException;
			}
			else {
				BlacklistedExceptions.rethrowIfBlacklisted(actualException);
				String message = buildPrefix(nullSafeGet(messageOrSupplier))
						+ format(expectedType, actualException.getClass(), "Unexpected exception type thrown");
				throw new AssertionFailedError(message, actualException);
			}
		}

		String message = buildPrefix(nullSafeGet(messageOrSupplier))
				+ String.format("Expected %s to be thrown, but nothing was thrown.", getCanonicalName(expectedType));
		throw new AssertionFailedError(message);
	}

}
