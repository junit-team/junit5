/*
 * Copyright 2015 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.junit.gen5.console.tasks;

import java.io.PrintWriter;

import org.junit.gen5.console.options.CommandLineOptionsParser;

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
