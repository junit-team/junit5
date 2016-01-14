/*
 * Copyright 2015-2016 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.junit.gen5.engine;

import static org.junit.gen5.engine.BlacklistedExceptions.rethrowIfBlacklisted;
import static org.junit.gen5.engine.TestExecutionResult.aborted;
import static org.junit.gen5.engine.TestExecutionResult.failed;
import static org.junit.gen5.engine.TestExecutionResult.successful;

import org.opentest4j.TestAbortedException;

/**
 * @since 5.0
 */
class SingleTestExecutor {

	interface Executable {

		void execute() throws Throwable;

	}

	TestExecutionResult executeSafely(Executable executable) {
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
