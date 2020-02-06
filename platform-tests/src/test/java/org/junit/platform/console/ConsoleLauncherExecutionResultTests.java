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

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.junit.platform.console.options.CommandLineOptions;
import org.junit.platform.launcher.listeners.TestExecutionSummary;

/**
 * @since 1.3
 */
class ConsoleLauncherExecutionResultTests {

	private final CommandLineOptions options = new CommandLineOptions();
	private final TestExecutionSummary summary = mock(TestExecutionSummary.class);

	@Test
	void hasStatusCode0ForNoTotalFailures() {
		when(summary.getTotalFailureCount()).thenReturn(0L);

		int exitCode = ConsoleLauncherExecutionResult.computeExitCode(summary, options);

		assertThat(exitCode).isEqualTo(0);
	}

	@Test
	void hasStatusCode1ForForAnyFailure() {
		when(summary.getTotalFailureCount()).thenReturn(1L);

		int exitCode = ConsoleLauncherExecutionResult.computeExitCode(summary, options);

		assertThat(exitCode).isEqualTo(1);
	}

	/**
	 * @since 1.3
	 */
	@Test
	void hasStatusCode2ForNoTestsAndHasOptionFailIfNoTestsFound() {
		options.setFailIfNoTests(true);
		when(summary.getTestsFoundCount()).thenReturn(0L);

		int exitCode = ConsoleLauncherExecutionResult.computeExitCode(summary, options);

		assertThat(exitCode).isEqualTo(2);
	}

	/**
	 * @since 1.3
	 */
	@Test
	void hasStatusCode0ForTestsAndHasOptionFailIfNoTestsFound() {
		options.setFailIfNoTests(true);
		when(summary.getTestsFoundCount()).thenReturn(1L);
		when(summary.getTotalFailureCount()).thenReturn(0L);

		int exitCode = ConsoleLauncherExecutionResult.computeExitCode(summary, options);

		assertThat(exitCode).isEqualTo(0);
	}

	/**
	 * @since 1.3
	 */
	@Test
	void hasStatusCode0ForNoTestsAndNotFailIfNoTestsFound() {
		options.setFailIfNoTests(false);
		when(summary.getTestsFoundCount()).thenReturn(0L);

		int exitCode = ConsoleLauncherExecutionResult.computeExitCode(summary, options);

		assertThat(exitCode).isEqualTo(0);
	}

}
