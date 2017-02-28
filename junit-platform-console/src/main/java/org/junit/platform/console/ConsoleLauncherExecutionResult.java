/*
 * Copyright 2015-2017 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.junit.platform.console;

import static org.junit.platform.commons.meta.API.Usage.Internal;

import java.util.Optional;

import org.junit.platform.commons.meta.API;
import org.junit.platform.launcher.listeners.TestExecutionSummary;

/**
 * @since 1.0
 */
@API(Internal)
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
	 * Exit code indicating any failure(s)
	 */
	private static final int FAILED = -1;

	public static int computeExitCode(TestExecutionSummary summary) {
		return summary.getTotalFailureCount() == 0 ? SUCCESS : TEST_FAILED;
	}

	static ConsoleLauncherExecutionResult success() {
		return new ConsoleLauncherExecutionResult(SUCCESS);
	}

	static ConsoleLauncherExecutionResult failed() {
		return new ConsoleLauncherExecutionResult(FAILED);
	}

	static ConsoleLauncherExecutionResult forSummary(TestExecutionSummary summary) {
		return new ConsoleLauncherExecutionResult(summary);
	}

	private final int exitCode;
	private final TestExecutionSummary testExecutionSummary;

	private ConsoleLauncherExecutionResult(int exitCode) {
		this.exitCode = exitCode;
		this.testExecutionSummary = null;
	}

	private ConsoleLauncherExecutionResult(TestExecutionSummary testExecutionSummary) {
		this.testExecutionSummary = testExecutionSummary;
		this.exitCode = computeExitCode(testExecutionSummary);
	}

	public int getExitCode() {
		return exitCode;
	}

	public Optional<TestExecutionSummary> getTestExecutionSummary() {
		return Optional.ofNullable(testExecutionSummary);
	}
}
