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

import static org.junit.platform.console.tasks.Color.BLUE;
import static org.junit.platform.console.tasks.Color.NONE;
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
class TreePrintingListener implements TestExecutionListener {

	private final PrintWriter out;
	private final boolean monochrome;
	final Theme theme;
	final Deque<Frame> frames;
	final String[] verticals;
	long executionStartedNanos;

	TreePrintingListener(PrintWriter out, boolean monochrome) {
		this(out, monochrome, 16, Theme.valueOf(Charset.defaultCharset()));
	}

	TreePrintingListener(PrintWriter out, boolean monochrome, int maxContainerNestingLevel, Theme theme) {
		this.out = out;
		this.monochrome = monochrome;
		this.theme = theme;
		// create frame stack and push initial root frame
		this.frames = new ArrayDeque<>();
		this.frames.push(new Frame("/"));
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
		frames.push(new Frame(testPlan.toString()));
	}

	@Override
	public void testPlanExecutionFinished(TestPlan testPlan) {
		frames.pop();
	}

	@Override
	public void executionStarted(TestIdentifier testIdentifier) {
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
		Color color = Color.valueOf(testExecutionResult);
		printVerticals(theme.entry());
		printf(NONE, " %s", testIdentifier.getDisplayName());
		// printf(NONE, " %d ms", durationInMillis(System.nanoTime() - executionStartedNanos));
		printf(color, " %s", theme.computeStatusTile(testExecutionResult));
		testExecutionResult.getThrowable().ifPresent(t -> printf(color, " %s", t.getMessage()));
		printf(NONE, "%n");
	}

	@Override
	public void executionSkipped(TestIdentifier testIdentifier, String reason) {
		printVerticals(theme.entry());
		printf(testIdentifier.isContainer() ? BLUE : NONE, " %s", testIdentifier.getDisplayName());
		printf(YELLOW, " %s %s%n", theme.skipped(), reason);
	}

	void printf(Color color, String message, Object... args) {
		if (monochrome || color == NONE) {
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
		printf(NONE, verticals[Math.min(frames.size(), verticals.length)]);
		printf(NONE, tile);
	}

	long durationInMillis(long nanos) {
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
		ASCII(".", "| ", "+--", "---", "[OK]", "[A]", "[X]", "[S]"),

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
		UTF_8(".", "│  ", "├─", "└─", "✔", "■", "✘", "↷");

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

		String end() {
			return tiles[3];
		}

		String successful() {
			return tiles[4];
		}

		String aborted() {
			return tiles[5];
		}

		String failed() {
			return tiles[6];
		}

		String skipped() {
			return tiles[7];
		}

		String computeStatusTile(TestExecutionResult result) {
			switch (result.getStatus()) {
				case SUCCESSFUL:
					return successful();
				case ABORTED:
					return aborted();
				case FAILED:
					return failed();
				default:
					return result.getStatus().name();
			}
		}
	}

	static class Frame {
		final String uniqueId;
		final long creationNanos;

		Frame(String uniqueId) {
			this.uniqueId = uniqueId;
			this.creationNanos = System.nanoTime();
		}
	}
}
