/*
 * Copyright 2015 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.junit.gen5.console.tasks;

import static org.junit.gen5.engine.TestPlanSpecification.allTests;
import static org.junit.gen5.engine.TestPlanSpecification.byTags;
import static org.junit.gen5.engine.TestPlanSpecification.classNameMatches;

import java.io.File;
import java.io.PrintWriter;
import java.io.Writer;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.gen5.commons.util.Preconditions;
import org.junit.gen5.commons.util.ReflectionUtils;
import org.junit.gen5.console.options.CommandLineOptions;
import org.junit.gen5.engine.TestPlanSpecification;
import org.junit.gen5.engine.TestPlanSpecificationElement;
import org.junit.gen5.launcher.Launcher;
import org.junit.gen5.launcher.listeners.SummaryCreatingTestListener;
import org.junit.gen5.launcher.listeners.TestExecutionSummary;

public class ExecuteTestsTask implements ConsoleTask {

	private final CommandLineOptions options;

	public ExecuteTestsTask(CommandLineOptions options) {
		this.options = options;
	}

	@Override
	public int execute(PrintWriter out) {
		updateClassLoader();

		// TODO Configure launcher?
		Launcher launcher = new Launcher();

		TestExecutionSummary summary = new TestExecutionSummary();
		registerListeners(launcher, summary, out);

		TestPlanSpecification testPlanSpecification = createTestPlanSpecification();
		launcher.execute(testPlanSpecification);

		printSummary(summary, out);

		return computeExitCode(summary);
	}

	private void updateClassLoader() {
		List<String> additionalClasspathEntries = options.getAdditionalClasspathEntries();
		if (!additionalClasspathEntries.isEmpty()) {
			URL[] urls = new ClasspathEntriesParser().toURLs(additionalClasspathEntries);
			ClassLoader parentClassLoader = ReflectionUtils.getDefaultClassLoader();
			URLClassLoader customClassLoader = URLClassLoader.newInstance(urls, parentClassLoader);
			Thread.currentThread().setContextClassLoader(customClassLoader);
		}
	}

	private void registerListeners(Launcher launcher, TestExecutionSummary summary, PrintWriter out) {
		SummaryCreatingTestListener testSummaryListener = new SummaryCreatingTestListener(summary);
		launcher.registerTestPlanExecutionListeners(testSummaryListener);
		if (!options.isHideDetails()) {
			launcher.registerTestPlanExecutionListeners(
				new ColoredPrintingTestListener(out, options.isAnsiColorOutputDisabled()));
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

	private void printSummary(TestExecutionSummary summary, Writer out) {
		PrintWriter printWriter = new PrintWriter(out);

		if (options.isHideDetails()) { // Otherwise the failures have already been printed
			summary.printFailuresOn(printWriter);
		}

		summary.printOn(printWriter);
	}

	private List<TestPlanSpecificationElement> testPlanSpecificationElementsFromArguments() {
		Preconditions.notEmpty(options.getArguments(), "No arguments given");
		return toTestPlanSpecificationElements(options.getArguments());
	}

	private List<TestPlanSpecificationElement> toTestPlanSpecificationElements(List<String> arguments) {
		return TestPlanSpecification.forNames(arguments);
	}

	private int computeExitCode(TestExecutionSummary summary) {
		if (options.isExitCodeEnabled()) {
			long failedTests = summary.countFailedTests();
			return (int) Math.min(Integer.MAX_VALUE, failedTests);
		}
		return 0;
	}
}
