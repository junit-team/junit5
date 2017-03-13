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

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import org.junit.platform.commons.util.Preconditions;

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
			for (int i = 0; i < expectedSize; i++) {
				assertMatches(expectedLines.get(i), actualLines.get(i), i, i);
			}
			return;
		}

		assertLinesMatchWithDSL(expectedLines, actualLines);
	}

	private static void assertLinesMatchWithDSL(List<String> expectedLines, List<String> actualLines) {
		assert expectedLines.size() < actualLines.size() : "unexpected lines sizes";

		for (int e = 0, a = 0; e < expectedLines.size() && a < actualLines.size(); e++, a++) {
			String expectedLine = expectedLines.get(e);
			String actualLine = actualLines.get(a);
			// trivial case: take the fast path when both lines are equal
			if (expectedLine.equals(actualLine)) {
				continue;
			}
			// fast forward markers found in expected line: fast forward actual line until next match
			if (isFastForwardLine(expectedLine.trim())) {
				int nextExpectedIndex = e + 1;
				if (nextExpectedIndex >= expectedLines.size()) {
					// trivial case: marker was last line in expected list
					return;
				}
				expectedLine = expectedLines.get(nextExpectedIndex);
				int ahead = a;
				while (!matches(expectedLine, actualLine, false)) {
					actualLine = actualLines.get(ahead++);
					if (ahead > actualLines.size()) {
						fail("ran out of actual bounds");
					}
				}
				a = ahead - 2; // "side-effect" assignment to for-loop variable on purpose
				continue;
			}
			// now, assert equality of expect and actual line
			assertMatches(expectedLine, actualLine, e, a);
		}
	}

	private static boolean isFastForwardLine(String line) {
		return line.equals("S T A C K T R A C E") || line.startsWith("{{") && line.endsWith("}}");
	}

	private static int getUpperLimit(String fastForwardLine) {
		// TODO implement and use
		return Integer.MAX_VALUE;
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
