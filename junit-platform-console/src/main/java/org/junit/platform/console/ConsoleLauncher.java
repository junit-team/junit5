/*
 * Copyright 2015-2019 the original author or authors.
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

import java.io.BufferedWriter;
import java.io.OutputStreamWriter;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.charset.Charset;

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
		CommandLineOptionsParser parser = new PicocliCommandLineOptionsParser();
		ConsoleLauncher consoleLauncher = new ConsoleLauncher(parser, out, err);
		return consoleLauncher.execute(args);
	}

	private final CommandLineOptionsParser commandLineOptionsParser;
	private final PrintStream outStream;
	private final PrintStream errStream;
	private final Charset charset;

	ConsoleLauncher(CommandLineOptionsParser commandLineOptionsParser, PrintStream out, PrintStream err) {
		this(commandLineOptionsParser, out, err, Charset.defaultCharset());
	}

	ConsoleLauncher(CommandLineOptionsParser commandLineOptionsParser, PrintStream out, PrintStream err,
			Charset charset) {
		this.commandLineOptionsParser = commandLineOptionsParser;
		this.outStream = out;
		this.errStream = err;
		this.charset = charset;
	}

	ConsoleLauncherExecutionResult execute(String... args) {

		CommandLineOptions options = null;
		try {
			options = commandLineOptionsParser.parse(args);
		}
		catch (JUnitException ex) {
			errStream.println(ex.getMessage());
			StringWriter sw = new StringWriter();
			commandLineOptionsParser.printHelp(new PrintWriter(sw));
			errStream.println(sw);
			return ConsoleLauncherExecutionResult.failed();
		}
		try (PrintWriter out = new PrintWriter(new BufferedWriter(new OutputStreamWriter(outStream, charset)))) {
			if (!options.isBannerDisabled()) {
				displayBanner(out);
			}
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
			exception.printStackTrace(errStream);
			errStream.println();
			commandLineOptionsParser.printHelp(out);
		}
		return ConsoleLauncherExecutionResult.failed();
	}

}
