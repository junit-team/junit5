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

import static org.junit.platform.console.tasks.Color.BLUE;
import static org.junit.platform.console.tasks.Color.NONE;
import static org.junit.platform.console.tasks.Color.RED;
import static org.junit.platform.console.tasks.Color.YELLOW;

import java.io.PrintWriter;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.concurrent.TimeUnit;

import org.junit.platform.engine.TestExecutionResult;
import org.junit.platform.launcher.TestExecutionListener;
import org.junit.platform.launcher.TestIdentifier;
import org.junit.platform.launcher.TestPlan;

/**
 * @since 1.0
 */
class TreePrinter implements TestExecutionListener {

	private final PrintWriter out;
	private final boolean disableAnsiColors;
	final Theme theme;
	final Deque<Frame> frames;
	final String[] verticals;
	long executionStartedNanos;

	TreePrinter(PrintWriter out, boolean disableAnsiColors) {
		this(out, disableAnsiColors, 16, Theme.valueOf(Charset.defaultCharset()));
	}

	TreePrinter(PrintWriter out, boolean disableAnsiColors, int maxContainerNestingLevel, Theme theme) {
		this.out = out;
		this.disableAnsiColors = disableAnsiColors;
		this.theme = theme;
		// Create frame stack and push initial root frame.
		this.frames = new ArrayDeque<>();
		this.frames.push(new Frame("/"));
		// Create and populate vertical indentation lookup table
		this.verticals = new String[Math.max(10, maxContainerNestingLevel)];
		this.verticals[0] = ""; // no frame
		this.verticals[1] = ""; // synthetic root "/" level
		this.verticals[2] = ""; // "engine" level
		for (int i = 3; i < verticals.length; i++) {
			verticals[i] = verticals[i - 1] + theme.vertical();
		}
	}

	@Override
	public void testPlanExecutionStarted(TestPlan testPlan) {
		frames.push(new Frame(testPlan.toString()));
	}

	@Override
	public void testPlanExecutionFinished(TestPlan testPlan) {
		frames.pop();
	}

	@Override
	public void executionStarted(TestIdentifier testIdentifier) {
		frames.peek().numberOfStarted++;
		executionStartedNanos = System.nanoTime();
		if (testIdentifier.isContainer()) {
			printVerticals(theme.entry());
			printf(BLUE, " %s", testIdentifier.getDisplayName());
			printf(NONE, "%n");
			frames.push(new Frame(testIdentifier.getUniqueId()));
		}
	}

	@Override
	public void executionFinished(TestIdentifier testIdentifier, TestExecutionResult testExecutionResult) {
		if (testIdentifier.isContainer()) {
			frames.pop();
			return;
		}
		frames.peek().count(testExecutionResult);
		long nanos = System.nanoTime() - executionStartedNanos;
		Color color = Color.valueOf(testExecutionResult);
		printVerticals(theme.entry());
		printf(color, " %s", testIdentifier.getDisplayName());
		printf(NONE, " (%d ms)", duration(nanos));
		testExecutionResult.getThrowable().ifPresent(t -> printf(RED, " %s", t));
		printf(NONE, "%n");
	}

	@Override
	public void executionSkipped(TestIdentifier testIdentifier, String reason) {
		frames.peek().numberOfSkipped++;
		printVerticals(theme.entry());
		printf(YELLOW, " %s %s%n", testIdentifier.getDisplayName(), reason);
	}

	void printf(Color color, String message, Object... args) {
		if (disableAnsiColors || color == NONE) {
			out.printf(message, args);
		}
		else {
			// Use string concatenation to avoid ANSI disruption on console
			out.printf(color + message + NONE, args);
		}
		out.flush();
	}

	/** Print verticals and stay in the current line. */
	void printVerticals(String tile) {
		printf(NONE, verticals[frames.size()]);
		printf(NONE, tile);
	}

	long duration(long nanos) {
		return TimeUnit.NANOSECONDS.toMillis(nanos);
	}

	enum Theme {
		/**
		 * ASCII 7-bit characters form the tree branch.
		 *
		 * <pre>
		 * .
		 * +-- JUnit Vintage
		 * |  +-- example.JUnit4Tests
		 * |  |  -  standardJUnit4Test
		 * +-- JUnit Jupiter
		 * |  +-- AssertionsDemo
		 * |  |  -  timeoutExceeded()
		 * |  |  -  exceptionTesting()
		 * </pre>
		 */
		ASCII(".", "| ", "+--"),

		/**
		 * Extended ASCII characters are used to display the test execution tree.
		 *
		 * <pre>
		 * .
		 * ├─ JUnit Vintage
		 * │  ├─ example.JUnit4Tests
		 * │  │  ├─ standardJUnit4Test
		 * ├─ JUnit Jupiter
		 * │  ├─ A stack
		 * │  │  ├─ is instantiated with new Stack()
		 * </pre>
		 */
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

	static class Frame {
		final String uniqueId;
		final long creationNanos;
		int numberOfAborted;
		int numberOfSkipped;
		int numberOfFailed;
		int numberOfSuccessful;
		int numberOfStarted;

		Frame(String uniqueId) {
			this.uniqueId = uniqueId;
			this.creationNanos = System.nanoTime();
		}

		Frame count(TestExecutionResult result) {
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
