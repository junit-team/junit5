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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import org.junit.jupiter.api.Test;
import org.junit.platform.console.options.CommandLineOptions;
import org.junit.platform.console.options.CommandLineOptionsParser;

/**
 * @since 1.0
 */
class ConsoleLauncherTests {

	private final PrintStream printSink = new PrintStream(new ByteArrayOutputStream());

	@Test
	void displayHelp() {
		CommandLineOptions options = new CommandLineOptions();
		options.setDisplayHelp(true);

		CommandLineOptionsParser commandLineOptionsParser = mock(CommandLineOptionsParser.class);
		when(commandLineOptionsParser.parse(any())).thenReturn(options);

		ConsoleLauncher consoleLauncher = new ConsoleLauncher(commandLineOptionsParser, printSink, printSink);
		int exitCode = consoleLauncher.execute("--help").getExitCode();

		assertEquals(0, exitCode);
		verify(commandLineOptionsParser).parse("--help");
	}

	@Test
	void executeWithUnknownCommandLineOption() {
		CommandLineOptionsParser commandLineOptionsParser = mock(CommandLineOptionsParser.class);
		when(commandLineOptionsParser.parse(any())).thenReturn(new CommandLineOptions());

		ConsoleLauncher consoleLauncher = new ConsoleLauncher(commandLineOptionsParser, printSink, printSink);
		int exitCode = consoleLauncher.execute("--all").getExitCode();

		assertEquals(-1, exitCode);
		verify(commandLineOptionsParser).parse("--all");
	}

	@Test
	void executeWithSupportedCommandLineOption() {
		CommandLineOptionsParser commandLineOptionsParser = mock(CommandLineOptionsParser.class);
		when(commandLineOptionsParser.parse(any())).thenReturn(new CommandLineOptions());

		ConsoleLauncher consoleLauncher = new ConsoleLauncher(commandLineOptionsParser, printSink, printSink);
		int exitCode = consoleLauncher.execute("--scan-classpath").getExitCode();

		assertEquals(-1, exitCode);
		verify(commandLineOptionsParser).parse("--scan-classpath");
	}

}
