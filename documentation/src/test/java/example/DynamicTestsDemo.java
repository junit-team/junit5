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
import java.util.List;
import java.util.stream.Stream;

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

}
