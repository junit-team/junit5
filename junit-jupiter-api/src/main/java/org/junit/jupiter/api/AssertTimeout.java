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
import org.junit.platform.commons.util.ExceptionUtils;

/**
 * {@code AssertTimeout} is a collection of utility methods that support asserting
 * the execution of the code under test did not take longer than the timeout duration.
 *
 * @since 5.0
 */
class AssertTimeout {

	static void assertTimeout(Duration timeout, Executable executable) {
		assertTimeout(timeout, executable, () -> null);
	}

	static void assertTimeout(Duration timeout, Executable executable, String message) {
		assertTimeout(timeout, executable, () -> message);
	}

	static void assertTimeout(Duration timeout, Executable executable, Supplier<String> messageSupplier) {
		long timeoutInMillis = timeout.toMillis();
		long start = System.currentTimeMillis();
		try {
			executable.execute();
		}
		catch (Throwable ex) {
			ExceptionUtils.throwAsUncheckedException(ex);
		}

		long timeElapsed = System.currentTimeMillis() - start;
		if (timeElapsed > timeoutInMillis) {
			fail(buildPrefix(nullSafeGet(messageSupplier)) + "execution exceeded timeout of " + timeoutInMillis
					+ " ms by " + (timeElapsed - timeoutInMillis) + " ms");
		}
	}

	static void assertTimeoutPreemptively(Duration timeout, Executable executable) {
		assertTimeoutPreemptively(timeout, executable, () -> null);
	}

	static void assertTimeoutPreemptively(Duration timeout, Executable executable, String message) {
		assertTimeoutPreemptively(timeout, executable, () -> message);
	}

	static void assertTimeoutPreemptively(Duration timeout, Executable executable, Supplier<String> messageSupplier) {
		ExecutorService executorService = Executors.newSingleThreadExecutor();
		try {
			Future<Throwable> future = executorService.submit(() -> {
				try {
					executable.execute();
				}
				catch (Throwable ex) {
					return ex;
				}
				return null;
			});

			long timeoutInMillis = timeout.toMillis();
			Throwable throwable = null;

			try {
				throwable = future.get(timeoutInMillis, TimeUnit.MILLISECONDS);
			}
			catch (TimeoutException ex) {
				fail(
					buildPrefix(nullSafeGet(messageSupplier)) + "execution timed out after " + timeoutInMillis + " ms");
			}
			catch (ExecutionException ex) {
				throwable = ex.getCause();
			}
			catch (Throwable ex) {
				throwable = ex;
			}

			if (throwable != null) {
				ExceptionUtils.throwAsUncheckedException(throwable);
			}
		}
		finally {
			executorService.shutdownNow();
		}
	}

}
