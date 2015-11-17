/*
 * Copyright 2015 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.junit.gen5.console;

import static io.airlift.airline.SingleCommand.singleCommand;
import static java.util.Arrays.stream;

import java.util.List;

import javax.inject.Inject;

import io.airlift.airline.Arguments;
import io.airlift.airline.Command;
import io.airlift.airline.Help;
import io.airlift.airline.Option;
import io.airlift.airline.model.CommandMetadata;

import org.junit.gen5.commons.util.Preconditions;
import org.junit.gen5.engine.TestPlanSpecification;
import org.junit.gen5.engine.TestPlanSpecificationElement;
import org.junit.gen5.launcher.Launcher;

/**
 * @author Stefan Bechtold
 * @author Sam Brannen
 * @since 5.0
 */
@Command(name = "ConsoleRunner", description = "console test runner")
public class ConsoleRunner {

	// @formatter:off
    @Option(name = {"-h", "--help"}, description = "Display help information")
    private boolean help;

	@Option(name = { "-x", "--enable-exit-code" },
			description = "Exit process with number of failing tests as exit code")
	private boolean enableExitCode;

	@Option(name = { "-C", "--disable-ansi-colors" },
			description = "Disable colored output (not supported by all terminals)")
	private boolean disableAnsiColors;

	@Option(name = { "-m", "--argument-mode" },
			arity = 1,
			description = "How to treat arguments. Possible values: classes, packages")
	private String argumentMode = "classes";

	@Arguments(description = "Test classes or packages to execute (depending on --argument-mode/-m)")
	private List<String> arguments;

	// @formatter:on

	@Inject
	public CommandMetadata commandMetadata;

	public static void main(String... args) {
		ConsoleRunner consoleRunner = singleCommand(ConsoleRunner.class).parse(args);

		if (consoleRunner.help) {
			showHelp(consoleRunner);
		}

		try {
			consoleRunner.run();
		}
		catch (Exception e) {
			e.printStackTrace(System.err);
			System.err.println();
			showHelp(consoleRunner);
			System.exit(-1);
		}
	}

	private static void showHelp(ConsoleRunner consoleRunner) {
		Help.help(consoleRunner.commandMetadata);
	}

	private void run() {
		// TODO Configure launcher?
		Launcher launcher = new Launcher();

		TestSummaryReportingTestListener testSummaryListener = new TestSummaryReportingTestListener(System.out);
		launcher.registerTestPlanExecutionListeners(
			// @formatter:off
			new ColoredPrintingTestListener(System.out, disableAnsiColors),
			testSummaryListener
			// @formatter:on
		);

		TestPlanSpecification testPlanSpecification = TestPlanSpecification.build(
			testPlanSpecificationElementsFromArguments());

		// TODO Provide means to allow manipulation of test plan?
		launcher.execute(testPlanSpecification);

		if (enableExitCode) {
			long failedTests = testSummaryListener.getNumberOfFailedTests();
			int exitCode = (int) Math.min(Integer.MAX_VALUE, failedTests);
			System.exit(exitCode);
		}
	}

	private List<TestPlanSpecificationElement> testPlanSpecificationElementsFromArguments() {
		Preconditions.notNull(arguments, "No arguments given");
		ArgumentMode mode = ArgumentMode.parse(argumentMode);
		return mode.toTestPlanSpecificationElements(arguments);
	}

	private enum ArgumentMode {

		CLASSES {

			@Override
			List<TestPlanSpecificationElement> toTestPlanSpecificationElements(List<String> arguments) {
				return TestPlanSpecification.forClassNames(arguments);
			}
		},

		PACKAGES {

			@Override
			List<TestPlanSpecificationElement> toTestPlanSpecificationElements(List<String> arguments) {
				return TestPlanSpecification.forPackages(arguments);
			}
		};

		abstract List<TestPlanSpecificationElement> toTestPlanSpecificationElements(List<String> arguments);

		static ArgumentMode parse(String value) {
			// @formatter:off
			return stream(values())
					.filter(mode -> mode.name().equalsIgnoreCase(value))
					.findAny()
					.orElseThrow(() -> new IllegalArgumentException("Illegal argument mode: " + value));
			// @formatter:on
		}
	}

}
