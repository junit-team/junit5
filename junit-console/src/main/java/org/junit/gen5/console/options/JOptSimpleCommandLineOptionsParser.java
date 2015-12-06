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

import java.io.IOException;
import java.io.Writer;

import joptsimple.BuiltinHelpFormatter;
import joptsimple.OptionParser;
import joptsimple.OptionSet;

public class JOptSimpleCommandLineOptionsParser implements CommandLineOptionsParser {

	@Override
	public CommandLineOptions parse(String... arguments) {
		AvailableOptions availableOptions = getAvailableOptions();
		OptionParser parser = availableOptions.getParser();
		OptionSet detectedOptions = parser.parse(arguments);
		return toCommandLineOptions(availableOptions, detectedOptions);
	}

	@Override
	public void printHelp(Writer writer) {
		OptionParser optionParser = getAvailableOptions().getParser();
		optionParser.formatHelpWith(new BuiltinHelpFormatter(100, 4));
		try {
			optionParser.printHelpOn(writer);
		}
		catch (IOException e) {
			throw new RuntimeException("Error printing help", e);
		}
	}

	private CommandLineOptions toCommandLineOptions(AvailableOptions options, OptionSet detectedOptions) {
		CommandLineOptions result = new CommandLineOptions();
		result.setDisplayHelp(detectedOptions.has(options.help));
		result.setExitCodeEnabled(detectedOptions.has(options.enableExitCode));
		result.setAnsiColorOutputDisabled(detectedOptions.has(options.disableAnsiColors));
		result.setRunAllTests(detectedOptions.has(options.runAllTests));
		result.setHideDetails(detectedOptions.has(options.hideDetails));
		result.setClassnameFilter(detectedOptions.valueOf(options.classnameFilter));
		result.setTagsFilter(detectedOptions.valuesOf(options.tagFilter));
		result.setAdditionalClasspathEntries(detectedOptions.valuesOf(options.additionalClasspathEntries));
		result.setArguments(detectedOptions.valuesOf(options.arguments));
		return result;
	}

	private AvailableOptions getAvailableOptions() {
		return new AvailableOptions();
	}

}
