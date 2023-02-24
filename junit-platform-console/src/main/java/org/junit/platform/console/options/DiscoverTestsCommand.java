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

import org.junit.platform.console.tasks.ConsoleTestExecutor;

public class DiscoverTestsCommand implements Command<Void> {
	private final CommandLineOptions options;

	public DiscoverTestsCommand(CommandLineOptions options) {
		this.options = options;
	}

	@Override
	public CommandResult<Void> run(PrintWriter out, PrintWriter err) throws Exception {
		new ConsoleTestExecutor(options).discover(out);
		return CommandResult.success();
	}
}
