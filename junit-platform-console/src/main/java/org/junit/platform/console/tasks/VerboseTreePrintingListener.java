/*
 * Copyright 2015-2020 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * https://www.eclipse.org/legal/epl-v20.html
 */

package org.junit.platform.console.tasks;

import static org.junit.platform.commons.util.ExceptionUtils.readStackTrace;
import static org.junit.platform.console.tasks.Color.NONE;

import java.io.PrintWriter;
import java.util.ArrayDeque;
import java.util.Deque;

import org.junit.platform.console.options.Theme;
import org.junit.platform.engine.TestExecutionResult;
import org.junit.platform.engine.reporting.ReportEntry;
import org.junit.platform.launcher.TestExecutionListener;
import org.junit.platform.launcher.TestIdentifier;
import org.junit.platform.launcher.TestPlan;

/**
 * @since 1.0
 */
class VerboseTreePrintingListener implements TestExecutionListener {

	private final PrintWriter out;
	private final boolean disableAnsiColors;
	private final Theme theme;
	private final Deque<Long> frames;
	private final String[] verticals;
	private long executionStartedMillis;

	VerboseTreePrintingListener(PrintWriter out, boolean disableAnsiColors, int maxContainerNestingLevel, Theme theme) {
		this.out = out;
		this.disableAnsiColors = disableAnsiColors;
		this.theme = theme;

		// create frame stack and push initial root frame
		this.frames = new ArrayDeque<>();
		this.frames.push(0L);

		// create and populate vertical indentation lookup table
		this.verticals = new String[Math.max(10, maxContainerNestingLevel) + 1];
		this.verticals[0] = ""; // no frame
		this.verticals[1] = ""; // synthetic root "/" level
		this.verticals[2] = ""; // "engine" level

		for (int i = 3; i < verticals.length; i++) {
			verticals[i] = verticals[i - 1] + theme.vertical();
		}
	}

	@Override
	public void testPlanExecutionStarted(TestPlan testPlan) {
		frames.push(System.currentTimeMillis());

		long tests = testPlan.countTestIdentifiers(TestIdentifier::isTest);
		printf(NONE, "%s", "Test plan execution started. Number of static tests: ");
		printf(Color.TEST, "%d%n", tests);
		printf(Color.CONTAINER, "%s%n", theme.root());
	}

	@Override
	public void testPlanExecutionFinished(TestPlan testPlan) {
		frames.pop();

		long tests = testPlan.countTestIdentifiers(TestIdentifier::isTest);
		printf(NONE, "%s", "Test plan execution finished. Number of all tests: ");
		printf(Color.TEST, "%d%n", tests);
	}

	@Override
	public void executionStarted(TestIdentifier testIdentifier) {
		this.executionStartedMillis = System.currentTimeMillis();
		if (testIdentifier.isContainer()) {
			printVerticals(theme.entry());
			printf(Color.CONTAINER, " %s", testIdentifier.getDisplayName());
			printf(NONE, "%n");
			frames.push(System.currentTimeMillis());
		}
		if (testIdentifier.isContainer()) {
			return;
		}
		printVerticals(theme.entry());
		printf(Color.valueOf(testIdentifier), " %s%n", testIdentifier.getDisplayName());
		printDetails(testIdentifier);
	}

	@Override
	public void executionFinished(TestIdentifier testIdentifier, TestExecutionResult testExecutionResult) {
		testExecutionResult.getThrowable().ifPresent(t -> printDetail(Color.FAILED, "caught", readStackTrace(t)));
		if (testIdentifier.isContainer()) {
			Long creationMillis = frames.pop();
			printVerticals(theme.end());
			printf(Color.CONTAINER, " %s", testIdentifier.getDisplayName());
			printf(NONE, " finished after %d ms.%n", System.currentTimeMillis() - creationMillis);
			return;
		}
		printDetail(NONE, "duration", "%d ms%n", System.currentTimeMillis() - executionStartedMillis);
		String status = theme.status(testExecutionResult) + " " + testExecutionResult.getStatus();
		printDetail(Color.valueOf(testExecutionResult), "status", "%s%n", status);
	}

	@Override
	public void executionSkipped(TestIdentifier testIdentifier, String reason) {
		printVerticals(theme.entry());
		printf(Color.valueOf(testIdentifier), " %s%n", testIdentifier.getDisplayName());
		printDetails(testIdentifier);
		printDetail(Color.SKIPPED, "reason", reason);
		printDetail(Color.SKIPPED, "status", theme.skipped() + " SKIPPED");
	}

	@Override
	public void dynamicTestRegistered(TestIdentifier testIdentifier) {
		printVerticals(theme.entry());
		printf(Color.DYNAMIC, " %s", testIdentifier.getDisplayName());
		printf(NONE, "%s%n", " dynamically registered");
	}

	@Override
	public void reportingEntryPublished(TestIdentifier testIdentifier, ReportEntry entry) {
		printDetail(Color.REPORTED, "reports", entry.toString());
	}

	/**
	 * Print static information about the test identifier.
	 */
	private void printDetails(TestIdentifier testIdentifier) {
		printDetail(NONE, "tags", "%s%n", testIdentifier.getTags());
		printDetail(NONE, "uniqueId", "%s%n", testIdentifier.getUniqueId());
		printDetail(NONE, "parent", "%s%n", testIdentifier.getParentId().orElse("[]"));
		testIdentifier.getSource().ifPresent(source -> printDetail(NONE, "source", "%s%n", source));
	}

	private String verticals() {
		return verticals(frames.size());
	}

	private String verticals(int index) {
		return verticals[Math.min(index, verticals.length)];
	}

	private void printVerticals(String tile) {
		printf(NONE, verticals());
		printf(NONE, tile);
	}

	private void printf(Color color, String message, Object... args) {
		if (disableAnsiColors || color == NONE) {
			out.printf(message, args);
		}
		else {
			out.printf(color + message + NONE, args);
		}
		out.flush();
	}

	/**
	 * Print single detail with a potential multi-line message.
	 */
	private void printDetail(Color color, String detail, String format, Object... args) {
		// print initial verticals - expecting to be at start of the line
		String verticals = verticals(frames.size() + 1);
		printf(NONE, verticals);
		String detailFormat = "%9s";
		// omit detail string if it's empty
		if (!detail.isEmpty()) {
			printf(NONE, "%s", String.format(detailFormat + ": ", detail));
		}
		// trivial case: at least one arg is given? Let printf do the entire work
		if (args.length > 0) {
			printf(color, format, args);
			return;
		}
		// still here? Split format into separate lines and indent them from the second line on
		String[] lines = format.split("\\R");
		printf(color, "%s", lines[0]);
		if (lines.length > 1) {
			String delimiter = System.lineSeparator() + verticals + String.format(detailFormat + "    ", "");
			for (int i = 1; i < lines.length; i++) {
				printf(NONE, "%s", delimiter);
				printf(color, "%s", lines[i]);
			}
		}
		printf(NONE, "%n");
	}

}
