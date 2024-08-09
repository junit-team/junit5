/*
 * Copyright 2015-2024 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * https://www.eclipse.org/legal/epl-v20.html
 */

package org.junit.jupiter.params;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.params.ParameterizedTest.ARGUMENTS_PLACEHOLDER;
import static org.junit.jupiter.params.ParameterizedTest.ARGUMENTS_WITH_NAMES_PLACEHOLDER;
import static org.junit.jupiter.params.ParameterizedTest.ARGUMENT_SET_NAME_OR_ARGUMENTS_WITH_NAMES_PLACEHOLDER;
import static org.junit.jupiter.params.ParameterizedTest.ARGUMENT_SET_NAME_PLACEHOLDER;
import static org.junit.jupiter.params.ParameterizedTest.DEFAULT_DISPLAY_NAME;
import static org.junit.jupiter.params.ParameterizedTest.DISPLAY_NAME_PLACEHOLDER;
import static org.junit.jupiter.params.ParameterizedTest.INDEX_PLACEHOLDER;
import static org.junit.jupiter.params.provider.Arguments.argumentSet;
import static org.junit.jupiter.params.provider.Arguments.arguments;
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
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtensionConfigurationException;
import org.junit.jupiter.api.extension.ParameterContext;
import org.junit.jupiter.params.aggregator.AggregateWith;
import org.junit.jupiter.params.aggregator.ArgumentsAccessor;
import org.junit.jupiter.params.aggregator.ArgumentsAggregator;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.platform.commons.JUnitException;
import org.junit.platform.commons.util.ReflectionUtils;

/**
 * @since 5.0
 */
class ParameterizedTestNameFormatterTests {

	private final Locale originalLocale = Locale.getDefault();

	@AfterEach
	void restoreLocale() {
		Locale.setDefault(originalLocale);
	}

	@Test
	void formatsDisplayName() {
		var formatter = formatter(DISPLAY_NAME_PLACEHOLDER, "enigma");

		assertEquals("enigma", format(formatter, 1, arguments()));
		assertEquals("enigma", format(formatter, 2, arguments()));
	}

	@Test
	void formatsDisplayNameContainingApostrophe() {
		String displayName = "display'Zero";
		var formatter = formatter(DISPLAY_NAME_PLACEHOLDER, "display'Zero");

		assertEquals(displayName, format(formatter, 1, arguments()));
		assertEquals(displayName, format(formatter, 2, arguments()));
	}

	@Test
	void formatsDisplayNameContainingFormatElements() {
		String displayName = "{enigma} {0} '{1}'";
		var formatter = formatter(DISPLAY_NAME_PLACEHOLDER, displayName);

		assertEquals(displayName, format(formatter, 1, arguments()));
		assertEquals(displayName, format(formatter, 2, arguments()));
	}

	@Test
	void formatsInvocationIndex() {
		var formatter = formatter(INDEX_PLACEHOLDER, "enigma");

		assertEquals("1", format(formatter, 1, arguments()));
		assertEquals("2", format(formatter, 2, arguments()));
	}

	@Test
	void defaultDisplayName() {
		var formatter = formatter(DEFAULT_DISPLAY_NAME, "IGNORED");

		var formattedName = format(formatter, 1, arguments("apple", "banana"));

		assertThat(formattedName).isEqualTo("[1] apple, banana");
	}

	@Test
	void formatsIndividualArguments() {
		var formatter = formatter("{0} -> {1}", "enigma");

		assertEquals("foo -> 42", format(formatter, 1, arguments("foo", 42)));
	}

	@Test
	void formatsCompleteArgumentsList() {
		var formatter = formatter(ARGUMENTS_PLACEHOLDER, "enigma");

		// @formatter:off
		Arguments args = arguments(
			42,
			99,
			"enigma",
			null,
			new int[] { 1, 2, 3 },
			new String[] { "foo", "bar" },
			new Integer[][] { { 2, 4 }, { 3, 9 } }
		);
		// @formatter:on

		assertEquals("42, 99, enigma, null, [1, 2, 3], [foo, bar], [[2, 4], [3, 9]]", format(formatter, 1, args));
	}

	@Test
	void formatsCompleteArgumentsListWithNames() {
		var testMethod = ParameterizedTestCases.getMethod("parameterizedTest", int.class, String.class, Object[].class);
		var formatter = formatter(ARGUMENTS_WITH_NAMES_PLACEHOLDER, "enigma", testMethod);

		var formattedName = format(formatter, 1, arguments(42, "enigma", new Object[] { "foo", 1 }));
		assertEquals("someNumber=42, someString=enigma, someArray=[foo, 1]", formattedName);
	}

