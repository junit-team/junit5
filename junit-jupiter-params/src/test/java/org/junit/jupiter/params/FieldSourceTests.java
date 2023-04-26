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
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Named.named;
import static org.junit.jupiter.api.TestInstance.Lifecycle.PER_CLASS;
import static org.junit.jupiter.params.provider.Arguments.arguments;

import java.util.List;
import java.util.function.Supplier;
import java.util.stream.Stream;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.FieldSource;

/**
 * Tests for {@link FieldSource @FieldSource}.
 *
 * @since 5.11
 */
@SuppressWarnings("unused")
class FieldSourceTests {

	private static final String[] arrayOfStrings = { "apple", "banana" };

	private static final Object listOfStrings = List.of("apple", "banana");

	private static final List<String> reversedStrings = List.of("banana", "apple");

	private static final Stream<Arguments> namedArguments = Stream.of(//
		arguments(named("Apfel", "apple")), //
		arguments(named("Banane", "banana"))//
	);

	private static final Supplier<Stream<Arguments>> namedArgumentsSupplier = () -> Stream.of(//
		arguments(named("Apfel", "apple")), //
		arguments(named("Banane", "banana"))//
	);

	@ParameterizedTest
	@FieldSource // default field name
	void arrayOfStrings(String fruit) {
		assertFruit(fruit);
	}

	@ParameterizedTest
	@FieldSource("listOfStrings")
	void singleFieldSource(String fruit) {
		assertFruit(fruit);
	}

	@ParameterizedTest
	@FieldSource({ "listOfStrings", "reversedStrings" })
	void multipleFieldSources(String fruit) {
		assertFruit(fruit);
	}

	@Disabled("@FieldSource field cannot be of type Stream")
	@ParameterizedTest
	@FieldSource
	void namedArguments(String fruit) {
		assertFruit(fruit);
	}

	@ParameterizedTest
	@FieldSource
	void namedArgumentsSupplier(String fruit) {
		assertFruit(fruit);
	}

	@ParameterizedTest
	@FieldSource("org.junit.jupiter.params.FruitUtils#fruits")
	void externalField(String fruit) {
		assertFruit(fruit);
	}

	@Nested
	@TestInstance(Lifecycle.PER_CLASS)
	class NestedTests {

		// Non-static field
		final List<String> strings = List.of("apple", "banana");

		@ParameterizedTest
		@FieldSource("strings")
		void nonStaticFieldSource(String fruit) {
			assertFruit(fruit);
		}

	}

	static void assertFruit(String fruit) {
		assertTrue("apple".equals(fruit) || "banana".equals(fruit));
	}

	static class Base {
		final List<String> field = List.of("base-1", "base-2");
	}

	@Nested
	@TestInstance(PER_CLASS)
	class NestedSubclass extends Base {

		final List<String> field = List.of("sub-1", "sub-2");

		/**
		 * Verify that a local field takes precedence over an inherited field
		 * with the same name.
		 */
		@ParameterizedTest
		@FieldSource("field")
		void test(String value) {
			assertThat(value).startsWith("sub-");
		}
	}

}

class FruitUtils {

	public static final List<String> fruits = List.of("apple", "banana");

}
