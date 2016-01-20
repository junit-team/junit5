/*
 * Copyright 2015-2016 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v10.html
 */

package com.example;

import static org.junit.gen5.api.Assertions.assertAll;
import static org.junit.gen5.api.Assertions.assertEquals;
import static org.junit.gen5.api.Assertions.assertTrue;
import static org.junit.gen5.api.Assertions.fail;
import static org.junit.gen5.api.Assumptions.assumeTrue;

import java.util.HashMap;
import java.util.Map;

import org.junit.gen5.api.AfterEach;
import org.junit.gen5.api.BeforeEach;
import org.junit.gen5.api.Disabled;
import org.junit.gen5.api.DisplayName;
import org.junit.gen5.api.Nested;
import org.junit.gen5.api.Test;

/**
 * Named *TestCase so Gradle will not try to run it.
 */
class SampleTestCase {

	boolean beforeInvoked = false;

	boolean throwExceptionInAfterMethod = false;

	@BeforeEach
	void before() {
		this.beforeInvoked = true;
	}

	@AfterEach
	void after() {
		if (this.throwExceptionInAfterMethod) {
			throw new RuntimeException("Exception thrown from @AfterEach method");
		}
	}

	@Test
	void methodLevelCallbacks() {
		assertTrue(this.beforeInvoked, "@BeforeEach was not invoked on instance method");
		this.throwExceptionInAfterMethod = true;
	}

	@Test
	@Disabled
	void skippedTest() {
		fail("This test should have been skipped");
	}

	@Test
	void abortedTest() {
		assumeTrue((2 * 3 == 4), () -> "Assumed that 2*3=4, but 2*3=" + (2 * 3));
	}

	@Test
	void failingTest() {
		fail("This test will always fail");
	}

	@Test
	@DisplayName("custom name")
	void succeedingTest() {
		// no-op
	}

	@Test
	@DisplayName("with succeeding assertAll")
	void assertAllTest() {
		Map<String, String> person = new HashMap<String, String>();
		person.put("firstName", "Johannes");
		person.put("lastName", "Link");
		person.put("city", "Heidelberg");

		// @formatter:off
		assertAll("Check person",
			() -> assertEquals("Johannes", person.get("firstName")),
			() -> assertEquals("Link", person.get("lastName")),
			() -> assertEquals("Heidelberg", person.get("city"))
		);
		// @formatter:on
	}

	@Test
	@DisplayName("with failing assertAll")
	void assertAllFailingTest() {
		Map<String, String> person = new HashMap<String, String>();
		person.put("firstName", "Johanes");
		person.put("lastName", "Link");
		person.put("city", "Aschaffenburg");

		// @formatter:off
		assertAll("Check person",
			() -> assertEquals("Johannes", person.get("firstName")),
			() -> assertEquals("Link", person.get("lastName")),
			() -> assertEquals("Heidelberg", person.get("city"))
		);
		// @formatter:on
	}

	@Nested
	@DisplayName("An inner test context")
	class ANestedTestCase {

		@Test
		void innerTest() {

		}
	}

}
