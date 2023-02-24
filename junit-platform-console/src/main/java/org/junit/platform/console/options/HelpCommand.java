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

public class HelpCommand<T> implements SafeCommand<T> {

	private final boolean ansiColorOutputDisabled;
	private final CommandLineOptionsParser optionsParser;
	private final CommandResult<T> result;

	public HelpCommand(CommandLineOptionsParser optionsParser, boolean ansiColorOutputDisabled,
			CommandResult<T> result) {
		this.ansiColorOutputDisabled = ansiColorOutputDisabled;
		this.optionsParser = optionsParser;
		this.result = result;
	}

	@Override
	public CommandResult<T> run(PrintWriter out, PrintWriter err) {
		optionsParser.printHelp(out, ansiColorOutputDisabled);
		return result;
	}
}
