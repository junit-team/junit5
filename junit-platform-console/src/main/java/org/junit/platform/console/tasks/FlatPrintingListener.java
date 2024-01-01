/*
 * Copyright 2015-2024 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * https://www.eclipse.org/legal/epl-v20.html
 */

package org.junit.platform.console.tasks;

import java.io.PrintWriter;

import org.junit.platform.commons.util.ExceptionUtils;
import org.junit.platform.engine.TestExecutionResult;
import org.junit.platform.engine.reporting.ReportEntry;
import org.junit.platform.launcher.TestIdentifier;
import org.junit.platform.launcher.TestPlan;

/**
 * @since 1.0
 */
class FlatPrintingListener implements DetailsPrintingListener {

	static final String INDENTATION = "             ";

	private final PrintWriter out;
	private final ColorPalette colorPalette;

	FlatPrintingListener(PrintWriter out, ColorPalette colorPalette) {
		this.out = out;
		this.colorPalette = colorPalette;
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
		printlnTestDescriptor(Style.DYNAMIC, "Registered:", testIdentifier);
	}

	@Override
	public void executionSkipped(TestIdentifier testIdentifier, String reason) {
		printlnTestDescriptor(Style.SKIPPED, "Skipped:", testIdentifier);
		printlnMessage(Style.SKIPPED, "Reason", reason);
	}

	@Override
	public void executionStarted(TestIdentifier testIdentifier) {
		printlnTestDescriptor(Style.valueOf(testIdentifier), "Started:", testIdentifier);
	}

	@Override
	public void executionFinished(TestIdentifier testIdentifier, TestExecutionResult testExecutionResult) {
		Style style = Style.valueOf(testExecutionResult);
		printlnTestDescriptor(style, "Finished:", testIdentifier);
		testExecutionResult.getThrowable().ifPresent(t -> printlnException(style, t));
	}

	@Override
	public void reportingEntryPublished(TestIdentifier testIdentifier, ReportEntry entry) {
		printlnTestDescriptor(Style.REPORTED, "Reported:", testIdentifier);
		printlnMessage(Style.REPORTED, "Reported values", entry.toString());
	}

	private void printlnTestDescriptor(Style style, String message, TestIdentifier testIdentifier) {
		println(style, "%-10s   %s (%s)", message, testIdentifier.getDisplayName(), testIdentifier.getUniqueId());
	}

	private void printlnException(Style style, Throwable throwable) {
		printlnMessage(style, "Exception", ExceptionUtils.readStackTrace(throwable));
	}

	private void printlnMessage(Style style, String message, String detail) {
		println(style, INDENTATION + "=> " + message + ": %s", indented(detail));
	}

	private void println(Style style, String format, Object... args) {
		this.out.println(colorPalette.paint(style, String.format(format, args)));
	}

	/**
	 * Indent the given message if it is a multi-line string.
	 *
	 * <p>{@link #INDENTATION} is used to prefix the start of each new line
	 * except the first one.
	 *
	 * @param message the message to indent
	 * @return indented message
	 */
	private static String indented(String message) {
		return DetailsPrintingListener.indented(message, INDENTATION);
	}

	@Override
	public void listTests(TestPlan testPlan) {
		testPlan.accept(new TestPlan.Visitor() {
			@Override
			public void visit(TestIdentifier testIdentifier) {
				println(Style.valueOf(testIdentifier), "%s (%s)", testIdentifier.getDisplayName(),
					testIdentifier.getUniqueId());
			}
		});
	}
}
