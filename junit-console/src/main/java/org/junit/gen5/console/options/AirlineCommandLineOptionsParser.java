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

import static io.airlift.airline.Help.help;
import static io.airlift.airline.SingleCommand.singleCommand;
import static io.airlift.airline.model.MetadataLoader.loadCommand;

public class AirlineCommandLineOptionsParser implements CommandLineOptionsParser {

	@Override
	public CommandLineOptions parse(String... arguments) {
		return singleCommand(AirlineCommandLineOptions.class).parse(arguments);
	}

	@Override
	public void printHelp() {
		help(loadCommand(AirlineCommandLineOptions.class));
	}

}
