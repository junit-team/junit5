/*
 * Copyright 2015-2018 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v20.html
 */

package example;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.params.provider.EnumSource.Mode.EXCLUDE;
import static org.junit.jupiter.params.provider.EnumSource.Mode.MATCH_ALL;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.TestInfo;
import org.junit.jupiter.api.TestReporter;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.ParameterContext;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.aggregator.AggregateWith;
import org.junit.jupiter.params.aggregator.ArgumentsAccessor;
import org.junit.jupiter.params.aggregator.ArgumentsAggregator;
import org.junit.jupiter.params.converter.ConvertWith;
import org.junit.jupiter.params.converter.JavaTimeConversionPattern;
import org.junit.jupiter.params.converter.SimpleArgumentConverter;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.ArgumentsProvider;
import org.junit.jupiter.params.provider.ArgumentsSource;
import org.junit.jupiter.params.provider.CsvFileSource;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;

class ParameterizedTestDemo {

	private static boolean isPalindrome(String candidate) {
		int length = candidate.length();
		for (int i = 0; i < length / 2; i++) {
			if (candidate.charAt(i) != candidate.charAt(length - (i + 1))) {
				return false;
			}
		}
		return true;
	}

	// tag::first_example[]
	@ParameterizedTest
	@ValueSource(strings = { "racecar", "radar", "able was I ere I saw elba" })
	void palindromes(String candidate) {
		assertTrue(isPalindrome(candidate));
	}
	// end::first_example[]

	// tag::ValueSource_example[]
	@ParameterizedTest
	@ValueSource(ints = { 1, 2, 3 })
	void testWithValueSource(int argument) {
		assertTrue(argument > 0 && argument < 4);
	}
	// end::ValueSource_example[]

	// tag::EnumSource_example[]
	@ParameterizedTest
	@EnumSource(TimeUnit.class)
	void testWithEnumSource(TimeUnit timeUnit) {
		assertNotNull(timeUnit);
	}
	// end::EnumSource_example[]

	// tag::EnumSource_include_example[]
	@ParameterizedTest
	@EnumSource(value = TimeUnit.class, names = { "DAYS", "HOURS" })
	void testWithEnumSourceInclude(TimeUnit timeUnit) {
		assertTrue(EnumSet.of(TimeUnit.DAYS, TimeUnit.HOURS).contains(timeUnit));
	}
	// end::EnumSource_include_example[]

	// tag::EnumSource_exclude_example[]
	@ParameterizedTest
	@EnumSource(value = TimeUnit.class, mode = EXCLUDE, names = { "DAYS", "HOURS" })
	void testWithEnumSourceExclude(TimeUnit timeUnit) {
		assertFalse(EnumSet.of(TimeUnit.DAYS, TimeUnit.HOURS).contains(timeUnit));
		assertTrue(timeUnit.name().length() > 5);
	}
	// end::EnumSource_exclude_example[]

	// tag::EnumSource_regex_example[]
	@ParameterizedTest
	@EnumSource(value = TimeUnit.class, mode = MATCH_ALL, names = "^(M|N).+SECONDS$")
	void testWithEnumSourceRegex(TimeUnit timeUnit) {
		String name = timeUnit.name();
		assertTrue(name.startsWith("M") || name.startsWith("N"));
		assertTrue(name.endsWith("SECONDS"));
	}
	// end::EnumSource_regex_example[]

	// tag::simple_MethodSource_example[]
	@ParameterizedTest
	@MethodSource("stringProvider")
	void testWithSimpleMethodSource(String argument) {
		assertNotNull(argument);
	}

	static Stream<String> stringProvider() {
		return Stream.of("foo", "bar");
	}
	// end::simple_MethodSource_example[]

	// tag::simple_MethodSource_without_value_example[]
	@ParameterizedTest
	@MethodSource
	void testWithSimpleMethodSourceHavingNoValue(String argument) {
		assertNotNull(argument);
	}

	static Stream<String> testWithSimpleMethodSourceHavingNoValue() {
		return Stream.of("foo", "bar");
	}
	// end::simple_MethodSource_without_value_example[]

	// tag::primitive_MethodSource_example[]
	@ParameterizedTest
	@MethodSource("range")
	void testWithRangeMethodSource(int argument) {
		assertNotEquals(9, argument);
	}

	static IntStream range() {
		return IntStream.range(0, 20).skip(10);
	}
	// end::primitive_MethodSource_example[]

	// @formatter:off
	// tag::multi_arg_MethodSource_example[]
	@ParameterizedTest
	@MethodSource("stringIntAndListProvider")
	void testWithMultiArgMethodSource(String str, int num, List<String> list) {
		assertEquals(3, str.length());
		assertTrue(num >=1 && num <=2);
		assertEquals(2, list.size());
	}

	static Stream<Arguments> stringIntAndListProvider() {
		return Stream.of(
			Arguments.of("foo", 1, Arrays.asList("a", "b")),
			Arguments.of("bar", 2, Arrays.asList("x", "y"))
		);
	}
	// end::multi_arg_MethodSource_example[]
	// @formatter:on

