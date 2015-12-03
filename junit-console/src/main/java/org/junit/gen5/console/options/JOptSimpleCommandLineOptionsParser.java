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

import java.io.IOException;

import joptsimple.BuiltinHelpFormatter;
import joptsimple.OptionParser;
import joptsimple.OptionSet;

public class JOptSimpleCommandLineOptionsParser implements CommandLineOptionsParser {

	@Override
	public CommandLineOptions parse(String... arguments) {
		OptionParser parser = getOptionParser();
		OptionSet optionSet = parser.parse(arguments);
		return new JOptSimpleCommandLineOptions(optionSet);
	}

	@Override
	public void printHelp() {
		OptionParser optionParser = getOptionParser();
		optionParser.formatHelpWith(new BuiltinHelpFormatter(100, 4));
		try {
			optionParser.printHelpOn(System.out);
		}
		catch (IOException e) {
			throw new RuntimeException("Error printing help", e);
		}
	}

	private OptionParser getOptionParser() {
		OptionParser parser = new OptionParser();
		parser.acceptsAll(asList("h", "help"), //
			"Display help information");
		parser.acceptsAll(asList("x", "enable-exit-code"), //
			"Exit process with number of failing tests as exit code");
		parser.acceptsAll(asList("C", "disable-ansi-colors"),
			"Disable colored output (not supported by all terminals)");
		parser.acceptsAll(asList("a", "all"), //
			"Run all tests");
		parser.acceptsAll(asList("D", "hide-details"),
			"Hide details while tests are being executed. Only show the summary and test failures.");
		parser.acceptsAll(asList("n", "filter-classname"),
			"Give a regular expression to include only classes whose fully qualified names match.")//
			.withRequiredArg();
		parser.acceptsAll(asList("t", "filter-tags"),
			"Give a tag to include in the test run. This option can be repeated.")//
			.withRequiredArg();
		parser.allowsUnrecognizedOptions();
		return parser;
	}

}
