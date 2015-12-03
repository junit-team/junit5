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

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import io.airlift.airline.Arguments;
import io.airlift.airline.Command;
import io.airlift.airline.Option;

@Command(name = "ConsoleRunner", description = "console test runner")
public class AirlineCommandLineOptions implements CommandLineOptions {

	// @formatter:off
    @Option(name = {"-h", "--help"}, description = "Display help information")
    private boolean displayHelp;

	@Option(name = { "-x", "--enable-exit-code" },
			description = "Exit process with number of failing tests as exit code")
	private boolean exitCodeEnabled;

	@Option(name = { "-C", "--disable-ansi-colors" },
			description = "Disable colored output (not supported by all terminals)")
	private boolean ansiColorOutputDisabled;

	@Option(name = {"-a", "--all"}, description = "Run all tests")
	private boolean runAllTests;

	@Option(name = {"-D", "--hide-details"}, description = "Hide details while tests are being executed. "
			+ "Only show the summary and test failures.")
	private boolean hideDetails;

	@Option(name = {"-n", "--filter-classname"}, description = "Give a regular expression to include only classes whose fully qualified names match.")
	private String classnameFilter;

	@Option(name = {"-t", "--filter-tags"}, description = "Give a tag to include in the test run. This option can be repeated.")
	private List<String> tagsFilter = new ArrayList<>();

	@Arguments(description = "Test classes, methods or packages to execute."
			+ " If --all|-a has been chosen, arguments can list all classpath roots that should be considered for test scanning,"
			+ " or none if the full classpath shall be scanned.")
	private List<String> arguments = new ArrayList<>();
	// @formatter:on

	@Override
	public boolean isDisplayHelp() {
		return displayHelp;
	}

	@Override
	public boolean isExitCodeEnabled() {
		return exitCodeEnabled;
	}

	@Override
	public boolean isAnsiColorOutputDisabled() {
		return ansiColorOutputDisabled;
	}

	@Override
	public boolean isRunAllTests() {
		return runAllTests;
	}

	@Override
	public boolean isHideDetails() {
		return hideDetails;
	}

	@Override
	public Optional<String> getClassnameFilter() {
		return Optional.ofNullable(classnameFilter);
	}

	@Override
	public List<String> getTagsFilter() {
		return tagsFilter;
	}

	@Override
	public List<String> getArguments() {
		return arguments;
	}

}