	@Test
	void formatsCompleteArgumentsListWithoutNamesForAggregators() {
		var testMethod = ParameterizedTestCases.getMethod("parameterizedTestWithAggregator", int.class, String.class);
		var formatter = formatter(ARGUMENTS_WITH_NAMES_PLACEHOLDER, "enigma", testMethod);

		var formattedName = format(formatter, 1, arguments(42, "foo", "bar"));
		assertEquals("someNumber=42, foo, bar", formattedName);
	}

	@Test
	void formatsCompleteArgumentsListWithArrays() {
		var formatter = formatter(ARGUMENTS_PLACEHOLDER, "enigma");

		// Explicit test for https://github.com/junit-team/junit5/issues/814
		assertEquals("[foo, bar]", format(formatter, 1, arguments((Object) new String[] { "foo", "bar" })));

		assertEquals("[foo, bar], 42, true", format(formatter, 1, arguments(new String[] { "foo", "bar" }, 42, true)));
	}

	@Test
	void formatsEverythingUsingCustomPattern() {
		var pattern = DISPLAY_NAME_PLACEHOLDER + " " + INDEX_PLACEHOLDER + " :: " + ARGUMENTS_PLACEHOLDER + " :: {1}";
		var formatter = formatter(pattern, "enigma");

		assertEquals("enigma 1 :: foo, bar :: bar", format(formatter, 1, arguments("foo", "bar")));
		assertEquals("enigma 2 :: foo, 42 :: 42", format(formatter, 2, arguments("foo", 42)));
	}

	@Test
	void formatDoesNotAlterArgumentsArray() {
		Object[] actual = { 1, "two", Byte.valueOf("-128"), new Integer[][] { { 2, 4 }, { 3, 9 } } };
		var formatter = formatter(ARGUMENTS_PLACEHOLDER, "enigma");
		var expected = Arrays.copyOf(actual, actual.length);
		assertEquals("1, two, -128, [[2, 4], [3, 9]]", format(formatter, 1, arguments(actual)));
		assertArrayEquals(expected, actual);
	}

	@Test
	void formatDoesNotRaiseAnArrayStoreException() {
		var formatter = formatter("{0} -> {1}", "enigma");

		Object[] arguments = new Number[] { 1, 2 };
		assertEquals("1 -> 2", format(formatter, 1, arguments(arguments)));
	}

	@Test
	void throwsReadableExceptionForInvalidPattern() {
		var exception = assertThrows(JUnitException.class, () -> formatter("{index", "enigma"));
		assertNotNull(exception.getCause());
		assertEquals(IllegalArgumentException.class, exception.getCause().getClass());
	}

	@Test
	void formattingDoesNotFailIfArgumentToStringImplementationReturnsNull() {
		var formatter = formatter(ARGUMENTS_PLACEHOLDER, "enigma");

		var formattedName = format(formatter, 1, arguments(new ToStringReturnsNull(), "foo"));

		assertThat(formattedName).isEqualTo("null, foo");
	}

	@Test
	void formattingDoesNotFailIfArgumentToStringImplementationThrowsAnException() {
		var formatter = formatter(ARGUMENTS_PLACEHOLDER, "enigma");

		var formattedName = format(formatter, 1, arguments(new ToStringThrowsException(), "foo"));

		assertThat(formattedName).startsWith(ToStringThrowsException.class.getName() + "@");
		assertThat(formattedName).endsWith("foo");
	}

	@ParameterizedTest(name = "{0}")
	@CsvSource(delimiter = '|', textBlock = """
			US | 42.23 is positive on 2019 Jan 13 at 12:34:56
			DE | 42,23 is positive on 13.01.2019 at 12:34:56
			""")
	void customFormattingExpressionsAreSupported(Locale locale, String expectedValue) {
		var pattern = "[{index}] {1,number,#.##} is {1,choice,0<positive} on {0,date} at {0,time} even though {2}";
		Locale.setDefault(Locale.US);

		var date = Date.from(
			LocalDate.of(2019, 1, 13).atTime(LocalTime.of(12, 34, 56)).atZone(ZoneId.systemDefault()).toInstant());
		Locale.setDefault(locale);

		var formatter = formatter(pattern, "enigma");
		var formattedName = format(formatter, 1,
			arguments(date, new BigDecimal("42.23"), new ToStringThrowsException()));

		assertThat(formattedName).startsWith(
			"[1] " + expectedValue + " even though " + ToStringThrowsException.class.getName() + "@");
	}

	@Test
	void ignoresExcessPlaceholders() {
		var formatter = formatter("{0}, {1}", "enigma");

		var formattedName = format(formatter, 1, arguments("foo"));

		assertThat(formattedName).isEqualTo("foo, {1}");
	}

	@Test
	void placeholdersCanBeOmitted() {
		var formatter = formatter("{0}", "enigma");

		var formattedName = format(formatter, 1, arguments("foo", "bar"));

		assertThat(formattedName).isEqualTo("foo");
	}

