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

import static org.junit.gen5.engine.TestPlanSpecification.allTests;
import static org.junit.gen5.engine.TestPlanSpecification.byTags;
import static org.junit.gen5.engine.TestPlanSpecification.classNameMatches;

import java.io.File;
import java.io.PrintWriter;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.gen5.commons.util.Preconditions;
import org.junit.gen5.commons.util.ReflectionUtils;
import org.junit.gen5.console.options.CommandLineOptions;
import org.junit.gen5.console.options.CommandLineOptionsParser;
import org.junit.gen5.console.options.JOptSimpleCommandLineOptionsParser;
import org.junit.gen5.engine.TestPlanSpecification;
import org.junit.gen5.engine.TestPlanSpecificationElement;
import org.junit.gen5.launcher.Launcher;
import org.junit.gen5.launcher.listeners.SummaryCreatingTestListener;
import org.junit.gen5.launcher.listeners.TestExecutionSummary;

/**
 * @since 5.0
 */
public class ConsoleRunner {

	public static void main(String... args) {
		CommandLineOptionsParser parser = new JOptSimpleCommandLineOptionsParser();
		CommandLineOptions options = parser.parse(args);

		if (options.isDisplayHelp()) {
			parser.printHelp();
			System.exit(0);
		}

		try {
			TestExecutionSummary summary = new ConsoleRunner(options).run();
			int exitCode = computeExitCode(options, summary);
			System.exit(exitCode);
		}
		catch (Exception e) {
			e.printStackTrace(System.err);
			System.err.println();
			parser.printHelp();
			System.exit(-1);
		}
	}

	private static int computeExitCode(CommandLineOptions options, TestExecutionSummary summary) {
		if (options.isExitCodeEnabled()) {
			long failedTests = summary.countFailedTests();
			return (int) Math.min(Integer.MAX_VALUE, failedTests);
		}
		return 0;
	}

	private final CommandLineOptions options;

	public ConsoleRunner(CommandLineOptions options) {
		this.options = options;
	}

	private TestExecutionSummary run() {
		// TODO Configure launcher?
		Launcher launcher = new Launcher();

		TestExecutionSummary summary = new TestExecutionSummary();
		registerListeners(launcher, summary);

		TestPlanSpecification testPlanSpecification = createTestPlanSpecification();
		launcher.execute(testPlanSpecification);

		printSummaryToStandardOut(summary);
		return summary;
	}

	private void registerListeners(Launcher launcher, TestExecutionSummary summary) {
		SummaryCreatingTestListener testSummaryListener = new SummaryCreatingTestListener(summary);
		launcher.registerTestPlanExecutionListeners(testSummaryListener);
		if (!options.isHideDetails()) {
			launcher.registerTestPlanExecutionListeners(
				new ColoredPrintingTestListener(System.out, options.isAnsiColorOutputDisabled()));
		}
	}

	private TestPlanSpecification createTestPlanSpecification() {
		TestPlanSpecification testPlanSpecification;
		if (options.isRunAllTests()) {
			Set<File> rootDirectoriesToScan = new HashSet<>();
			if (options.getArguments().isEmpty()) {
				rootDirectoriesToScan.addAll(ReflectionUtils.getAllClasspathRootDirectories());
			}
			else {
				options.getArguments().stream().map(File::new).forEach(rootDirectoriesToScan::add);
			}
			testPlanSpecification = TestPlanSpecification.build(allTests(rootDirectoriesToScan));
		}
		else {
			testPlanSpecification = TestPlanSpecification.build(testPlanSpecificationElementsFromArguments());
		}
		options.getClassnameFilter().ifPresent(
			classnameFilter -> testPlanSpecification.filterWith(classNameMatches(classnameFilter)));
		if (!options.getTagsFilter().isEmpty()) {
			testPlanSpecification.filterWith(byTags(options.getTagsFilter()));
		}
		return testPlanSpecification;
	}

	private void printSummaryToStandardOut(TestExecutionSummary summary) {
		PrintWriter stdout = new PrintWriter(System.out);

		if (options.isHideDetails()) { // Otherwise the failures have already been printed
			summary.printFailuresOn(stdout);
		}

		summary.printOn(stdout);
		stdout.close();
	}

	private List<TestPlanSpecificationElement> testPlanSpecificationElementsFromArguments() {
		Preconditions.notEmpty(options.getArguments(), "No arguments given");
		return toTestPlanSpecificationElements(options.getArguments());
	}

	List<TestPlanSpecificationElement> toTestPlanSpecificationElements(List<String> arguments) {
		return TestPlanSpecification.forNames(arguments);
	}

}
