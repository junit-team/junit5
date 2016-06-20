/*
 * Copyright 2015-2016 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.junit.platform.console.tasks;

import static org.junit.platform.commons.meta.API.Usage.Internal;

import java.io.PrintWriter;

import org.junit.platform.commons.meta.API;
import org.junit.platform.console.options.CommandLineOptionsParser;

/**
 * @since 5.0
 */
@API(Internal)
public class DisplayHelpTask implements ConsoleTask {

	private final CommandLineOptionsParser parser;

	public DisplayHelpTask(CommandLineOptionsParser parser) {
		this.parser = parser;
	}

	@Override
	public int execute(PrintWriter out) {
		parser.printHelp(out);
		return SUCCESS;
	}

}
