/*
 * Copyright 2015-2016 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.junit.platform.console.tasks;

import static org.junit.platform.commons.util.ExceptionUtils.readStackTrace;
import static org.junit.platform.console.tasks.Color.BLUE;
import static org.junit.platform.console.tasks.Color.CYAN;
import static org.junit.platform.console.tasks.Color.NONE;
import static org.junit.platform.console.tasks.Color.PURPLE;
import static org.junit.platform.console.tasks.Color.RED;
import static org.junit.platform.console.tasks.Color.YELLOW;

import java.io.PrintWriter;
import java.nio.charset.Charset;

import org.junit.platform.engine.TestExecutionResult;
import org.junit.platform.engine.reporting.ReportEntry;
import org.junit.platform.launcher.TestIdentifier;
import org.junit.platform.launcher.TestPlan;

/**
 * @since 1.0
 */
class VerboseTreePrinter extends TreePrinter {

	VerboseTreePrinter(PrintWriter out, boolean disableAnsiColors) {
		this(out, disableAnsiColors, 50, Theme.valueOf(Charset.defaultCharset()));
	}

	VerboseTreePrinter(PrintWriter out, boolean disableAnsiColors, int maxContainerNestingLevel, Theme theme) {
		super(out, disableAnsiColors, maxContainerNestingLevel, theme);
	}

	@Override
	public void testPlanExecutionStarted(TestPlan testPlan) {
		frames.push(new Frame(testPlan.toString()));
		long tests = testPlan.countTestIdentifiers(TestIdentifier::isTest);
		printf(NONE, "Test plan execution started. Number of static tests: ");
		printf(BLUE, "%d%n", tests);
		printf(BLUE, "%s%n", theme.root());
	}

	@Override
	public void testPlanExecutionFinished(TestPlan testPlan) {
		frames.pop();
		long tests = testPlan.countTestIdentifiers(TestIdentifier::isTest);
		printf(NONE, "Test plan execution finished. Number of all tests: ");
		printf(BLUE, "%d%n", tests);
	}

	@Override
	public void executionStarted(TestIdentifier testIdentifier) {
		super.executionStarted(testIdentifier);
		if (testIdentifier.isContainer()) {
			return;
		}
		printVerticals(theme.entry());
		printf(CYAN, " %s", testIdentifier.getDisplayName());
		printf(NONE, "%n");
		printDetails(testIdentifier);
	}

	@Override
	public void executionFinished(TestIdentifier testIdentifier, TestExecutionResult testExecutionResult) {
		if (testIdentifier.isContainer()) {
			Frame frame = frames.pop();
			long nanos = System.nanoTime() - frame.creationNanos;
			printVerticals(theme.entry());
			printf(BLUE, " %s", testIdentifier.getDisplayName());
			printf(NONE, " finished after %d ms.%n", duration(nanos));
			return;
		}
		frames.peek().count(testExecutionResult);
		long nanos = System.nanoTime() - executionStartedNanos;
		Color color = Color.valueOf(testExecutionResult);
		testExecutionResult.getThrowable().ifPresent(t -> printDetail(RED, "caught", readStackTrace(t)));
		printDetail(NONE, "duration", "%d ms%n", duration(nanos));
		printDetail(color, "status", "%s%n", testExecutionResult.getStatus());
	}

	@Override
	public void executionSkipped(TestIdentifier testIdentifier, String reason) {
		frames.peek().numberOfSkipped++;
		printVerticals(theme.entry());
		printf(NONE, " %s not executed%n", testIdentifier.getDisplayName());
		printDetails(testIdentifier);
		printDetail(YELLOW, "reason", reason);
		printDetail(YELLOW, "status", "SKIPPED");
	}

	@Override
	public void dynamicTestRegistered(TestIdentifier testIdentifier) {
		printVerticals(theme.entry());
		printf(PURPLE, " %s", testIdentifier.getDisplayName());
		printf(NONE, " dynamically registered%n");
	}

	@Override
	public void reportingEntryPublished(TestIdentifier testIdentifier, ReportEntry entry) {
		printDetail(PURPLE, "reports", entry.toString());
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
