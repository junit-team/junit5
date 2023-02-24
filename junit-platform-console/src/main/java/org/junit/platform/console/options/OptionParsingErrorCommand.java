/*
 * Copyright 2015-2023 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * https://www.eclipse.org/legal/epl-v20.html
 */

package org.junit.platform.console.options;

import java.io.PrintWriter;

class OptionParsingErrorCommand<T> implements SafeCommand<T> {

	private final Exception failure;
	private final HelpCommand<T> helpCommand;

	public OptionParsingErrorCommand(Exception failure, HelpCommand<T> helpCommand) {
		this.failure = failure;
		this.helpCommand = helpCommand;
	}

	@Override
	public CommandResult<T> run(PrintWriter out, PrintWriter err) {
		out.println("Error parsing command-line arguments: " + failure.getMessage());
		return helpCommand.run(out, err);
	}
}
