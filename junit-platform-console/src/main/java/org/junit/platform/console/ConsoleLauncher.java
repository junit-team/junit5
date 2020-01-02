/*
 * Copyright 2015-2020 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * https://www.eclipse.org/legal/epl-v20.html
 */

package org.junit.platform.console;

import static org.apiguardian.api.API.Status.INTERNAL;
import static org.apiguardian.api.API.Status.MAINTAINED;

import java.io.PrintStream;
import java.io.PrintWriter;

import org.apiguardian.api.API;
import org.junit.platform.commons.JUnitException;
import org.junit.platform.console.options.CommandLineOptions;
import org.junit.platform.console.options.CommandLineOptionsParser;
import org.junit.platform.console.options.PicocliCommandLineOptionsParser;
import org.junit.platform.console.tasks.ConsoleTestExecutor;
import org.junit.platform.launcher.listeners.TestExecutionSummary;

/**
 * The {@code ConsoleLauncher} is a stand-alone application for launching the
 * JUnit Platform from the console.
 *
 * @since 1.0
 */
@API(status = MAINTAINED, since = "1.0")
public class ConsoleLauncher {

	public static void main(String... args) {
		int exitCode = execute(System.out, System.err, args).getExitCode();
		System.exit(exitCode);
	}

	@API(status = INTERNAL, since = "1.0")
	public static ConsoleLauncherExecutionResult execute(PrintStream out, PrintStream err, String... args) {
		return execute(new PrintWriter(out), new PrintWriter(err), args);
	}

	@API(status = INTERNAL, since = "1.0")
	public static ConsoleLauncherExecutionResult execute(PrintWriter out, PrintWriter err, String... args) {
		CommandLineOptionsParser parser = new PicocliCommandLineOptionsParser();
		ConsoleLauncher consoleLauncher = new ConsoleLauncher(parser, out, err);
		return consoleLauncher.execute(args);
	}

	private final CommandLineOptionsParser commandLineOptionsParser;
	private final PrintWriter out;
	private final PrintWriter err;

	ConsoleLauncher(CommandLineOptionsParser commandLineOptionsParser, PrintWriter out, PrintWriter err) {
		this.commandLineOptionsParser = commandLineOptionsParser;
		this.out = out;
		this.err = err;
	}

	ConsoleLauncherExecutionResult execute(String... args) {
		try {
			CommandLineOptions options = commandLineOptionsParser.parse(args);
			if (!options.isBannerDisabled()) {
				displayBanner(out);
			}
			if (options.isDisplayHelp()) {
				commandLineOptionsParser.printHelp(out);
				return ConsoleLauncherExecutionResult.success();
			}
			return executeTests(options, out);
		}
		catch (JUnitException ex) {
			err.println(ex.getMessage());
			err.println();
			commandLineOptionsParser.printHelp(err);
			return ConsoleLauncherExecutionResult.failed();
		}
		finally {
			out.flush();
			err.flush();
		}
	}

	void displayBanner(PrintWriter out) {
		out.println();
		out.println("Thanks for using JUnit! Support its development at https://junit.org/sponsoring");
		out.println();
	}

	private ConsoleLauncherExecutionResult executeTests(CommandLineOptions options, PrintWriter out) {
		try {
			TestExecutionSummary testExecutionSummary = new ConsoleTestExecutor(options).execute(out);
			return ConsoleLauncherExecutionResult.forSummary(testExecutionSummary, options);
		}
		catch (Exception exception) {
			exception.printStackTrace(err);
			err.println();
			commandLineOptionsParser.printHelp(out);
		}
		return ConsoleLauncherExecutionResult.failed();
	}

}
