/*
 * Copyright 2015-2018 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v20.html
 */

package example;

// tag::user_guide[]

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import example.util.Calculator;

public class ExampleTestCase {

	private final Calculator calculator = new Calculator();

	@Test
	@Disabled("for demonstration purposes")
	void skippedTest() {
		// skipped ...
	}

	@Test
	void succeedingTest() {
		assertEquals(42, calculator.multiply(6, 7));
	}

	@Test
	void failingTest() {
		// The following throws an ArithmeticException: "/ by zero"
		calculator.divide(1, 0);
	}

	@Test
	void abortedTest() {
		assumeTrue("abc".contains("Z"));
		// aborted ...
	}

}
// end::user_guide[]