	@Test
	void placeholdersCanBeSkipped() {
		var formatter = formatter("{0}, {2}", "enigma");

		var formattedName = format(formatter, 1, arguments("foo", "bar", "baz"));

		assertThat(formattedName).isEqualTo("foo, baz");
	}

	@Test
	void truncatesArgumentsThatExceedMaxLength() {
		var formatter = formatter("{arguments}", "display name", 3);

		var formattedName = format(formatter, 1, arguments("fo", "foo", "fooo"));

		assertThat(formattedName).isEqualTo("fo, foo, fo…");
	}

	@Nested
	class ArgumentSetTests {

		@Test
		void throwsExceptionForArgumentSetNamePlaceholderWithoutArgumentSet() {
			var formatter = formatter(ARGUMENT_SET_NAME_PLACEHOLDER, "IGNORED");

			// @formatter:off
			assertThatExceptionOfType(JUnitException.class)
				.isThrownBy(() -> format(formatter, 1, arguments()))
				.havingCause()
					.isExactlyInstanceOf(ExtensionConfigurationException.class)
					.withMessage("When the display name pattern for a @ParameterizedTest contains %s, "
						+ "the arguments must be supplied as an ArgumentSet.", ARGUMENT_SET_NAME_PLACEHOLDER);
			// @formatter:on
		}

		@Test
		void defaultDisplayName() {
			var formatter = formatter(DEFAULT_DISPLAY_NAME, "IGNORED");

			var formattedName = format(formatter, 42, argumentSet("Fruits", "apple", "banana"));

			assertThat(formattedName).isEqualTo("[42] Fruits");
		}

		@Test
		void argumentSetNameAndArgumentsPlaceholders() {
			var pattern = ARGUMENT_SET_NAME_PLACEHOLDER + " :: " + ARGUMENTS_PLACEHOLDER;
			var formatter = formatter(pattern, "IGNORED");

			var formattedName = format(formatter, -1, argumentSet("Fruits", "apple", "banana"));

			assertThat(formattedName).isEqualTo("Fruits :: apple, banana");
		}

		@Test
		void mixedTypesOfArgumentsImplementationsAndCustomDisplayNamePattern() {
			var pattern = "[%s] %s :: %s".formatted(INDEX_PLACEHOLDER, DISPLAY_NAME_PLACEHOLDER,
				ARGUMENT_SET_NAME_OR_ARGUMENTS_WITH_NAMES_PLACEHOLDER);
			var testMethod = ParameterizedTestCases.getMethod("processFruits", String.class, String.class);
			var formatter = formatter(pattern, "Mixed Arguments Types", testMethod);

			var name1 = format(formatter, 1, argumentSet("Fruits", "apple", "banana"));
			var name2 = format(formatter, 2, arguments("apple", "banana"));

			assertThat(name1).isEqualTo("[1] Mixed Arguments Types :: Fruits");
			assertThat(name2).isEqualTo("[2] Mixed Arguments Types :: fruit1=apple, fruit2=banana");
		}

	}

	// -------------------------------------------------------------------------

	private static ParameterizedTestNameFormatter formatter(String pattern, String displayName) {
		return formatter(pattern, displayName, 512);
	}

	private static ParameterizedTestNameFormatter formatter(String pattern, String displayName, int argumentMaxLength) {
		return new ParameterizedTestNameFormatter(pattern, displayName, mock(), argumentMaxLength);
	}

	private static ParameterizedTestNameFormatter formatter(String pattern, String displayName, Method method) {
		return new ParameterizedTestNameFormatter(pattern, displayName, new ParameterizedTestMethodContext(method),
			512);
	}

	private static String format(ParameterizedTestNameFormatter formatter, int invocationIndex, Arguments arguments) {
		return formatter.format(invocationIndex, arguments, arguments.get());
	}

	private static class ToStringReturnsNull {

		@Override
		public String toString() {
			return null;
		}
	}

	private static class ToStringThrowsException {

		@Override
		public String toString() {
			throw new RuntimeException("Boom!");
		}
	}

	private static class ParameterizedTestCases {

		static Method getMethod(String methodName, Class<?>... parameterTypes) {
			return ReflectionUtils.findMethod(ParameterizedTestCases.class, methodName, parameterTypes).orElseThrow();
		}

		@SuppressWarnings("unused")
		void parameterizedTest(int someNumber, String someString, Object[] someArray) {
		}

		@SuppressWarnings("unused")
		void parameterizedTestWithAggregator(int someNumber,
				@AggregateWith(CustomAggregator.class) String someAggregatedString) {
		}

		@SuppressWarnings("unused")
		void processFruits(String fruit1, String fruit2) {
		}

		private static class CustomAggregator implements ArgumentsAggregator {
			@Override
			public Object aggregateArguments(ArgumentsAccessor accessor, ParameterContext context) {
				return accessor.get(0);
			}
		}
	}

}
