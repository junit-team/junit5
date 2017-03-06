/*
 * Copyright 2015-2017 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.junit.platform.console;

import static org.junit.platform.commons.meta.API.Usage.Maintained;

import java.io.PrintStream;
import java.io.PrintWriter;

import org.junit.platform.commons.meta.API;
import org.junit.platform.console.options.CommandLineOptions;
import org.junit.platform.console.options.CommandLineOptionsParser;
import org.junit.platform.console.options.JOptSimpleCommandLineOptionsParser;
import org.junit.platform.console.tasks.ConsoleTestExecutor;
import org.junit.platform.launcher.listeners.TestExecutionSummary;

/**
 * The {@code ConsoleLauncher} is a stand-alone application for launching the
 * JUnit Platform from the console.
 *
 * @since 1.0
 */
@API(Maintained)
public class ConsoleLauncher {

	@API(Maintained)
	public static void main(String... args) {
		CommandLineOptionsParser parser = new JOptSimpleCommandLineOptionsParser();
		ConsoleLauncher consoleLauncher = new ConsoleLauncher(parser, System.out, System.err);
		int exitCode = consoleLauncher.execute(args).getExitCode();
		System.exit(exitCode);
	}

	private final CommandLineOptionsParser commandLineOptionsParser;
	private final PrintStream outStream;
	private final PrintStream errStream;

	ConsoleLauncher(CommandLineOptionsParser commandLineOptionsParser, PrintStream out, PrintStream err) {
		this.commandLineOptionsParser = commandLineOptionsParser;
		this.outStream = out;
		this.errStream = err;
	}

	ConsoleLauncherExecutionResult execute(String... args) {
		CommandLineOptions options = commandLineOptionsParser.parse(args);
		try (PrintWriter out = new PrintWriter(outStream)) {
			if (options.isDisplayHelp()) {
				commandLineOptionsParser.printHelp(out);
				return ConsoleLauncherExecutionResult.success();
			}
			return executeTests(options, out);
		}
		finally {
			outStream.flush();
			errStream.flush();
		}
	}

	private ConsoleLauncherExecutionResult executeTests(CommandLineOptions options, PrintWriter out) {
		try {
			TestExecutionSummary testExecutionSummary = new ConsoleTestExecutor(options).execute(out);
			return ConsoleLauncherExecutionResult.forSummary(testExecutionSummary);
		}
		catch (Exception exception) {
			exception.printStackTrace(errStream);
			errStream.println();
			commandLineOptionsParser.printHelp(out);
		}
		return ConsoleLauncherExecutionResult.failed();
	}

}
