/*
 * Copyright 2015-2019 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * https://www.eclipse.org/legal/epl-v20.html
 */

package org.junit.jupiter.api;

import static org.junit.jupiter.api.AssertLinesMatch.isFastForwardLine;
import static org.junit.jupiter.api.AssertLinesMatch.parseFastForwardLimit;
import static org.junit.jupiter.api.AssertionTestUtils.assertMessageEquals;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertLinesMatch;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.function.Supplier;

import org.junit.platform.commons.util.PreconditionViolationException;
import org.opentest4j.AssertionFailedError;

/**
 * Unit tests for JUnit Jupiter {@link Assertions}.
 *
 * @since 5.0
 */
class AssertLinesMatchAssertionsTests {

	@Test
	void assertLinesMatchEmptyLists() {
		assertLinesMatch(Collections.emptyList(), new ArrayList<>());
	}

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
	void assertLinesMatchWithNullFails() {
		assertThrows(PreconditionViolationException.class, () -> assertLinesMatch(null, null));
		assertThrows(PreconditionViolationException.class, () -> assertLinesMatch(null, Collections.emptyList()));
		assertThrows(PreconditionViolationException.class, () -> assertLinesMatch(Collections.emptyList(), null));
	}

	@Test
	void assertLinesMatchWithNullElementsFails() {
		var list = List.of("1", "2", "3");
		var withNullElement = Arrays.asList("1", null, "3"); // List.of() doesn't permit null values.
		assertDoesNotThrow(() -> assertLinesMatch(withNullElement, withNullElement));
		var e1 = assertThrows(PreconditionViolationException.class, () -> assertLinesMatch(withNullElement, list));
		assertEquals("expected line must not be null", e1.getMessage());
		var e2 = assertThrows(PreconditionViolationException.class, () -> assertLinesMatch(list, withNullElement));
		assertEquals("actual line must not be null", e2.getMessage());
	}

	@Test
	void assertLinesMatchMoreExpectedThanActualAvailableFails() {
		List<String> expected = Arrays.asList("first line", "second line", "third line");
		List<String> actual = Arrays.asList("first line", "third line");
		Error error = assertThrows(AssertionFailedError.class, () -> assertLinesMatch(expected, actual));
		List<String> expectedErrorMessageLines = Arrays.asList( //
			"expected 3 lines, but only got 2 ==> expected: <", //
			"first line", //
			"second line", //
			"third line", //
			"> but was: <", //
			"first line", //
			"third line", //
			">");
		assertLinesMatch(expectedErrorMessageLines, Arrays.asList(error.getMessage().split("\\R")));
	}

	@Test
	void assertLinesMatchFailsWithDescriptiveErrorMessage() {
		List<String> expected = Arrays.asList("first line", "second line", "third line");
		List<String> actual = Arrays.asList("first line", "sec0nd line", "third line");
		Error error = assertThrows(AssertionFailedError.class, () -> assertLinesMatch(expected, actual));
		List<String> expectedErrorMessageLines = Arrays.asList( //
			"expected line #2:`second line` doesn't match ==> expected: <", //
			"first line", //
			"second line", //
			"third line", //
			"> but was: <", //
			"first line", //
			"sec0nd line", //
			"third line", //
			">");
		assertLinesMatch(expectedErrorMessageLines, Arrays.asList(error.getMessage().split("\\R")));
	}

	@Test
	void assertLinesMatchMoreActualLinesThenExpectedFails() {
		List<String> expected = Arrays.asList("first line", "second line", "third line");
		List<String> actual = Arrays.asList("first line", "second line", "third line", "last line");
		Error error = assertThrows(AssertionFailedError.class, () -> assertLinesMatch(expected, actual));
		List<String> expectedErrorMessageLines = Arrays.asList( //
			"more actual lines than expected: 1 ==> expected: <", //
			"first line", //
			"second line", //
			"third line", "> but was: <", //
			"first line", //
			"second line", //
			"third line", //
			"last line", //
			">");
		assertLinesMatch(expectedErrorMessageLines, Arrays.asList(error.getMessage().split("\\R")));
	}

	@Test
	void assertLinesMatchUsingFastForwardMarkerWithTooLowLimitFails() {
		List<String> expected = Arrays.asList("first line", ">> 1 >>");
		List<String> actual = Arrays.asList("first line", "skipped", "last line");
		Error error = assertThrows(AssertionFailedError.class, () -> assertLinesMatch(expected, actual));
		List<String> expectedErrorMessageLines = Arrays.asList( //
			"terminal fast-forward(1) error: fast-forward(2) expected ==> expected: <", //
			"first line", //
			">> 1 >>", //
			"> but was: <", //
			"first line", //
			"skipped", //
			"last line", //
			">");
		assertLinesMatch(expectedErrorMessageLines, Arrays.asList(error.getMessage().split("\\R")));
	}

	@Test
	void assertLinesMatchUsingFastForwardMarkerWithTooHighLimitFails() {
		List<String> expected = Arrays.asList("first line", ">> 100 >>");
		List<String> actual = Arrays.asList("first line", "skipped", "last line");
		Error error = assertThrows(AssertionFailedError.class, () -> assertLinesMatch(expected, actual));
		List<String> expectedErrorMessageLines = Arrays.asList( //
			"terminal fast-forward(100) error: fast-forward(2) expected ==> expected: <", //
			"first line", //
			">> 100 >>", "> but was: <", "first line", //
			"skipped", //
			"last line", //
			">");
		assertLinesMatch(expectedErrorMessageLines, Arrays.asList(error.getMessage().split("\\R")));
	}

