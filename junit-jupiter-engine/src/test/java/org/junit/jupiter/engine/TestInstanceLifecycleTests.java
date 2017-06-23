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
	private static int beforeAllCount;
	private static int afterAllCount;
	private static int beforeEachCount;
	private static int afterEachCount;

	@BeforeEach
	void init() {
		instanceCount = 0;
		beforeAllCount = 0;
		afterAllCount = 0;
		beforeEachCount = 0;
		afterEachCount = 0;
	}

	@Test
	void instancePerMethod() {
		performAssertions(InstancePerMethodTestCase.class, 2, 1, 1);
	}

	@Test
	void instancePerClass() {
		performAssertions(InstancePerClassTestCase.class, 1, 2, 2);
	}

	private void performAssertions(Class<?> testClass, int expectedInstanceCount, int expectedBeforeAllCount,
			int expectedAfterAllCount) {

		ExecutionEventRecorder eventRecorder = executeTestsForClass(testClass);

		// eventRecorder.eventStream().forEach(System.out::println);

		// @formatter:off
		assertAll(
			() -> assertEquals(2, eventRecorder.getContainerStartedCount(), "# containers started"),
			() -> assertEquals(2, eventRecorder.getContainerFinishedCount(), "# containers finished"),
			() -> assertEquals(2, eventRecorder.getTestStartedCount(), "# tests started"),
			() -> assertEquals(2, eventRecorder.getTestSuccessfulCount(), "# tests succeeded"),
			() -> assertEquals(expectedInstanceCount, instanceCount, "instance count"),
			() -> assertEquals(expectedBeforeAllCount, beforeAllCount, "@BeforeAll count"),
			() -> assertEquals(expectedAfterAllCount, afterAllCount, "@AfterAll count"),
			() -> assertEquals(2, beforeEachCount, "@BeforeEach count"),
			() -> assertEquals(2, afterEachCount, "@AfterEach count")
		);
		// @formatter:on
	}

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

}
