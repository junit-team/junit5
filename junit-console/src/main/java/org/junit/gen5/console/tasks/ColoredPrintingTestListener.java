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
	public void dynamicTestFound(TestIdentifier testIdentifier) {
		printlnTestDescriptor(BLUE, "Test found:", testIdentifier);
	}

	@Override
	public void testStarted(TestIdentifier testIdentifier) {
		printlnTestDescriptor(NONE, "Test started:", testIdentifier);
	}

	@Override
	public void testSkipped(TestIdentifier testIdentifier, Throwable t) {
		printlnTestDescriptor(YELLOW, "Test skipped:", testIdentifier);
		printlnException(YELLOW, t);
	}

	@Override
	public void testAborted(TestIdentifier testIdentifier, Throwable t) {
		printlnTestDescriptor(YELLOW, "Test aborted:", testIdentifier);
		printlnException(YELLOW, t);
	}

	@Override
	public void testFailed(TestIdentifier testIdentifier, Throwable t) {
		printlnTestDescriptor(RED, "Test failed:", testIdentifier);
		printlnException(RED, t);
	}

	@Override
	public void testSucceeded(TestIdentifier testIdentifier) {
		printlnTestDescriptor(GREEN, "Test succeeded:", testIdentifier);
	}

	private void printlnTestDescriptor(Color color, String message, TestIdentifier testIdentifier) {
		println(color, "%-15s   %s [%s]", message, testIdentifier.getDisplayName(), testIdentifier.getUniqueId());
	}

	private void printlnException(Color color, Throwable throwable) {
		println(color, "                  => Exception: %s", throwable.getLocalizedMessage());
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
