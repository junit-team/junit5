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
import org.junit.jupiter.params.sources.CsvFileSource;
import org.junit.jupiter.params.sources.CsvSource;
import org.junit.jupiter.params.sources.EnumSource;
import org.junit.jupiter.params.sources.MethodSource;
import org.junit.jupiter.params.sources.ValueSource;
import org.junit.jupiter.params.support.ObjectArrayArguments;

public class ParamsApiPlayground {

	@ParameterizedTest
	@EnumSource(TimeUnit.class)
	void testWithParametersFromEnum(TimeUnit unit) {
	}

	@ParameterizedTest
	@EnumSource(value = TimeUnit.class, names = { "DAYS", "MINUTES" })
	void testWithParametersFromEnumWithNamedSubset(TimeUnit unit) {
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
	@ValueSource(strings = { "31.12.2016", "01.01.2017" })
	void testWithExplicitConverter(@JavaTimeConversionPattern("dd.mm.YYYY") LocalDate parameter) {
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
	@CsvFileSource(path = "classpath:bar.csv")
	@ArgumentsSource(MyArgumentsProvider.class)
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
