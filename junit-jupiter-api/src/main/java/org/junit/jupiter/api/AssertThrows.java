/*
 * Copyright 2015-2021 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * https://www.eclipse.org/legal/epl-v20.html
 */

package org.junit.jupiter.api;

import static org.apiguardian.api.API.Status.EXPERIMENTAL;
import static org.junit.jupiter.api.AssertionUtils.buildPrefix;
import static org.junit.jupiter.api.AssertionUtils.format;
import static org.junit.jupiter.api.AssertionUtils.getCanonicalName;
import static org.junit.jupiter.api.AssertionUtils.nullSafeGet;

import java.util.function.BiPredicate;
import java.util.function.Supplier;

import org.apiguardian.api.API;
import org.junit.jupiter.api.function.Executable;
import org.junit.platform.commons.util.UnrecoverableExceptions;
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
		return assertThrows(expectedType, executable, (Object) null, Class::isAssignableFrom);
	}

	static <T extends Throwable> T assertThrows(Class<T> expectedType, Executable executable, String message) {
		return assertThrows(expectedType, executable, (Object) message, Class::isAssignableFrom);
	}

	static <T extends Throwable> T assertThrows(Class<T> expectedType, Executable executable,
			Supplier<String> messageSupplier) {

		return assertThrows(expectedType, executable, (Object) messageSupplier, Class::isAssignableFrom);
	}

	/**
	 * <em>Assert</em> that execution of the supplied {@code executable} throws
	 * an exception of exactly the {@code expectedType} and return the exception.
	 *
	 * <p>If no exception is thrown, or if an exception of a different type is
	 * thrown, this method will fail.
	 *
	 * <p>If you do not want to perform additional checks on the exception instance,
	 * ignore the return value.
	 *
	 * @since 5.8
	 */
	@API(status = EXPERIMENTAL, since = "5.8")
	static <T extends Throwable> T assertThrowsExactly(Class<T> expectedType, Executable executable) {
		return assertThrows(expectedType, executable, (Object) null, Class::equals);
	}

	/**
	 * <em>Assert</em> that execution of the supplied {@code executable} throws
	 * an exception of exactly the {@code expectedType} and return the exception.
	 *
	 * <p>If no exception is thrown, or if an exception of a different type is
	 * thrown, this method will fail.
	 *
	 * <p>If you do not want to perform additional checks on the exception instance,
	 * ignore the return value.
	 *
	 * <p>Fails with the supplied failure {@code message}.
	 *
	 * @since 5.8
	 */
	static <T extends Throwable> T assertThrowsExactly(Class<T> expectedType, Executable executable, String message) {
		return assertThrows(expectedType, executable, (Object) message, Class::equals);
	}

	/**
	 * <em>Assert</em> that execution of the supplied {@code executable} throws
	 * an exception of exactly the {@code expectedType} and return the exception.
	 *
	 * <p>If no exception is thrown, or if an exception of a different type is
	 * thrown, this method will fail.
	 *
	 * <p>If necessary, the failure message will be retrieved lazily from the
	 * supplied {@code messageSupplier}.
	 *
	 * <p>If you do not want to perform additional checks on the exception instance,
	 * ignore the return value.
	 *
	 * @since 5.8
	 */
	static <T extends Throwable> T assertThrowsExactly(Class<T> expectedType, Executable executable,
			Supplier<String> messageSupplier) {

		return assertThrows(expectedType, executable, (Object) messageSupplier, Class::equals);
	}

	@SuppressWarnings("unchecked")
	private static <T extends Throwable> T assertThrows(Class<T> expectedType, Executable executable,
			Object messageOrSupplier, BiPredicate<Class<T>, Class<?>> exceptionCheck) {

		try {
			executable.execute();
		}
		catch (Throwable actualException) {
			if (exceptionCheck.test(expectedType, actualException.getClass())) {
				return (T) actualException;
			}
			else {
				UnrecoverableExceptions.rethrowIfUnrecoverable(actualException);
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
