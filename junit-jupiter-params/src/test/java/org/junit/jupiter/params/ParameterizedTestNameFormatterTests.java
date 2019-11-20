/*
 * Copyright 2015-2019 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * https://www.eclipse.org/legal/epl-v20.html
 */

package org.junit.jupiter.params;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.params.ParameterizedTest.ARGUMENTS_PLACEHOLDER;
import static org.junit.jupiter.params.ParameterizedTest.ARGUMENTS_WITH_NAMES_PLACEHOLDER;
import static org.junit.jupiter.params.ParameterizedTest.DISPLAY_NAME_PLACEHOLDER;
import static org.junit.jupiter.params.ParameterizedTest.INDEX_PLACEHOLDER;
import static org.mockito.Mockito.mock;

import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.sql.Date;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.Arrays;
import java.util.Locale;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ParameterContext;
import org.junit.jupiter.params.aggregator.AggregateWith;
import org.junit.jupiter.params.aggregator.ArgumentsAccessor;
import org.junit.jupiter.params.aggregator.ArgumentsAggregationException;
import org.junit.jupiter.params.aggregator.ArgumentsAggregator;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.platform.commons.JUnitException;
import org.junit.platform.commons.util.ReflectionUtils;

/**
 * @since 5.0
 */
class ParameterizedTestNameFormatterTests {

	private Locale originalLocale = Locale.getDefault();

	@AfterEach
	void restoreLocale() {
		Locale.setDefault(originalLocale);
	}

	@Test
	void formatsDisplayName() {
		ParameterizedTestNameFormatter formatter = formatter(DISPLAY_NAME_PLACEHOLDER, "enigma");

		assertEquals("enigma", formatter.format(1));
		assertEquals("enigma", formatter.format(2));
	}

	@Test
	void formatsInvocationIndex() {
		ParameterizedTestNameFormatter formatter = formatter(INDEX_PLACEHOLDER, "enigma");

		assertEquals("1", formatter.format(1));
		assertEquals("2", formatter.format(2));
	}

	@Test
	void formatsIndividualArguments() {
		ParameterizedTestNameFormatter formatter = formatter("{0} -> {1}", "enigma");

		assertEquals("foo -> 42", formatter.format(1, "foo", 42));
	}

	@Test
	void formatsCompleteArgumentsList() {
		ParameterizedTestNameFormatter formatter = formatter(ARGUMENTS_PLACEHOLDER, "enigma");

		// @formatter:off
		assertEquals("42, 99, enigma, null, [1, 2, 3], [foo, bar], [[2, 4], [3, 9]]",
			formatter.format(1,
				Integer.valueOf(42),
				99,
				"enigma",
				null,
				new int[] { 1, 2, 3 },
				new String[] { "foo", "bar" },
				new Integer[][] { { 2, 4 }, { 3, 9 } }
			));
		// @formatter:on
	}

	@Test
	void formatsCompleteArgumentsListWithNames() {
		Method testMethod = ParameterizedTestCases.getMethod("parameterizedTest", int.class, String.class,
			Object[].class);
		ParameterizedTestNameFormatter formatter = formatter(ARGUMENTS_WITH_NAMES_PLACEHOLDER, "enigma", testMethod);

		String formattedName = formatter.format(1, 42, "enigma", new Object[] { "foo", 1 });
		assertEquals("someNumber=42, someString=enigma, someArray=[foo, 1]", formattedName);
	}

	@Test
	void formatsCompleteArgumentsListWithoutNamesForAggregators() {
		Method testMethod = ParameterizedTestCases.getMethod("parameterizedTestWithAggregator", int.class,
			String.class);
		ParameterizedTestNameFormatter formatter = formatter(ARGUMENTS_WITH_NAMES_PLACEHOLDER, "enigma", testMethod);

		String formattedName = formatter.format(1, 42, "foo", "bar");
		assertEquals("someNumber=42, foo, bar", formattedName);
	}

	@Test
	void formatsCompleteArgumentsListWithArrays() {
		ParameterizedTestNameFormatter formatter = formatter(ARGUMENTS_PLACEHOLDER, "enigma");

		// Explicit test for https://github.com/junit-team/junit5/issues/814
		assertEquals("[foo, bar]", formatter.format(1, (Object) new String[] { "foo", "bar" }));

		assertEquals("[foo, bar], 42, true", formatter.format(1, new String[] { "foo", "bar" }, 42, true));
	}

	@Test
	void formatsEverythingUsingCustomPattern() {
		String pattern = DISPLAY_NAME_PLACEHOLDER + " " + INDEX_PLACEHOLDER + " :: " + ARGUMENTS_PLACEHOLDER
				+ " :: {1}";
		ParameterizedTestNameFormatter formatter = formatter(pattern, "enigma");

		assertEquals("enigma 1 :: foo, bar :: bar", formatter.format(1, "foo", "bar"));
		assertEquals("enigma 2 :: foo, 42 :: 42", formatter.format(2, "foo", 42));
	}

