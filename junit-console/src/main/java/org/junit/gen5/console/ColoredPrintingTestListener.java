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

import static org.junit.gen5.console.ColoredPrintingTestListener.Color.*;

import java.io.PrintStream;

import org.junit.gen5.engine.TestDescriptor;
import org.junit.gen5.engine.TestEngine;
import org.junit.gen5.engine.TestExecutionListener;
import org.junit.gen5.launcher.TestPlan;
import org.junit.gen5.launcher.TestPlanExecutionListener;

/**
 * @author Stefan Bechtold
 * @author Marc Philipp
 * @author Sam Brannen
 * @since 5.0
 */
public class ColoredPrintingTestListener implements TestPlanExecutionListener, TestExecutionListener {

	private final PrintStream out;
	private final boolean disableAnsiColors;

	public ColoredPrintingTestListener(PrintStream out, boolean disableAnsiColors) {
		this.out = out;
		this.disableAnsiColors = disableAnsiColors;
	}

	@Override
	public void testPlanExecutionStarted(TestPlan testPlan) {
		out.printf("Test execution started. Number of static tests: %d%n", testPlan.countStaticTests());
	}

	@Override
	public void testPlanExecutionPaused(TestPlan testPlan) {
		out.println("Test execution paused.");
	}

	@Override
	public void testPlanExecutionRestarted(TestPlan testPlan) {
		out.println("Test execution continued.");
	}

	@Override
	public void testPlanExecutionStopped(TestPlan testPlan) {
		out.println("Test execution canceled.");
	}

	@Override
	public void testPlanExecutionFinished(TestPlan testPlan) {
		out.println("Test execution finished.");
	}

	@Override
	public void testPlanExecutionStartedOnEngine(TestPlan testPlan, TestEngine testEngine) {
		println(BLUE, "Engine started: %s", testEngine.getId());
	}

	@Override
	public void testPlanExecutionFinishedOnEngine(TestPlan testPlan, TestEngine testEngine) {
		println(BLUE, "Engine finished: %s", testEngine.getId());
	}

	@Override
	public void dynamicTestFound(TestDescriptor testDescriptor) {
		printlnTestDescriptor(BLUE, "Test found:", testDescriptor);
	}

	@Override
	public void testStarted(TestDescriptor testDescriptor) {
		printlnTestDescriptor(NONE, "Test started:", testDescriptor);
	}

	@Override
	public void testSkipped(TestDescriptor testDescriptor, Throwable t) {
		printlnTestDescriptor(YELLOW, "Test skipped:", testDescriptor);
		printlnException(YELLOW, t);
	}

	@Override
	public void testAborted(TestDescriptor testDescriptor, Throwable t) {
		printlnTestDescriptor(YELLOW, "Test aborted:", testDescriptor);
		printlnException(YELLOW, t);
	}

	@Override
	public void testFailed(TestDescriptor testDescriptor, Throwable t) {
		printlnTestDescriptor(RED, "Test failed:", testDescriptor);
		printlnException(RED, t);
	}

	@Override
	public void testSucceeded(TestDescriptor testDescriptor) {
		printlnTestDescriptor(GREEN, "Test succeeded:", testDescriptor);
	}

	private void printlnTestDescriptor(Color color, String message, TestDescriptor testDescriptor) {
		println(color, "%-15s   %s [%s]", message, testDescriptor.getDisplayName(), testDescriptor.getUniqueId());
	}

	private void printlnException(Color color, Throwable throwable) {
		println(color, "                  => Exception:   %s", throwable.getLocalizedMessage());
	}

	private void println(Color color, String format, Object... args) {
		println(color, String.format(format, args));
	}

	private void println(Color color, String message) {
		if (disableAnsiColors) {
			out.println(message);
		}
		else {
			// Use string concatenation to avoid ansi disruption on console
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
