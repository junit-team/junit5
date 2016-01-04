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
import static org.junit.gen5.api.Assertions.assertNotNull;
import static org.junit.gen5.api.Assertions.assertTrue;
import static org.junit.gen5.api.Assertions.fail;
import static org.junit.gen5.api.Assumptions.assumeTrue;

import java.util.HashMap;
import java.util.Map;

import org.junit.gen5.api.AfterEach;
import org.junit.gen5.api.BeforeEach;
import org.junit.gen5.api.Disabled;
import org.junit.gen5.api.Name;
import org.junit.gen5.api.Nested;
import org.junit.gen5.api.Test;
import org.junit.gen5.api.TestName;
import org.junit.gen5.api.extension.ExtendWith;
import org.opentest4j.TestSkippedException;

/**
 * Named *TestCase so Gradle will not try to run it.
 */
@ExtendWith({ CustomTypeParameterResolver.class, CustomAnnotationParameterResolver.class })
class SampleTestCase {

	static boolean staticBeforeInvoked = false;

	boolean beforeInvoked = false;

	boolean throwExceptionInAfterMethod = false;

	@BeforeEach
	static void staticBefore() {
		staticBeforeInvoked = true;
	}

	@BeforeEach
	void before() {
		this.beforeInvoked = true;
		// Reset state, since the test instance is retained across all test methods;
		// otherwise, after() always throws an exception.
		this.throwExceptionInAfterMethod = false;
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
		assertTrue(staticBeforeInvoked, "@BeforeEach was not invoked on static method");
		this.throwExceptionInAfterMethod = true;
	}

	@Test
	@Disabled
	void skippedTest() {
		throw new TestSkippedException("This test will be skipped");
	}

	@Test
	void abortedTest() {
		assumeTrue((2 * 3 == 4), () -> "Assumed that 2*3=4, but 2*3=" + (2 * 3));
	}

	@Test
	@Fast
	void failingTest() {
		fail("This test will always fail");
	}

	@Test
	@Name("custom name")
	void succeedingTest() {
		// no-op
	}

	@Test
	@Name("Method Injection")
	void methodInjectionTest(@TestName String testName, CustomType customType, @CustomAnnotation String value) {
		assertEquals("Method Injection", testName);
		assertNotNull(customType);
		assertNotNull(value);
	}

	@Test
	@Name("with succeeding assertAll")
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
	@Name("with failing assertAll")
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
	@Name("An inner test context")
	class ANestedTestCase {

		@Test
		void innerTest() {

		}
	}

}
