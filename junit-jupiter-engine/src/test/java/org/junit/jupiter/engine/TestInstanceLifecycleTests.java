/*
 * Copyright 2015-2017 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.junit.jupiter.engine;

import static org.junit.Assert.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;
import org.junit.platform.engine.test.event.ExecutionEventRecorder;
import org.junit.platform.runner.JUnitPlatform;
import org.junit.runner.RunWith;

/**
 * Tests for {@link TestInstance @TestInstance} lifecycle support.
 *
 * @since 5.0
 */
@RunWith(JUnitPlatform.class)
public

class TestInstanceLifecycleTests extends AbstractJupiterTestEngineTests {

	private static int instanceCount;
	private static int nestedInstanceCount;
	private static int beforeAllCount;
	private static int afterAllCount;
	private static int beforeEachCount;
	private static int afterEachCount;

	@BeforeEach
	void init() {
		instanceCount = 0;
		nestedInstanceCount = 0;
		beforeAllCount = 0;
		afterAllCount = 0;
		beforeEachCount = 0;
		afterEachCount = 0;
	}

	@Test
	void instancePerMethod() {
		int containers = 2;
		int tests = 2;
		int instances = 2;
		int nestedInstances = 0;
		int allMethods = 1;
		int eachMethods = 2;

		performAssertions(InstancePerMethodTestCase.class, containers, tests, instances, nestedInstances, allMethods,
			eachMethods);
	}

	@Test
	void instancePerClass() {
		int containers = 2;
		int tests = 2;
		int instances = 1;
		int nestedInstances = 0;
		int allMethods = 2;
		int eachMethods = 2;

		performAssertions(InstancePerClassTestCase.class, containers, tests, instances, nestedInstances, allMethods,
			eachMethods);
	}

	@Test
	void instancePerMethodWithNestedTestClass() {
		int containers = 3;
		int tests = 3;
		int instances = 3;
		int nestedInstances = 2;
		int allMethods = 1;
		int eachMethods = 2;

		performAssertions(InstancePerMethodOuterTestCase.class, containers, tests, instances, nestedInstances,
			allMethods, eachMethods);
	}

	@Test
	void instancePerClassWithNestedTestClass() {
		int containers = 3;
		int tests = 3;
		int instances = 1;
		int nestedInstances = 1;
		int allMethods = 2;
		int eachMethods = 2;

		performAssertions(InstancePerClassOuterTestCase.class, containers, tests, instances, nestedInstances,
			allMethods, eachMethods);
	}

	@Test
	void instancePerMethodOnOuterTestClassWithInstancePerClassOnNestedTestClass() {
		int containers = 3;
		int tests = 3;
		int instances = 2;
		int nestedInstances = 1;
		int allMethods = 1;
		int eachMethods = 5;

		performAssertions(MixedLifecyclesOuterTestCase.class, containers, tests, instances, nestedInstances, allMethods,
			eachMethods);
	}

	private void performAssertions(Class<?> testClass, int containers, int tests, int instances, int nestedInstances,
			int allMethods, int eachMethods) {

		ExecutionEventRecorder eventRecorder = executeTestsForClass(testClass);

		// eventRecorder.eventStream().forEach(System.out::println);

		// @formatter:off
		assertAll(
			() -> assertEquals(containers, eventRecorder.getContainerStartedCount(), "# containers started"),
			() -> assertEquals(containers, eventRecorder.getContainerFinishedCount(), "# containers finished"),
			() -> assertEquals(tests, eventRecorder.getTestStartedCount(), "# tests started"),
			() -> assertEquals(tests, eventRecorder.getTestSuccessfulCount(), "# tests succeeded"),
			() -> assertEquals(instances, instanceCount, "instance count"),
			() -> assertEquals(nestedInstances, nestedInstanceCount, "nested instance count"),
			() -> assertEquals(allMethods, beforeAllCount, "@BeforeAll count"),
			() -> assertEquals(allMethods, afterAllCount, "@AfterAll count"),
			() -> assertEquals(eachMethods, beforeEachCount, "@BeforeEach count"),
			() -> assertEquals(eachMethods, afterEachCount, "@AfterEach count")
		);
		// @formatter:on
	}

	// -------------------------------------------------------------------------

	// The following is commented out b/c it's the default.
	// @TestInstance(Lifecycle.PER_METHOD)
	private static class InstancePerMethodTestCase {

		InstancePerMethodTestCase() {
			instanceCount++;
		}

		@BeforeAll
		static void beforeAllStatic(TestInfo testInfo) {
			assertNotNull(testInfo);
			beforeAllCount++;
		}

