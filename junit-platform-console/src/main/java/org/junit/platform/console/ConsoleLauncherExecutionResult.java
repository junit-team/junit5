/*
 * Copyright 2015-2020 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * https://www.eclipse.org/legal/epl-v20.html
 */

package org.junit.platform.console;

import static org.apiguardian.api.API.Status.INTERNAL;

import java.util.Optional;

import org.apiguardian.api.API;
import org.junit.platform.console.options.CommandLineOptions;
import org.junit.platform.launcher.listeners.TestExecutionSummary;

/**
 * @since 1.0
 */
@API(status = INTERNAL, since = "1.0")
public class ConsoleLauncherExecutionResult {

	/**
	 * Exit code indicating successful execution
	 */
	private static final int SUCCESS = 0;

	/**
	 * Exit code indicating test failure(s)
	 */
	private static final int TEST_FAILED = 1;

	/**
	 * Exit code indicating no tests found
	 */
	private static final int NO_TESTS_FOUND = 2;

	/**
	 * Exit code indicating any failure(s)
	 */
	private static final int FAILED = -1;

	public static int computeExitCode(TestExecutionSummary summary, CommandLineOptions options) {
		if (options.isFailIfNoTests() && summary.getTestsFoundCount() == 0) {
			return NO_TESTS_FOUND;
		}
		return summary.getTotalFailureCount() == 0 ? SUCCESS : TEST_FAILED;
	}

	static ConsoleLauncherExecutionResult success() {
		return new ConsoleLauncherExecutionResult(SUCCESS, null);
	}

	static ConsoleLauncherExecutionResult failed() {
		return new ConsoleLauncherExecutionResult(FAILED, null);
	}

	static ConsoleLauncherExecutionResult forSummary(TestExecutionSummary summary, CommandLineOptions options) {
		int exitCode = computeExitCode(summary, options);
		return new ConsoleLauncherExecutionResult(exitCode, summary);
	}

	private final int exitCode;
	private final TestExecutionSummary testExecutionSummary;

	private ConsoleLauncherExecutionResult(int exitCode, TestExecutionSummary testExecutionSummary) {
		this.testExecutionSummary = testExecutionSummary;
		this.exitCode = exitCode;
	}

	public int getExitCode() {
		return exitCode;
	}

	public Optional<TestExecutionSummary> getTestExecutionSummary() {
		return Optional.ofNullable(testExecutionSummary);
	}
}
