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

import java.time.LocalDate;
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
	@CsvSource({ "Jane, Doe, 1980-04-16, F, red", "Jack, Smith, 2000-11-22, M, blue" })
	void personAggregation(@AggregateWith(PersonAggregator.class) Person person) {
		if (person.firstName.equals("Jane")) {
			assertEquals("Jane Doe", person.getFullName());
			assertEquals(1980, person.dateOfBirth.getYear());
			assertEquals(Gender.F, person.gender);
		}

		if (person.firstName.equals("Jack")) {
			assertEquals("Jack Smith", person.getFullName());
			assertEquals(2000, person.dateOfBirth.getYear());
			assertEquals(Gender.M, person.gender);
		}
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

	enum Gender {
		F, M
	}

	static class Person {

		final String firstName;
		final String lastName;
		final Gender gender;
		final LocalDate dateOfBirth;

		Person(String firstName, String lastName, LocalDate dateOfBirth, Gender gender) {
			this.firstName = firstName;
			this.lastName = lastName;
			this.gender = gender;
			this.dateOfBirth = dateOfBirth;
		}

		String getFullName() {
			return this.firstName + " " + this.lastName;
		}
	}

	static class PersonAggregator implements ArgumentsAggregator {

		@Override
		public Object aggregateArguments(ArgumentsAccessor accessor, ParameterContext context) {
			// @formatter:off
			return new Person(
				accessor.getString(0),
				accessor.getString(1),
				accessor.get(2, LocalDate.class),
				accessor.get(3, Gender.class)
			);
			// @formatter:on
		}
	}

	/**
	 * Maps from String to length of String.
	 */
	static class MapAggregator implements ArgumentsAggregator {

		@Override
		public Object aggregateArguments(ArgumentsAccessor accessor, ParameterContext context) {
			// @formatter:off
			return IntStream.range(0, accessor.size())
					.mapToObj(i -> accessor.getString(i))
					.collect(toMap(s -> s, String::length));
			// @formatter:on
		}
	}

}
