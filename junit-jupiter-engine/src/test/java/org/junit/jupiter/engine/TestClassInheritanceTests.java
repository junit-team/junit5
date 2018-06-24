/*
 * Copyright 2015-2018 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v20.html
 */

package org.junit.jupiter.engine;

import static java.util.Arrays.asList;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;
import static org.junit.jupiter.api.Assumptions.assumeTrue;
import static org.junit.platform.engine.discovery.DiscoverySelectors.selectMethod;
import static org.junit.platform.launcher.core.LauncherDiscoveryRequestBuilder.request;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.platform.engine.test.event.ExecutionEventRecorder;
import org.junit.platform.launcher.LauncherDiscoveryRequest;

/**
 * Integration tests for test class hierarchy support in the {@link JupiterTestEngine}.
 *
 * @since 5.0
 */
class TestClassInheritanceTests extends AbstractJupiterTestEngineTests {

	private static final List<String> callSequence = new ArrayList<>();

	@BeforeEach
	void initStatics() {
		callSequence.clear();
		LocalTestCase.countBeforeInvoked = 0;
		LocalTestCase.countAfterInvoked = 0;
		AbstractTestCase.countSuperBeforeInvoked = 0;
		AbstractTestCase.countSuperAfterInvoked = 0;
	}

	@Test
	void executeAllTestsInClass() {
		ExecutionEventRecorder eventRecorder = executeTestsForClass(LocalTestCase.class);

		assertEquals(6, eventRecorder.getTestStartedCount(), "# tests started");
		assertEquals(3, eventRecorder.getTestSuccessfulCount(), "# tests succeeded");
		assertEquals(0, eventRecorder.getTestSkippedCount(), "# tests skipped");
		assertEquals(1, eventRecorder.getTestAbortedCount(), "# tests aborted");
		assertEquals(2, eventRecorder.getTestFailedCount(), "# tests failed");

		assertEquals(6, LocalTestCase.countBeforeInvoked, "# before calls");
		assertEquals(6, LocalTestCase.countAfterInvoked, "# after calls");
		assertEquals(6, AbstractTestCase.countSuperBeforeInvoked, "# super before calls");
		assertEquals(6, AbstractTestCase.countSuperAfterInvoked, "# super after calls");
	}

	@Test
	void executeSingleTest() {
		LauncherDiscoveryRequest request = request().selectors(
			selectMethod(LocalTestCase.class, "alwaysPasses")).build();

		ExecutionEventRecorder eventRecorder = executeTests(request);

		assertEquals(1, eventRecorder.getTestStartedCount(), "# tests started");
		assertEquals(1, eventRecorder.getTestSuccessfulCount(), "# tests succeeded");
		assertEquals(0, eventRecorder.getTestSkippedCount(), "# tests skipped");
		assertEquals(0, eventRecorder.getTestAbortedCount(), "# tests aborted");
		assertEquals(0, eventRecorder.getTestFailedCount(), "# tests failed");
	}

	@Test
	void executeTestDeclaredInSuperClass() {
		LauncherDiscoveryRequest request = request().selectors(selectMethod(LocalTestCase.class, "superTest")).build();

		ExecutionEventRecorder eventRecorder = executeTests(request);

		assertEquals(1, eventRecorder.getTestStartedCount(), "# tests started");
		assertEquals(1, eventRecorder.getTestSuccessfulCount(), "# tests succeeded");
		assertEquals(0, eventRecorder.getTestSkippedCount(), "# tests skipped");
		assertEquals(0, eventRecorder.getTestAbortedCount(), "# tests aborted");
		assertEquals(0, eventRecorder.getTestFailedCount(), "# tests failed");

		assertEquals(1, LocalTestCase.countBeforeInvoked, "# after calls");
		assertEquals(1, LocalTestCase.countAfterInvoked, "# after calls");
		assertEquals(1, AbstractTestCase.countSuperBeforeInvoked, "# super before calls");
		assertEquals(1, AbstractTestCase.countSuperAfterInvoked, "# super after calls");

	}

