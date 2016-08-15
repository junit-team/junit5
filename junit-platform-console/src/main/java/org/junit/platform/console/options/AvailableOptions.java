/*
 * Copyright 2015-2016 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.junit.platform.console.options;

import static java.util.Arrays.asList;

import java.io.File;

import joptsimple.OptionParser;
import joptsimple.OptionSet;
import joptsimple.OptionSpec;

/**
 * @since 1.0
 */
class AvailableOptions {

	private final OptionParser parser = new OptionParser();

	// General Purpose
	private final OptionSpec<Void> help;
	private final OptionSpec<Void> disableAnsiColors;
	private final OptionSpec<Void> hideDetails;
	private final OptionSpec<Void> runAllTests;
	private final OptionSpec<File> additionalClasspathEntries;

	// Reports
	private final OptionSpec<File> xmlReportsDir;

	// Selectors
	private final OptionSpec<String> arguments;

	// Filters
	private final OptionSpec<String> includeClassNamePattern;
	private final OptionSpec<String> includeTag;
	private final OptionSpec<String> excludeTag;
	private final OptionSpec<String> includeEngine;
	private final OptionSpec<String> excludeEngine;

	AvailableOptions() {

		// --- General Purpose -------------------------------------------------

		help = parser.acceptsAll(asList("h", "help"), //
			"Display help information.");

		disableAnsiColors = parser.acceptsAll(asList("C", "disable-ansi-colors"),
			"Disable colored output (not supported by all terminals).");

		hideDetails = parser.acceptsAll(asList("D", "hide-details"),
			"Hide details while tests are being executed. Only show the summary and test failures.");

		runAllTests = parser.acceptsAll(asList("a", "all"), //
			"Run all tests.");

		additionalClasspathEntries = parser.acceptsAll(asList("cp", "classpath", "class-path"), //
			"Provide additional classpath entries -- for example, for adding engines and their dependencies. "
					+ "This option can be repeated.") //
				.withRequiredArg().ofType(File.class).withValuesSeparatedBy(File.pathSeparatorChar).describedAs(
					"path1" + File.pathSeparator + "path2" + File.pathSeparator + "...");

		// --- Reports ---------------------------------------------------------

		xmlReportsDir = parser.acceptsAll(asList("r", "xml-reports-dir"), //
			"Enable XML report output into a specified local directory (will be created if it does not exist).") //
				.withRequiredArg().ofType(File.class);

		// --- Selectors -------------------------------------------------------

		arguments = parser.nonOptions("Test classes, methods, or packages to execute. If --all|-a has been specified, "
				+ "non-option arguments represent all classpath roots that should be considered for test scanning, "
				+ "or none if the full classpath should be scanned.");

		// --- Filters ---------------------------------------------------------

		includeClassNamePattern = parser.acceptsAll(asList("n", "include-classname"),
			"Provide a regular expression to include only classes whose fully qualified names match. " //
					+ "By default any class name is accepted, and thus all classes with tests are included.") //
				.withRequiredArg();

		includeTag = parser.acceptsAll(asList("t", "include-tag"),
			"Provide a tag to be included in the test run. This option can be repeated.") //
				.withRequiredArg();
		excludeTag = parser.acceptsAll(asList("T", "exclude-tag"),
			"Provide a tag to be excluded from the test run. This option can be repeated.") //
				.withRequiredArg();

		includeEngine = parser.acceptsAll(asList("e", "include-engine"),
			"Provide the ID of an engine to be included in the test run. This option can be repeated.") //
				.withRequiredArg();
		excludeEngine = parser.acceptsAll(asList("E", "exclude-engine"),
			"Provide the ID of an engine to be excluded from the test run. This option can be repeated.") //
				.withRequiredArg();
	}

	OptionParser getParser() {
		return parser;
	}

	CommandLineOptions toCommandLineOptions(OptionSet detectedOptions) {

		CommandLineOptions result = new CommandLineOptions();

		// General Purpose
		result.setDisplayHelp(detectedOptions.has(this.help));
		result.setAnsiColorOutputDisabled(detectedOptions.has(this.disableAnsiColors));
		result.setHideDetails(detectedOptions.has(this.hideDetails));
		result.setRunAllTests(detectedOptions.has(this.runAllTests));
		result.setAdditionalClasspathEntries(detectedOptions.valuesOf(this.additionalClasspathEntries));

		// Reports
		result.setXmlReportsDir(detectedOptions.valueOf(this.xmlReportsDir));

		// Selectors
		result.setArguments(detectedOptions.valuesOf(this.arguments));

		// Filters
		result.setIncludeClassNamePattern(detectedOptions.valueOf(this.includeClassNamePattern));
		result.setIncludedTags(detectedOptions.valuesOf(this.includeTag));
		result.setExcludedTags(detectedOptions.valuesOf(this.excludeTag));
		result.setIncludedEngines(detectedOptions.valuesOf(this.includeEngine));
		result.setExcludedEngines(detectedOptions.valuesOf(this.excludeEngine));

		return result;
	}

}
