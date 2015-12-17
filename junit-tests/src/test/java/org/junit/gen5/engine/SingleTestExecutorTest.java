/*
 * Copyright 2015 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.junit.gen5.engine;

import static org.junit.gen5.api.Assertions.*;
import static org.junit.gen5.engine.TestExecutionResult.Status.*;

import java.util.Optional;

import org.junit.Test;
import org.opentest4j.TestAbortedException;

public class SingleTestExecutorTest {

	@Test
	public void executeSafelySuccessful() {
		TestExecutionResult result = new SingleTestExecutor().executeSafely(() -> {
		});

		assertEquals(SUCCESSFUL, result.getStatus());
		assertEquals(Optional.empty(), result.getThrowable());
	}

	@Test
	public void executeSafelyAborted() {
		TestAbortedException testAbortedException = new TestAbortedException("assumption violated");

		TestExecutionResult result = new SingleTestExecutor().executeSafely(() -> {
			throw testAbortedException;
		});

		assertEquals(ABORTED, result.getStatus());
		assertSame(testAbortedException, result.getThrowable().get());
	}

	@Test
	public void executeSafelyFailed() {
		AssertionError assertionError = new AssertionError("assumption violated");

		TestExecutionResult result = new SingleTestExecutor().executeSafely(() -> {
			throw assertionError;
		});

		assertEquals(FAILED, result.getStatus());
		assertSame(assertionError, result.getThrowable().get());
	}
}
