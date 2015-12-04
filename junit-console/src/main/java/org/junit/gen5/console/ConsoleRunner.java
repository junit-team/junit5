/*
 * Copyright 2015 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.junit.gen5.console;

import java.io.PrintStream;
import java.io.PrintWriter;

import org.junit.gen5.console.options.CommandLineOptions;
import org.junit.gen5.console.options.CommandLineOptionsParser;
import org.junit.gen5.console.options.JOptSimpleCommandLineOptionsParser;
import org.junit.gen5.console.tasks.ConsoleTask;
import org.junit.gen5.console.tasks.DisplayHelpTask;
import org.junit.gen5.console.tasks.ExecuteTestsTask;

/**
 * @since 5.0
 */
public class ConsoleRunner {

	public static void main(String... args) {
		CommandLineOptionsParser parser = new JOptSimpleCommandLineOptionsParser();
		CommandLineOptions options = parser.parse(args);
		ConsoleTask task = determineTask(parser, options);

		PrintWriter out = new PrintWriter(System.out);
		try {
			int exitCode = task.execute(out);
			System.exit(exitCode);
		}
		catch (Exception e) {
			printException(e, System.err);
			displayHelp(parser, out);
			System.exit(-1);
		}
	}

	private static ConsoleTask determineTask(CommandLineOptionsParser parser, CommandLineOptions options) {
		if (options.isDisplayHelp()) {
			return new DisplayHelpTask(parser);
		}
		return new ExecuteTestsTask(options);
	}

	private static void printException(Exception exception, PrintStream out) {
		exception.printStackTrace(out);
		out.println();
	}

	private static void displayHelp(CommandLineOptionsParser parser, PrintWriter out) {
		new DisplayHelpTask(parser).execute(out);
	}

}
