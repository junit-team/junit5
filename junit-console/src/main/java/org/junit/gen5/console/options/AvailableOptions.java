/*
 * Copyright 2015-2016 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.junit.gen5.console.options;

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
	private final OptionSpec<String> classnameFilter;
	private final OptionSpec<String> requiredTagsFilter;
	private final OptionSpec<String> excludedTagsFilter;
	private final OptionSpec<String> requiredEngineFilter;
	private final OptionSpec<String> additionalClasspathEntries;
	private final OptionSpec<String> xmlReportsDir;
	private final OptionSpec<String> arguments;

	AvailableOptions() {
		runAllTests = parser.acceptsAll(asList("a", "all"), //
			"Run all tests");
		additionalClasspathEntries = parser.acceptsAll(asList("p", "classpath"), //
			"Additional classpath entries, e.g. for adding engines and their dependencies") //
				.withRequiredArg();

		classnameFilter = parser.acceptsAll(asList("n", "filter-classname"),
			"Give a regular expression to include only classes whose fully qualified names match. " //
					+ "By default any class name is accepted and thus all classes with tests are included.") //
				.withRequiredArg();
		requiredTagsFilter = parser.acceptsAll(asList("t", "require-tag"),
			"Give a tag to be required in the test run. This option can be repeated.") //
				.withRequiredArg();
		excludedTagsFilter = parser.acceptsAll(asList("T", "exclude-tag"),
			"Give a tag to exclude from the test run. This option can be repeated.") //
				.withRequiredArg();
		requiredEngineFilter = parser.acceptsAll(asList("e", "require-engine"),
			"Give the engine id of the only engine to be used in the test run.") //
				.withRequiredArg();

		xmlReportsDir = parser.acceptsAll(asList("r", "xml-reports-dir"), //
			"Enable XML report output into a specified local directory (will be created if it does not exist)") //
				.withRequiredArg();

		enableExitCode = parser.acceptsAll(asList("x", "enable-exit-code"), //
			"Exit process with number of failing tests as exit code");
		disableAnsiColors = parser.acceptsAll(asList("C", "disable-ansi-colors"),
			"Disable colored output (not supported by all terminals)");
		hideDetails = parser.acceptsAll(asList("D", "hide-details"),
			"Hide details while tests are being executed. Only show the summary and test failures.");
		help = parser.acceptsAll(asList("h", "help"), //
			"Display help information");

		arguments = parser.nonOptions("Test classes, methods or packages to execute. If --all|-a has been chosen, "
				+ "arguments can list all classpath roots that should be considered for test scanning, "
				+ "or none if the full classpath shall be scanned.");
	}

	OptionParser getParser() {
		return parser;
	}

	CommandLineOptions toCommandLineOptions(OptionSet detectedOptions) {
		CommandLineOptions result = new CommandLineOptions();
		result.setDisplayHelp(detectedOptions.has(help));
		result.setExitCodeEnabled(detectedOptions.has(enableExitCode));
		result.setAnsiColorOutputDisabled(detectedOptions.has(disableAnsiColors));
		result.setRunAllTests(detectedOptions.has(runAllTests));
		result.setHideDetails(detectedOptions.has(hideDetails));
		result.setClassnameFilter(detectedOptions.valueOf(classnameFilter));
		result.setRequiredTagsFilter(detectedOptions.valuesOf(requiredTagsFilter));
		result.setExcludedTagsFilter(detectedOptions.valuesOf(excludedTagsFilter));
		result.setRequiredEngineFilter(detectedOptions.valueOf(requiredEngineFilter));
		result.setAdditionalClasspathEntries(detectedOptions.valuesOf(additionalClasspathEntries));
		result.setXmlReportsDir(detectedOptions.valueOf(xmlReportsDir));
		result.setArguments(detectedOptions.valuesOf(arguments));
		return result;
	}

}
