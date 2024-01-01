/*
 * Copyright 2015-2024 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * https://www.eclipse.org/legal/epl-v20.html
 */

package org.junit.jupiter.api;

import static org.junit.jupiter.api.AssertionFailureBuilder.assertionFailure;
import static org.junit.platform.commons.util.ExceptionUtils.throwAsUncheckedException;

import java.time.Duration;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Supplier;

import org.junit.jupiter.api.function.Executable;
import org.junit.jupiter.api.function.ThrowingSupplier;
import org.junit.platform.commons.JUnitException;
import org.opentest4j.AssertionFailedError;

/**
 * {@code AssertTimeout} is a collection of utility methods that support asserting
 * the execution of the code under test did not take longer than the timeout duration
 * using a preemptive approach.
 *
 * @since 5.9.1
 */
class AssertTimeoutPreemptively {

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
		return assertTimeoutPreemptively(timeout, supplier, null, AssertTimeoutPreemptively::createAssertionFailure);
	}

	static <T> T assertTimeoutPreemptively(Duration timeout, ThrowingSupplier<T> supplier, String message) {
		return assertTimeoutPreemptively(timeout, supplier, message == null ? null : () -> message,
			AssertTimeoutPreemptively::createAssertionFailure);
	}

	static <T> T assertTimeoutPreemptively(Duration timeout, ThrowingSupplier<T> supplier,
			Supplier<String> messageSupplier) {
		return assertTimeoutPreemptively(timeout, supplier, messageSupplier,
			AssertTimeoutPreemptively::createAssertionFailure);
	}

	static <T, E extends Throwable> T assertTimeoutPreemptively(Duration timeout, ThrowingSupplier<T> supplier,
			Supplier<String> messageSupplier, Assertions.TimeoutFailureFactory<E> failureFactory) throws E {
		AtomicReference<Thread> threadReference = new AtomicReference<>();
		ExecutorService executorService = Executors.newSingleThreadExecutor(new TimeoutThreadFactory());

		try {
			Future<T> future = submitTask(supplier, threadReference, executorService);
			return resolveFutureAndHandleException(future, timeout, messageSupplier, threadReference::get,
				failureFactory);
		}
		finally {
			executorService.shutdownNow();
		}
	}

	private static <T> Future<T> submitTask(ThrowingSupplier<T> supplier, AtomicReference<Thread> threadReference,
			ExecutorService executorService) {
		return executorService.submit(() -> {
			try {
				threadReference.set(Thread.currentThread());
				return supplier.get();
			}
			catch (Throwable throwable) {
				throw throwAsUncheckedException(throwable);
			}
		});
	}

	private static <T, E extends Throwable> T resolveFutureAndHandleException(Future<T> future, Duration timeout,
			Supplier<String> messageSupplier, Supplier<Thread> threadSupplier,
			Assertions.TimeoutFailureFactory<E> failureFactory) throws E {
		try {
			return future.get(timeout.toMillis(), TimeUnit.MILLISECONDS);
		}
		catch (TimeoutException ex) {
			Thread thread = threadSupplier.get();
			ExecutionTimeoutException cause = null;
			if (thread != null) {
				cause = new ExecutionTimeoutException("Execution timed out in thread " + thread.getName());
				cause.setStackTrace(thread.getStackTrace());
			}
			throw failureFactory.createTimeoutFailure(timeout, messageSupplier, cause);
		}
		catch (ExecutionException ex) {
			throw throwAsUncheckedException(ex.getCause());
		}
		catch (Throwable ex) {
			throw throwAsUncheckedException(ex);
		}
	}

	private static AssertionFailedError createAssertionFailure(Duration timeout, Supplier<String> messageSupplier,
			Throwable cause) {
		return assertionFailure() //
				.message(messageSupplier) //
				.reason("execution timed out after " + timeout.toMillis() + " ms") //
				.cause(cause) //
				.build();
	}

	private static class ExecutionTimeoutException extends JUnitException {

		private static final long serialVersionUID = 1L;

		ExecutionTimeoutException(String message) {
			super(message);
		}
	}

	/**
	 * The thread factory used for preemptive timeout.
	 * <p>
	 * The factory creates threads with meaningful names, helpful for debugging purposes.
	 */
	private static class TimeoutThreadFactory implements ThreadFactory {
		private static final AtomicInteger threadNumber = new AtomicInteger(1);

		public Thread newThread(Runnable r) {
			return new Thread(r, "junit-timeout-thread-" + threadNumber.getAndIncrement());
		}
	}
}
