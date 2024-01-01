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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.platform.engine.TestExecutionResult.Status.ABORTED;
import static org.junit.platform.engine.TestExecutionResult.Status.FAILED;
import static org.junit.platform.engine.TestExecutionResult.Status.SUCCESSFUL;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.opentest4j.TestAbortedException;

/**
 * @since 1.0
 */
@SuppressWarnings("deprecation")
class SingleTestExecutorTests {

	@Test
	void executeSafelySuccessful() {
		var result = new SingleTestExecutor().executeSafely(() -> {
		});

		assertEquals(SUCCESSFUL, result.getStatus());
		assertEquals(Optional.empty(), result.getThrowable());
	}

	@Test
	void executeSafelyAborted() {
		var testAbortedException = new TestAbortedException("assumption violated");

		var result = new SingleTestExecutor().executeSafely(() -> {
			throw testAbortedException;
		});

		assertEquals(ABORTED, result.getStatus());
		assertSame(testAbortedException, result.getThrowable().get());
	}

	@Test
	void executeSafelyFailed() {
		var assertionError = new AssertionError("assumption violated");

		var result = new SingleTestExecutor().executeSafely(() -> {
			throw assertionError;
		});

		assertEquals(FAILED, result.getStatus());
		assertSame(assertionError, result.getThrowable().get());
	}
}
