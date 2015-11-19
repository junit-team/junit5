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

import java.io.File;
import java.util.List;
import java.util.Set;

import javax.inject.Inject;

import io.airlift.airline.Arguments;
import io.airlift.airline.Command;
import io.airlift.airline.Help;
import io.airlift.airline.Option;
import io.airlift.airline.model.CommandMetadata;

import org.junit.gen5.commons.util.Preconditions;
import org.junit.gen5.commons.util.ReflectionUtils;
import org.junit.gen5.engine.TestPlanSpecification;
import org.junit.gen5.engine.TestPlanSpecificationElement;
import org.junit.gen5.launcher.Launcher;

/**
 * @author Stefan Bechtold
 * @author Sam Brannen
 * @since 5.0
 */
@Command(name = "ConsoleRunner", description = "console test runner")
public class ConsoleRunner {

	// @formatter:off
    @Option(name = {"-h", "--help"}, description = "Display help information")
    private boolean help;

	@Option(name = { "-x", "--enable-exit-code" },
			description = "Exit process with number of failing tests as exit code")
	private boolean enableExitCode;

	@Option(name = { "-C", "--disable-ansi-colors" },
			description = "Disable colored output (not supported by all terminals)")
	private boolean disableAnsiColors;

	@Option(name = {"-a", "--all"}, description = "Run all tests")
	private boolean runAllTests;

	@Arguments(description = "Test classes, methods or packages to execute (ignore if --all|-a has been chosen)")
	private List<String> arguments;

	// @formatter:on

	@Inject
	public CommandMetadata commandMetadata;

	public static void main(String... args) {
		ConsoleRunner consoleRunner = singleCommand(ConsoleRunner.class).parse(args);

		if (consoleRunner.help) {
			showHelp(consoleRunner);
		}

		try {
			consoleRunner.run();
		}
		catch (Exception e) {
			e.printStackTrace(System.err);
			System.err.println();
			showHelp(consoleRunner);
			System.exit(-1);
		}
	}

	private static void showHelp(ConsoleRunner consoleRunner) {
		Help.help(consoleRunner.commandMetadata);
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

		TestPlanSpecification testPlanSpecification;
		if (runAllTests) {
			Set<File> rootDirectories = ReflectionUtils.getAllClasspathRootDirectories();
			testPlanSpecification = TestPlanSpecification.build(TestPlanSpecification.allTests(rootDirectories));
		}
		else {
			testPlanSpecification = TestPlanSpecification.build(testPlanSpecificationElementsFromArguments());
		}

		// TODO Provide means to allow manipulation of test plan?
		launcher.execute(testPlanSpecification);

		if (enableExitCode) {
			long failedTests = testSummaryListener.getNumberOfFailedTests();
			int exitCode = (int) Math.min(Integer.MAX_VALUE, failedTests);
			System.exit(exitCode);
		}
	}

	private List<TestPlanSpecificationElement> testPlanSpecificationElementsFromArguments() {
		Preconditions.notNull(arguments, "No arguments given");
		return toTestPlanSpecificationElements(arguments);
	}

	List<TestPlanSpecificationElement> toTestPlanSpecificationElements(List<String> arguments) {
		return TestPlanSpecification.forNames(arguments);
	}

}
