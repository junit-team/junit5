/*
 * Copyright 2015-2018 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v20.html
 */

package org.junit.platform.testkit;

import java.util.Optional;
import java.util.function.Supplier;

import org.apiguardian.api.API;
import org.junit.platform.engine.TestExecutionResult;

/**
 * Union type that allows propagation of terminated test state, supporting both the reason {@link String}
 * if the test was skipped, or the {@link org.junit.platform.engine.TestExecutionResult} if the test was finished.
 *
 * @since 1.4.0
 */
@API(status = API.Status.EXPERIMENTAL, since = "1.4.0")
public class TerminationInfo {

	private static final Supplier<UnsupportedOperationException> NOT_EXECUTION_RESULT = () -> new UnsupportedOperationException(
		"No execution result contained, this is a skip reason.");

	private static final Supplier<UnsupportedOperationException> NOT_SKIP_REASON = () -> new UnsupportedOperationException(
		"No skip reason contained, this is an execution result.");

	private String maybeSkipReason;
	private TestExecutionResult maybeExecutionResult;

	private TerminationInfo(String skipReason, TestExecutionResult executionResult) {
		this.maybeSkipReason = skipReason;
		this.maybeExecutionResult = executionResult;
	}

	public static TerminationInfo skipped(String skipReason) {
		return new TerminationInfo(skipReason, null);
	}

	public static TerminationInfo executed(TestExecutionResult executionResult) {
		return new TerminationInfo(null, executionResult);
	}

	public boolean isSkipReason() {
		return Optional.ofNullable(maybeSkipReason).isPresent();
	}

	public boolean isExecutionResult() {
		return Optional.ofNullable(maybeExecutionResult).isPresent();
	}

	public String getSkipReason() {
		return Optional.ofNullable(maybeSkipReason).orElseThrow(NOT_SKIP_REASON);
	}

	public TestExecutionResult getExecutionResult() {
		return Optional.ofNullable(maybeExecutionResult).orElseThrow(NOT_EXECUTION_RESULT);
	}

}
