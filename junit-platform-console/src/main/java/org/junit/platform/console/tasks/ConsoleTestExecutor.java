/*
 * Copyright 2015-2020 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * https://www.eclipse.org/legal/epl-v20.html
 */

package org.junit.platform.console.tasks;

import static org.apiguardian.api.API.Status.INTERNAL;

import java.io.PrintWriter;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Path;
import java.util.EnumSet;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;

import org.apiguardian.api.API;
import org.junit.platform.commons.JUnitException;
import org.junit.platform.commons.util.ClassLoaderUtils;
import org.junit.platform.console.options.CommandLineOptions;
import org.junit.platform.console.options.Details;
import org.junit.platform.console.options.Theme;
import org.junit.platform.launcher.Launcher;
import org.junit.platform.launcher.LauncherDiscoveryRequest;
import org.junit.platform.launcher.TestExecutionListener;
import org.junit.platform.launcher.core.LauncherFactory;
import org.junit.platform.launcher.listeners.SummaryGeneratingListener;
import org.junit.platform.launcher.listeners.TestExecutionSummary;
import org.junit.platform.reporting.legacy.xml.LegacyXmlReportGeneratingListener;

/**
 * @since 1.0
 */
@API(status = INTERNAL, since = "1.0")
public class ConsoleTestExecutor {

	private final CommandLineOptions options;
	private final Supplier<Launcher> launcherSupplier;

	public ConsoleTestExecutor(CommandLineOptions options) {
		this(options, LauncherFactory::create);
	}

	// for tests only
	ConsoleTestExecutor(CommandLineOptions options, Supplier<Launcher> launcherSupplier) {
		this.options = options;
		this.launcherSupplier = launcherSupplier;
	}

	public TestExecutionSummary execute(PrintWriter out) throws Exception {
		return new CustomContextClassLoaderExecutor(createCustomClassLoader()).invoke(() -> executeTests(out));
	}

	private TestExecutionSummary executeTests(PrintWriter out) {
		Launcher launcher = launcherSupplier.get();
		SummaryGeneratingListener summaryListener = registerListeners(out, launcher);

		LauncherDiscoveryRequest discoveryRequest = new DiscoveryRequestCreator().toDiscoveryRequest(options);
		launcher.execute(discoveryRequest);

		TestExecutionSummary summary = summaryListener.getSummary();
		if (summary.getTotalFailureCount() > 0 || options.getDetails() != Details.NONE) {
			printSummary(summary, out);
		}

		return summary;
	}

	private Optional<ClassLoader> createCustomClassLoader() {
		List<Path> additionalClasspathEntries = options.getAdditionalClasspathEntries();
		if (!additionalClasspathEntries.isEmpty()) {
			URL[] urls = additionalClasspathEntries.stream().map(this::toURL).toArray(URL[]::new);
			ClassLoader parentClassLoader = ClassLoaderUtils.getDefaultClassLoader();
			ClassLoader customClassLoader = URLClassLoader.newInstance(urls, parentClassLoader);
			return Optional.of(customClassLoader);
		}
		return Optional.empty();
	}

	private URL toURL(Path path) {
		try {
			return path.toUri().toURL();
		}
		catch (Exception ex) {
			throw new JUnitException("Invalid classpath entry: " + path, ex);
		}
	}

	private SummaryGeneratingListener registerListeners(PrintWriter out, Launcher launcher) {
		// always register summary generating listener
		SummaryGeneratingListener summaryListener = new SummaryGeneratingListener();
		launcher.registerTestExecutionListeners(summaryListener);
		// optionally, register test plan execution details printing listener
		createDetailsPrintingListener(out).ifPresent(launcher::registerTestExecutionListeners);
		// optionally, register XML reports writing listener
		createXmlWritingListener(out).ifPresent(launcher::registerTestExecutionListeners);
		return summaryListener;
	}

	private Optional<TestExecutionListener> createDetailsPrintingListener(PrintWriter out) {
		boolean disableAnsiColors = options.isAnsiColorOutputDisabled();
		Theme theme = options.getTheme();
		switch (options.getDetails()) {
			case SUMMARY:
				// summary listener is always created and registered
				return Optional.empty();
			case FLAT:
				return Optional.of(new FlatPrintingListener(out, disableAnsiColors));
			case TREE:
				return Optional.of(new TreePrintingListener(out, disableAnsiColors, theme));
			case VERBOSE:
				return Optional.of(new VerboseTreePrintingListener(out, disableAnsiColors, 16, theme));
			default:
				return Optional.empty();
		}
	}

	private Optional<TestExecutionListener> createXmlWritingListener(PrintWriter out) {
		return options.getReportsDir().map(reportsDir -> new LegacyXmlReportGeneratingListener(reportsDir, out));
	}

	private void printSummary(TestExecutionSummary summary, PrintWriter out) {
		// Otherwise the failures have already been printed in detail
		if (EnumSet.of(Details.NONE, Details.SUMMARY, Details.TREE).contains(options.getDetails())) {
			summary.printFailuresTo(out);
		}
		summary.printTo(out);
	}

}