	@Test
	void executeTestWithExceptionThrownInAfterMethod() {
		LauncherDiscoveryRequest request = request().selectors(
			selectMethod(LocalTestCase.class, "throwExceptionInAfterMethod")).build();

		ExecutionEventRecorder eventRecorder = executeTests(request);

		assertEquals(1, eventRecorder.getTestStartedCount(), "# tests started");
		assertEquals(0, eventRecorder.getTestSuccessfulCount(), "# tests succeeded");
		assertEquals(0, eventRecorder.getTestSkippedCount(), "# tests skipped");
		assertEquals(0, eventRecorder.getTestAbortedCount(), "# tests aborted");
		assertEquals(1, eventRecorder.getTestFailedCount(), "# tests failed");
	}

	@Test
	void beforeAndAfterMethodsInTestClassHierarchy() {
		ExecutionEventRecorder eventRecorder = executeTestsForClass(TestCase3.class);

		// @formatter:off
		assertAll(
			() -> assertEquals(1, eventRecorder.getTestStartedCount(), "# tests started"),
			() -> assertEquals(1, eventRecorder.getTestSuccessfulCount(), "# tests succeeded"),
			() -> assertEquals(0, eventRecorder.getTestSkippedCount(), "# tests skipped"),
			() -> assertEquals(0, eventRecorder.getTestAbortedCount(), "# tests aborted"),
			() -> assertEquals(0, eventRecorder.getTestFailedCount(), "# tests failed")
		);
		// @formatter:on

		// @formatter:off
		assertEquals(asList(
			"beforeAll1",
				"beforeAll2",
					"beforeAll3",
						"beforeEach1",
							"beforeEach2",
								"beforeEach3",
									"test3",
								"afterEach3",
							"afterEach2",
						"afterEach1",
					"afterAll3",
				"afterAll2",
			"afterAll1"
		), callSequence, "wrong call sequence");
		// @formatter:on
	}

	// -------------------------------------------------------------------

	private static abstract class AbstractTestCase {

		static int countSuperBeforeInvoked = 0;
		static int countSuperAfterInvoked = 0;

		@BeforeEach
		void superBefore() {
			countSuperBeforeInvoked++;
		}

		@AfterEach
		void superAfter() {
			countSuperAfterInvoked++;
		}

		@Test
		void superTest() {
			/* no-op */
		}
	}

	static class LocalTestCase extends AbstractTestCase {

		boolean throwExceptionInAfterMethod = false;

		static int countBeforeInvoked = 0;
		static int countAfterInvoked = 0;

		@BeforeEach
		void before() {
			countBeforeInvoked++;
			// Reset state, since the test instance is retained across all test methods;
			// otherwise, after() always throws an exception.
			this.throwExceptionInAfterMethod = false;
		}

		@AfterEach
		void after() {
			countAfterInvoked++;
			if (this.throwExceptionInAfterMethod) {
				throw new RuntimeException("Exception thrown from @AfterEach method");
			}
		}

		@Test
		void otherTest() {
			/* no-op */
		}

		@Test
		void throwExceptionInAfterMethod() {
			this.throwExceptionInAfterMethod = true;
		}

		@Test
		void alwaysPasses() {
			/* no-op */
		}

		@Test
		void aborted() {
			assumeTrue(false);
		}

		@Test
		void alwaysFails() {
			fail("#fail");
		}
	}

	static class TestCase1 {

		@BeforeAll
		static void beforeAll1() {
			callSequence.add("beforeAll1");
		}

		@BeforeEach
		void beforeEach1() {
			callSequence.add("beforeEach1");
		}

		@AfterEach
		void afterEach1() {
			callSequence.add("afterEach1");
		}

		@AfterAll
		static void afterAll1() {
			callSequence.add("afterAll1");
		}
	}

	static class TestCase2 extends TestCase1 {

		@BeforeAll
		static void beforeAll2() {
			callSequence.add("beforeAll2");
		}

		@BeforeEach
		void beforeEach2() {
			callSequence.add("beforeEach2");
		}

		@AfterEach
		void afterEach2() {
			callSequence.add("afterEach2");
		}

		@AfterAll
		static void afterAll2() {
			callSequence.add("afterAll2");
		}
	}

	static class TestCase3 extends TestCase2 {

		@BeforeAll
		static void beforeAll3() {
			callSequence.add("beforeAll3");
		}

		@BeforeEach
		void beforeEach3() {
			callSequence.add("beforeEach3");
		}

		@Test
		void test3() {
			callSequence.add("test3");
		}

		@AfterEach
		void afterEach3() {
			callSequence.add("afterEach3");
		}

		@AfterAll
		static void afterAll3() {
			callSequence.add("afterAll3");
		}
	}

}