	@Test
	void assertLinesMatchUsingFastForwardMarkerWithoutMatchingNextLineFails() {
		List<String> expected = Arrays.asList("first line", ">> fails, because next line is >>", "not present");
		List<String> actual = Arrays.asList("first line", "skipped", "last line");
		Error error = assertThrows(AssertionFailedError.class, () -> assertLinesMatch(expected, actual));
		List<String> expectedErrorMessageLines = Arrays.asList( //
			"fast-forward(∞) didn't find: `not present` ==> expected: <", //
			"first line", //
			">> fails, because next line is >>", //
			"not present", //
			"> but was: <", //
			"first line", //
			"skipped", //
			"last line", //
			">");
		assertLinesMatch(expectedErrorMessageLines, Arrays.asList(error.getMessage().split("\\R")));
	}

	@Test
	void assertLinesMatchUsingFastForwardMarkerWithExtraExpectLineFails() {
		List<String> expected = Arrays.asList("first line", ">> fails, because final line is missing >>", "last line",
			"not present");
		List<String> actual = Arrays.asList("first line", "first skipped", "second skipped", "last line");
		Error error = assertThrows(AssertionFailedError.class, () -> assertLinesMatch(expected, actual));
		List<String> expectedErrorMessageLines = Arrays.asList( //
			"expected line #4:`not present` not found - actual lines depleted ==> expected: <", //
			"first line", //
			">> fails, because final line is missing >>", //
			"last line", //
			"not present", //
			"> but was: <", //
			"first line", //
			"first skipped", //
			"second skipped", //
			"last line", //
			">");
		assertLinesMatch(expectedErrorMessageLines, Arrays.asList(error.getMessage().split("\\R")));
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
		Throwable error = assertThrows(PreconditionViolationException.class, () -> parseFastForwardLimit(">>0>>"));
		assertMessageEquals(error, "fast-forward(0) limit must be greater than zero");
		error = assertThrows(PreconditionViolationException.class, () -> parseFastForwardLimit(">>-1>>"));
		assertMessageEquals(error, "fast-forward(-1) limit must be greater than zero");
		error = assertThrows(PreconditionViolationException.class, () -> parseFastForwardLimit(">>-2147483648>>"));
		assertMessageEquals(error, "fast-forward(-2147483648) limit must be greater than zero");
	}

	@Test
	void assertLinesMatchMatches() {
		Random random = new Random();
		assertAll("do match", //
			() -> assertTrue(
				AssertLinesMatch.matches("duration: [\\d]+ ms", "duration: " + random.nextInt(1000) + " ms")),
			() -> assertTrue(AssertLinesMatch.matches("123", "123")),
			() -> assertTrue(AssertLinesMatch.matches(".*", "123")),
			() -> assertTrue(AssertLinesMatch.matches("\\d+", "123")));
		assertAll("don't match", //
			() -> assertFalse(
				AssertLinesMatch.matches("duration: [\\d]+ ms", "duration: " + random.nextGaussian() + " ms")),
			() -> assertFalse(AssertLinesMatch.matches("12", "123")),
			() -> assertFalse(AssertLinesMatch.matches("..+", "1")),
			() -> assertFalse(AssertLinesMatch.matches("\\d\\d+", "1")));
	}

	/**
	 * @since 5.5
	 */
	@Nested
	class WithCustomFailureMessage {
		@Test
		void simpleStringMessage() {
			String message = "XXX";
			List<String> expected = Arrays.asList("a", "b", "c");
			List<String> actual = Arrays.asList("a", "d", "c");
			Error error = assertThrows(AssertionFailedError.class, () -> assertLinesMatch(expected, actual, message));
			List<String> expectedErrorMessageLines = Arrays.asList( //
				message + " ==> " + "expected line #2:`b` doesn't match ==> expected: <", //
				"a", //
				"b", //
				"c", //
				"> but was: <", //
				"a", //
				"d", //
				"c", //
				">");
			assertLinesMatch(expectedErrorMessageLines, Arrays.asList(error.getMessage().split("\\R")));
		}

		@Test
		void stringSupplierWithMultiLineMessage() {
			String message = "XXX\nYYY";
			Supplier<String> supplier = () -> message;
			List<String> expected = Arrays.asList("a", "b", "c");
			List<String> actual = Arrays.asList("a", "d", "c");
			Error error = assertThrows(AssertionFailedError.class, () -> assertLinesMatch(expected, actual, supplier));
			List<String> expectedErrorMessageLines = Arrays.asList( //
				"XXX", //
				"YYY ==> " + "expected line #2:`b` doesn't match ==> expected: <", //
				"a", //
				"b", //
				"c", //
				"> but was: <", //
				"a", //
				"d", //
				"c", //
				">");
			assertLinesMatch(expectedErrorMessageLines, Arrays.asList(error.getMessage().split("\\R")));
		}
	}
}