		@BeforeEach
		void beforeEach() {
			beforeEachCount++;
		}

		@Test
		void test1() {
		}

		@Test
		void test2() {
		}

		@AfterEach
		void afterEach() {
			afterEachCount++;
		}

		@AfterAll
		static void afterAllStatic(TestInfo testInfo) {
			assertNotNull(testInfo);
			afterAllCount++;
		}

	}

	@TestInstance(Lifecycle.PER_CLASS)
	private static class InstancePerClassTestCase extends InstancePerMethodTestCase {

		@BeforeAll
		void beforeAll(TestInfo testInfo) {
			assertNotNull(testInfo);
			beforeAllCount++;
		}

		@AfterAll
		void afterAll(TestInfo testInfo) {
			assertNotNull(testInfo);
			afterAllCount++;
		}

	}

	// The following is commented out b/c it's the default.
	// @TestInstance(Lifecycle.PER_METHOD)
	private static class InstancePerMethodOuterTestCase {

		@SuppressWarnings("unused")
		InstancePerMethodOuterTestCase() {
			instanceCount++;
		}

		@BeforeAll
		static void beforeAll(TestInfo testInfo) {
			assertNotNull(testInfo);
			beforeAllCount++;
		}

		@Test
		void outerTest() {
		}

		@AfterAll
		static void afterAll(TestInfo testInfo) {
			assertNotNull(testInfo);
			afterAllCount++;
		}

		@Nested
		// The following is commented out b/c it's the default.
		// @TestInstance(Lifecycle.PER_METHOD)
		class NestedInstancePerMethodTestCase {

			@SuppressWarnings("unused")
			NestedInstancePerMethodTestCase() {
				nestedInstanceCount++;
			}

			@BeforeEach
			void beforeEach() {
				beforeEachCount++;
			}

			@Test
			void test1() {
			}

			@Test
			void test2() {
			}

			@AfterEach
			void afterEach() {
				afterEachCount++;
			}
		}
	}

	@TestInstance(Lifecycle.PER_CLASS)
	private static class InstancePerClassOuterTestCase {

		@SuppressWarnings("unused")
		InstancePerClassOuterTestCase() {
			instanceCount++;
		}

		@BeforeAll
		static void beforeAll(TestInfo testInfo) {
			assertNotNull(testInfo);
			beforeAllCount++;
		}

		@Test
		void outerTest() {
		}

		@AfterAll
		static void afterAll(TestInfo testInfo) {
			assertNotNull(testInfo);
			afterAllCount++;
		}

		@Nested
		@TestInstance(Lifecycle.PER_CLASS)
		class NestedInstancePerClassTestCase {

			@SuppressWarnings("unused")
			NestedInstancePerClassTestCase() {
				nestedInstanceCount++;
			}

			@BeforeAll
			void beforeAll(TestInfo testInfo) {
				assertNotNull(testInfo);
				beforeAllCount++;
			}

			@BeforeEach
			void beforeEach() {
				beforeEachCount++;
			}

			@Test
			void test1() {
			}

			@Test
			void test2() {
			}

			@AfterEach
			void afterEach() {
				afterEachCount++;
			}

			@AfterAll
			void afterAll(TestInfo testInfo) {
				assertNotNull(testInfo);
				afterAllCount++;
			}
		}
	}

	// The following is commented out b/c it's the default.
	// @TestInstance(Lifecycle.PER_METHOD)
	private static class MixedLifecyclesOuterTestCase {

		@SuppressWarnings("unused")
		MixedLifecyclesOuterTestCase() {
			instanceCount++;
		}

		@BeforeEach
		void beforeEach() {
			beforeEachCount++;
		}

		@Test
		void outerTest() {
		}

		@AfterEach
		void afterEach() {
			afterEachCount++;
		}

		@Nested
		@TestInstance(Lifecycle.PER_CLASS)
		class NestedInstancePerClassTestCase {

			@SuppressWarnings("unused")
			NestedInstancePerClassTestCase() {
				nestedInstanceCount++;
			}

			@BeforeAll
			void beforeAll(TestInfo testInfo) {
				assertNotNull(testInfo);
				beforeAllCount++;
			}

			@BeforeEach
			void beforeEach() {
				beforeEachCount++;
			}

			@Test
			void test1() {
			}

			@Test
			void test2() {
			}

			@AfterEach
			void afterEach() {
				afterEachCount++;
			}

			@AfterAll
			void afterAll(TestInfo testInfo) {
				assertNotNull(testInfo);
				afterAllCount++;
			}
		}
	}

}
