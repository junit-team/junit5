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

	private AssertTimeout() {
		/* no-op */
	}

	static void assertTimeout(Duration timeout, Executable executable) {
		assertTimeout(timeout, executable, (String) null);
	}

	static void assertTimeout(Duration timeout, Executable executable, String message) {
		assertTimeout(timeout, () -> {
			executable.execute();
			return null;
		}, message);
	}

	static void assertTimeout(Duration timeout, Executable executable, Supplier<String> messageSupplier) {
		assertTimeout(timeout, () -> {
			executable.execute();
			return null;
		}, messageSupplier);
	}

	static <T> T assertTimeout(Duration timeout, ThrowingSupplier<T> supplier) {
		return assertTimeout(timeout, supplier, (Object) null);
	}

	static <T> T assertTimeout(Duration timeout, ThrowingSupplier<T> supplier, String message) {
		return assertTimeout(timeout, supplier, (Object) message);
	}

	static <T> T assertTimeout(Duration timeout, ThrowingSupplier<T> supplier, Supplier<String> messageSupplier) {
		return assertTimeout(timeout, supplier, (Object) messageSupplier);
	}

	private static <T> T assertTimeout(Duration timeout, ThrowingSupplier<T> supplier, Object messageOrSupplier) {
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
			fail(buildPrefix(nullSafeGet(messageOrSupplier)) + "execution exceeded timeout of " + timeoutInMillis
					+ " ms by " + (timeElapsed - timeoutInMillis) + " ms");
		}
		return result;
	}

	static void assertTimeoutPreemptively(Duration timeout, Executable executable) {
		assertTimeoutPreemptively(timeout, executable, (String) null);
	}

	static void assertTimeoutPreemptively(Duration timeout, Executable executable, String message) {
		assertTimeoutPreemptively(timeout, () -> {
			executable.execute();
			return null;
		}, message);
	}

	static void assertTimeoutPreemptively(Duration timeout, Executable executable, Supplier<String> messageSupplier) {
		assertTimeoutPreemptively(timeout, () -> {
			executable.execute();
			return null;
		}, messageSupplier);
	}

	static <T> T assertTimeoutPreemptively(Duration timeout, ThrowingSupplier<T> supplier) {
		return assertTimeoutPreemptively(timeout, supplier, (Object) null);
	}

	static <T> T assertTimeoutPreemptively(Duration timeout, ThrowingSupplier<T> supplier, String message) {
		return assertTimeoutPreemptively(timeout, supplier, (Object) message);
	}

	static <T> T assertTimeoutPreemptively(Duration timeout, ThrowingSupplier<T> supplier,
			Supplier<String> messageSupplier) {

		return assertTimeoutPreemptively(timeout, supplier, (Object) messageSupplier);
	}

	private static <T> T assertTimeoutPreemptively(Duration timeout, ThrowingSupplier<T> supplier,
			Object messageOrSupplier) {

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
				throw new AssertionFailedError(buildPrefix(nullSafeGet(messageOrSupplier))
						+ "execution timed out after " + timeoutInMillis + " ms");
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