	// tag::CsvSource_example[]
	@ParameterizedTest
	@CsvSource({ "foo, 1", "bar, 2", "'baz, qux', 3" })
	void testWithCsvSource(String first, int second) {
		assertNotNull(first);
		assertNotEquals(0, second);
	}
	// end::CsvSource_example[]

	// tag::CsvFileSource_example[]
	@ParameterizedTest
	@CsvFileSource(resources = "/two-column.csv", numLinesToSkip = 1)
	void testWithCsvFileSource(String first, int second) {
		assertNotNull(first);
		assertNotEquals(0, second);
	}
	// end::CsvFileSource_example[]

	// tag::ArgumentsSource_example[]
	@ParameterizedTest
	@ArgumentsSource(MyArgumentsProvider.class)
	void testWithArgumentsSource(String argument) {
		assertNotNull(argument);
	}

	static class MyArgumentsProvider implements ArgumentsProvider {

		@Override
		public Stream<? extends Arguments> provideArguments(ExtensionContext context) {
			return Stream.of("foo", "bar").map(Arguments::of);
		}
	}
	// end::ArgumentsSource_example[]

	// tag::ParameterResolver_example[]
	@BeforeEach
	void beforeEach(TestInfo testInfo) {
		// ...
	}

	@ParameterizedTest
	@ValueSource(strings = "foo")
	void testWithRegularParameterResolver(String argument, TestReporter testReporter) {
		testReporter.publishEntry("argument", argument);
	}

	@AfterEach
	void afterEach(TestInfo testInfo) {
		// ...
	}
	// end::ParameterResolver_example[]

	// tag::implicit_conversion_example[]
	@ParameterizedTest
	@ValueSource(strings = "SECONDS")
	void testWithImplicitArgumentConversion(TimeUnit argument) {
		assertNotNull(argument.name());
	}
	// end::implicit_conversion_example[]

	// tag::implicit_fallback_conversion_example[]
	@ParameterizedTest
	@ValueSource(strings = "42 Cats")
	void testWithImplicitFallbackArgumentConversion(Book book) {
		assertEquals("42 Cats", book.getTitle());
	}

	static class Book {

		private final String title;

		private Book(String title) {
			this.title = title;
		}

		public static Book fromTitle(String title) {
			return new Book(title);
		}

		public String getTitle() {
			return this.title;
		}
	}
	// end::implicit_fallback_conversion_example[]

	// @formatter:off
	// tag::explicit_conversion_example[]
	@ParameterizedTest
	@EnumSource(TimeUnit.class)
	void testWithExplicitArgumentConversion(
			@ConvertWith(ToStringArgumentConverter.class) String argument) {

		assertNotNull(TimeUnit.valueOf(argument));
	}

	static class ToStringArgumentConverter extends SimpleArgumentConverter {

		@Override
		protected Object convert(Object source, Class<?> targetType) {
			assertEquals(String.class, targetType, "Can only convert to String");
			return String.valueOf(source);
		}
	}
	// end::explicit_conversion_example[]

	// tag::explicit_java_time_converter[]
	@ParameterizedTest
	@ValueSource(strings = { "01.01.2017", "31.12.2017" })
	void testWithExplicitJavaTimeConverter(
			@JavaTimeConversionPattern("dd.MM.yyyy") LocalDate argument) {

		assertEquals(2017, argument.getYear());
	}
	// end::explicit_java_time_converter[]
	// @formatter:on

	// tag::Accessor_example[]
	@ParameterizedTest
	@CsvSource({ "foo, 1, bar, 2", "baz, 3, qux, 4" })
	void argumentsAccessorTest(ArgumentsAccessor accessor) {
		assertNotEquals(accessor.get(0), accessor.get(2));
		assertTrue(accessor.getInteger(3) > accessor.getInteger(1));
	}
	// end::Accessor_example[]

	// @formatter:off
	// tag::Aggregator_example[]
	@ParameterizedTest
	@CsvSource({ "3, 22, 16, 39", "45, 15, 30" })
	void aggregationTest(@AggregateWith(SummaryAggregator.class) int sum) {
		assertTrue(sum <= 100);
	}

	static class SummaryAggregator implements ArgumentsAggregator {
		@Override
		public Object aggregateArguments(ArgumentsAccessor argumentsAccessor, ParameterContext parameterContext) {
			return IntStream.range(0, argumentsAccessor.size())
					.map(i -> argumentsAccessor.getInteger(i))
					.sum();
		}
	}
	// end::Aggregator_example[]
	// @formatter:on

	// tag::custom_display_names[]
	@DisplayName("Display name of container")
	@ParameterizedTest(name = "{index} ==> first=''{0}'', second={1}")
	@CsvSource({ "foo, 1", "bar, 2", "'baz, qux', 3" })
	void testWithCustomDisplayNames(String first, int second) {
	}
	// end::custom_display_names[]
}
