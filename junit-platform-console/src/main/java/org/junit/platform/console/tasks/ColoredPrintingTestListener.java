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
import static org.junit.platform.console.tasks.ColoredPrintingTestListener.Color.BLUE;
import static org.junit.platform.console.tasks.ColoredPrintingTestListener.Color.CYAN;
import static org.junit.platform.console.tasks.ColoredPrintingTestListener.Color.NONE;
import static org.junit.platform.console.tasks.ColoredPrintingTestListener.Color.PURPLE;
import static org.junit.platform.console.tasks.ColoredPrintingTestListener.Color.RED;
import static org.junit.platform.console.tasks.ColoredPrintingTestListener.Color.YELLOW;

import java.io.PrintWriter;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.concurrent.TimeUnit;

import org.junit.platform.engine.TestExecutionResult;
import org.junit.platform.engine.reporting.ReportEntry;
import org.junit.platform.launcher.TestExecutionListener;
import org.junit.platform.launcher.TestIdentifier;
import org.junit.platform.launcher.TestPlan;

/**
 * @since 1.0
 */
class ColoredPrintingTestListener implements TestExecutionListener {

	private final PrintWriter out;
	private final boolean disableAnsiColors;
	private final Theme theme;
	private final boolean verbose;
	private final Deque<Frame> frames;
	private final String[] verticals;
	private long executionStartedNanos;

	ColoredPrintingTestListener(PrintWriter out, boolean disableAnsiColors) {
		this(out, disableAnsiColors, 50, Theme.valueOf(Charset.defaultCharset()), true);
	}

	ColoredPrintingTestListener(PrintWriter out, boolean disableAnsiColors, int maxContainerNestingLevel, Theme theme,
			boolean verbose) {
		this.out = out;
		this.disableAnsiColors = disableAnsiColors;
		this.theme = theme;
		this.verbose = verbose;
		// Create frame stack and push initial root frame.
		this.frames = new ArrayDeque<>();
		this.frames.push(new Frame("/"));
		// Create and populate vertical indentation lookup table
		this.verticals = new String[maxContainerNestingLevel];
		this.verticals[0] = "";
		this.verticals[1] = "";
		for (int i = 2; i < verticals.length; i++) {
			verticals[i] = verticals[i - 1] + theme.vertical();
		}
	}

	@Override
	public void testPlanExecutionStarted(TestPlan testPlan) {
		frames.push(new Frame(testPlan.toString()));
		if (verbose) {
			long tests = testPlan.countTestIdentifiers(TestIdentifier::isTest);
			printf(NONE, "Test plan execution started. Number of static tests: ");
			printf(BLUE, "%d%n", tests);
			printf(BLUE, "%s%n", theme.root());
		}
	}

	@Override
	public void testPlanExecutionFinished(TestPlan testPlan) {
		Frame frame = frames.pop();
		if (verbose) {
			long tests = testPlan.countTestIdentifiers(TestIdentifier::isTest);
			printf(NONE, "Test plan execution finished. Number of all tests: ");
			printf(BLUE, "%d%n", tests);
			printf(NONE, "%s%n", frame);
		}
	}

	@Override
	public void executionStarted(TestIdentifier testIdentifier) {
		frames.peek().numberOfStarted++;
		executionStartedNanos = System.nanoTime();
		if (testIdentifier.isContainer()) {
			printVerticals();
			printf(NONE, theme.entry());
			printf(BLUE, " %s", testIdentifier.getDisplayName());
			printf(NONE, "%n"); // "started%n"
			frames.push(new Frame(testIdentifier.getUniqueId()));
			return;
		}
		if (verbose) {
			printVerticals();
			printf(NONE, theme.entry());
			printf(CYAN, " %s", testIdentifier.getDisplayName());
			printf(NONE, "%n"); // "started%n"
			printDetails(testIdentifier);
		}
	}

	@Override
	public void executionFinished(TestIdentifier testIdentifier, TestExecutionResult testExecutionResult) {
		if (testIdentifier.isContainer()) {
			Frame frame = frames.pop();
			if (verbose) {
				printVerticals();
				printf(NONE, theme.entry());
				printf(BLUE, " %s", testIdentifier.getDisplayName());
				printf(NONE, " finished. %s%n", frame);
				printVerticals();
				printf(NONE, "%n");
			}
			return;
		}
		frames.peek().count(testExecutionResult);
		long nanos = System.nanoTime() - executionStartedNanos;
		Color color = Color.valueOf(testExecutionResult);
		if (verbose) {
			testExecutionResult.getThrowable().ifPresent(t -> printDetail(RED, "caught", readStackTrace(t)));
			printDetail(NONE, "duration", "%d ms%n", duration(nanos));
			printDetail(color, "", "%s%n", testExecutionResult.getStatus());
			printVerticals();
			printf(NONE, "%n");
		}
		else {
			printVerticals();
			printf(NONE, theme.entry());
			printf(color, " %s", testIdentifier.getDisplayName());
			// printf(NONE, " (%d ms)", duration(nanos));
			testExecutionResult.getThrowable().ifPresent(t -> printf(RED, " %s", t));
			printf(NONE, "%n");
		}
	}

