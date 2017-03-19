/*
 * Copyright 2015-2017 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.junit.jupiter.api;

import static java.lang.String.format;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.platform.commons.util.Preconditions.condition;
import static org.junit.platform.commons.util.Preconditions.notNull;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;
import java.util.regex.PatternSyntaxException;

/**
 * {@code AssertLinesMatch} is a collection of utility methods that support asserting
 * lines of {@link String} equality or {@link java.util.regex.Pattern}-match in tests.
 *
 * @since 5.0
 */
class AssertLinesMatch {

	private final static int MAX_SNIPPET_LENGTH = 21;

	static void assertLinesMatch(List<String> expectedLines, List<String> actualLines) {
		notNull(expectedLines, "expectedLines must not be null");
		notNull(actualLines, "actualLines must not be null");

		// trivial case: same list instance
		if (expectedLines == actualLines) {
			return;
		}

		int expectedSize = expectedLines.size();
		int actualSize = actualLines.size();

		// trivial case: when expecting more than actual lines available, something is wrong
		if (expectedSize > actualSize) {
			fail(expectedLines, actualLines, "expected %d lines, but only got %d", expectedSize, actualSize);
		}

		// simple case: both list are equally sized, compare them line-by-line
		if (expectedSize == actualSize) {
			boolean allOk = true;
			for (int i = 0; i < expectedSize; i++) {
				if (matches(expectedLines.get(i), actualLines.get(i))) {
					continue;
				}
				allOk = false;
				break;
			}
			if (allOk) {
				return;
			}
		}

		assertLinesMatchWithFastForward(expectedLines, actualLines);
	}

	private static void assertLinesMatchWithFastForward(List<String> expectedLines, List<String> actualLines) {
		Deque<String> expectedDeque = new ArrayDeque<>(expectedLines);
		Deque<String> actualDeque = new ArrayDeque<>(actualLines);

		main: while (!expectedDeque.isEmpty()) {
			String expectedLine = expectedDeque.pop();
			String actualLine = actualDeque.peek();

			// trivial case: take the fast path when they simply match
			if (matches(expectedLine, actualLine)) {
				actualDeque.pop();
				continue; // main
			}

			// fast-forward marker found in expected line: fast-forward actual line...
			if (isFastForwardLine(expectedLine)) {
				int fastForwardLimit = parseFastForwardLimit(expectedLine);

				// trivial case: fast-forward marker was in last expected line
				if (expectedDeque.isEmpty()) {
					int actualRemaining = actualDeque.size();
					// no limit given or perfect match? we're done.
					if (fastForwardLimit == Integer.MAX_VALUE || fastForwardLimit == actualRemaining) {
						return;
					}
					fail(expectedLines, actualLines, "terminal fast-forward(%d) error: fast-forward(%d) expected",
						fastForwardLimit, actualRemaining);
				}

				// fast-forward limit was given: use it
				if (fastForwardLimit != Integer.MAX_VALUE) {
					// fast-forward now: actualDeque.pop(fastForwardLimit)
					for (int i = 0; i < fastForwardLimit; i++) {
						actualDeque.pop();
					}
					continue; // main
				}

				// peek next expected line
				expectedLine = expectedDeque.peek();
				// fast-forward "unlimited": until next match
				while (true) {
					if (actualDeque.isEmpty()) {
						fail(expectedLines, actualLines, "fast-forward(∞) didn't find: `%s`", snippet(expectedLine));
					}
					if (matches(expectedLine, actualDeque.peek())) {
						continue main;
					}
					actualDeque.pop();
				}
			}

			int number = expectedLines.size() - expectedDeque.size(); // 1-based line number
			fail(expectedLines, actualLines, "expected line #%d:`%s` doesn't match", number, snippet(expectedLine));
		}

		// after math
		if (!actualDeque.isEmpty()) {
			fail(expectedLines, actualLines, "more actual lines than expected: %d", actualDeque.size());
		}
	}

	private static String snippet(String line) {
		if (line.length() <= MAX_SNIPPET_LENGTH) {
			return line;
		}
		return line.substring(0, MAX_SNIPPET_LENGTH - 5) + "[...]";
	}

	private static void fail(List<String> expectedLines, List<String> actualLines, String format, Object... args) {
		if (expectedLines.size() > MAX_SNIPPET_LENGTH) {
			expectedLines.subList(0, MAX_SNIPPET_LENGTH);
		}
		if (actualLines.size() > MAX_SNIPPET_LENGTH) {
			actualLines.subList(0, MAX_SNIPPET_LENGTH);
		}
		// use standard assertEquals(Object, Object, message) to let IDEs present the textual difference
		String expected = String.join(System.lineSeparator(), expectedLines);
		String actual = String.join(System.lineSeparator(), actualLines);
		assertEquals(expected, actual, format(format, args));
	}

	static boolean isFastForwardLine(String line) {
		line = line.trim();
		return line.length() >= 4 && line.startsWith(">>") && line.endsWith(">>");
	}

	static int parseFastForwardLimit(String fastForwardLine) {
		String text = fastForwardLine.trim().substring(2, fastForwardLine.length() - 2).trim();
		try {
			int limit = Integer.parseInt(text);
			condition(limit > 0, () -> format("fast-forward(%d) limit must be greater than zero", limit));
			return limit;
		}
		catch (NumberFormatException e) {
			return Integer.MAX_VALUE;
		}
	}

	static boolean matches(String expectedLine, String actualLine) {
		if (expectedLine.equals(actualLine)) {
			return true;
		}
		try {
			return actualLine.matches(expectedLine);
		}
		catch (PatternSyntaxException ignore) {
			return false;
		}
	}
}
