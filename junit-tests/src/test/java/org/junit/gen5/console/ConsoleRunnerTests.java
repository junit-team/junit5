/*
 * Copyright 2015-2016 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.junit.gen5.console;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.gen5.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

import java.io.PrintWriter;
import java.io.StringWriter;

import org.junit.gen5.api.Test;
import org.junit.gen5.console.options.CommandLineOptions;
import org.junit.gen5.console.options.CommandLineOptionsParser;
import org.junit.gen5.console.tasks.ConsoleTaskExecutor;
import org.junit.gen5.console.tasks.DisplayHelpTask;
import org.junit.gen5.console.tasks.ExecuteTestsTask;

public class ConsoleRunnerTests {

	@Test
	public void executeDisplayHelpTask() {
		CommandLineOptions options = new CommandLineOptions();
		options.setDisplayHelp(true);

		CommandLineOptionsParser commandLineOptionsParser = mock(CommandLineOptionsParser.class);
		when(commandLineOptionsParser.parse(any())).thenReturn(options);

		ConsoleTaskExecutor consoleTaskExecutor = mock(ConsoleTaskExecutor.class);
		when(consoleTaskExecutor.executeTask(any(), any())).thenReturn(42);

		ConsoleRunner consoleRunner = new ConsoleRunner(commandLineOptionsParser, consoleTaskExecutor);
		int exitCode = consoleRunner.execute("--help");

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

		ConsoleRunner consoleRunner = new ConsoleRunner(commandLineOptionsParser, consoleTaskExecutor);
		int exitCode = consoleRunner.execute("--all");

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

		ConsoleRunner consoleRunner = new ConsoleRunner(commandLineOptionsParser, mock(ConsoleTaskExecutor.class));
		consoleRunner.displayHelp(new PrintWriter(stringWriter));

		assertThat(stringWriter.toString()).contains("Keep Calm and Carry On");
	}
}
