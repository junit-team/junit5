/*
 * Copyright 2015-2018 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v20.html
 */

package org.junit.jupiter.params;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.Arrays;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.ValueSource;
import org.junit.platform.commons.JUnitException;

/**
 * @since 5.0
 */
class ParameterizedTestNameFormatterTests {

	private static final Arguments EMPTY_ARGUMENTS = Arguments.of();

	@Test
	void formatsInvocationIndex() {
		ParameterizedTestNameFormatter formatter = new ParameterizedTestNameFormatter("{index}");

		assertEquals("1", formatter.format(1, EMPTY_ARGUMENTS));
		assertEquals("2", formatter.format(2, EMPTY_ARGUMENTS));
	}

	@Test
	void formatsIndividualArguments() {
		ParameterizedTestNameFormatter formatter = new ParameterizedTestNameFormatter("{0} -> {1}");

		assertEquals("foo -> 42", formatter.format(1, arguments("foo", 42)));
	}

	@Test
	void formatsCompleteArgumentsList() {
		ParameterizedTestNameFormatter formatter = new ParameterizedTestNameFormatter("{arguments}");

		// @formatter:off
		assertEquals("42, 99, enigma, null, [1, 2, 3], [foo, bar], [[2, 4], [3, 9]]",
			formatter.format(1,
					arguments(Integer.valueOf(42),
							99,
							"enigma",
							null,
							new int[] { 1, 2, 3 },
							new String[] { "foo", "bar" },
							new Integer[][] { { 2, 4 }, { 3, 9 } }
				)));
		// @formatter:on
	}

	@Test
	void formatsArgumentsDescription() {
		ParameterizedTestNameFormatter formatter = new ParameterizedTestNameFormatter(
				"{arguments.description}"
		);

		assertEquals("test case description",
				formatter.format(1, Arguments.of().describedAs("test case description"))
		);
	}

	@Test
	void formatsArgumentsAndTheirDescription() {
		ParameterizedTestNameFormatter formatter = new ParameterizedTestNameFormatter(
				"[{index}] {arguments}: {arguments.description}"
		);

		int invocationIndex = 1;
		Arguments arguments = Arguments.of(2, 4)
				.describedAs("squared(2) = 4");

		assertEquals("[1] 2, 4: squared(2) = 4",
				formatter.format(invocationIndex, arguments)
		);
	}

	// todo: default pattern is likely to be changed? Or its interpretation:
	// ({arguments} = {arguments + arguments.description)?
	@Test
	void formatsInvocationIndexAndCompleteArgumentsListUsingDefaultPattern() {
		ParameterizedTestNameFormatter formatter = new ParameterizedTestNameFormatter("[{index}] {arguments}");

		// Explicit test for https://github.com/junit-team/junit5/issues/814
		assertEquals("[1] [foo, bar]",
				formatter.format(1, arguments((Object) new String[] { "foo", "bar" }))
		);

		assertEquals("[1] [foo, bar], 42, true",
				formatter.format(1, arguments(new String[] { "foo", "bar" }, 42, true))
		);
	}

	@Test
	void formatDoesNotAlterArgumentsArray() {
		ParameterizedTestNameFormatter formatter = new ParameterizedTestNameFormatter("{arguments}");
		Object[] actual = { 1, "two", Byte.valueOf("-128"), new Integer[][] { { 2, 4 }, { 3, 9 } } };
		Object[] expected = Arrays.copyOf(actual, actual.length);
		assertEquals("1, two, -128, [[2, 4], [3, 9]]", formatter.format(1, arguments(actual)));
		assertArrayEquals(expected, actual);
	}

	@ParameterizedTest
	@ValueSource(strings = {
			"{}",
			"{index",
			"{-1}"
	})
	void throwsReadableExceptionForInvalidPatterns(String invalidPattern) {
		ParameterizedTestNameFormatter formatter = new ParameterizedTestNameFormatter(invalidPattern);

		JUnitException exception = assertThrows(JUnitException.class,
				() -> formatter.format(1, EMPTY_ARGUMENTS)
		);
		assertNotNull(exception.getCause());
		assertEquals(IllegalArgumentException.class, exception.getCause().getClass());
	}

	private static Arguments arguments(Object... arguments) {
		return Arguments.of(arguments);
	}
}
