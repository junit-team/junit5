/*
 * Copyright 2015-2020 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * https://www.eclipse.org/legal/epl-v20.html
 */

package org.junit.platform.console.options;

import static org.apiguardian.api.API.Status.INTERNAL;

import java.io.IOException;
import java.io.Writer;

import org.apiguardian.api.API;
import org.junit.platform.commons.JUnitException;

import picocli.CommandLine;
import picocli.CommandLine.ParseResult;

/**
 * @since 1.0
 */
@API(status = INTERNAL, since = "1.0")
public class PicocliCommandLineOptionsParser implements CommandLineOptionsParser {

	@Override
	public CommandLineOptions parse(String... arguments) {
		AvailableOptions availableOptions = getAvailableOptions();
		CommandLine parser = availableOptions.getParser();
		try {
			ParseResult detectedOptions = parser.parseArgs(arguments);
			return availableOptions.toCommandLineOptions(detectedOptions);
		}
		catch (Exception ex) {
			throw new JUnitException("Error parsing command-line arguments: " + ex.getMessage(), ex);
		}
	}

	@Override
	public void printHelp(Writer writer, boolean ansiColorOutputDisabled) {
		try {
			CommandLine parser = getAvailableOptions().getParser();
			if (ansiColorOutputDisabled) {
				parser.setColorScheme(CommandLine.Help.defaultColorScheme(CommandLine.Help.Ansi.OFF));
			}
			writer.append(parser.getUsageMessage());
		}
		catch (IOException ex) {
			throw new JUnitException("Error printing help", ex);
		}
	}

	private AvailableOptions getAvailableOptions() {
		return new AvailableOptions();
	}

}
