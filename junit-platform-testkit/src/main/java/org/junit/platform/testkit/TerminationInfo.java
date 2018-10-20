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

import static org.apiguardian.api.API.Status.EXPERIMENTAL;

import org.apiguardian.api.API;
import org.junit.platform.commons.util.Preconditions;
import org.junit.platform.engine.TestExecutionResult;

/**
 * {@code TerminationInfo} is a union type that allows propagation of terminated
 * container/test state, supporting either the <em>reason</em> if the container/test
 * was skipped or the {@link TestExecutionResult} if the container/test was executed.
 *
 * @since 1.4
 */
@API(status = EXPERIMENTAL, since = "1.4")
public class TerminationInfo {

	private final String skipReason;
	private final TestExecutionResult executionResult;

	private TerminationInfo(String skipReason, TestExecutionResult executionResult) {
		boolean skipped = (skipReason != null);
		boolean executed = (executionResult != null);
		Preconditions.condition((skipped ^ executed),
			"Either a skip reason or TestExecutionResult must be provided but not both");

		this.skipReason = skipReason;
		this.executionResult = executionResult;
	}

	public static TerminationInfo skipped(String skipReason) {
		return new TerminationInfo(skipReason, null);
	}

	public static TerminationInfo executed(TestExecutionResult executionResult) {
		return new TerminationInfo(null, executionResult);
	}

	public boolean isSkipReason() {
		return this.skipReason != null;
	}

	public boolean isExecutionResult() {
		return this.executionResult != null;
	}

	public String getSkipReason() {
		if (isSkipReason()) {
			return this.skipReason;
		}
		// else
		throw new UnsupportedOperationException("No skip reason contained in this TerminationInfo");
	}

	public TestExecutionResult getExecutionResult() {
		if (isExecutionResult()) {
			return this.executionResult;
		}
		// else
		throw new UnsupportedOperationException("No TestExecutionResult contained in this TerminationInfo");
	}

}
