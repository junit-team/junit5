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

import static org.junit.gen5.api.Assertions.assertTrue;
import static org.junit.gen5.api.Assertions.fail;

import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Stream;

import org.junit.gen5.api.DynamicTest;
import org.junit.gen5.api.Tag;
import org.junit.gen5.api.TestFactory;

// end::user_guide[]
@Tag("exclude")
// tag::user_guide[]
class DynamicTestsDemo {

	// This will result in a JUnitException!
	@TestFactory
	List<String> dynamicTestsWithInvalidReturnType() {
		return Arrays.asList("Hello");
	}

	@TestFactory
	Stream<DynamicTest> dynamicTestsFromStream() {
		// end::user_guide[]
		// @formatter:off
		// tag::user_guide[]
		return Stream.of("test1", "test2", "test3")
				.map(displayName -> new DynamicTest(displayName, () -> { /* ... */ }));
		// end::user_guide[]
		// @formatter:on
		// tag::user_guide[]
	}

	@TestFactory
	Collection<DynamicTest> dynamicTestsFromCollection() {
		// end::user_guide[]
		// @formatter:off
		// tag::user_guide[]
		return Arrays.asList(
			new DynamicTest("succeedingTest", () -> assertTrue(true)),
			new DynamicTest("failingTest", () -> fail("failing"))
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
			new DynamicTest("succeedingTest", () -> assertTrue(true)),
			new DynamicTest("failingTest", () -> fail("failing"))
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
			new DynamicTest("succeedingTest", () -> assertTrue(true)),
			new DynamicTest("failingTest", () -> fail("failing"))
		).iterator();
		// end::user_guide[]
		// @formatter:on
		// tag::user_guide[]
	}

	@TestFactory
	Iterator<DynamicTest> dynamicTestsFromCustomIterator() {
		return new Iterator<DynamicTest>() {

			int counter = 0;

			@Override
			public boolean hasNext() {
				return counter < 100;
			}

			@Override
			public DynamicTest next() {
				int index = counter++;
				return new DynamicTest("test" + index, () -> assertTrue(index % 11 != 0));
			}
		};
	}

	@TestFactory
	Stream<DynamicTest> generateRandomNumberOfTests() {

		// Generates random positive integers between 0 and 100 until
		// a number evenly divisible by 7 is encountered.
		Iterator<Integer> inputGenerator = new Iterator<Integer>() {

			final Random random = new Random();
			int last = -1;

			@Override
			public boolean hasNext() {
				return last % 7 != 0;
			}

			@Override
			public Integer next() {
				last = random.nextInt(100);
				return last;
			}
		};

		// Generates display names like: input:5, input:37, input:85, etc.
		Function<? super Integer, String> displayNameGenerator = (input) -> "input:" + input;

		// Generates tests based on the current input value.
		Consumer<? super Integer> testGenerator = (input) -> assertTrue(input % 3 == 0);

		// Creates a stream of dynamic tests.
		return DynamicTest.stream(inputGenerator, displayNameGenerator, testGenerator);
	}

}
// end::user_guide[]
