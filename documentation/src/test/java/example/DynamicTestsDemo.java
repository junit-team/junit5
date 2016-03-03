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
import java.util.stream.Stream;

import org.junit.gen5.api.Assertions;
import org.junit.gen5.api.Dynamic;
import org.junit.gen5.api.DynamicTest;
import org.junit.gen5.api.Tag;
import org.junit.gen5.junit4.runner.JUnit5;
import org.junit.runner.RunWith;

@RunWith(JUnit5.class)
@Tag("exclude")
public class DynamicTestsDemo {

	//	@Dynamic
	List<String> dynamicTestsWithWrongReturnType() {
		List<String> tests = new ArrayList<>();
		tests.add("Hallo");
		return tests;
	}

	@Dynamic
	List<DynamicTest> dynamicTestsFromList() {
		List<DynamicTest> tests = new ArrayList<>();

		tests.add(new DynamicTest("succeedingTest", () -> Assertions.assertTrue(true, "succeeding")));
		tests.add(new DynamicTest("failingTest", () -> Assertions.assertTrue(false, "failing")));

		return tests;
	}

	@Dynamic
	Stream<DynamicTest> dynamicTestsFromStream() {
		String[] testNames = new String[] { "test1", "test2" };
		return Arrays.stream(testNames).map(name -> new DynamicTest(name, () -> {
		}));
	}

	@Dynamic
	Iterator<DynamicTest> dynamicTestStreamFromIterator() {
		List<DynamicTest> tests = new ArrayList<>();
		tests.add(new DynamicTest("succeedingTest", () -> Assertions.assertTrue(true, "succeeding")));
		tests.add(new DynamicTest("failingTest", () -> Assertions.assertTrue(false, "failing")));
		return tests.iterator();
	}

	@Dynamic
	Iterable<DynamicTest> dynamicTestStreamFromIterable() {
		List<DynamicTest> tests = new ArrayList<>();
		tests.add(new DynamicTest("succeedingTest", () -> Assertions.assertTrue(true, "succeeding")));
		tests.add(new DynamicTest("failingTest", () -> Assertions.assertTrue(false, "failing")));
		return tests;
	}

	@Dynamic
	Iterator<DynamicTest> generatedTestsFromGeneratorFunction() {
		Iterator<DynamicTest> generator = new Iterator<DynamicTest>() {
			int counter = 0;

			@Override
			public boolean hasNext() {
				return counter < 100;
			}

			@Override
			public DynamicTest next() {
				int index = counter++;
				return new DynamicTest("test" + index, () -> Assertions.assertTrue(index % 11 != 0));
			}
		};
		return generator;
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
		return DynamicTest.streamFrom(generator, index -> "test" + index,
			index -> Assertions.assertFalse(index % AVERAGE == 0));
	}

}
