/*
 * Copyright 2015-2018 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v20.html
 */

package org.junit.jupiter.params.aggregator;

import static java.util.stream.Collectors.toMap;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.entry;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assumptions.assumingThat;

import java.util.Map;
import java.util.stream.IntStream;

import org.junit.jupiter.api.extension.ParameterContext;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

/**
 * @since 5.2
 */
class AggregatorTests {

	@ParameterizedTest
	@CsvSource({ "John, Doe", "Jack, Smith" })
	void personAggregation(@AggregateWith(PersonAggregator.class) Person person) {
		assumingThat(person.firstName.equals("John"), () -> assertEquals("John Doe", person.getFullName()));
		assumingThat(person.firstName.equals("Jack"), () -> assertEquals("Jack Smith", person.getFullName()));
	}

	@ParameterizedTest
	@CsvSource({ "cat, bird, mouse", "mouse, cat, bird", "mouse, bird, cat" })
	void mapAggregation(@AggregateWith(MapAggregator.class) Map<String, Integer> map) {
		assertThat(map).containsOnly(entry("cat", 3), entry("bird", 4), entry("mouse", 5));
	}

	@ParameterizedTest
	@CsvSource({ "1, 2, 3, 4, 5, 6, 7, 8, 9, 10" })
	void argumentsAccessorAsArgumentToMethod(ArgumentsAccessor accessor) {
		assertEquals(55, IntStream.range(0, accessor.size()).map(i -> accessor.getInteger(i)).sum());
	}

	static class Person {

		final String firstName;
		final String lastName;

		Person(String firstName, String lastName) {
			this.firstName = firstName;
			this.lastName = lastName;
		}

		String getFullName() {
			return this.firstName + " " + this.lastName;
		}
	}

	static class PersonAggregator implements ArgumentsAggregator {

		@Override
		public Object aggregateArguments(ArgumentsAccessor accessor, ParameterContext context) {
			assertEquals(2, accessor.size());
			return new Person(accessor.getString(0), accessor.getString(1));
		}
	}

	/**
	 * Maps from String to length of String.
	 */
	static class MapAggregator implements ArgumentsAggregator {

		@Override
		public Object aggregateArguments(ArgumentsAccessor accessor, ParameterContext context) {
			// @formatter:off
			Map<String, Integer> map = IntStream.range(0, accessor.size())
					.boxed()
					.map(i -> accessor.getString(i))
					.collect(
						toMap(
							s -> s,
							String::length
						)
					);
			// @formatter:on
			return map;
		}
	}

}
