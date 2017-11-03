/*
 * Copyright 2015-2017 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v20.html
 */

package org.junit.jupiter.engine;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;
import static org.junit.platform.engine.discovery.DiscoverySelectors.selectClass;
import static org.junit.platform.launcher.core.LauncherDiscoveryRequestBuilder.request;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.AfterEngineExecution;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.BeforeEngineExecution;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.AfterEngineExecutionCallback;
import org.junit.jupiter.api.extension.BeforeEngineExecutionCallback;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.platform.engine.TestDescriptor;
import org.junit.platform.engine.test.event.ExecutionEventRecorder;
import org.junit.platform.launcher.LauncherDiscoveryRequest;
import org.opentest4j.TestAbortedException;

/**
 * Testing execution in standard test cases {@link JupiterTestEngine}.
 *
 * @since 5.0
 */
class StandardTestClassTests extends AbstractJupiterTestEngineTests {

	@BeforeEach
	void init() {
		MyStandardTestCase.countBefore0 = 0;
		MyStandardTestCase.countBefore1 = 0;
		MyStandardTestCase.countBefore2 = 0;
		MyStandardTestCase.countAfter0 = 0;
		MyStandardTestCase.countAfter1 = 0;
	}

	@Test
	void standardTestClassIsCorrectlyDiscovered() {
		LauncherDiscoveryRequest request = request().selectors(selectClass(MyStandardTestCase.class)).build();
		TestDescriptor engineDescriptor = discoverTests(request);
		assertEquals(5, engineDescriptor.getDescendants().size(), "# resolved test descriptors");
	}

	@Test
	void moreThanOneTestClassIsCorrectlyDiscovered() {
		LauncherDiscoveryRequest request = request().selectors(selectClass(SecondOfTwoTestCases.class)).build();
		TestDescriptor engineDescriptor = discoverTests(request);
		assertEquals(2 + 2, engineDescriptor.getDescendants().size(), "# resolved test descriptors");
	}

	@Test
	void moreThanOneTestClassIsExecuted() {
		LauncherDiscoveryRequest request = request().selectors(selectClass(FirstOfTwoTestCases.class),
			selectClass(SecondOfTwoTestCases.class)).build();

		ExecutionEventRecorder eventRecorder = executeTests(request);

		assertEquals(6, eventRecorder.getTestStartedCount(), "# tests started");
		assertEquals(5, eventRecorder.getTestSuccessfulCount(), "# tests succeeded");
		assertEquals(1, eventRecorder.getTestFailedCount(), "# tests failed");

		assertEquals(3, eventRecorder.getContainerStartedCount(), "# containers started");
		assertEquals(3, eventRecorder.getContainerFinishedCount(), "# containers finished");
	}

	@Test
	void allTestsInClassAreRunWithBeforeEach() {
		ExecutionEventRecorder eventRecorder = executeTestsForClass(MyStandardTestCase.class);

		assertEquals(4, eventRecorder.getTestStartedCount(), "# tests started");
		assertEquals(2, eventRecorder.getTestSuccessfulCount(), "# tests succeeded");
		assertEquals(1, eventRecorder.getTestAbortedCount(), "# tests aborted");
		assertEquals(1, eventRecorder.getTestFailedCount(), "# tests failed");

		assertEquals(2, eventRecorder.getContainerStartedCount(), "# containers started");
		assertEquals(2, eventRecorder.getContainerFinishedCount(), "# containers finished");

		assertEquals(4, MyStandardTestCase.countBefore1, "# before1 calls");
		assertEquals(4, MyStandardTestCase.countBefore2, "# before2 calls");
	}

	@Test
	void allTestsInClassAreRunWithAfterEach() {
		ExecutionEventRecorder eventRecorder = executeTestsForClass(MyStandardTestCase.class);

		assertEquals(4, eventRecorder.getTestStartedCount(), "# tests started");
		assertEquals(4, MyStandardTestCase.countAfter1, "# after each calls");

		assertEquals(2, eventRecorder.getContainerStartedCount(), "# containers started");
		assertEquals(2, eventRecorder.getContainerFinishedCount(), "# containers finished");
	}

	@Test
	void allTestsInClassAreRunWithEngineLevelCallbacks() {
		ExecutionEventRecorder eventRecorder = executeTestsForClass(MyStandardTestCase.class);

		assertEquals(4, eventRecorder.getTestStartedCount(), "# tests started");
		assertEquals(2, MyStandardTestCase.countBefore0, "# before0 calls");
		assertEquals(2, MyStandardTestCase.countAfter0, "# after0 calls");

		assertEquals(2, eventRecorder.getContainerStartedCount(), "# containers started");
		assertEquals(2, eventRecorder.getContainerFinishedCount(), "# containers finished");
	}

