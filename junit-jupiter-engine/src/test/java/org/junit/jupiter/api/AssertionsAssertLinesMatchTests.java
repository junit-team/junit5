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

import java.util.Arrays;
import java.util.List;

/**
 * Unit tests for JUnit Jupiter {@link Assertions}.
 *
 * @since 5.0
 */
public class AssertionsAssertLinesMatchTests {

	@Test
	void assertLinesMatchesToSelf() {
		List<String> list = Arrays.asList("first line", "second line", "third line", "last line");
		AssertLinesMatch.assertLinesMatch(list, list);
	}

	@Test
	void assertLinesMatchesPlainEqualLists() {
		List<String> expected = Arrays.asList("first line", "second line", "third line", "last line");
		List<String> actual = Arrays.asList("first line", "second line", "third line", "last line");
		AssertLinesMatch.assertLinesMatch(expected, actual);
	}

	@Test
	void assertLinesMatchesUsingRegexPatterns() {
		List<String> expected = Arrays.asList("^first.+line", "second\\s*line", "th.rd l.ne", "last line$");
		List<String> actual = Arrays.asList("first line", "second line", "third line", "last line");
		AssertLinesMatch.assertLinesMatch(expected, actual);
	}

	@Test
	void assertLinesMatchesUsingFastForwardCommand() {
		List<String> expected = Arrays.asList("first line", "{{ skip lines until next matches }}", "last line");
		List<String> actual = Arrays.asList("first line", "second line", "third line", "last line");
		AssertLinesMatch.assertLinesMatch(expected, actual);
	}

}
