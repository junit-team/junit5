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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import org.junit.gen5.api.Assertions;
import org.junit.gen5.api.Dynamic;
import org.junit.gen5.api.DynamicTest;
import org.junit.gen5.junit4.runner.JUnit5;
import org.junit.runner.RunWith;

@RunWith(JUnit5.class)
public class DynamicTestsDemo {

	@Dynamic
	Stream<DynamicTest> myDynamicTest() {
		List<DynamicTest> tests = new ArrayList<>();

		tests.add(new DynamicTest("succeedingTest", () -> Assertions.assertTrue(true, "succeeding")));
		tests.add(new DynamicTest("failingTest", () -> Assertions.assertTrue(false, "failing")));

		return tests.stream();
	}

	@Dynamic
	Stream<DynamicTest> generatedTestsFromIterator() {
		Iterator<String> stringIterator = Arrays.asList("ATest", "BTest", "CTest").iterator();
		Stream<String> targetStream = StreamSupport.stream(
			Spliterators.spliteratorUnknownSize(stringIterator, Spliterator.ORDERED), false);
		return targetStream.map(s -> new DynamicTest(s, () -> {
		}));
	}

	@Dynamic
	Stream<DynamicTest> generatedTestsFromGeneratorFunction() {
		Iterator<Integer> generator = new Iterator<Integer>() {
			int counter = 0;

			@Override
			public boolean hasNext() {
				return counter < 100;
			}

			@Override
			public Integer next() {
				return counter++;
			}
		};
		Stream<Integer> targetStream = StreamSupport.stream(
			Spliterators.spliteratorUnknownSize(generator, Spliterator.ORDERED), false);
		return targetStream.map(index -> new DynamicTest("test" + index, () -> Assertions.assertTrue(index % 11 != 0)));
	}

	@Dynamic
	Stream<DynamicTest> generatedRandomNumberOfTests() {
		final int AVERAGE = 49;

		Iterator<Integer> generator = new Iterator<Integer>() {
			int last = -1;
			Random random = new Random();

			@Override
			public boolean hasNext() {
				return last % AVERAGE != 0;
			}

			@Override
			public Integer next() {
				last = random.nextInt();
				return last;
			}
		};
		Stream<Integer> targetStream = StreamSupport.stream(
			Spliterators.spliteratorUnknownSize(generator, Spliterator.ORDERED), false);
		return targetStream.map(
			index -> new DynamicTest("test" + index, () -> Assertions.assertFalse(index % AVERAGE == 0)));
	}

}
