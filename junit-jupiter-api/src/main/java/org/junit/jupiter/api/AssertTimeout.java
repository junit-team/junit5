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

import static org.junit.jupiter.api.AssertionUtils.buildPrefix;
import static org.junit.jupiter.api.AssertionUtils.nullSafeGet;
import static org.junit.jupiter.api.Assertions.fail;

import java.time.Duration;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.function.Supplier;

import org.junit.jupiter.api.function.Executable;
import org.junit.jupiter.api.function.ThrowingSupplier;
import org.junit.platform.commons.util.ExceptionUtils;
import org.opentest4j.AssertionFailedError;

/**
 * {@code AssertTimeout} is a collection of utility methods that support asserting
 * the execution of the code under test did not take longer than the timeout duration.
 *
 * @since 5.0
 */
class AssertTimeout {

	///CLOVER:OFF
	private AssertTimeout() {
		/* no-op */
	}
	///CLOVER:ON

	static void assertTimeout(Duration timeout, Executable executable) {
		assertTimeout(timeout, executable, () -> null);
	}

	static void assertTimeout(Duration timeout, Executable executable, String message) {
		assertTimeout(timeout, executable, () -> message);
	}

	static void assertTimeout(Duration timeout, Executable executable, Supplier<String> messageSupplier) {
		assertTimeout(timeout, () -> {
			executable.execute();
			return null;
		}, messageSupplier);
	}

	static <T> T assertTimeout(Duration timeout, ThrowingSupplier<T> supplier) {
		return assertTimeout(timeout, supplier, () -> null);
	}

	static <T> T assertTimeout(Duration timeout, ThrowingSupplier<T> supplier, String message) {
		return assertTimeout(timeout, supplier, () -> message);
	}

	static <T> T assertTimeout(Duration timeout, ThrowingSupplier<T> supplier, Supplier<String> messageSupplier) {
		long timeoutInMillis = timeout.toMillis();
		long start = System.currentTimeMillis();
		T result = null;
		try {
			result = supplier.get();
		}
		catch (Throwable ex) {
			ExceptionUtils.throwAsUncheckedException(ex);
		}

		long timeElapsed = System.currentTimeMillis() - start;
		if (timeElapsed > timeoutInMillis) {
			fail(buildPrefix(nullSafeGet(messageSupplier)) + "execution exceeded timeout of " + timeoutInMillis
					+ " ms by " + (timeElapsed - timeoutInMillis) + " ms");
		}
		return result;
	}

	static void assertTimeoutPreemptively(Duration timeout, Executable executable) {
		assertTimeoutPreemptively(timeout, executable, () -> null);
	}

	static void assertTimeoutPreemptively(Duration timeout, Executable executable, String message) {
		assertTimeoutPreemptively(timeout, executable, () -> message);
	}

	static void assertTimeoutPreemptively(Duration timeout, Executable executable, Supplier<String> messageSupplier) {
		assertTimeoutPreemptively(timeout, () -> {
			executable.execute();
			return null;
		}, messageSupplier);
	}

	static <T> T assertTimeoutPreemptively(Duration timeout, ThrowingSupplier<T> supplier) {
		return assertTimeoutPreemptively(timeout, supplier, () -> null);
	}

	static <T> T assertTimeoutPreemptively(Duration timeout, ThrowingSupplier<T> supplier, String message) {
		return assertTimeoutPreemptively(timeout, supplier, () -> message);
	}

	static <T> T assertTimeoutPreemptively(Duration timeout, ThrowingSupplier<T> supplier,
			Supplier<String> messageSupplier) {
		ExecutorService executorService = Executors.newSingleThreadExecutor();

		try {
			Future<T> future = executorService.submit(() -> {
				try {
					return supplier.get();
				}
				catch (Throwable throwable) {
					throw ExceptionUtils.throwAsUncheckedException(throwable);
				}
			});

			long timeoutInMillis = timeout.toMillis();
			try {
				return future.get(timeoutInMillis, TimeUnit.MILLISECONDS);
			}
			catch (TimeoutException ex) {
				throw new AssertionFailedError(
					buildPrefix(nullSafeGet(messageSupplier)) + "execution timed out after " + timeoutInMillis + " ms");
			}
			catch (ExecutionException ex) {
				throw ExceptionUtils.throwAsUncheckedException(ex.getCause());
			}
			catch (Throwable ex) {
				throw ExceptionUtils.throwAsUncheckedException(ex);
			}
		}
		finally {
			executorService.shutdownNow();
		}
	}

}
