/*
 * Copyright 2015 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.junit.gen5.console;

import static io.airlift.airline.SingleCommand.singleCommand;

import java.util.List;

import javax.inject.Inject;

import io.airlift.airline.Arguments;
import io.airlift.airline.Command;
import io.airlift.airline.HelpOption;
import io.airlift.airline.Option;

import org.junit.gen5.engine.TestPlanSpecification;
import org.junit.gen5.launcher.Launcher;

/**
 * @author Stefan Bechtold
 * @author Sam Brannen
 * @since 5.0
 */
@Command(name = "ConsoleRunner", description = "console test runner")
public class ConsoleRunner {

	@Inject
	public HelpOption helpOption;

	// @formatter:off
	@Option(name = { "-x", "--enable-exit-code" },
			description = "Exit process with number of failing tests as exit code")
	private boolean enableExitCode;
	// @formatter:on

	// @formatter:off
	@Option(name = { "-C", "--disable-ansi-colors" },
			description = "Disable colored output (not supported by all terminals)")
	private boolean disableAnsiColors;
	// @formatter:on

	@Arguments(description = "Test classes to execute")
	private List<String> testClasses;

	public static void main(String... args) {
		ConsoleRunner consoleRunner = singleCommand(ConsoleRunner.class).parse(args);

		if (!consoleRunner.helpOption.showHelpIfRequested()) {
			consoleRunner.run();
		}
	}

	private void run() {
		// TODO Configure launcher?
		Launcher launcher = new Launcher();

		TestSummaryReportingTestListener testSummaryListener = new TestSummaryReportingTestListener(System.out);
		launcher.registerTestPlanExecutionListeners(
			// @formatter:off
			new ColoredPrintingTestListener(System.out, disableAnsiColors),
			testSummaryListener
			// @formatter:on
		);

		TestPlanSpecification testPlanSpecification = TestPlanSpecification.build(
			TestPlanSpecification.forClassNames(testClasses));

		// TODO Provide means to allow manipulation of test plan?
		launcher.execute(testPlanSpecification);

		if (enableExitCode) {
			long failedTests = testSummaryListener.getNumberOfFailedTests();
			int exitCode = (int) Math.min(Integer.MAX_VALUE, failedTests);
			System.exit(exitCode);
		}
	}

}
