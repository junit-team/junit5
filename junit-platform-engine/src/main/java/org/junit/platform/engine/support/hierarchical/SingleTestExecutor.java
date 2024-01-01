/*
 * Copyright 2015-2024 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * https://www.eclipse.org/legal/epl-v20.html
 */

package org.junit.platform.engine.support.hierarchical;

import static org.apiguardian.api.API.Status.DEPRECATED;
import static org.junit.platform.engine.TestExecutionResult.aborted;
import static org.junit.platform.engine.TestExecutionResult.failed;
import static org.junit.platform.engine.TestExecutionResult.successful;

import org.apiguardian.api.API;
import org.junit.platform.commons.util.UnrecoverableExceptions;
import org.junit.platform.engine.TestExecutionResult;
import org.opentest4j.TestAbortedException;

/**
 * {@code SingleTestExecutor} encapsulates the execution of a single test
 * wrapped in an {@link Executable}.
 *
 * @since 1.0
 * @see #executeSafely(Executable)
 * @deprecated Please use {@link ThrowableCollector#execute} and
 * {@link ThrowableCollector#toTestExecutionResult} instead.
 */
@Deprecated
@API(status = DEPRECATED, since = "1.2")
@SuppressWarnings("missing-explicit-ctor")
public class SingleTestExecutor {

	/**
	 * Functional interface for a single test to be executed by
	 * {@link SingleTestExecutor}.
	 */
	@FunctionalInterface
	public interface Executable {

		/**
		 * Execute the test.
		 *
		 * @throws TestAbortedException to signal aborted execution
		 * @throws Throwable to signal failure
		 */
		void execute() throws TestAbortedException, Throwable;

	}

	/**
	 * Execute the supplied {@link Executable} and return a
	 * {@link TestExecutionResult} based on the outcome.
	 *
	 * <p>If the {@code Executable} throws an <em>unrecoverable</em> exception
	 * &mdash; for example, an {@link OutOfMemoryError} &mdash; this method will
	 * rethrow it.
	 *
	 * @param executable the test to be executed
	 * @return {@linkplain TestExecutionResult#aborted aborted} if the
	 * {@code Executable} throws a {@link TestAbortedException};
	 * {@linkplain TestExecutionResult#failed failed} if any other
	 * {@link Throwable} is thrown; and {@linkplain TestExecutionResult#successful
	 * successful} otherwise
	 */
	public TestExecutionResult executeSafely(Executable executable) {
		try {
			executable.execute();
			return successful();
		}
		catch (TestAbortedException e) {
			return aborted(e);
		}
		catch (Throwable t) {
			UnrecoverableExceptions.rethrowIfUnrecoverable(t);
			return failed(t);
		}
	}

}
