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

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.isA;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.PrintWriter;
import java.io.StringWriter;

import org.junit.jupiter.api.Test;
import org.junit.platform.console.options.CommandLineOptions;
import org.junit.platform.console.options.CommandLineOptionsParser;
import org.junit.platform.console.tasks.ConsoleTaskExecutor;
import org.junit.platform.console.tasks.DisplayHelpTask;
import org.junit.platform.console.tasks.ExecuteTestsTask;

/**
 * @since 1.0
 */
public class ConsoleLauncherTests {

	@Test
	public void executeDisplayHelpTask() {
		CommandLineOptions options = new CommandLineOptions();
		options.setDisplayHelp(true);

		CommandLineOptionsParser commandLineOptionsParser = mock(CommandLineOptionsParser.class);
		when(commandLineOptionsParser.parse(any())).thenReturn(options);

		ConsoleTaskExecutor consoleTaskExecutor = mock(ConsoleTaskExecutor.class);
		when(consoleTaskExecutor.executeTask(any(), any())).thenReturn(42);

		ConsoleLauncher consoleLauncher = new ConsoleLauncher(commandLineOptionsParser, consoleTaskExecutor);
		int exitCode = consoleLauncher.execute("--help");

		assertEquals(42, exitCode);
		verify(commandLineOptionsParser).parse("--help");
		verify(consoleTaskExecutor).executeTask(isA(DisplayHelpTask.class), any());
	}

	@Test
	public void executeExecuteTestsTask() {
		CommandLineOptionsParser commandLineOptionsParser = mock(CommandLineOptionsParser.class);
		when(commandLineOptionsParser.parse(any())).thenReturn(new CommandLineOptions());

		ConsoleTaskExecutor consoleTaskExecutor = mock(ConsoleTaskExecutor.class);
		when(consoleTaskExecutor.executeTask(any(), any())).thenReturn(23);

		ConsoleLauncher consoleLauncher = new ConsoleLauncher(commandLineOptionsParser, consoleTaskExecutor);
		int exitCode = consoleLauncher.execute("--all");

		assertEquals(23, exitCode);
		verify(commandLineOptionsParser).parse("--all");
		verify(consoleTaskExecutor).executeTask(isA(ExecuteTestsTask.class), any());
	}

	@Test
	public void displayHelpCallsParser() {
		CommandLineOptionsParser commandLineOptionsParser = mock(CommandLineOptionsParser.class);
		doAnswer(invocation -> {
			PrintWriter out = invocation.getArgumentAt(0, PrintWriter.class);
			out.print("Keep Calm and Carry On");
			return null;
		}).when(commandLineOptionsParser).printHelp(any());

		StringWriter stringWriter = new StringWriter();

		ConsoleLauncher consoleLauncher = new ConsoleLauncher(commandLineOptionsParser,
			mock(ConsoleTaskExecutor.class));
		consoleLauncher.displayHelp(new PrintWriter(stringWriter));

		assertThat(stringWriter.toString()).contains("Keep Calm and Carry On");
	}
}
