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

import static org.junit.jupiter.api.Assertions.fail;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

public class ExampleTestCase {

	@Test
	@Disabled("for demonstration purposes")
	void skippedTest() {
	}

	@Test
	void succeedingTest() {
	}

	@Test
	void failingTest() {
		fail("a failing test");
	}

	@Test
	void abortedTest() {
		assumeTrue("abc".contains("Z"));
		fail("test should have been aborted");
	}

}
// end::user_guide[]
