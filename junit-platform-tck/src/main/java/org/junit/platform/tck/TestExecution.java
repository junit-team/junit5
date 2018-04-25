/*
 * Copyright 2015-2018 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v20.html
 */

package org.junit.platform.tck;

import java.time.LocalDateTime;

import org.apiguardian.api.API;
import org.junit.platform.engine.TestDescriptor;
import org.junit.platform.engine.TestExecutionResult;

/**
 * Encapsulates metadata around the execution of a single test instance, this includes metadata of a test that could
 * have been skipped.
 */
@API(status = API.Status.EXPERIMENTAL, since = "1.2.0")
public class TestExecution {

	private TestDescriptor descriptor;
	private LocalDateTime startDateTime;
	private LocalDateTime endDateTime;
	private TestTerminationInfo termination;

	private TestExecution(TestDescriptor descriptor, LocalDateTime startDateTime, LocalDateTime endDateTime,
			TestTerminationInfo termination) {
		this.descriptor = descriptor;
		this.startDateTime = startDateTime;
		this.endDateTime = endDateTime;
		this.termination = termination;
	}

	public static TestExecution executed(TestDescriptor descriptor, LocalDateTime startDateTime,
			LocalDateTime endDateTime, TestExecutionResult executionResult) {
		return new TestExecution(descriptor, startDateTime, endDateTime, TestTerminationInfo.executed(executionResult));
	}

	public static TestExecution skipped(TestDescriptor descriptor, LocalDateTime startDateTime,
			LocalDateTime endDateTime, String skipReason) {
		return new TestExecution(descriptor, startDateTime, endDateTime, TestTerminationInfo.skipped(skipReason));
	}

	public TestDescriptor getTestDescriptor() {
		return descriptor;
	}

	public LocalDateTime getStartDateTime() {
		return startDateTime;
	}

	public LocalDateTime getEndDateTime() {
		return endDateTime;
	}

	public TestTerminationInfo getTerminationInfo() {
		return termination;
	}

}
