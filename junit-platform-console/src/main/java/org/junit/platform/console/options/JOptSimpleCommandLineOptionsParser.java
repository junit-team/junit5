/*
 * Copyright 2015-2018 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v20.html
 */

package org.junit.platform.console.options;

import static org.apiguardian.api.API.Status.INTERNAL;

import java.io.IOException;
import java.io.Writer;
import java.util.LinkedHashSet;
import java.util.Map;

import joptsimple.BuiltinHelpFormatter;
import joptsimple.OptionDescriptor;
import joptsimple.OptionException;
import joptsimple.OptionParser;
import joptsimple.OptionSet;

import org.apiguardian.api.API;
import org.junit.platform.commons.JUnitException;

/**
 * @since 1.0
 */
@API(status = INTERNAL, since = "1.0")
public class JOptSimpleCommandLineOptionsParser implements CommandLineOptionsParser {

	@Override
	public CommandLineOptions parse(String... arguments) {
		AvailableOptions availableOptions = getAvailableOptions();
		OptionParser parser = availableOptions.getParser();
		try {
			OptionSet detectedOptions = parser.parse(arguments);
			return availableOptions.toCommandLineOptions(detectedOptions);
		}
		catch (OptionException e) {
			throw new JUnitException("Error parsing command-line arguments", e);
		}
	}

	@Override
	public void printHelp(Writer writer) {
		OptionParser optionParser = getAvailableOptions().getParser();
		optionParser.formatHelpWith(new OrderPreservingHelpFormatter());
		try {
			optionParser.printHelpOn(writer);
		}
		catch (IOException e) {
			throw new JUnitException("Error printing help", e);
		}
	}

	private AvailableOptions getAvailableOptions() {
		return new AvailableOptions();
	}

	private static final class OrderPreservingHelpFormatter extends BuiltinHelpFormatter {

		private OrderPreservingHelpFormatter() {
			super(90, 4);
		}

		@Override
		public String format(Map<String, ? extends OptionDescriptor> options) {
			addRows(new LinkedHashSet<>(options.values()));
			return formattedHelpOutput();
		}
	}
}
