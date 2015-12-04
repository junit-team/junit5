/*
 * Copyright 2015 the original author or authors.
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
import joptsimple.OptionSpec;

class AvailableOptions {

	private final OptionParser parser = new OptionParser();

	final OptionSpec<Void> help;
	final OptionSpec<Void> enableExitCode;
	final OptionSpec<Void> disableAnsiColors;
	final OptionSpec<Void> runAllTests;
	final OptionSpec<Void> hideDetails;
	final OptionSpec<String> classnameFilter;
	final OptionSpec<String> tagFilter;
	final OptionSpec<String> additionalClasspathEntries;
	final OptionSpec<String> arguments;

	AvailableOptions() {
		help = parser.acceptsAll(asList("h", "help"), //
			"Display help information");
		enableExitCode = parser.acceptsAll(asList("x", "enable-exit-code"), //
			"Exit process with number of failing tests as exit code");
		disableAnsiColors = parser.acceptsAll(asList("C", "disable-ansi-colors"),
			"Disable colored output (not supported by all terminals)");
		runAllTests = parser.acceptsAll(asList("a", "all"), //
			"Run all tests");
		hideDetails = parser.acceptsAll(asList("D", "hide-details"),
			"Hide details while tests are being executed. Only show the summary and test failures.");
		classnameFilter = parser.acceptsAll(asList("n", "filter-classname"),
			"Give a regular expression to include only classes whose fully qualified names match.") //
			.withRequiredArg();
		tagFilter = parser.acceptsAll(asList("t", "filter-tags"),
			"Give a tag to include in the test run. This option can be repeated.") //
			.withRequiredArg();
		additionalClasspathEntries = parser.acceptsAll(asList("p", "classpath"), //
			"Additional classpath entries, e.g. for adding engines and their dependencies") //
			.withRequiredArg();
		arguments = parser.nonOptions("Test classes, methods or packages to execute. If --all|-a has been chosen, "
				+ "arguments can list all classpath roots that should be considered for test scanning, "
				+ "or none if the full classpath shall be scanned.");
	}

	public OptionParser getParser() {
		return parser;
	}
}
