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

import picocli.CommandLine.Command;
import picocli.CommandLine.Mixin;

@Command(//
		name = "discover", //
		description = "Discover tests")
class DiscoverTestsCommand extends BaseCommand<Void> {

	private final Function<CommandLineOptions, ConsoleTestExecutor> consoleTestExecutorFactory;
	@Mixin
	CommandLineOptionsMixin options;

	DiscoverTestsCommand(Function<CommandLineOptions, ConsoleTestExecutor> consoleTestExecutorFactory) {
		this.consoleTestExecutorFactory = consoleTestExecutorFactory;
	}

	@Override
	protected Void execute(PrintWriter out) {
		CommandLineOptions options = this.options.toCommandLineOptions();
		options.setAnsiColorOutputDisabled(outputOptions.isDisableAnsiColors());
		consoleTestExecutorFactory.apply(options).discover(out);
		return null;
	}
}