	@Test
	void formatDoesNotAlterArgumentsArray() {
		ParameterizedTestNameFormatter formatter = formatter(ARGUMENTS_PLACEHOLDER, "enigma");
		Object[] actual = { 1, "two", Byte.valueOf("-128"), new Integer[][] { { 2, 4 }, { 3, 9 } } };
		Object[] expected = Arrays.copyOf(actual, actual.length);
		assertEquals("1, two, -128, [[2, 4], [3, 9]]", formatter.format(1, actual));
		assertArrayEquals(expected, actual);
	}

	@Test
	void formatDoesNotRaiseAnArrayStoreException() {
		ParameterizedTestNameFormatter formatter = formatter("{0} -> {1}", "enigma");

		Object[] arguments = new Number[] { 1, 2 };
		assertEquals("1 -> 2", formatter.format(1, arguments));
	}

	@Test
	void throwsReadableExceptionForInvalidPattern() {
		ParameterizedTestNameFormatter formatter = formatter("{index", "enigma");

		JUnitException exception = assertThrows(JUnitException.class, () -> formatter.format(1));
		assertNotNull(exception.getCause());
		assertEquals(IllegalArgumentException.class, exception.getCause().getClass());
	}

	@Test
	void formattingDoesNotFailIfArgumentToStringImplementationThrowsAnException() {
		ParameterizedTestNameFormatter formatter = formatter(ARGUMENTS_PLACEHOLDER, "enigma");

		String formattedName = formatter.format(1, new Object[] { new ToStringThrowsException(), "foo" });

		assertThat(formattedName).startsWith(ToStringThrowsException.class.getName() + "@");
		assertThat(formattedName).endsWith("foo");
	}

	@ParameterizedTest(name = "{0}")
	@CsvSource(delimiter = '|', value = { "US | 42.23 is positive on 2019 Jan 13 at 12:34:56",
			"DE | 42,23 is positive on 13.01.2019 at 12:34:56" })
	void customFormattingExpressionsAreSupported(Locale locale, String expectedValue) {
		var pattern = "[{index}] {1,number,#.##} is {1,choice,0<positive} on {0,date} at {0,time} even though {2}";
		ParameterizedTestNameFormatter formatter = formatter(pattern, "enigma");
		Locale.setDefault(Locale.US);

		var date = Date.from(
			LocalDate.of(2019, 1, 13).atTime(LocalTime.of(12, 34, 56)).atZone(ZoneId.systemDefault()).toInstant());
		Locale.setDefault(locale);
		String formattedName = formatter.format(1, date, new BigDecimal("42.23"), new ToStringThrowsException());

		assertThat(formattedName).startsWith(
			"[1] " + expectedValue + " even though " + ToStringThrowsException.class.getName() + "@");
	}

	@Test
	void ignoresExcessPlaceholders() {
		ParameterizedTestNameFormatter formatter = formatter("{0}, {1}", "enigma");

		String formattedName = formatter.format(1, "foo");

		assertThat(formattedName).isEqualTo("foo, {1}");
	}

	@Test
	void placeholdersCanBeOmitted() {
		ParameterizedTestNameFormatter formatter = formatter("{0}", "enigma");

		String formattedName = formatter.format(1, "foo", "bar");

		assertThat(formattedName).isEqualTo("foo");
	}

	@Test
	void placeholdersCanBeSkipped() {
		ParameterizedTestNameFormatter formatter = formatter("{0}, {2}", "enigma");

		String formattedName = formatter.format(1, "foo", "bar", "baz");

		assertThat(formattedName).isEqualTo("foo, baz");
	}

	private static ParameterizedTestNameFormatter formatter(String pattern, String displayName) {
		return new ParameterizedTestNameFormatter(pattern, displayName, mock(ParameterizedTestMethodContext.class));
	}

	private static ParameterizedTestNameFormatter formatter(String pattern, String displayName, Method method) {
		return new ParameterizedTestNameFormatter(pattern, displayName, new ParameterizedTestMethodContext(method));
	}

	// -------------------------------------------------------------------

	private static class ToStringThrowsException {

		@Override
		public String toString() {
			throw new RuntimeException("Boom!");
		}
	}

	private static class ParameterizedTestCases {

		static Method getMethod(String methodName, Class<?>... parameterTypes) {
			return ReflectionUtils.findMethod(ParameterizedTestCases.class, methodName, parameterTypes).get();
		}

		void parameterizedTest(int someNumber, String someString, Object[] someArray) {

		}

		void parameterizedTestWithAggregator(int someNumber,
				@AggregateWith(CustomAggregator.class) String someAggregatedString) {

		}

		private static class CustomAggregator implements ArgumentsAggregator {
			@Override
			public Object aggregateArguments(ArgumentsAccessor accessor, ParameterContext context)
					throws ArgumentsAggregationException {
				return accessor.get(0);
			}
		}
	}

}
