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
 * Executes a single test wrapped in an {@link Executable} and returns a
 * {@link TestExecutionResult} by converting exceptions.
 *
 * @since 5.0
 */
@API(Experimental)
public class SingleTestExecutor {

	public interface Executable {

		void execute() throws Throwable;

	}

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
