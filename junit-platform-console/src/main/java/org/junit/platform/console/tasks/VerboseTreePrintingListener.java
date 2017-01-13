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

	VerboseTreePrintingListener(PrintWriter out, boolean disableAnsiColors) {
		this(out, disableAnsiColors, 50, Theme.valueOf(Charset.defaultCharset()));
	}

	VerboseTreePrintingListener(PrintWriter out, boolean disableAnsiColors, int maxContainerNestingLevel, Theme theme) {
		super(out, disableAnsiColors, maxContainerNestingLevel, theme);
	}

	@Override
	public void testPlanExecutionStarted(TestPlan testPlan) {
		super.testPlanExecutionStarted(testPlan);
		long tests = testPlan.countTestIdentifiers(TestIdentifier::isTest);
		printf(NONE, "Test plan execution started. Number of static tests: ");
		printf(Color.test(), "%d%n", tests);
		printf(Color.container(), "%s%n", theme.root());
	}

	@Override
	public void testPlanExecutionFinished(TestPlan testPlan) {
		super.testPlanExecutionFinished(testPlan);
		long tests = testPlan.countTestIdentifiers(TestIdentifier::isTest);
		printf(NONE, "Test plan execution finished. Number of all tests: ");
		printf(Color.test(), "%d%n", tests);
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
			long nanos = System.nanoTime() - frame.creationNanos;
			printVerticals(theme.end());
			printf(Color.container(), " %s", testIdentifier.getDisplayName());
			printf(NONE, " finished after %d ms.%n", durationInMillis(nanos));
			return;
		}
		long nanos = System.nanoTime() - executionStartedNanos;
		Color color = Color.valueOf(testExecutionResult);
		testExecutionResult.getThrowable().ifPresent(t -> printDetail(Color.failed(), "caught", readStackTrace(t)));
		printDetail(NONE, "duration", "%d ms%n", durationInMillis(nanos));
		String status = theme.computeStatusTile(testExecutionResult) + " " + testExecutionResult.getStatus();
		printDetail(color, "status", "%s%n", status);
	}

	@Override
	public void executionSkipped(TestIdentifier testIdentifier, String reason) {
		printVerticals(theme.entry());
		printf(NONE, " %s not executed%n", testIdentifier.getDisplayName());
		printDetails(testIdentifier);
		printDetail(Color.skipped(), "reason", reason);
		printDetail(Color.skipped(), "status", theme.skipped() + " SKIPPED");
	}

	@Override
	public void dynamicTestRegistered(TestIdentifier testIdentifier) {
		printVerticals(theme.entry());
		printf(Color.dynamic(), " %s", testIdentifier.getDisplayName());
		printf(NONE, " dynamically registered%n");
	}

	@Override
	public void reportingEntryPublished(TestIdentifier testIdentifier, ReportEntry entry) {
		printDetail(Color.reported(), "reports", entry.toString());
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
		// Print initial verticals - expecting to be at start of the line.
		String indent = verticals[frames.size() + 1];
		printf(NONE, indent);
		String detailFormat = "%9s";
		// Omit detail string if it's empty.
		if (!detail.isEmpty()) {
			printf(NONE, String.format(detailFormat + ": ", detail));
		}
		// Trivial case: at least one arg is given? Let printf do the entire work.
		if (args.length > 0) {
			printf(color, format, args);
			return;
		}
		// Still here? Split format into separate lines and indent them from the second on.
		String[] lines = format.split("\\R");
		printf(color, lines[0]);
		if (lines.length > 1) {
			String delimiter = System.lineSeparator() + indent + String.format(detailFormat + "    ", "");
			for (int i = 1; i < lines.length; i++) {
				printf(NONE, delimiter);
				printf(color, lines[i]);
			}
		}
		printf(NONE, "%n");
	}
}
