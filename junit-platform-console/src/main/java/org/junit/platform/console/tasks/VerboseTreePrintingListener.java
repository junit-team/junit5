/*
 * Copyright 2015-2017 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.junit.platform.console.tasks;

import static org.junit.platform.commons.util.ExceptionUtils.readStackTrace;
import static org.junit.platform.console.tasks.Color.NONE;

import java.io.PrintWriter;
import java.nio.charset.Charset;

import org.junit.platform.engine.TestExecutionResult;
import org.junit.platform.engine.reporting.ReportEntry;
import org.junit.platform.launcher.TestIdentifier;
import org.junit.platform.launcher.TestPlan;

/**
 * @since 1.0
 */
class VerboseTreePrintingListener extends TreePrintingListener {

	VerboseTreePrintingListener(PrintWriter out, boolean monochrome) {
		this(out, monochrome, 16, Theme.valueOf(Charset.defaultCharset()));
	}

	VerboseTreePrintingListener(PrintWriter out, boolean monochrome, int maxContainerNestingLevel, Theme theme) {
		super(out, monochrome, maxContainerNestingLevel, theme);
	}

	@Override
	public void testPlanExecutionStarted(TestPlan testPlan) {
		super.testPlanExecutionStarted(testPlan);
		long tests = testPlan.countTestIdentifiers(TestIdentifier::isTest);
		printf(NONE, "Test plan execution started. Number of static tests: ");
		printf(Color.TEST, "%d%n", tests);
		printf(Color.CONTAINER, "%s%n", theme.root());
	}

	@Override
	public void testPlanExecutionFinished(TestPlan testPlan) {
		super.testPlanExecutionFinished(testPlan);
		long tests = testPlan.countTestIdentifiers(TestIdentifier::isTest);
		printf(NONE, "Test plan execution finished. Number of all tests: ");
		printf(Color.TEST, "%d%n", tests);
	}

	@Override
	public void executionStarted(TestIdentifier testIdentifier) {
		super.executionStarted(testIdentifier);
		if (testIdentifier.isContainer()) {
			return;
		}
		printVerticals(theme.entry());
		printf(Color.valueOf(testIdentifier), " %s%n", testIdentifier.getDisplayName());
		printDetails(testIdentifier);
	}

	@Override
	public void executionFinished(TestIdentifier testIdentifier, TestExecutionResult testExecutionResult) {
		if (testIdentifier.isContainer()) {
			Frame frame = frames.pop();
			printVerticals(theme.end());
			printf(Color.CONTAINER, " %s", testIdentifier.getDisplayName());
			printf(NONE, " finished after %d ms.%n", durationInMillis(System.nanoTime() - frame.creationNanos));
			return;
		}
		testExecutionResult.getThrowable().ifPresent(t -> printDetail(Color.FAILED, "caught", readStackTrace(t)));
		printDetail(NONE, "duration", "%d ms%n", durationInMillis(System.nanoTime() - executionStartedNanoTime));
		String status = theme.computeStatusTile(testExecutionResult) + " " + testExecutionResult.getStatus();
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
		printf(NONE, " dynamically registered%n");
	}

	@Override
	public void reportingEntryPublished(TestIdentifier testIdentifier, ReportEntry entry) {
		printDetail(Color.REPORTED, "reports", entry.toString());
	}

	/** Print static information about the test identifier. */
	private void printDetails(TestIdentifier testIdentifier) {
		printDetail(NONE, "tags", "%s%n", testIdentifier.getTags());
		printDetail(NONE, "uniqueId", "%s%n", testIdentifier.getUniqueId());
		printDetail(NONE, "parent", "%s%n", testIdentifier.getParentId().orElse("[]"));
		testIdentifier.getSource().ifPresent(source -> printDetail(NONE, "source", "%s%n", source));
	}

	/** Print single detail with a potential multi-line message. */
	private void printDetail(Color color, String detail, String format, Object... args) {
		// print initial verticals - expecting to be at start of the line
		String verticals = verticals(frames.size() + 1);
		printf(NONE, verticals);
		String detailFormat = "%9s";
		// omit detail string if it's empty
		if (!detail.isEmpty()) {
			printf(NONE, String.format(detailFormat + ": ", detail));
		}
		// trivial case: at least one arg is given? Let printf do the entire work
		if (args.length > 0) {
			printf(color, format, args);
			return;
		}
		// still here? Split format into separate lines and indent them from the second line on
		String[] lines = format.split("\\R");
		printf(color, lines[0]);
		if (lines.length > 1) {
			String delimiter = System.lineSeparator() + verticals + String.format(detailFormat + "    ", "");
			for (int i = 1; i < lines.length; i++) {
				printf(NONE, delimiter);
				printf(color, lines[i]);
			}
		}
		printf(NONE, "%n");
	}
}
