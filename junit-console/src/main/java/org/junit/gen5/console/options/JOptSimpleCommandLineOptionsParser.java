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
		return availableOptions.toCommandLineOptions(detectedOptions);
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

	private AvailableOptions getAvailableOptions() {
		return new AvailableOptions();
	}

}
