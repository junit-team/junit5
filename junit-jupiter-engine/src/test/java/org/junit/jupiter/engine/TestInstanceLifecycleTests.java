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

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
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
	private static int beforeCount;
	private static int afterCount;

	@BeforeEach
	void init() {
		instanceCount = 0;
		beforeCount = 0;
		afterCount = 0;
	}

	@Test
	void instancePerMethod() {
		performAssertions(InstancePerMethodTestCase.class, 2);
	}

	@Test
	void instancePerClass() {
		performAssertions(InstancePerClassTestCase.class, 1);
	}

	private void performAssertions(Class<?> testClass, int expectedInstanceCount) {
		ExecutionEventRecorder eventRecorder = executeTestsForClass(testClass);

		// @formatter:off
		assertAll(
			() -> assertEquals(expectedInstanceCount, instanceCount, "instance count"),
			() -> assertEquals(2, eventRecorder.getContainerStartedCount(), "# containers started"),
			() -> assertEquals(2, eventRecorder.getContainerFinishedCount(), "# containers finished"),
			() -> assertEquals(2, eventRecorder.getTestStartedCount(), "# tests started"),
			() -> assertEquals(2, eventRecorder.getTestSuccessfulCount(), "# tests succeeded"),
			() -> assertEquals(2, beforeCount, "# before calls"),
			() -> assertEquals(2, afterCount, "# after calls")
		);
		// @formatter:on
	}

	// The following is commented out b/c it's the default.
	// @TestInstance(Lifecycle.PER_METHOD)
	private static class InstancePerMethodTestCase {

		InstancePerMethodTestCase() {
			instanceCount++;
		}

		@BeforeEach
		void before() {
			beforeCount++;
		}

		@AfterEach
		void after() {
			afterCount++;
		}

		@Test
		void test1() {
		}

		@Test
		void test2() {
		}

	}

	@TestInstance(Lifecycle.PER_CLASS)
	private static class InstancePerClassTestCase extends InstancePerMethodTestCase {
	}

}
