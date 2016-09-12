/*
 * Copyright 2015-2016 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v10.html
 */

package example;

//tag::user_guide[]
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.DynamicTest.dynamicTest;

import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.function.Function;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.TestFactory;
import org.junit.jupiter.api.function.ThrowingConsumer;

class DynamicTestsDemo {

	// end::user_guide[]
	@Tag("exclude")
	// tag::user_guide[]
	// This will result in a JUnitException!
	@TestFactory
	List<String> dynamicTestsWithInvalidReturnType() {
		return Arrays.asList("Hello");
	}

	@TestFactory
	Collection<DynamicTest> dynamicTestsFromCollection() {
		// end::user_guide[]
		// @formatter:off
		// tag::user_guide[]
		return Arrays.asList(
			dynamicTest("1st dynamic test", () -> assertTrue(true)),
			dynamicTest("2nd dynamic test", () -> assertEquals(4, 2 * 2))
		);
		// end::user_guide[]
		// @formatter:on
		// tag::user_guide[]
	}

	@TestFactory
	Iterable<DynamicTest> dynamicTestsFromIterable() {
		// end::user_guide[]
		// @formatter:off
		// tag::user_guide[]
		return Arrays.asList(
			dynamicTest("3rd dynamic test", () -> assertTrue(true)),
			dynamicTest("4th dynamic test", () -> assertEquals(4, 2 * 2))
		);
		// end::user_guide[]
		// @formatter:on
		// tag::user_guide[]
	}

	@TestFactory
	Iterator<DynamicTest> dynamicTestsFromIterator() {
		// end::user_guide[]
		// @formatter:off
		// tag::user_guide[]
		return Arrays.asList(
			dynamicTest("5th dynamic test", () -> assertTrue(true)),
			dynamicTest("6th dynamic test", () -> assertEquals(4, 2 * 2))
		).iterator();
		// end::user_guide[]
		// @formatter:on
		// tag::user_guide[]
	}

	@TestFactory
	Stream<DynamicTest> dynamicTestsFromStream() {
		// end::user_guide[]
		// @formatter:off
		// tag::user_guide[]
		return Stream.of("A", "B", "C").map(
			str -> dynamicTest("test" + str, () -> { /* ... */ }));
		// end::user_guide[]
		// @formatter:on
		// tag::user_guide[]
	}

	@TestFactory
	Stream<DynamicTest> dynamicTestsFromIntStream() {
		// end::user_guide[]
		// @formatter:off
		// tag::user_guide[]
		// Generates tests for the first 10 even integers.
		return IntStream.iterate(0, n -> n + 2).limit(10).mapToObj(
			n -> dynamicTest("test" + n, () -> assertTrue(n % 2 == 0)));
		// end::user_guide[]
		// @formatter:on
		// tag::user_guide[]
	}

	@TestFactory
	Stream<DynamicTest> generateRandomNumberOfTests() {

		// Generates random positive integers between 0 and 100 until
		// a number evenly divisible by 7 is encountered.
		Iterator<Integer> inputGenerator = new Iterator<Integer>() {

			Random random = new Random();
			// end::user_guide[]
			{
				// Use fixed seed to always produce the same number of tests for execution on the CI server
				random = new Random(23);
			}
			// tag::user_guide[]
			int current;

			@Override
			public boolean hasNext() {
				current = random.nextInt(100);
				return current % 7 != 0;
			}

			@Override
			public Integer next() {
				return current;
			}
		};

		// Generates display names like: input:5, input:37, input:85, etc.
		Function<Integer, String> displayNameGenerator = (input) -> "input:" + input;

		// Executes tests based on the current input value.
		ThrowingConsumer<Integer> testExecutor = (input) -> assertTrue(input % 7 != 0);

		// Returns a stream of dynamic tests.
		return DynamicTest.stream(inputGenerator, displayNameGenerator, testExecutor);
	}

}
// end::user_guide[]
