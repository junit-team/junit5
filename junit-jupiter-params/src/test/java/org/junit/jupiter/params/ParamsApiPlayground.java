/*
 * Copyright 2015-2017 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.junit.jupiter.params;

import static java.util.Collections.singleton;

import java.time.LocalDate;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

import org.junit.jupiter.api.extension.ContainerExtensionContext;
import org.junit.jupiter.params.converter.JavaTimeConversionPattern;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.ArgumentsProvider;
import org.junit.jupiter.params.provider.ArgumentsSource;
import org.junit.jupiter.params.provider.CsvFileSource;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ObjectArrayArguments;
import org.junit.jupiter.params.provider.ValueSource;

class ParamsApiPlayground {

	@ParameterizedTest
	@EnumSource(TimeUnit.class)
	void testWithParametersFromEnum(TimeUnit unit) {
	}

	@ParameterizedTest
	@EnumSource(value = TimeUnit.class, names = { "DAYS", "MINUTES" })
	void testWithParametersFromEnumWithNamedSubset(TimeUnit unit) {
	}

	@ParameterizedTest
	@ValueSource(strings = { "DAYS", "MINUTES" })
	void testWithImplicitEnumConverter(TimeUnit unit) {
	}

	@ParameterizedTest
	@ValueSource(longs = { 1_000, 2_000 })
	void testWithParametersFromLongArray(long number) {
	}

	@ParameterizedTest
	@CsvSource({ "foo, 1", "bar, 2" })
	void testWithParametersFromAnnotation(String parameter, int i) {
	}

	@ParameterizedTest
	@CsvSource(value = { //
			"foo      | 1", //
			"bar      | 2", //
			"baz, qux | 3" //
	}, delimiter = '|')
	void testWithParametersFromAnnotationWithCustomDelimiter(String parameter, int i) {
	}

	@ParameterizedTest
	@ValueSource(strings = { "2016-12-31", "2017-01-01" })
	void testWithImplicitJavaTimeConverter(LocalDate parameter) {
	}

	@ParameterizedTest
	@ValueSource(strings = { "31.12.2016", "01.01.2017" })
	void testWithExplicitJavaTimeConverter(@JavaTimeConversionPattern("dd.MM.yyyy") LocalDate parameter) {
	}

	@ParameterizedTest
	@MethodSource("first")
	void testWithParametersFromMethods(String parameter) {
	}

	@ParameterizedTest
	@ArgumentsSource(MyArgumentsProvider.class)
	void testWithParametersFromProvider(String parameter) {
	}

	@ParameterizedTest
	@CsvFileSource(path = "foo.csv")
	void testWithParametersFromFile(String parameter) {
	}

	@ParameterizedTest
	@CsvSource("foo")
	@MethodSource("first")
	@ArgumentsSource(MyArgumentsProvider.class)
	@CsvFileSource(path = "classpath:bar.csv")
	void testWithMultipleDifferentSources(String parameter) {
	}

	static Iterable<String> first() {
		return singleton("foo");
	}

	static Iterable<String> second() {
		return singleton("foo");
	}

	static class MyArgumentsProvider implements ArgumentsProvider {
		@Override
		public Stream<? extends Arguments> arguments(ContainerExtensionContext context) {
			return Stream.of("foo", "bar").map(ObjectArrayArguments::create);
		}
	}

}
