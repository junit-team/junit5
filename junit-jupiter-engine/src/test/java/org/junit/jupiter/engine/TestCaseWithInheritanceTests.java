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
import org.junit.platform.engine.TestExecutionResult;
import org.junit.platform.launcher.LauncherDiscoveryRequest;
import org.junit.platform.tck.ExecutionGraph;

/**
 * Integration tests for test class hierarchy support in the {@link JupiterTestEngine}.
 *
 * @since 5.0
 */
class TestCaseWithInheritanceTests extends AbstractJupiterTestEngineTests {

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
		ExecutionGraph executionGraph = executeTestsForClass(LocalTestCase.class).getExecutionGraph();

		assertEquals(6, executionGraph.getTestExecutionsFinished().size(), "# tests started");
		assertEquals(3, executionGraph.getTestExecutionsFinished(TestExecutionResult.Status.SUCCESSFUL).size(),
			"# tests succeeded");
		assertEquals(0, executionGraph.getTestExecutionsSkipped().size(), "# tests skipped");
		assertEquals(1, executionGraph.getTestExecutionsFinished(TestExecutionResult.Status.ABORTED).size(),
			"# tests aborted");
		assertEquals(2, executionGraph.getTestExecutionsFinished(TestExecutionResult.Status.FAILED).size(),
			"# tests failed");

		assertEquals(6, LocalTestCase.countBeforeInvoked, "# before calls");
		assertEquals(6, LocalTestCase.countAfterInvoked, "# after calls");
		assertEquals(6, AbstractTestCase.countSuperBeforeInvoked, "# super before calls");
		assertEquals(6, AbstractTestCase.countSuperAfterInvoked, "# super after calls");
	}

	@Test
	void executeSingleTest() {
		LauncherDiscoveryRequest request = request().selectors(
			selectMethod(LocalTestCase.class, "alwaysPasses")).build();

		ExecutionGraph executionGraph = executeTests(request).getExecutionGraph();

		assertEquals(1, executionGraph.getTestExecutionsFinished().size(), "# tests started");
		assertEquals(1, executionGraph.getTestExecutionsFinished(TestExecutionResult.Status.SUCCESSFUL).size(),
			"# tests succeeded");
		assertEquals(0, executionGraph.getTestExecutionsSkipped().size(), "# tests skipped");
		assertEquals(0, executionGraph.getTestExecutionsFinished(TestExecutionResult.Status.ABORTED).size(),
			"# tests aborted");
		assertEquals(0, executionGraph.getTestExecutionsFinished(TestExecutionResult.Status.FAILED).size(),
			"# tests failed");
	}

	@Test
	void executeTestDeclaredInSuperClass() {
		LauncherDiscoveryRequest request = request().selectors(selectMethod(LocalTestCase.class, "superTest")).build();

		ExecutionGraph executionGraph = executeTests(request).getExecutionGraph();

		assertEquals(1, executionGraph.getTestExecutionsFinished().size(), "# tests started");
		assertEquals(1, executionGraph.getTestExecutionsFinished(TestExecutionResult.Status.SUCCESSFUL).size(),
			"# tests succeeded");
		assertEquals(0, executionGraph.getTestExecutionsSkipped().size(), "# tests skipped");
		assertEquals(0, executionGraph.getTestExecutionsFinished(TestExecutionResult.Status.ABORTED).size(),
			"# tests aborted");
		assertEquals(0, executionGraph.getTestExecutionsFinished(TestExecutionResult.Status.FAILED).size(),
			"# tests failed");

		assertEquals(1, LocalTestCase.countBeforeInvoked, "# after calls");
		assertEquals(1, LocalTestCase.countAfterInvoked, "# after calls");
		assertEquals(1, AbstractTestCase.countSuperBeforeInvoked, "# super before calls");
		assertEquals(1, AbstractTestCase.countSuperAfterInvoked, "# super after calls");

	}

	@Test
	void executeTestWithExceptionThrownInAfterMethod() {
		LauncherDiscoveryRequest request = request().selectors(
			selectMethod(LocalTestCase.class, "throwExceptionInAfterMethod")).build();

		ExecutionGraph executionGraph = executeTests(request).getExecutionGraph();

		assertEquals(1, executionGraph.getTestExecutionsFinished().size(), "# tests started");
		assertEquals(0, executionGraph.getTestExecutionsFinished(TestExecutionResult.Status.SUCCESSFUL).size(),
			"# tests succeeded");
		assertEquals(0, executionGraph.getTestExecutionsSkipped().size(), "# tests skipped");
		assertEquals(0, executionGraph.getTestExecutionsFinished(TestExecutionResult.Status.ABORTED).size(),
			"# tests aborted");
		assertEquals(1, executionGraph.getTestExecutionsFinished(TestExecutionResult.Status.FAILED).size(),
			"# tests failed");
	}

	@Test
	void beforeAndAfterMethodsInTestClassHierarchy() {
		ExecutionGraph executionGraph = executeTestsForClass(TestCase3.class).getExecutionGraph();

		// @formatter:off
		assertAll(
			() -> assertEquals(1, executionGraph.getTestExecutionsFinished().size(), "# tests started"),
			() -> assertEquals(1, executionGraph.getTestExecutionsFinished(TestExecutionResult.Status.SUCCESSFUL).size(), "# tests succeeded"),
			() -> assertEquals(0, executionGraph.getTestExecutionsSkipped().size(), "# tests skipped"),
			() -> assertEquals(0, executionGraph.getTestExecutionsFinished(TestExecutionResult.Status.ABORTED).size(), "# tests aborted"),
			() -> assertEquals(0, executionGraph.getTestExecutionsFinished(TestExecutionResult.Status.FAILED).size(), "# tests failed")
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

		static int countBeforeInvoked = 0;
		static int countAfterInvoked = 0;
		boolean throwExceptionInAfterMethod = false;

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

		@AfterAll
		static void afterAll1() {
			callSequence.add("afterAll1");
		}

		@BeforeEach
		void beforeEach1() {
			callSequence.add("beforeEach1");
		}

		@AfterEach
		void afterEach1() {
			callSequence.add("afterEach1");
		}
	}

	static class TestCase2 extends TestCase1 {

		@BeforeAll
		static void beforeAll2() {
			callSequence.add("beforeAll2");
		}

		@AfterAll
		static void afterAll2() {
			callSequence.add("afterAll2");
		}

		@BeforeEach
		void beforeEach2() {
			callSequence.add("beforeEach2");
		}

		@AfterEach
		void afterEach2() {
			callSequence.add("afterEach2");
		}
	}

	static class TestCase3 extends TestCase2 {

		@BeforeAll
		static void beforeAll3() {
			callSequence.add("beforeAll3");
		}

		@AfterAll
		static void afterAll3() {
			callSequence.add("afterAll3");
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
	}

}
