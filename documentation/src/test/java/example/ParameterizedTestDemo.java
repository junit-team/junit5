/*
 * Copyright 2015-2017 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v10.html
 */

package example;

import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

import org.junit.jupiter.api.TestReporter;
import org.junit.jupiter.api.extension.ContainerExtensionContext;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.ArgumentsProvider;
import org.junit.jupiter.params.provider.ArgumentsSource;
import org.junit.jupiter.params.provider.CsvFileSource;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ObjectArrayArguments;
import org.junit.jupiter.params.provider.ValueSource;

class ParameterizedTestDemo {

	// tag::first_example[]
	@ParameterizedTest
	@ValueSource(strings = { "a", "b" })
	void testWithStringParameter(String argument) {
		assertNotNull(argument);
	}
	// end::first_example[]

	// tag::ValueSource_example[]
	@ParameterizedTest
	@ValueSource(ints = { 1, 2, 3 })
	void testWithValueSource(int argument) {
		assertNotNull(argument);
	}
	// end::ValueSource_example[]

	// tag::EnumSource_example[]
	@ParameterizedTest
	@EnumSource(TimeUnit.class)
	void testWithEnumSource(TimeUnit timeUnit) {
		assertNotNull(timeUnit.name());
	}
	// end::EnumSource_example[]

	// tag::simple_MethodSource_example[]
	@ParameterizedTest
	@MethodSource(names = "stringProvider")
	void testWithSimpleMethodSource(String argument) {
		assertNotNull(argument);
	}

	static Stream<String> stringProvider() {
		return Stream.of("foo", "bar");
	}
	// end::simple_MethodSource_example[]

	// tag::multi_arg_MethodSource_example[]
	@ParameterizedTest
	@MethodSource(names = "stringAndIntProvider")
	void testWithMultiArgMethodSource(String first, int second) {
		assertNotNull(first);
		assertNotEquals(0, second);
	}

	static Stream<Arguments> stringAndIntProvider() {
		return Stream.of(ObjectArrayArguments.create("foo", 1), ObjectArrayArguments.create("bar", 2));
	}
	// end::multi_arg_MethodSource_example[]

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
	@CsvFileSource(resources = "/two-column.csv")
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
		public Stream<? extends Arguments> arguments(ContainerExtensionContext context) {
			return Stream.of("foo", "bar").map(ObjectArrayArguments::create);
		}
	}
	// end::ArgumentsSource_example[]

	// tag::ParameterResolver_example[]
	@ParameterizedTest
	@ValueSource(strings = "foo")
	void testWithRegularParameterResolver(String argument, TestReporter testReporter) {
		testReporter.publishEntry("argument", argument);
	}
	// end::ParameterResolver_example[]
}
