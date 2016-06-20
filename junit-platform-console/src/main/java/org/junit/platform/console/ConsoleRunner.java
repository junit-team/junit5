/*
 * Copyright 2015-2016 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.junit.platform.console;

import static org.junit.platform.commons.meta.API.Usage.Maintained;

import java.io.PrintWriter;

import org.junit.platform.commons.meta.API;
import org.junit.platform.console.options.CommandLineOptions;
import org.junit.platform.console.options.CommandLineOptionsParser;
import org.junit.platform.console.options.JOptSimpleCommandLineOptionsParser;
import org.junit.platform.console.tasks.ConsoleTask;
import org.junit.platform.console.tasks.ConsoleTaskExecutor;
import org.junit.platform.console.tasks.DisplayHelpTask;
import org.junit.platform.console.tasks.ExecuteTestsTask;

/**
 * @since 5.0
 */
@API(Maintained)
public class ConsoleRunner {

	@API(Maintained)
	public static void main(String... args) {
		ConsoleRunner consoleRunner = new ConsoleRunner(new JOptSimpleCommandLineOptionsParser(),
			new ConsoleTaskExecutor(System.out, System.err));
		int exitCode = consoleRunner.execute(args);
		System.exit(exitCode);
	}

	private final CommandLineOptionsParser commandLineOptionsParser;
	private final ConsoleTaskExecutor consoleTaskExecutor;

	ConsoleRunner(CommandLineOptionsParser commandLineOptionsParser, ConsoleTaskExecutor consoleTaskExecutor) {
		this.commandLineOptionsParser = commandLineOptionsParser;
		this.consoleTaskExecutor = consoleTaskExecutor;
	}

	int execute(String... args) {
		CommandLineOptions options = commandLineOptionsParser.parse(args);
		ConsoleTask task = determineTask(options);
		return consoleTaskExecutor.executeTask(task, this::displayHelp);
	}

	private ConsoleTask determineTask(CommandLineOptions options) {
		if (options.isDisplayHelp()) {
			return new DisplayHelpTask(commandLineOptionsParser);
		}
		return new ExecuteTestsTask(options);
	}

	void displayHelp(PrintWriter out) {
		new DisplayHelpTask(commandLineOptionsParser).execute(out);
	}

}
