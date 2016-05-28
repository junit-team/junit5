/*
 * Copyright 2015-2016 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.junit.gen5.console.tasks;

import static org.junit.gen5.commons.meta.API.Usage.Internal;

import java.io.PrintWriter;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;

import org.junit.gen5.commons.meta.API;
import org.junit.gen5.commons.util.ReflectionUtils;
import org.junit.gen5.console.options.CommandLineOptions;
import org.junit.gen5.launcher.Launcher;
import org.junit.gen5.launcher.TestDiscoveryRequest;
import org.junit.gen5.launcher.listeners.SummaryGeneratingListener;
import org.junit.gen5.launcher.listeners.TestExecutionSummary;
import org.junit.gen5.launcher.main.LauncherFactory;

/**
 * @since 5.0
 */
@API(Internal)
public class ExecuteTestsTask implements ConsoleTask {

	private final CommandLineOptions options;
	private final Supplier<Launcher> launcherSupplier;

	public ExecuteTestsTask(CommandLineOptions options) {
		this(options, LauncherFactory::create);
	}

	// for tests only
	ExecuteTestsTask(CommandLineOptions options, Supplier<Launcher> launcherSupplier) {
		this.options = options;
		this.launcherSupplier = launcherSupplier;
	}

	@Override
	public int execute(PrintWriter out) throws Exception {
		return new CustomContextClassLoaderExecutor(createCustomClassLoader()).invoke(() -> executeTests(out));
	}

	private int executeTests(PrintWriter out) {
		Launcher launcher = launcherSupplier.get();
		// TODO Configure launcher?

		SummaryGeneratingListener summaryListener = registerListeners(out, launcher);

		TestDiscoveryRequest discoveryRequest = new DiscoveryRequestCreator().toDiscoveryRequest(options);
		launcher.execute(discoveryRequest);

		TestExecutionSummary summary = summaryListener.getSummary();
		printSummary(summary, out);

		return computeExitCode(summary);
	}

	private Optional<ClassLoader> createCustomClassLoader() {
		List<String> additionalClasspathEntries = options.getAdditionalClasspathEntries();
		if (!additionalClasspathEntries.isEmpty()) {
			URL[] urls = new ClasspathEntriesParser().toURLs(additionalClasspathEntries);
			ClassLoader parentClassLoader = ReflectionUtils.getDefaultClassLoader();
			ClassLoader customClassLoader = URLClassLoader.newInstance(urls, parentClassLoader);
			return Optional.of(customClassLoader);
		}
		return Optional.empty();
	}

	private SummaryGeneratingListener registerListeners(PrintWriter out, Launcher launcher) {
		SummaryGeneratingListener summaryListener = new SummaryGeneratingListener();
		launcher.registerTestExecutionListeners(summaryListener);
		if (!options.isHideDetails()) {
			launcher.registerTestExecutionListeners(
				new ColoredPrintingTestListener(out, options.isAnsiColorOutputDisabled()));
		}
		if (options.getXmlReportsDir().isPresent()) {
			launcher.registerTestExecutionListeners(
				new XmlReportsWritingListener(options.getXmlReportsDir().get(), out));
		}
		return summaryListener;
	}

	private void printSummary(TestExecutionSummary summary, PrintWriter out) {
		if (options.isHideDetails()) { // Otherwise the failures have already been printed
			summary.printFailuresTo(out);
		}
		summary.printTo(out);
	}

	private int computeExitCode(TestExecutionSummary summary) {
		if (options.isExitCodeEnabled()) {
			long failedTests = summary.getTestsFailedCount();
			return (int) Math.min(Integer.MAX_VALUE, failedTests);
		}
		return SUCCESS;
	}
}
