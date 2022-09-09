/*
 * Copyright 2015-2022 the original author or authors.
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
 * the execution of the code under test did not take longer than the timeout duration.
 *
 * @since 5.0
 */
class AssertTimeout {

	private static final PreemptiveTimeoutAssertionExecutor<AssertionFailedError> ASSERTION_ERROR_TIMEOUT_EXECUTOR = new PreemptiveTimeoutAssertionExecutor<>(
		new AssertionTimeoutFailureFactory());
	private static final PreemptiveTimeoutAssertionExecutor<TimeoutException> TIMEOUT_EXCEPTION_TIMEOUT_EXECUTOR = new PreemptiveTimeoutAssertionExecutor<>(
		(e, __, ___, ____) -> e);

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
			throwAsUncheckedException(ex);
		}

		long timeElapsed = System.currentTimeMillis() - start;
		if (timeElapsed > timeoutInMillis) {
			assertionFailure() //
					.message(messageOrSupplier) //
					.reason("execution exceeded timeout of " + timeoutInMillis + " ms by "
							+ (timeElapsed - timeoutInMillis) + " ms") //
					.buildAndThrow();
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
		return ASSERTION_ERROR_TIMEOUT_EXECUTOR.executeThrowing(timeout, supplier, null);
	}

	static <T> T assertTimeoutPreemptively(Duration timeout, ThrowingSupplier<T> supplier, String message) {
		return ASSERTION_ERROR_TIMEOUT_EXECUTOR.executeThrowing(timeout, supplier, message);
	}

	static <T> T assertTimeoutPreemptively(Duration timeout, ThrowingSupplier<T> supplier,
			Supplier<String> messageSupplier) {
		return ASSERTION_ERROR_TIMEOUT_EXECUTOR.executeThrowing(timeout, supplier, messageSupplier);
	}

	static <T> T assertTimeoutPreemptivelyThrowingTimeoutException(Duration timeout, ThrowingSupplier<T> supplier,
			Supplier<String> messageSupplier) throws TimeoutException {
		return TIMEOUT_EXCEPTION_TIMEOUT_EXECUTOR.executeThrowing(timeout, supplier, messageSupplier);
	}

	static class PreemptiveTimeoutAssertionExecutor<T extends Throwable> {
		private final TimeoutFailureFactory<T> failureFactory;

		PreemptiveTimeoutAssertionExecutor(TimeoutFailureFactory<T> failureFactory) {
			this.failureFactory = failureFactory;
		}

		<V> V executeThrowing(Duration timeout, ThrowingSupplier<V> supplier, Object messageOrSupplier) throws T {
			AtomicReference<Thread> threadReference = new AtomicReference<>();
			ExecutorService executorService = Executors.newSingleThreadExecutor(new TimeoutThreadFactory());

			try {
				Future<V> future = submitTask(supplier, threadReference, executorService);
				return resolveFutureAndHandleException(future, timeout.toMillis(), messageOrSupplier,
					threadReference::get);
			}
			finally {
				executorService.shutdownNow();
			}
		}

		private <V> Future<V> submitTask(ThrowingSupplier<V> supplier, AtomicReference<Thread> threadReference,
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

		private <V> V resolveFutureAndHandleException(Future<V> future, long timeoutInMillis, Object messageOrSupplier,
				Supplier<Thread> threadSupplier) throws T {
			try {
				return future.get(timeoutInMillis, TimeUnit.MILLISECONDS);
			}
			catch (TimeoutException ex) {
				Thread thread = threadSupplier.get();
				ExecutionTimeoutException cause = null;
				if (thread != null) {
					cause = new ExecutionTimeoutException("Execution timed out in thread " + thread.getName());
					cause.setStackTrace(thread.getStackTrace());
				}
				throw failureFactory.handleTimeout(ex, timeoutInMillis, messageOrSupplier, cause);
			}
			catch (ExecutionException ex) {
				throw throwAsUncheckedException(ex.getCause());
			}
			catch (Throwable ex) {
				throw throwAsUncheckedException(ex);
			}
		}
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

	private interface TimeoutFailureFactory<T extends Throwable> {
		T handleTimeout(TimeoutException exception, long timeoutInMillis, Object messageOrSupplier, Throwable cause);
	}

	private static class AssertionTimeoutFailureFactory implements TimeoutFailureFactory<AssertionFailedError> {

		@Override
		public AssertionFailedError handleTimeout(TimeoutException exception, long timeoutInMillis,
				Object messageOrSupplier, Throwable cause) {
			return assertionFailure() //
					.message(messageOrSupplier) //
					.reason("execution timed out after " + timeoutInMillis + " ms") //
					.cause(cause) //
					.build();
		}
	}
}
