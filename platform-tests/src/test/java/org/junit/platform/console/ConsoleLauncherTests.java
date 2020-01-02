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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertLinesMatch;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.List;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Test;
import org.junit.platform.console.options.CommandLineOptions;
import org.junit.platform.console.options.CommandLineOptionsParser;

/**
 * @since 1.0
 */
class ConsoleLauncherTests {

	private final StringWriter stringWriter = new StringWriter();
	private final PrintWriter printSink = new PrintWriter(stringWriter);

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
	void displayBanner() {
		CommandLineOptions options = new CommandLineOptions();
		options.setBannerDisabled(false);
		options.setDisplayHelp(true);

		CommandLineOptionsParser commandLineOptionsParser = mock(CommandLineOptionsParser.class);
		when(commandLineOptionsParser.parse(any())).thenReturn(options);

		ConsoleLauncher consoleLauncher = new ConsoleLauncher(commandLineOptionsParser, printSink, printSink);
		int exitCode = consoleLauncher.execute("--help").getExitCode();

		assertEquals(0, exitCode);
		assertLinesMatch(
			List.of("", "Thanks for using JUnit! Support its development at https://junit.org/sponsoring", ""),
			stringWriter.toString().lines().collect(Collectors.toList()));
	}

	@Test
	void disableBanner() {
		CommandLineOptions options = new CommandLineOptions();
		options.setBannerDisabled(true);
		options.setDisplayHelp(true);

		CommandLineOptionsParser commandLineOptionsParser = mock(CommandLineOptionsParser.class);
		when(commandLineOptionsParser.parse(any())).thenReturn(options);

		ConsoleLauncher consoleLauncher = new ConsoleLauncher(commandLineOptionsParser, printSink, printSink);
		int exitCode = consoleLauncher.execute("--help", "--disable-banner").getExitCode();

		assertEquals(0, exitCode);
		assertLinesMatch(List.of(), stringWriter.toString().lines().collect(Collectors.toList()));
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