	@Test
	void testsFailWhenBeforeEachFails() {
		ExecutionEventRecorder eventRecorder = executeTestsForClass(TestCaseWithFailingBefore.class);

		assertEquals(2, eventRecorder.getTestStartedCount(), "# tests started");
		assertEquals(0, eventRecorder.getTestSuccessfulCount(), "# tests succeeded");
		assertEquals(2, eventRecorder.getTestFailedCount(), "# tests failed");

		assertEquals(2, eventRecorder.getContainerStartedCount(), "# containers started");
		assertEquals(2, eventRecorder.getContainerFinishedCount(), "# containers finished");

		assertEquals(2, TestCaseWithFailingBefore.countBefore, "# before each calls");
	}

	@Test
	void testsFailWhenAfterEachFails() {
		ExecutionEventRecorder eventRecorder = executeTestsForClass(TestCaseWithFailingAfter.class);

		assertEquals(1, eventRecorder.getTestStartedCount(), "# tests started");
		assertEquals(0, eventRecorder.getTestSuccessfulCount(), "# tests succeeded");
		assertEquals(1, eventRecorder.getTestFailedCount(), "# tests failed");

		assertEquals(2, eventRecorder.getContainerStartedCount(), "# containers started");
		assertEquals(2, eventRecorder.getContainerFinishedCount(), "# containers finished");

		assertTrue(TestCaseWithFailingAfter.testExecuted, "test executed?");
	}

	public static class AlphaOmega implements BeforeEngineExecutionCallback, AfterEngineExecutionCallback {
		public AlphaOmega() {
			// System.out.println("AlphaOmega.<init>");
		}

		@Override
		public void beforeEngineExecution(ExtensionContext context) {
			// System.out.println("AlphaOmega.beforeEngineExecution() context = [" + context + "]");
		}

		@Override
		public void afterEngineExecution(ExtensionContext context) {
			// System.out.println("AlphaOmega.afterEngineExecution() context = [" + context + "]");
		}
	}

	@ExtendWith(AlphaOmega.class)
	@TestInstance(TestInstance.Lifecycle.PER_CLASS)
	private static class MyStandardTestCase {

		static int countBefore0 = 0;
		static int countBefore1 = 0;
		static int countBefore2 = 0;
		static int countAfter0 = 0;
		static int countAfter1 = 0;

		@BeforeEngineExecution
		static void before0() {
			countBefore0++;
		}

		@BeforeEngineExecution
		static void before0(ExtensionContext context) {
			context.getStore(ExtensionContext.Namespace.GLOBAL).put("alpha", "omega");
			countBefore0++;
		}

		@AfterEngineExecution
		static void after0(ExtensionContext context) {
			assertEquals("omega", context.getStore(ExtensionContext.Namespace.GLOBAL).get("alpha"));
			countAfter0++;
		}

		@AfterEngineExecution
		void after0NonStatic(ExtensionContext context) {
			assertEquals("omega", context.getStore(ExtensionContext.Namespace.GLOBAL).get("alpha"));
			countAfter0++;
		}

		@BeforeEach
		void before1() {
			countBefore1++;
		}

		@BeforeEach
		void before2() {
			countBefore2++;
		}

		@AfterEach
		void after1() {
			countAfter1++;
		}

		@Test
		void succeedingTest1() {
			assertTrue(true);
		}

		@Test
		void succeedingTest2() {
			assertTrue(true);
		}

		@Test
		void failingTest() {
			fail("always fails");
		}

		@Test
		void abortedTest() {
			throw new TestAbortedException("aborted!");
		}

	}

	private static class FirstOfTwoTestCases {

		@Test
		void succeedingTest1() {
			assertTrue(true);
		}

		@Test
		void succeedingTest2() {
			assertTrue(true);
		}

		@Test
		void failingTest() {
			fail("always fails");
		}

	}

	private static class SecondOfTwoTestCases {

		@Test
		void succeedingTest1() {
			assertTrue(true);
		}

		@Test
		void succeedingTest2() {
			assertTrue(true);
		}

		@Test
		void succeedingTest3() {
			assertTrue(true);
		}

	}

	private static class TestCaseWithFailingBefore {

		static int countBefore = 0;

		@BeforeEach
		void before() {
			countBefore++;
			throw new RuntimeException("Problem during setup");
		}

		@Test
		void test1() {
		}

		@Test
		void test2() {
		}

	}

	private static class TestCaseWithFailingAfter {

		static boolean testExecuted = false;

		@AfterEach
		void after() {
			throw new RuntimeException("Problem during 'after'");
		}

		@Test
		void test1() {
			testExecuted = true;
		}

	}

}
