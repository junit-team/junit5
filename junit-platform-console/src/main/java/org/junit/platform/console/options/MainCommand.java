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
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.Callable;

import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.IExitCodeGenerator;
import picocli.CommandLine.Model.CommandSpec;
import picocli.CommandLine.Option;
import picocli.CommandLine.Spec;
import picocli.CommandLine.Unmatched;

/**
 * @since 1.0
 */
@Command(//
		name = "junit", //
		abbreviateSynopsis = true, //
		sortOptions = false, //
		usageHelpWidth = 95, //
		showAtFileInUsageHelp = true, //
		usageHelpAutoWidth = true, //
		description = "Launches the JUnit Platform for test discovery and execution.", //
		footerHeading = "%n", //
		footer = "For more information, please refer to the JUnit User Guide at%n" //
				+ "@|underline https://junit.org/junit5/docs/current/user-guide/|@", //
		subcommands = { DiscoverTestsCommand.class, ExecuteTestsCommand.class, ListTestEnginesCommand.class,
				CommandLine.HelpCommand.class }, //
		scope = CommandLine.ScopeType.INHERIT)
public class MainCommand implements Callable<Object>, IExitCodeGenerator {

	@Option(names = { "-h", "--help" }, help = true, hidden = true)
	private boolean helpRequested;

	@Option(names = { "--h", "-help" }, help = true, hidden = true)
	private boolean helpRequested2;

	@Unmatched
	private List<String> allParameters = new ArrayList<>();

	@Spec
	CommandSpec commandSpec;

	CommandResult<?> commandResult;

	@Override
	public Object call() {
		if (helpRequested || helpRequested2) {
			commandSpec.commandLine().usage(commandSpec.commandLine().getOut());
			commandResult = CommandResult.success();
			return null;
		}
		if (allParameters.contains("--list-engines")) {
			return runCommand("engines", Optional.of("--list-engines"));
		}
		return runCommand("execute", Optional.empty());
	}

	@Override
	public int getExitCode() {
		return commandResult.getExitCode();
	}

	@SuppressWarnings("OptionalUsedAsFieldOrParameterType")
	private Object runCommand(String subcommand, Optional<String> triggeringOption) {
		CommandLine commandLine = commandSpec.commandLine();
		Object command = commandLine.getSubcommands().get(subcommand).getCommandSpec().userObject();

		printDeprecationWarning(subcommand, triggeringOption, commandLine);

		List<String> args = new ArrayList<>(commandLine.getParseResult().expandedArgs());
		triggeringOption.ifPresent(args::remove);
		CommandResult<?> result = runCommand(commandLine.getOut(), //
			commandLine.getErr(), //
			args.toArray(new String[0]), //
			command);
		this.commandResult = result;
		return result.getValue().orElse(null);
	}

	private static void printDeprecationWarning(String subcommand, Optional<String> triggeringOption,
			CommandLine commandLine) {
		PrintWriter err = commandLine.getErr();
		String reason = triggeringOption.map(it -> " due to use of '" + it + "'").orElse("");
		err.printf("WARNING: Delegating to the '%s' command%s.%n", subcommand, reason);
		err.println("WARNING: This behaviour has been deprecated and will be removed in a future release.");
		err.println("WARNING: Please use the '" + subcommand + "' command directly.");
		err.flush();
	}

	public static CommandResult<?> run(PrintWriter out, PrintWriter err, String[] args) {
		Object command = new MainCommand();
		return runCommand(out, err, args, command);
	}

	private static CommandResult<?> runCommand(PrintWriter out, PrintWriter err, String[] args, Object command) {
		CommandLine commandLine = new CommandLine(command);
		int exitCode = commandLine //
				.setOut(out) //
				.setErr(err) //
				.setUnmatchedArgumentsAllowed(false) //
				.setCaseInsensitiveEnumValuesAllowed(true) //
				.setAtFileCommentChar(null) // for --select-method com.acme.Foo#m()
				.execute(args);
		return CommandResult.create(exitCode, commandLine.getExecutionResult());
	}
}
