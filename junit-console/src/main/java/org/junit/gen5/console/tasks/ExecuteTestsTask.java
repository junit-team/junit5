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

import java.io.PrintWriter;
import java.io.Writer;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.List;

import org.junit.gen5.commons.util.ReflectionUtils;
import org.junit.gen5.console.options.CommandLineOptions;
import org.junit.gen5.engine.TestPlanSpecification;
import org.junit.gen5.launcher.Launcher;
import org.junit.gen5.launcher.listeners.SummaryCreatingTestListener;
import org.junit.gen5.launcher.listeners.TestExecutionSummary;

/**
 * @since 5.0
 */
public class ExecuteTestsTask implements ConsoleTask {

	private final CommandLineOptions options;

	public ExecuteTestsTask(CommandLineOptions options) {
		this.options = options;
	}

	@Override
	public int execute(PrintWriter out) {
		// Track original Thread Context ClassLoader
		ClassLoader originalTccl = Thread.currentThread().getContextClassLoader();

		try {
			replaceThreadContextClassLoader();

			// TODO Configure launcher?
			Launcher launcher = new Launcher();

			TestExecutionSummary summary = new TestExecutionSummary();
			registerListeners(launcher, summary, out);

			TestPlanSpecification specification = new TestPlanSpecificationCreator().toTestPlanSpecification(options);
			launcher.execute(specification);

			printSummary(summary, out);

			return computeExitCode(summary);
		}
		finally {
			Thread.currentThread().setContextClassLoader(originalTccl);
		}
	}

	private void replaceThreadContextClassLoader() {
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

	private void printSummary(TestExecutionSummary summary, Writer out) {
		PrintWriter printWriter = new PrintWriter(out);

		if (options.isHideDetails()) { // Otherwise the failures have already been printed
			summary.printFailuresOn(printWriter);
		}

		summary.printOn(printWriter);
	}

	private int computeExitCode(TestExecutionSummary summary) {
		if (options.isExitCodeEnabled()) {
			long failedTests = summary.countFailedTests();
			return (int) Math.min(Integer.MAX_VALUE, failedTests);
		}
		return 0;
	}
}
