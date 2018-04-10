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
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.time.LocalDate;
import java.util.Map;
import java.util.stream.IntStream;

import org.junit.jupiter.api.TestInfo;
import org.junit.jupiter.api.extension.ParameterContext;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

/**
 * Integration tests for {@link ArgumentsAccessor}, {@link AggregateWith},
 * and {@link ArgumentsAggregator}.
 *
 * @since 5.2
 */
class AggregatorIntegrationTests {

	@ParameterizedTest
	@CsvSource({ "Jane, Doe, 1980-04-16, F, red", "Jack, Smith, 2000-11-22, M, blue" })
	void personAggregator(@AggregateWith(PersonAggregator.class) Person person) {
		testPersonAggregator(person);
	}

	@ParameterizedTest
	@CsvSource({ "Jane, Doe, 1980-04-16, F, red", "Jack, Smith, 2000-11-22, M, blue" })
	void personAggregatorRegisteredViaCustomAnnotation(@CsvToPerson Person person) {
		testPersonAggregator(person);
	}

	@ParameterizedTest
	@CsvSource({ "Jane, Doe, 1980-04-16, F, red", "Jack, Smith, 2000-11-22, M, blue" })
	void personAggregatorsRegisteredViaCustomAnnotation(@CsvToPerson Person person1, @CsvToPerson Person person2) {
		assertEquals(person1.getFullName(), person2.getFullName());
		testPersonAggregator(person1);
		testPersonAggregator(person2);
	}

	private void testPersonAggregator(Person person) {
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
	void mapAggregator(@AggregateWith(MapAggregator.class) Map<String, Integer> map) {
		assertThat(map).containsOnly(entry("cat", 3), entry("bird", 4), entry("mouse", 5));
	}

	@ParameterizedTest
	@CsvSource({ "1, 2, 3, 4, 5, 6, 7, 8, 9, 10" })
	void argumentsAccessor(ArgumentsAccessor accessor) {
		assertEquals(55, IntStream.range(0, accessor.size()).map(i -> accessor.getInteger(i)).sum());
	}

	@ParameterizedTest(name = "2 ArgumentsAccessors: {arguments}")
	@CsvSource({ "1, 2, 3, 4, 5, 6, 7, 8, 9, 10" })
	void argumentsAccessors(ArgumentsAccessor accessor1, ArgumentsAccessor accessor2) {
		assertArrayEquals(accessor1.toArray(), accessor2.toArray());
	}

	@ParameterizedTest(name = "ArgumentsAccessor and TestInfo: {arguments}")
	@CsvSource({ "1, 2, 3, 4, 5, 6, 7, 8, 9, 10" })
	void argumentsAccessorAndTestInfo(ArgumentsAccessor accessor, TestInfo testInfo) {
		assertEquals(55, IntStream.range(0, accessor.size()).map(i -> accessor.getInteger(i)).sum());
		assertThat(testInfo.getDisplayName()).contains("ArgumentsAccessor and TestInfo");
	}

	@ParameterizedTest(name = "Indexed Arguments and ArgumentsAccessor: {arguments}")
	@CsvSource({ "1, 2, 3, 4, 5, 6, 7, 8, 9, 10" })
	void indexedArgumentsAndArgumentsAccessor(int num1, int num2, ArgumentsAccessor accessor) {
		assertEquals(1, num1);
		assertEquals(2, num2);
		assertEquals(55, IntStream.range(0, accessor.size()).map(i -> accessor.getInteger(i)).sum());
	}

	@ParameterizedTest(name = "Indexed Arguments, ArgumentsAccessor, and TestInfo: {arguments}")
	@CsvSource({ "1, 2, 3, 4, 5, 6, 7, 8, 9, 10" })
	void indexedArgumentsArgumentsAccessorAndTestInfo(int num1, int num2, ArgumentsAccessor accessor,
			TestInfo testInfo) {

		assertEquals(1, num1);
		assertEquals(2, num2);
		assertEquals(55, IntStream.range(0, accessor.size()).map(i -> accessor.getInteger(i)).sum());
		assertThat(testInfo.getDisplayName()).contains("Indexed Arguments, ArgumentsAccessor, and TestInfo");
	}

	@ParameterizedTest(name = "Indexed Arguments, 2 ArgumentsAccessors, and TestInfo: {arguments}")
	@CsvSource({ "1, 2, 3, 4, 5, 6, 7, 8, 9, 10" })
	void indexedArgumentsArgumentsAccessorsAndTestInfo(int num1, int num2, ArgumentsAccessor accessor1,
			ArgumentsAccessor accessor2, TestInfo testInfo) {

		assertEquals(1, num1);
		assertEquals(2, num2);
		assertArrayEquals(accessor1.toArray(), accessor2.toArray());
		assertEquals(55, IntStream.range(0, accessor1.size()).map(i -> accessor1.getInteger(i)).sum());
		assertThat(testInfo.getDisplayName()).contains("Indexed Arguments, 2 ArgumentsAccessors, and TestInfo");
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

	@Retention(RetentionPolicy.RUNTIME)
	@Target(ElementType.PARAMETER)
	@AggregateWith(PersonAggregator.class)
	@interface CsvToPerson {
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
