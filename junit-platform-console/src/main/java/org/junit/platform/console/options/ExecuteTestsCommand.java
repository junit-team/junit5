/*
 * Copyright 2015-2023 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * https://www.eclipse.org/legal/epl-v20.html
 */

package org.junit.platform.console.options;

import java.io.PrintWriter;
import java.util.function.Function;

import org.junit.platform.console.tasks.ConsoleTestExecutor;
import org.junit.platform.launcher.listeners.TestExecutionSummary;

import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Mixin;

@Command(//
		name = "execute", //
		description = "Execute tests", //
		exitCodeOnInvalidInput = CommandResult.FAILURE, //
		exitCodeOnExecutionException = CommandResult.FAILURE //
)
public class ExecuteTestsCommand extends BaseCommand<TestExecutionSummary> implements CommandLine.IExitCodeGenerator {

	private final Function<CommandLineOptions, ConsoleTestExecutor> consoleTestExecutorFactory;
	@Mixin
	CommandLineOptionsMixin options;

	public ExecuteTestsCommand(Function<CommandLineOptions, ConsoleTestExecutor> consoleTestExecutorFactory) {
		this.consoleTestExecutorFactory = consoleTestExecutorFactory;
	}

	@Override
	protected TestExecutionSummary execute(PrintWriter out) {
		CommandLineOptions options = this.options.toCommandLineOptions();
		options.setAnsiColorOutputDisabled(outputOptions.isDisableAnsiColors());
		return consoleTestExecutorFactory.apply(options).execute(out);
	}

	@Override
	public int getExitCode() {
		return CommandResult.computeExitCode(commandSpec.commandLine().getExecutionResult(),
			options.toCommandLineOptions());
	}

}
