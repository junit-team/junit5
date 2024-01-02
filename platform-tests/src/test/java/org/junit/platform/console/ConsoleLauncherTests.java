/*
 * Copyright 2015-2024 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * https://www.eclipse.org/legal/epl-v20.html
 */

package org.junit.platform.console;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.params.provider.Arguments.arguments;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.stream.Stream;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.platform.console.tasks.ConsoleTestExecutor;

/**
 * @since 1.0
 */
class ConsoleLauncherTests {

	private final StringWriter stringWriter = new StringWriter();
	private final PrintWriter printSink = new PrintWriter(stringWriter);

	@ParameterizedTest(name = "{0}")
	@MethodSource("commandsWithEmptyOptionExitCodes")
	void displayHelp(String command) {
		var consoleLauncher = new ConsoleLauncher(ConsoleTestExecutor::new, printSink, printSink);
		var exitCode = consoleLauncher.run(command, "--help").getExitCode();

		assertEquals(0, exitCode);
		assertThat(stringWriter.toString()).contains("--help", "--disable-banner" /* ... */);
	}

	@ParameterizedTest(name = "{0}")
	@MethodSource("commandsWithEmptyOptionExitCodes")
	void displayBanner(String command) {
		var consoleLauncher = new ConsoleLauncher(ConsoleTestExecutor::new, printSink, printSink);
		consoleLauncher.run(command);

		assertThat(stringWriter.toString()).contains("Thanks for using JUnit!");
	}

	@ParameterizedTest(name = "{0}")
	@MethodSource("commandsWithEmptyOptionExitCodes")
	void disableBanner(String command, int expectedExitCode) {
		var consoleLauncher = new ConsoleLauncher(ConsoleTestExecutor::new, printSink, printSink);
		var exitCode = consoleLauncher.run(command, "--disable-banner").getExitCode();

		assertEquals(expectedExitCode, exitCode);
		assertThat(stringWriter.toString()).doesNotContain("Thanks for using JUnit!");
	}

	@ParameterizedTest(name = "{0}")
	@MethodSource("commandsWithEmptyOptionExitCodes")
	void executeWithUnknownCommandLineOption(String command) {
		var consoleLauncher = new ConsoleLauncher(ConsoleTestExecutor::new, printSink, printSink);
		var exitCode = consoleLauncher.run(command, "--all").getExitCode();

		assertEquals(-1, exitCode);
		assertThat(stringWriter.toString()).contains("Unknown option: '--all'").contains("Usage:");
	}

	@ParameterizedTest(name = "{0}")
	@MethodSource("commandsWithEmptyOptionExitCodes")
	void executeWithoutCommandLineOptions(String command, int expectedExitCode) {
		var consoleLauncher = new ConsoleLauncher(ConsoleTestExecutor::new, printSink, printSink);
		var actualExitCode = consoleLauncher.run(command).getExitCode();

		assertEquals(expectedExitCode, actualExitCode);
	}

	static Stream<Arguments> commandsWithEmptyOptionExitCodes() {
		return Stream.of( //
			arguments("execute", -1), //
			arguments("discover", -1), //
			arguments("engines", 0) //
		);
	}

}
