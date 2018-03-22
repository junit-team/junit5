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
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Map;
import java.util.stream.IntStream;

import org.junit.jupiter.api.extension.ParameterContext;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

public class AggregatorTests {

	@ParameterizedTest
	@CsvSource({ "John, Doe", "Jack, Smith" })
	public void personAggregationTest(@AggregateWith(PersonAggregator.class) Person person) {
		String fullName = person.name + " " + person.surname;
		assertTrue(fullName.equals("John Doe") || fullName.equals("Jack Smith"));
	}

	@ParameterizedTest
	@CsvSource({ "Foo,Bar,Baz", "Baz,Bar,Foo" })
	public void hashMapAggregationTest(@AggregateWith(TestHashMapAggregator.class) Map map) {
		assertEquals(3, map.size());

		// @formatter:off
		assertAll("Map contents",
				() -> assertTrue(map.containsValue("Baz")),
				() -> assertTrue(map.containsValue("Bar")),
				() -> assertTrue(map.containsValue("Foo"))
		);
		// @formatter:on

	}

	@ParameterizedTest
	@CsvSource({ "1,2,3,4,5,6,7,8,9,10" })
	public void directConversionToAccessorTest(ArgumentsAccessor accessor) {
		assertTrue(accessor.size() == 10);
		int sum = IntStream.range(0, 10).map(i -> accessor.getInteger(i)).sum();
		assertEquals(55, sum);
	}

	static class Person {
		final String name;
		final String surname;

		public Person(String name, String surname) {
			this.name = name;
			this.surname = surname;
		}
	}

	static class PersonAggregator implements ArgumentsAggregator {

		@Override
		public Object aggregateArguments(ArgumentsAccessor accessor, ParameterContext context) {
			assertTrue(accessor.size() >= 2);
			return new Person(accessor.getString(0), accessor.getString(1));
		}
	}

	static class TestHashMapAggregator implements ArgumentsAggregator {
		@Override
		public Object aggregateArguments(ArgumentsAccessor accessor, ParameterContext context) {
			// @formatter:off
			return IntStream
					.range(0, accessor.size())
					.boxed()
					.collect(
							toMap(
									i -> Integer.toString(i),
									i -> accessor.get(i)
							)
					);
			// @formatter:on`
		}
	}

}
