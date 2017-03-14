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
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.util.Deque;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import org.junit.platform.commons.util.Preconditions;
import org.opentest4j.AssertionFailedError;

/**
 * {@code AssertLinesMatch} is a collection of utility methods that support asserting
 * lines of {@link String} equality or {@link java.util.regex.Pattern}-match in tests.
 *
 * @since 5.0
 */
class AssertLinesMatch {

	static void assertLinesMatch(List<String> expectedLines, List<String> actualLines) {
		Preconditions.notNull(expectedLines, "expectedLines must not be null");
		Preconditions.notNull(actualLines, "actualLines must not be null");

		// trivial case: same list instance
		if (expectedLines == actualLines) {
			return;
		}

		int expectedSize = expectedLines.size();
		int actualSize = actualLines.size();

		// trivial case: when expecting more then actual lines available, something is wrong
		if (expectedSize > actualSize) {
			// use standard assertEquals(Object, Object, message) to let IDEs present the textual difference
			String expected = String.join(System.lineSeparator(), expectedLines);
			String actual = String.join(System.lineSeparator(), actualLines);
			assertEquals(expected, actual, "expected " + expectedSize + " lines, but only got " + actualSize);
			fail("should not happen as expected != actual was asserted");
		}

		// simple case: both list are equally sized, compare them line-by-line
		if (expectedSize == actualSize) {
			try {
				for (int i = 0; i < expectedSize; i++) {
					assertMatches(expectedLines.get(i), actualLines.get(i), i, i);
				}
				return;
			}
			catch (AssertionFailedError ignore) {
				// fall through and try with fast-forward support
			}
		}

		assertLinesMatchWithFastForward(expectedLines, actualLines);
	}

	private static void assertLinesMatchWithFastForward(List<String> expectedLines, List<String> actualLines) {
		Deque<String> expectedDeque = new LinkedList<>(expectedLines);
		Deque<String> actualDeque = new LinkedList<>(actualLines);
		while (!expectedDeque.isEmpty()) {
			String expectedLine = expectedDeque.pop();
			String actualLine = actualDeque.peek();
			// trivial case: take the fast path when they simply match
			if (matches(expectedLine, actualLine, false)) {
				actualDeque.pop();
				continue;
			}
			// fast-forward markers found in expected line: fast-forward actual line...
			if (isFastForwardLine(expectedLine)) {
				int fastForwardLimit = parseFastForwardLimit(expectedLine);
				// value was given
				if (fastForwardLimit != Integer.MAX_VALUE) {
					if (fastForwardLimit > actualDeque.size()) {
						fail(format("fast-forward %d lines failed, only %d lines left", fastForwardLimit,
							actualDeque.size()));
					}
					if (expectedDeque.isEmpty()) {
						assertEquals(fastForwardLimit, actualDeque.size(), "wrong number of actual lines remaining");
						return; // perfect match
					}
					// fast-forward now: actualDeque.pop(fastForwardLimit)
					for (int i = 0; i < fastForwardLimit; i++) {
						actualDeque.pop();
					}
					if (actualDeque.isEmpty()) {
						fail(format("%d more lines expected, actual lines is empty", expectedDeque.size()));
					}
				}
				else {
					if (expectedDeque.isEmpty()) {
						return; // ignore all remaining actual lines
					}
					// scan actual lines deque for next match
					while (!actualDeque.isEmpty()) {
						actualLine = actualDeque.pop();
						if (matches(expectedDeque.peek(), actualLine, false)) {
							actualDeque.push(actualLine);
							break;
						}
					}
				}
				expectedLine = expectedDeque.pop();
			}
			// now, assert equality of current expected and actual line
			int expectedIndex = expectedLines.size() - expectedDeque.size();
			int actualIndex = actualLines.size() - actualDeque.size();
			assertMatches(expectedLine, actualDeque.pop(), expectedIndex, actualIndex);
		}
		if (actualDeque.isEmpty()) {
			return;
		}
		fail("more actual lines than expected: " + actualDeque.size());
	}

	private static boolean isFastForwardLine(String line) {
		line = line.trim();
		return line.equals("S T A C K T R A C E") || line.startsWith(">>") && line.endsWith(">>");
	}

	private static int parseFastForwardLimit(String fastForwardLine) {
		String text = fastForwardLine.trim().substring(2, fastForwardLine.length() - 2).trim();
		try {
			int limit = Integer.parseInt(text);
			Preconditions.condition(limit > 0, "fast-forward must greater than zero, it is: " + limit);
			return limit;
		}
		catch (NumberFormatException e) {
			return Integer.MAX_VALUE;
		}
	}

	private static void assertMatches(String expectedLine, String actualLine, int expectedIndex, int actualIndex) {
		assertTrue(matches(expectedLine, actualLine, true), //
			() -> format("%nexpected:%d = %s%nactual:%d = %s", expectedIndex, expectedLine, actualIndex, actualLine));
	}

	private static boolean matches(String expectedLine, String actualLine, boolean failOnPatternSyntaxException) {
		if (expectedLine.equals(actualLine)) {
			return true;
		}
		try {
			Pattern pattern = Pattern.compile(expectedLine);
			Matcher matcher = pattern.matcher(actualLine);
			return matcher.matches();
		}
		catch (PatternSyntaxException exception) {
			if (failOnPatternSyntaxException) {
				fail("expected line is not a valid regex pattern" + expectedLine, exception);
			}
			return false;
		}
	}

}
