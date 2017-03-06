/*
 * Copyright 2015-2017 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.junit.jupiter.api;

import static org.junit.jupiter.api.AssertionUtils.format;
import static org.junit.jupiter.api.AssertionUtils.getCanonicalName;

import org.junit.jupiter.api.function.Executable;
import org.opentest4j.AssertionFailedError;

/**
 * {@code AssertThrows} is a collection of utility methods that support asserting
 * an exception of an expected type is thrown.
 *
 * @since 5.0
 */
class AssertThrows {

	@SuppressWarnings("unchecked")
	static <T extends Throwable> T assertThrows(Class<T> expectedType, Executable executable) {
		try {
			executable.execute();
		}
		catch (Throwable actualException) {
			if (expectedType.isInstance(actualException)) {
				return (T) actualException;
			}
			else {
				String message = format(expectedType, actualException.getClass(), "Unexpected exception type thrown");
				throw new AssertionFailedError(message, actualException);
			}
		}
		throw new AssertionFailedError(
			String.format("Expected %s to be thrown, but nothing was thrown.", getCanonicalName(expectedType)));
	}

}
