/*
 * Copyright 2015 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v10.html
 */

package com.example;

import static org.junit.gen5.api.Assertions.*;
import static org.junit.gen5.api.Assumptions.assumeTrue;

import java.util.HashMap;
import java.util.Map;

import org.junit.gen5.api.After;
import org.junit.gen5.api.Before;
import org.junit.gen5.api.Test;
import org.opentestalliance.TestSkippedException;

/**
 * Named *TestCase so Gradle will not try to run it.
 */
class SampleTestCase {

	static boolean staticBeforeInvoked = false;

	boolean beforeInvoked = false;

	boolean throwExceptionInAfterMethod = false;

	@Before
	static void staticBefore() {
		staticBeforeInvoked = true;
	}

	@Before
	void before() {
		this.beforeInvoked = true;
		// Reset state, since the test instance is retained across all test methods;
		// otherwise, after() always throws an exception.
		this.throwExceptionInAfterMethod = false;
	}

	@After
	void after() {
		if (this.throwExceptionInAfterMethod) {
			throw new RuntimeException("Exception thrown from @After method");
		}
	}

	@Test
	void methodLevelCallbacks() {
		assertTrue(this.beforeInvoked, "@Before was not invoked on instance method");
		assertTrue(staticBeforeInvoked, "@Before was not invoked on static method");
		this.throwExceptionInAfterMethod = true;
	}

	@Test
	void skippedTest() {
		throw new TestSkippedException("This test will be skipped");
	}

	@Test
	void abortedTest() {
		assumeTrue((2 * 3 == 4), () -> "Assumed that 2*3=4, but 2*3=" + (2 * 3));
	}

	@Test
	void failingTest() {
		fail("This test will always fail");
	}

	@Test(name = "custom name")
	void succeedingTest() {
		// no-op
	}

	@Test
	void argumentInjectionByType(CustomType customType) {

		//		assertTrue(customType != null);    /not yet there
		assertTrue(true);

	}

	@Test
	void argumentInjectionByAnnotation(@CustomAnnotation String value) {

		//		assertTrue(customType != null);    /not yet there
		assertTrue(true);

	}

	@Test(name = "with succeeding assertAll")
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

	@Test(name = "with failing assertAll")
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

	//Currently ignored by junit5 engine
	class InnerTestCase {

		@Test
		void innerTest() {

		}
	}
}
