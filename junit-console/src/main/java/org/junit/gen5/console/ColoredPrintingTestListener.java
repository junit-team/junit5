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
import org.junit.gen5.engine.TestExecutionListener;
import org.junit.gen5.engine.TestPlanExecutionListener;

/**
 * @author Stefan Bechtold
 * @author Marc Philipp
 * @author Sam Brannen
 * @since 5.0
 */
public class ColoredPrintingTestListener implements TestPlanExecutionListener, TestExecutionListener {

	private final PrintStream out;


	public ColoredPrintingTestListener(PrintStream out) {
		this.out = out;
	}

	@Override
	public void testPlanExecutionStarted(int numberOfStaticTests) {
		out.printf("Test execution started. Number of static tests: %d%n", numberOfStaticTests);
	}

	@Override
	public void testPlanExecutionPaused() {
		out.println("Test execution paused.");
	}

	@Override
	public void testPlanExecutionRestarted() {
		out.println("Test execution continued.");
	}

	@Override
	public void testPlanExecutionStopped() {
		out.println("Test execution canceled.");
	}

	@Override
	public void testPlanExecutionFinished() {
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

	void println(Color color, String format, Object... args) {
		out.print(color);
		out.format(format, args);
		out.println(NONE);
	}


	static enum Color {

		NONE("\u001B[0m"),

		BLACK("\u001B[30m"),

		RED("\u001B[31m"),

		GREEN("\u001B[32m"),

		YELLOW("\u001B[33m"),

		BLUE("\u001B[34m"),

		PURPLE("\u001B[35m"),

		CYAN("\u001B[36m"),

		WHITE("\u001B[37m");

		private final String ansiCode;


		Color(String ansiCode) {
			this.ansiCode = ansiCode;
		}

		@Override
		public String toString() {
			return this.ansiCode;
		}
	}

}