	@Override
	public void executionSkipped(TestIdentifier testIdentifier, String reason) {
		frames.peek().numberOfSkipped++;
		printVerticals();
		printf(NONE, theme.entry());
		if (verbose) {
			printf(NONE, " %s not executed%n", testIdentifier.getDisplayName());
			printDetails(testIdentifier);
			printDetail(YELLOW, "reason", reason);
			printDetail(YELLOW, "", "SKIPPED");
			printVerticals();
			printf(NONE, "%n");
		}
		else {
			printf(YELLOW, " %s %s%n", testIdentifier.getDisplayName(), reason);
		}
	}

	@Override
	public void dynamicTestRegistered(TestIdentifier testIdentifier) {
		if (verbose) {
			printVerticals();
			printf(NONE, theme.entry());
			printf(PURPLE, " %s", testIdentifier.getDisplayName());
			printf(NONE, " dynamically registered%n");
		}
	}

	@Override
	public void reportingEntryPublished(TestIdentifier testIdentifier, ReportEntry entry) {
		if (verbose) {
			printDetail(PURPLE, "reports", entry.toString());
		}
	}

	private void printf(Color color, String message, Object... args) {
		if (disableAnsiColors || color == NONE) {
			out.printf(message, args);
		}
		else {
			// Use string concatenation to avoid ANSI disruption on console
			out.printf(color + message + NONE, args);
		}
		out.flush();
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
		String[] lines = format.split("\r\n|\n|\r");
		printf(color, lines[0]);
		if (lines.length > 1) {
			String delimiter = System.lineSeparator() + indent + String.format(detailFormat + "    ", "");
			for (int i = 1; i < lines.length; i++) {
				printf(NONE, delimiter);
				printf(color, lines[i]);
			}
		}
		out.println();
		out.flush();
	}

	private void printVerticals() {
		printf(NONE, verticals());
	}

	private String verticals() {
		return verticals[frames.size()];
	}

	private long duration(long nanos) {
		return TimeUnit.NANOSECONDS.toMillis(nanos);
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

		static Color valueOf(TestExecutionResult result) {
			switch (result.getStatus()) {
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

		private final int ansiCode;

		Color(int ansiCode) {
			this.ansiCode = ansiCode;
		}

		@Override
		public String toString() {
			return "\u001B[" + this.ansiCode + "m";
		}
	}

	enum Theme {
		ASCII(".", "| ", "+--"),

		UTF_8(".", "│  ", "├─");

		static Theme valueOf(Charset charset) {
			if (StandardCharsets.UTF_8.equals(charset)) {
				return UTF_8;
			}
			return ASCII;
		}

		private final String[] tiles;

		Theme(String... tiles) {
			this.tiles = tiles;
		}

		String root() {
			return tiles[0];
		}

		String vertical() {
			return tiles[1];
		}

		String entry() {
			return tiles[2];
		}
	}

	class Frame {
		private final String uniqueId;
		private final long creationNanos;
		private int numberOfAborted;
		private int numberOfSkipped;
		private int numberOfFailed;
		private int numberOfSuccessful;
		private int numberOfStarted;

		private Frame(String uniqueId) {
			this.uniqueId = uniqueId;
			this.creationNanos = System.nanoTime();
		}

		private Frame count(TestExecutionResult result) {
			switch (result.getStatus()) {
				case SUCCESSFUL:
					numberOfSuccessful++;
					return this;
				case ABORTED:
					numberOfAborted++;
					return this;
				case FAILED:
					numberOfFailed++;
					return this;
				default:
					return this;
			}
		}

		@Override
		public String toString() {
			return "Frame{" + "numberOfAborted=" + numberOfAborted + ", numberOfSkipped=" + numberOfSkipped
					+ ", numberOfFailed=" + numberOfFailed + ", numberOfSuccessful=" + numberOfSuccessful
					+ ", numberOfStarted=" + numberOfStarted + '}';
		}
	}
}
