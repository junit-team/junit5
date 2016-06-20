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

import joptsimple.OptionParser;
import joptsimple.OptionSet;
import joptsimple.OptionSpec;

/**
 * @since 5.0
 */
class AvailableOptions {

	private final OptionParser parser = new OptionParser();

	private final OptionSpec<Void> help;
	private final OptionSpec<Void> enableExitCode;
	private final OptionSpec<Void> disableAnsiColors;
	private final OptionSpec<Void> runAllTests;
	private final OptionSpec<Void> hideDetails;
	private final OptionSpec<String> includeClassNamePattern;
	private final OptionSpec<String> includeTag;
	private final OptionSpec<String> excludeTag;
	private final OptionSpec<String> includeEngine;
	private final OptionSpec<String> excludeEngine;
	private final OptionSpec<String> additionalClasspathEntries;
	private final OptionSpec<String> xmlReportsDir;
	private final OptionSpec<String> arguments;

	AvailableOptions() {

		runAllTests = parser.acceptsAll(asList("a", "all"), //
			"Run all tests");

		additionalClasspathEntries = parser.acceptsAll(asList("p", "classpath"), //
			"Provide additional classpath entries -- for example, for adding engines and their dependencies.") //
				.withRequiredArg();

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

		xmlReportsDir = parser.acceptsAll(asList("r", "xml-reports-dir"), //
			"Enable XML report output into a specified local directory (will be created if it does not exist).") //
				.withRequiredArg();

		enableExitCode = parser.acceptsAll(asList("x", "enable-exit-code"), //
			"Exit process with number of failing tests as exit code.");
		disableAnsiColors = parser.acceptsAll(asList("C", "disable-ansi-colors"),
			"Disable colored output (not supported by all terminals).");
		hideDetails = parser.acceptsAll(asList("D", "hide-details"),
			"Hide details while tests are being executed. Only show the summary and test failures.");
		help = parser.acceptsAll(asList("h", "help"), //
			"Display help information.");

		arguments = parser.nonOptions("Test classes, methods, or packages to execute. If --all|-a has been provided, "
				+ "arguments can list all classpath roots that should be considered for test scanning, "
				+ "or none if the full classpath should be scanned.");
	}

	OptionParser getParser() {
		return parser;
	}

	CommandLineOptions toCommandLineOptions(OptionSet detectedOptions) {
		CommandLineOptions result = new CommandLineOptions();

		result.setDisplayHelp(detectedOptions.has(this.help));
		result.setExitCodeEnabled(detectedOptions.has(this.enableExitCode));
		result.setAnsiColorOutputDisabled(detectedOptions.has(this.disableAnsiColors));
		result.setRunAllTests(detectedOptions.has(this.runAllTests));
		result.setHideDetails(detectedOptions.has(this.hideDetails));

		result.setIncludeClassNamePattern(detectedOptions.valueOf(this.includeClassNamePattern));
		result.setIncludedTags(detectedOptions.valuesOf(this.includeTag));
		result.setExcludedTags(detectedOptions.valuesOf(this.excludeTag));
		result.setIncludedEngines(detectedOptions.valuesOf(this.includeEngine));
		result.setExcludedEngines(detectedOptions.valuesOf(this.excludeEngine));

		result.setAdditionalClasspathEntries(detectedOptions.valuesOf(this.additionalClasspathEntries));
		result.setXmlReportsDir(detectedOptions.valueOf(this.xmlReportsDir));
		result.setArguments(detectedOptions.valuesOf(this.arguments));

		return result;
	}

}
