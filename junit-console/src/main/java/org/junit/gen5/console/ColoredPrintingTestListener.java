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
 * @since 5.0
 */
public class ColoredPrintingTestListener implements TestPlanExecutionListener, TestExecutionListener {

	private static final String ANSI_RESET = "\u001B[0m";

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
		println(BLUE, "Test found:     %s", testDescriptor);
	}

	@Override
	public void testStarted(TestDescriptor testDescriptor) {
		println(BLACK, "Test started:   %s", testDescriptor);
	}

	@Override
	public void testSkipped(TestDescriptor testDescriptor, Throwable t) {
		println(YELLOW, "Test skipped:   %s\n=> Exception:   %s", testDescriptor,
				(t != null) ? t.getLocalizedMessage() : "none");
	}

	@Override
	public void testAborted(TestDescriptor testDescriptor, Throwable t) {
		println(YELLOW, "Test aborted:   %s\n=> Exception:   %s", testDescriptor,
				(t != null) ? t.getLocalizedMessage() : "none");
	}

	@Override
	public void testFailed(TestDescriptor testDescriptor, Throwable t) {
		println(RED, "Test failed:    %s\n=> Exception:   %s", testDescriptor, t.getLocalizedMessage());
	}

	@Override
	public void testSucceeded(TestDescriptor testDescriptor) {
		println(GREEN, "Test succeeded: %s", testDescriptor);
	}

	void println(Color color, String format, Object... args) {
		out.print(color.ansiCode);
		out.format(format, args);
		out.println(ANSI_RESET);
	}

	enum Color {
		BLACK("\u001B[30m"), //
		RED("\u001B[31m"), //
		GREEN("\u001B[32m"), //
		YELLOW("\u001B[33m"), //
		BLUE("\u001B[34m"), //
		PURPLE("\u001B[35m"), //
		CYAN("\u001B[36m"), //
		WHITE("\u001B[37m");

		private final String ansiCode;

		Color(String ansiCode) {
			this.ansiCode = ansiCode;
		}
	}

}
