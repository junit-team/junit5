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

import static org.junit.jupiter.api.AssertLinesMatch.isFastForwardLine;
import static org.junit.jupiter.api.AssertLinesMatch.parseFastForwardLimit;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertLinesMatch;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.opentest4j.AssertionFailedError;

/**
 * Unit tests for JUnit Jupiter {@link Assertions}.
 *
 * @since 5.0
 */
public class AssertionsAssertLinesMatchTests {

	@Test
	void assertLinesMatchSameListInstance() {
		List<String> list = Arrays.asList("first line", "second line", "third line", "last line");
		assertLinesMatch(list, list);
	}

	@Test
	void assertLinesMatchPlainEqualLists() {
		List<String> expected = Arrays.asList("first line", "second line", "third line", "last line");
		List<String> actual = Arrays.asList("first line", "second line", "third line", "last line");
		assertLinesMatch(expected, actual);
	}

	@Test
	void assertLinesMatchUsingRegexPatterns() {
		List<String> expected = Arrays.asList("^first.+line", "second\\s*line", "th.rd l.ne", "last line$");
		List<String> actual = Arrays.asList("first line", "second line", "third line", "last line");
		assertLinesMatch(expected, actual);
	}

	@Test
	void assertLinesMatchUsingFastForwardMarkerAtEndOfExpectedLines() {
		List<String> expected = Arrays.asList("first line", ">> ignore all following lines >>");
		List<String> actual = Arrays.asList("first line", "I", "II", "III", "IV", "V", "VI", "last line");
		assertLinesMatch(expected, actual);
	}

	@Test
	void assertLinesMatchUsingFastForwardMarker() {
		List<String> expected = Arrays.asList("first line", ">> skip lines until next matches >>", "V", "last line");
		List<String> actual = Arrays.asList("first line", "I", "II", "III", "IV", "V", "last line");
		assertLinesMatch(expected, actual);
	}

	@Test
	void assertLinesMatchUsingFastForwardMarkerWithLimit1() {
		List<String> expected = Arrays.asList("first line", ">> 1 >>", "last line");
		List<String> actual = Arrays.asList("first line", "skipped", "last line");
		assertLinesMatch(expected, actual);
	}

	@Test
	void assertLinesMatchUsingFastForwardMarkerWithLimit3() {
		List<String> expected = Collections.singletonList(">> 3 >>");
		List<String> actual = Arrays.asList("first line", "skipped", "last line");
		assertLinesMatch(expected, actual);
	}

	@Test
	void assertLinesMatchMoreActualLinesThenExpectedFails() {
		List<String> expected = Arrays.asList("first line", "second line", "third line");
		List<String> actual = Arrays.asList("first line", "second line", "third line", "last line");
		Error error = assertThrows(AssertionFailedError.class, () -> assertLinesMatch(expected, actual));
		assertEquals("more actual lines than expected: 1", error.getMessage());
	}

	@Test
	void assertLinesMatchUsingFastForwardMarkerWithTooLowLimitFails() {
		List<String> expected = Arrays.asList("first line", ">> 1 >>");
		List<String> actual = Arrays.asList("first line", "skipped", "last line");
		Error error = assertThrows(AssertionFailedError.class, () -> assertLinesMatch(expected, actual));
		assertEquals("terminal fast-forward(1) error: fast-forward(2) expected", error.getMessage());
	}

	@Test
	void assertLinesMatchUsingFastForwardMarkerWithTooHighLimitFails() {
		List<String> expected = Arrays.asList("first line", ">> 100 >>");
		List<String> actual = Arrays.asList("first line", "skipped", "last line");
		Error error = assertThrows(AssertionFailedError.class, () -> assertLinesMatch(expected, actual));
		assertEquals("terminal fast-forward(100) error: fast-forward(2) expected", error.getMessage());
	}

	@Test
	void assertLinesMatchIsFastForwardLine() {
		assertAll("valid fast-forward lines", //
			() -> assertTrue(isFastForwardLine(">>>>")), () -> assertTrue(isFastForwardLine(">> >>")),
			() -> assertTrue(isFastForwardLine(">> stacktrace >>")),
			() -> assertTrue(isFastForwardLine(">> single line, non Integer.parse()-able comment >>")),
			() -> assertTrue(isFastForwardLine(">>9>>")), () -> assertTrue(isFastForwardLine(">> 9 >>")),
			() -> assertTrue(isFastForwardLine(">> -9 >>")));
	}

	@Test
	void assertLinesMatchParseFastForwardLimit() {
		assertAll("valid fast-forward limits", //
			() -> assertEquals(Integer.MAX_VALUE, parseFastForwardLimit(">>>>")),
			() -> assertEquals(Integer.MAX_VALUE, parseFastForwardLimit(">> >>")),
			() -> assertEquals(Integer.MAX_VALUE, parseFastForwardLimit(">> stacktrace >>")),
			() -> assertEquals(Integer.MAX_VALUE, parseFastForwardLimit(">> non Integer.parse()-able comment >>")),
			() -> assertEquals(9, parseFastForwardLimit(">>9>>")),
			() -> assertEquals(9, parseFastForwardLimit(">> 9 >>")));
	}

	@Test
	void assertLinesMatchMatches() {
		assertAll("valid fast-forward lines", //
			() -> assertTrue(AssertLinesMatch.matches("123", "123")),
			() -> assertTrue(AssertLinesMatch.matches(".*", "123")),
			() -> assertTrue(AssertLinesMatch.matches("\\d+", "123")));
	}
}
