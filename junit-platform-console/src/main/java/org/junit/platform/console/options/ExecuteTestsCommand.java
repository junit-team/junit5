/*
 * Copyright 2015-2024 the original author or authors.
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
import java.nio.file.Path;
import java.util.Optional;

import org.junit.platform.console.tasks.ConsoleTestExecutor;
import org.junit.platform.launcher.listeners.TestExecutionSummary;

import picocli.CommandLine;
import picocli.CommandLine.ArgGroup;
import picocli.CommandLine.Command;
import picocli.CommandLine.Mixin;

@Command(//
		name = "execute", //
		description = "Execute tests" //
)
class ExecuteTestsCommand extends BaseCommand<TestExecutionSummary> implements CommandLine.IExitCodeGenerator {

	/**
	 * Exit code indicating test failure(s)
	 */
	private static final int TEST_FAILED = 1;

	/**
	 * Exit code indicating no tests found
	 */
	private static final int NO_TESTS_FOUND = 2;

	private final ConsoleTestExecutor.Factory consoleTestExecutorFactory;

	@Mixin
	TestDiscoveryOptionsMixin discoveryOptions;

	@Mixin
	TestConsoleOutputOptionsMixin testOutputOptions;

	@ArgGroup(validate = false, order = 6, heading = "%n@|bold REPORTING|@%n%n")
	ReportingOptions reportingOptions;

	ExecuteTestsCommand(ConsoleTestExecutor.Factory consoleTestExecutorFactory) {
		this.consoleTestExecutorFactory = consoleTestExecutorFactory;
	}

	@Override
	protected TestExecutionSummary execute(PrintWriter out) {
		return consoleTestExecutorFactory.create(toTestDiscoveryOptions(), toTestConsoleOutputOptions()) //
				.execute(out, getReportsDir());
	}

	Optional<Path> getReportsDir() {
		return getReportingOptions().flatMap(ReportingOptions::getReportsDir);
	}

	private Optional<ReportingOptions> getReportingOptions() {
		return Optional.ofNullable(reportingOptions);
	}

	TestDiscoveryOptions toTestDiscoveryOptions() {
		return this.discoveryOptions == null //
				? new TestDiscoveryOptions() //
				: this.discoveryOptions.toTestDiscoveryOptions();
	}

	TestConsoleOutputOptions toTestConsoleOutputOptions() {
		TestConsoleOutputOptions testOutputOptions = this.testOutputOptions.toTestConsoleOutputOptions();
		testOutputOptions.setAnsiColorOutputDisabled(outputOptions.isDisableAnsiColors());
		return testOutputOptions;
	}

	@Override
	public int getExitCode() {
		TestExecutionSummary executionResult = commandSpec.commandLine().getExecutionResult();
		boolean failIfNoTests = getReportingOptions().map(it -> it.failIfNoTests).orElse(false);
		if (failIfNoTests && executionResult.getTestsFoundCount() == 0) {
			return NO_TESTS_FOUND;
		}
		return executionResult.getTotalFailureCount() == 0 ? SUCCESS : TEST_FAILED;
	}

	static class ReportingOptions {

		@CommandLine.Option(names = "--fail-if-no-tests", description = "Fail and return exit status code 2 if no tests are found.")
		private boolean failIfNoTests; // no single-dash equivalent: was introduced in 5.3-M1

		@CommandLine.Option(names = "--reports-dir", paramLabel = "DIR", description = "Enable report output into a specified local directory (will be created if it does not exist).")
		private Path reportsDir;

		@CommandLine.Option(names = "-reports-dir", hidden = true)
		private Path reportsDir2;

		Optional<Path> getReportsDir() {
			return reportsDir == null ? Optional.ofNullable(reportsDir2) : Optional.of(reportsDir);
		}
	}
}
