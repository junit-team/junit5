/*
 * Copyright 2015-2024 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * https://www.eclipse.org/legal/epl-v20.html
 */

package org.junit.platform.console;

import java.io.PrintWriter;
import java.util.List;

import org.junit.platform.console.options.CommandResult;
import org.junit.platform.launcher.listeners.TestExecutionSummary;

/**
 * @since 1.0
 */
class ConsoleLauncherWrapperResult implements TestExecutionSummary {

	final String[] args;
	final String out;
	final String err;
	final int code;
	private final TestExecutionSummary summary;

	ConsoleLauncherWrapperResult(String[] args, String out, String err, CommandResult<?> result) {
		this.args = args;
		this.out = out;
		this.err = err;
		this.code = result.getExitCode();
		this.summary = (TestExecutionSummary) result.getValue() //
				.filter(it -> it instanceof TestExecutionSummary) //
				.orElse(null);
	}

	private void checkTestExecutionSummaryState() {
		if (summary == null) {
			throw new IllegalStateException("TestExecutionSummary not assigned. Exit code is: " + code);
		}
	}

	@Override
	public long getTimeStarted() {
		checkTestExecutionSummaryState();
		return summary.getTimeStarted();
	}

	@Override
	public long getTimeFinished() {
		checkTestExecutionSummaryState();
		return summary.getTimeFinished();
	}

	@Override
	public long getTotalFailureCount() {
		checkTestExecutionSummaryState();
		return summary.getTotalFailureCount();
	}

	@Override
	public long getContainersFoundCount() {
		checkTestExecutionSummaryState();
		return summary.getContainersFoundCount();
	}

	@Override
	public long getContainersStartedCount() {
		checkTestExecutionSummaryState();
		return summary.getContainersStartedCount();
	}

	@Override
	public long getContainersSkippedCount() {
		checkTestExecutionSummaryState();
		return summary.getContainersSkippedCount();
	}

	@Override
	public long getContainersAbortedCount() {
		checkTestExecutionSummaryState();
		return summary.getContainersAbortedCount();
	}

	@Override
	public long getContainersSucceededCount() {
		checkTestExecutionSummaryState();
		return summary.getContainersSucceededCount();
	}

	@Override
	public long getContainersFailedCount() {
		checkTestExecutionSummaryState();
		return summary.getContainersFailedCount();
	}

	@Override
	public long getTestsFoundCount() {
		checkTestExecutionSummaryState();
		return summary.getTestsFoundCount();
	}

	@Override
	public long getTestsStartedCount() {
		checkTestExecutionSummaryState();
		return summary.getTestsStartedCount();
	}

	@Override
	public long getTestsSkippedCount() {
		checkTestExecutionSummaryState();
		return summary.getTestsSkippedCount();
	}

	@Override
	public long getTestsAbortedCount() {
		checkTestExecutionSummaryState();
		return summary.getTestsAbortedCount();
	}

	@Override
	public long getTestsSucceededCount() {
		checkTestExecutionSummaryState();
		return summary.getTestsSucceededCount();
	}

	@Override
	public long getTestsFailedCount() {
		checkTestExecutionSummaryState();
		return summary.getTestsFailedCount();
	}

	@Override
	public void printTo(PrintWriter writer) {
		checkTestExecutionSummaryState();
		summary.printTo(writer);
	}

	@Override
	public void printFailuresTo(PrintWriter writer) {
		checkTestExecutionSummaryState();
		summary.printFailuresTo(writer);
	}

	@Override
	public void printFailuresTo(PrintWriter writer, int maxStackTraceLines) {
		checkTestExecutionSummaryState();
		summary.printFailuresTo(writer, maxStackTraceLines);
	}

	@Override
	public List<Failure> getFailures() {
		checkTestExecutionSummaryState();
		return summary.getFailures();
	}
}
