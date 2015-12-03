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
import static org.junit.gen5.engine.TestPlanSpecification.allTests;
import static org.junit.gen5.engine.TestPlanSpecification.byTags;
import static org.junit.gen5.engine.TestPlanSpecification.classNameMatches;

import java.io.File;
import java.io.PrintWriter;
import java.util.HashSet;
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
import org.junit.gen5.launcher.listeners.SummaryCreatingTestListener;
import org.junit.gen5.launcher.listeners.TestExecutionSummary;

/**
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

	@Option(name = {"-D", "--hide-details"}, description = "Hide details while tests are being executed. "
			+ "Only show the summary and test failures.")
	private boolean hideDetails;

	@Option(name = {"-n", "--filter-classname"}, description = "Give a regular expression to include only classes whose fully qualified names match.")
	private String classnameFilter;

	@Option(name = {"-t", "--filter-tags"}, description = "Give a tag to include in the test run. This option can be repeated.")
	private List<String> tagsFilter;

	@Arguments(description = "Test classes, methods or packages to execute."
			+ " If --all|-a has been chosen, arguments can list all classpath roots that should be considered for test scanning,"
			+ " or none if the full classpath shall be scanned.")
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

		TestExecutionSummary summary = new TestExecutionSummary();

		registerListeners(launcher, summary);

		TestPlanSpecification testPlanSpecification = createTestPlanSpecification();

		launcher.execute(testPlanSpecification);

		printSummaryToStandardOut(summary);

		if (enableExitCode) {
			long failedTests = summary.countFailedTests();
			int exitCode = (int) Math.min(Integer.MAX_VALUE, failedTests);
			System.exit(exitCode);
		}
	}

	private void registerListeners(Launcher launcher, TestExecutionSummary summary) {
		SummaryCreatingTestListener testSummaryListener = new SummaryCreatingTestListener(summary);
		launcher.registerTestPlanExecutionListeners(testSummaryListener);
		if (!hideDetails) {
			launcher.registerTestPlanExecutionListeners(new ColoredPrintingTestListener(System.out, disableAnsiColors));
		}
	}

	private TestPlanSpecification createTestPlanSpecification() {
		TestPlanSpecification testPlanSpecification;
		if (runAllTests) {
			Set<File> rootDirectoriesToScan = new HashSet<>();
			if (arguments == null || arguments.isEmpty()) {
				rootDirectoriesToScan.addAll(ReflectionUtils.getAllClasspathRootDirectories());
			}
			else {
				arguments.stream().map(File::new).forEach(rootDirectoriesToScan::add);
			}
			testPlanSpecification = TestPlanSpecification.build(allTests(rootDirectoriesToScan));
		}
		else {
			testPlanSpecification = TestPlanSpecification.build(testPlanSpecificationElementsFromArguments());
		}
		if (classnameFilter != null) {
			testPlanSpecification.filterWith(classNameMatches(classnameFilter));
		}
		if (tagsFilter != null && !tagsFilter.isEmpty()) {
			testPlanSpecification.filterWith(byTags(tagsFilter));
		}
		return testPlanSpecification;
	}

	private void printSummaryToStandardOut(TestExecutionSummary summary) {
		PrintWriter stdout = new PrintWriter(System.out);

		if (hideDetails) { //Otherwise the failures have already been printed
			summary.printFailuresOn(stdout);
		}

		summary.printOn(stdout);
		stdout.close();
	}

	private List<TestPlanSpecificationElement> testPlanSpecificationElementsFromArguments() {
		Preconditions.notNull(arguments, "No arguments given");
		return toTestPlanSpecificationElements(arguments);
	}

	List<TestPlanSpecificationElement> toTestPlanSpecificationElements(List<String> arguments) {
		return TestPlanSpecification.forNames(arguments);
	}

}
