/*
 * Copyright 2015 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.junit.gen5.console.tasks;

import static org.junit.gen5.console.tasks.ColoredPrintingTestListener.Color.*;

import java.io.PrintWriter;

import org.junit.gen5.engine.TestExecutionResult;
import org.junit.gen5.engine.TestExecutionResult.Status;
import org.junit.gen5.launcher.TestExecutionListener;
import org.junit.gen5.launcher.TestIdentifier;
import org.junit.gen5.launcher.TestPlan;

/**
 * @since 5.0
 */
class ColoredPrintingTestListener implements TestExecutionListener {

	private final PrintWriter out;
	private final boolean disableAnsiColors;

	ColoredPrintingTestListener(PrintWriter out, boolean disableAnsiColors) {
		this.out = out;
		this.disableAnsiColors = disableAnsiColors;
	}

	@Override
	public void testPlanExecutionStarted(TestPlan testPlan) {
		out.printf("Test execution started. Number of static tests: %d%n",
			testPlan.countTestIdentifiers(TestIdentifier::isTest));
	}

	@Override
	public void testPlanExecutionFinished(TestPlan testPlan) {
		out.println("Test execution finished.");
	}

	@Override
	public void dynamicTestRegistered(TestIdentifier testIdentifier) {
		printlnTestDescriptor(BLUE, "Test found:", testIdentifier);
	}

	@Override
	public void testSkipped(TestIdentifier testIdentifier, String reason) {
		printlnTestDescriptor(YELLOW, "Test skipped:", testIdentifier);
		printlnMessage(YELLOW, "Reason", reason);
	}

	@Override
	public void testStarted(TestIdentifier testIdentifier) {
		printlnTestDescriptor(NONE, "Test started:", testIdentifier);
	}

	@Override
	public void testFinished(TestIdentifier testIdentifier, TestExecutionResult testExecutionResult) {
		Color color = determineColor(testExecutionResult.getStatus());
		printlnTestDescriptor(color, "Test finished:", testIdentifier);
		testExecutionResult.getThrowable().ifPresent(t -> printlnException(color, t));
	}

	private Color determineColor(Status status) {
		switch (status) {
			case SUCCESSFUL:
				return GREEN;
			case ABORTED:
				return YELLOW;
			case FAILED:
				return RED;
			default:
				return NONE;
		}
	}

	private void printlnTestDescriptor(Color color, String message, TestIdentifier testIdentifier) {
		println(color, "%-15s   %s [%s]", message, testIdentifier.getDisplayName(), testIdentifier.getUniqueId());
	}

	private void printlnException(Color color, Throwable throwable) {
		printlnMessage(color, "Exception", throwable.getLocalizedMessage());
	}

	private void printlnMessage(Color color, String message, String detail) {
		println(color, "                  => " + message + ": %s", detail);
	}

	private void println(Color color, String format, Object... args) {
		println(color, String.format(format, args));
	}

	private void println(Color color, String message) {
		if (disableAnsiColors) {
			out.println(message);
		}
		else {
			// Use string concatenation to avoid ANSI disruption on console
			out.println(color + message + NONE);
		}
	}

	enum Color {

		NONE(0),

		BLACK(30),

		RED(31),

		GREEN(32),

		YELLOW(33),

		BLUE(34),

		PURPLE(35),

		CYAN(36),

		WHITE(37);

		private final int ansiCode;

		Color(int ansiCode) {
			this.ansiCode = ansiCode;
		}

		@Override
		public String toString() {
			return "\u001B[" + this.ansiCode + "m";
		}
	}

}
