/*
 * Copyright 2015-2017 the original author or authors.
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

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.platform.commons.annotation.ConcurrentExecution;
import org.junit.platform.commons.annotation.SameThreadExecution;

@SameThreadExecution
class StandardTests {

	@BeforeAll
	static void initAll() {
	}

	@BeforeEach
	void init() {
	}

	@Nested
	class NestedClass {

		@Test
		void succeedingTest() throws InterruptedException {
			Thread.sleep(5000);
		}

		@Test
		@ConcurrentExecution
		void concurrentMethod() throws InterruptedException {
			Thread.sleep(5000);
		}

		// end::user_guide[]
		@extensions.ExpectToFail
		// tag::user_guide[]
		@Test
		void failingTest() throws InterruptedException {
			Thread.sleep(5000);
			fail("a failing test");
		}

		@Test
		@Disabled("for demonstration purposes")
		void skippedTest() {
			// not executed
		}
	}

	@AfterEach
	void tearDown() {
	}

	@AfterAll
	static void tearDownAll() {
	}

}
// end::user_guide[]
