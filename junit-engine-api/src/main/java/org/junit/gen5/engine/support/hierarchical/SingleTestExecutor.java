/*
 * Copyright 2015-2016 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.junit.gen5.engine.support.hierarchical;

import static org.junit.gen5.commons.meta.API.Usage.Experimental;
import static org.junit.gen5.engine.TestExecutionResult.aborted;
import static org.junit.gen5.engine.TestExecutionResult.failed;
import static org.junit.gen5.engine.TestExecutionResult.successful;
import static org.junit.gen5.engine.support.hierarchical.BlacklistedExceptions.rethrowIfBlacklisted;

import org.junit.gen5.commons.meta.API;
import org.junit.gen5.engine.TestExecutionResult;
import org.opentest4j.TestAbortedException;

/**
 * Encapsulates execution of a single test wrapped in an {@link Executable} and
 * returns a {@link TestExecutionResult} by converting exceptions.
 *
 * @since 5.0
 */
@API(Experimental)
public class SingleTestExecutor {

	/**
	 * Functional interface of a single test to be executed by
	 * {@link SingleTestExecutor}.
	 */
	public interface Executable {

		/**
		 * Execute the test.
		 *
		 * @throws TestAbortedException to signal abortion
		 * @throws Throwable to signal failure
		 */
		void execute() throws TestAbortedException, Throwable;

	}

	/**
	 * Executes the supplied {@link Executable executable} and returns a
	 * {@link TestExecutionResult} based on its outcome.
	 *
	 * <p>In case {@code executable} throws a <em>blacklisted</em> exception,
	 * e.g. an {@link OutOfMemoryError}, this method will rethrow it.
	 *
	 * @param executable the test to be executed
	 * @return {@linkplain TestExecutionResult#aborted aborted}, when
	 * {@code executable} throws a {@link TestAbortedException};
	 * {@linkplain TestExecutionResult#failed failed}, on a different
	 * {@link Throwable}; {@linkplain TestExecutionResult#successful
	 * successful}, otherwise.
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
			rethrowIfBlacklisted(t);
			return failed(t);
		}
	}
}
