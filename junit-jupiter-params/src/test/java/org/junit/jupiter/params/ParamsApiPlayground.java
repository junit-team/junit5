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
import java.util.Iterator;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

import org.junit.jupiter.params.converter.JavaTimeConversionPattern;
import org.junit.jupiter.params.sources.EnumSource;
import org.junit.jupiter.params.sources.FileSource;
import org.junit.jupiter.params.sources.MethodSource;
import org.junit.jupiter.params.sources.StringSource;
import org.junit.jupiter.params.support.ObjectArrayArguments;

public class ParamsApiPlayground {

	@ParameterizedTest
	@EnumSource(TimeUnit.class)
	void testWithParametersFromEnum(TimeUnit unit) {
	}

	@ParameterizedTest
	@StringSource({ "foo, 1", "bar, 2" })
	void testWithParametersFromAnnotation(String parameter, int i) {
	}

	@ParameterizedTest
	@StringSource({ "31.12.2016", "01.01.2017" })
	void testWithExplicitConverter(@JavaTimeConversionPattern("dd.mm.YYYY") LocalDate parameter) {
	}

	@ParameterizedTest
	@MethodSource("first")
	@MethodSource("second")
	void testWithParametersFromMethods(String parameter) {
	}

	@ParameterizedTest
	@ArgumentsSource(MyArgumentsProvider.class)
	void testWithParametersFromProvider(String parameter) {
	}

	@ParameterizedTest
	@FileSource("foo.csv")
	@FileSource("bar.csv")
	void testWithParametersFromFile(String parameter) {
	}

	@ParameterizedTest
	@StringSource("foo")
	@MethodSource("first")
	@FileSource("classpath:bar.csv")
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
		public Iterator<? extends Arguments> arguments() {
			return Stream.of("foo", "bar").map(ObjectArrayArguments::create).iterator();
		}
	}

}
