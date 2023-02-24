/*
 * Copyright 2015-2023 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * https://www.eclipse.org/legal/epl-v20.html
 */

package org.junit.platform.console.options;

import static org.junit.platform.console.options.CommandResult.SUCCESS;

import java.io.PrintWriter;

import org.junit.platform.console.tasks.ConsoleTestExecutor;
import org.junit.platform.launcher.listeners.TestExecutionSummary;

public class ExecuteTestsCommand implements Command<TestExecutionSummary> {

	/**
	 * Exit code indicating test failure(s)
	 */
	private static final int TEST_FAILED = 1;

	/**
	 * Exit code indicating no tests found
	 */
	private static final int NO_TESTS_FOUND = 2;

	public static int computeExitCode(TestExecutionSummary summary, CommandLineOptions options) {
		if (options.isFailIfNoTests() && summary.getTestsFoundCount() == 0) {
			return NO_TESTS_FOUND;
		}
		return summary.getTotalFailureCount() == 0 ? SUCCESS : TEST_FAILED;
	}

	private final CommandLineOptions options;

	public ExecuteTestsCommand(CommandLineOptions options) {
		this.options = options;
	}

	@Override
	public CommandResult<TestExecutionSummary> run(PrintWriter out, PrintWriter err) throws Exception {
		TestExecutionSummary testExecutionSummary = new ConsoleTestExecutor(options).execute(out);
		int exitCode = computeExitCode(testExecutionSummary, options);
		return CommandResult.create(exitCode, testExecutionSummary);
	}
}
