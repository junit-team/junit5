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

import static org.apiguardian.api.API.Status.INTERNAL;
import static org.junit.platform.console.options.CommandResult.failure;

import org.apiguardian.api.API;
import org.junit.platform.commons.function.Try;

/**
 * @since 1.10
 */
@API(status = INTERNAL, since = "1.10")
public class CommandLineParser {

	private final CommandLineOptionsParser optionsParser;

	public CommandLineParser(CommandLineOptionsParser optionsParser) {
		this.optionsParser = optionsParser;
	}

	public SafeCommand<?> parse(String... arguments) {
		return Try.call(() -> parseOptions(arguments)) //
				.andThenTry(this::createDecoratedCommand) //
				.getOrElse(this::optionParsingErrorCommand);
	}

	private SafeCommand<?> createDecoratedCommand(CommandLineOptions options) {
		Command<?> command = createCommand(options);
		command = options.isBannerDisabled() ? command : new BannerPrintingCommand<>(command);
		return new ErrorHandlingCommand<>(options, command,
			helpCommand(options.isAnsiColorOutputDisabled(), failure()));
	}

	private Command<?> createCommand(CommandLineOptions options) {
		if (options.isDisplayHelp()) {
			return helpCommand(options.isAnsiColorOutputDisabled(), CommandResult.success());
		}
		if (options.isListEngines()) {
			return new ListTestEnginesCommand();
		}
		if (options.isListTests()) {
			return new DiscoverTestsCommand(options);
		}
		return new ExecuteTestsCommand(options);
	}

	private <T> OptionParsingErrorCommand<T> optionParsingErrorCommand(Exception failure) {
		return new OptionParsingErrorCommand<>(failure, helpCommand(false, failure()));
	}

	private <T> HelpCommand<T> helpCommand(boolean ansiColorOutputDisabled, CommandResult<T> result) {
		return new HelpCommand<>(optionsParser, ansiColorOutputDisabled, result);
	}

	private CommandLineOptions parseOptions(String[] arguments) {
		return optionsParser.parse(arguments);
	}

}
