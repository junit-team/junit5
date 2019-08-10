/*
 * Copyright 2015-2019 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * https://www.eclipse.org/legal/epl-v20.html
 */

package org.junit.platform.reporting.console;

import static org.apiguardian.api.API.Status.EXPERIMENTAL;
import static org.junit.platform.reporting.console.Color.NONE;

import java.io.PrintWriter;
import java.util.regex.Pattern;

import org.apiguardian.api.API;
import org.junit.platform.commons.util.ExceptionUtils;
import org.junit.platform.engine.TestExecutionResult;
import org.junit.platform.engine.reporting.ReportEntry;
import org.junit.platform.launcher.TestExecutionListener;
import org.junit.platform.launcher.TestIdentifier;
import org.junit.platform.launcher.TestPlan;

/**
 * Listener that prints output to the provided {@link PrintWriter} instance. The
 * output is flat rather than hierarchical.
 * <p>
 *
 * Optionally will use ANSI escape codes to colorize the output.
 * <p>
 *
 * For hierarchical output, see {@link TreePrintingListener} or
 * {@link VerboseTreePrintingListener}.
 *
 * @since 1.0
 * @see TreePrintingListener
 * @see VerboseTreePrintingListener
 */
@API(status = EXPERIMENTAL, since = "1.6")
public class FlatPrintingListener implements TestExecutionListener {

	private static final Pattern LINE_START_PATTERN = Pattern.compile("(?m)^");

	static final String INDENTATION = "             ";

	private final PrintWriter out;
	private final boolean useAnsiColors;

	/**
	 * Creates a new listener that prints monochromatic flat output to
	 * {@code System.out}.
	 */
	public FlatPrintingListener() {
		this(new PrintWriter(System.out));
	}

	/**
	 * Creates a new listener that prints monochromatic flat output to the given
	 * printer.
	 *
	 * @param out the printer to which the listener will print.
	 */
	public FlatPrintingListener(PrintWriter out) {
		this(out, false);
	}

	/**
	 * Creates a new listener that prints flat output to the given printer with ANSI
	 * colors disabled.
	 *
	 * @param out           the printer to which the listener will print.
	 * @param useAnsiColors {@code true} to use ANSI color codes to colorize the
	 *                      output, {@code false} to use monochromatic output.
	 */
	public FlatPrintingListener(PrintWriter out, boolean useAnsiColors) {
		this.out = out;
		this.useAnsiColors = useAnsiColors;
	}

	@Override
	public void testPlanExecutionStarted(TestPlan testPlan) {
		this.out.printf("Test execution started. Number of static tests: %d%n",
			testPlan.countTestIdentifiers(TestIdentifier::isTest));
	}

	@Override
	public void testPlanExecutionFinished(TestPlan testPlan) {
		this.out.println("Test execution finished.");
	}

	@Override
	public void dynamicTestRegistered(TestIdentifier testIdentifier) {
		printlnTestDescriptor(Color.DYNAMIC, "Registered:", testIdentifier);
	}

	@Override
	public void executionSkipped(TestIdentifier testIdentifier, String reason) {
		printlnTestDescriptor(Color.SKIPPED, "Skipped:", testIdentifier);
		printlnMessage(Color.SKIPPED, "Reason", reason);
	}

	@Override
	public void executionStarted(TestIdentifier testIdentifier) {
		printlnTestDescriptor(Color.valueOf(testIdentifier), "Started:", testIdentifier);
	}

	@Override
	public void executionFinished(TestIdentifier testIdentifier, TestExecutionResult testExecutionResult) {
		Color color = Color.valueOf(testExecutionResult);
		printlnTestDescriptor(color, "Finished:", testIdentifier);
		testExecutionResult.getThrowable().ifPresent(t -> printlnException(color, t));
	}

	@Override
	public void reportingEntryPublished(TestIdentifier testIdentifier, ReportEntry entry) {
		printlnTestDescriptor(Color.REPORTED, "Reported:", testIdentifier);
		printlnMessage(Color.REPORTED, "Reported values", entry.toString());
	}

	private void printlnTestDescriptor(Color color, String message, TestIdentifier testIdentifier) {
		println(color, "%-10s   %s (%s)", message, testIdentifier.getDisplayName(), testIdentifier.getUniqueId());
	}

	private void printlnException(Color color, Throwable throwable) {
		printlnMessage(color, "Exception", ExceptionUtils.readStackTrace(throwable));
	}

	private void printlnMessage(Color color, String message, String detail) {
		println(color, INDENTATION + "=> " + message + ": %s", indented(detail));
	}

	private void println(Color color, String format, Object... args) {
		println(color, String.format(format, args));
	}

	private void println(Color color, String message) {
		if (this.useAnsiColors) {
			// Use string concatenation to avoid ANSI disruption on console
			this.out.println(color + message + NONE);
		}
		else {
			this.out.println(message);
		}
	}

	/**
	 * Indent the given message if it is a multi-line string.
	 *
	 * <p>
	 * {@link #INDENTATION} is used to prefix the start of each new line except the
	 * first one.
	 *
	 * @param message the message to indent
	 * @return indented message
	 */
	private static String indented(String message) {
		return LINE_START_PATTERN.matcher(message).replaceAll(INDENTATION).trim();
	}

}
