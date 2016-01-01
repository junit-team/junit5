/*
 * Copyright 2015-2016 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.junit.gen5.engine.junit5;

import static org.junit.gen5.api.Assertions.*;

import org.junit.Before;
import org.junit.gen5.api.AfterEach;
import org.junit.gen5.api.Assertions;
import org.junit.gen5.api.BeforeEach;
import org.junit.gen5.api.Nested;
import org.junit.gen5.api.Test;
import org.junit.gen5.engine.TestPlanSpecification;
import org.opentest4j.TestAbortedException;

public class StandardTestClassTests extends AbstractJUnit5TestEngineTests {

	@Before
	public void init() {
		MyStandardTestCase.countBefore1 = 0;
		MyStandardTestCase.countBefore2 = 0;
		MyStandardTestCase.countAfter = 0;
	}

	@org.junit.Test
	public void moreThanOneTestClassIsExecuted() {
		TestPlanSpecification testPlanSpecification = TestPlanSpecification.build(
			TestPlanSpecification.forClass(FirstOfTwoTestCases.class),
			TestPlanSpecification.forClass(SecondOfTwoTestCases.class));

		TrackingEngineExecutionListener listener = executeTests(testPlanSpecification, 6 + 2);

		assertEquals(6, listener.testStartedCount.get(), "# tests started");
		assertEquals(5, listener.testSucceededCount.get(), "# tests succeeded");
		assertEquals(1, listener.testFailedCount.get(), "# tests failed");

		assertEquals(3, listener.containerStartedCount.get(), "# containers started");
		assertEquals(3, listener.containerFinishedCount.get(), "# containers finished");
	}

	@org.junit.Test
	public void allTestsInClassAreRunWithBeforeEach() {
		TrackingEngineExecutionListener listener = executeTestsForClass(MyStandardTestCase.class, 5);

		assertEquals(4, listener.testStartedCount.get(), "# tests started");
		assertEquals(2, listener.testSucceededCount.get(), "# tests succeeded");
		assertEquals(1, listener.testAbortedCount.get(), "# tests aborted");
		assertEquals(1, listener.testFailedCount.get(), "# tests failed");

		assertEquals(2, listener.containerStartedCount.get(), "# containers started");
		assertEquals(2, listener.containerFinishedCount.get(), "# containers finished");

		assertEquals(4, MyStandardTestCase.countBefore1, "# before1 calls");
		assertEquals(4, MyStandardTestCase.countBefore2, "# before2 calls");
	}

	@org.junit.Test
	public void allTestsInClassAreRunWithAfterEach() {
		TrackingEngineExecutionListener listener = executeTestsForClass(MyStandardTestCase.class, 5);

		assertEquals(4, listener.testStartedCount.get(), "# tests started");
		assertEquals(4, MyStandardTestCase.countAfter, "# after each calls");

		assertEquals(2, listener.containerStartedCount.get(), "# containers started");
		assertEquals(2, listener.containerFinishedCount.get(), "# containers finished");
	}

	@org.junit.Test
	public void testsFailWhenBeforeEachFails() {
		TrackingEngineExecutionListener listener = executeTestsForClass(TestCaseWithFailingBefore.class, 3);

		assertEquals(2, listener.testStartedCount.get(), "# tests started");
		assertEquals(0, listener.testSucceededCount.get(), "# tests succeeded");
		assertEquals(2, listener.testFailedCount.get(), "# tests failed");

		assertEquals(2, listener.containerStartedCount.get(), "# containers started");
		assertEquals(2, listener.containerFinishedCount.get(), "# containers finished");

		assertEquals(2, TestCaseWithFailingBefore.countBefore, "# before each calls");
	}

	@org.junit.Test
	public void testsFailWhenAfterEachFails() {
		TrackingEngineExecutionListener listener = executeTestsForClass(TestCaseWithFailingAfter.class, 2);

		assertEquals(1, listener.testStartedCount.get(), "# tests started");
		assertEquals(0, listener.testSucceededCount.get(), "# tests succeeded");
		assertEquals(1, listener.testFailedCount.get(), "# tests failed");

		assertEquals(2, listener.containerStartedCount.get(), "# containers started");
		assertEquals(2, listener.containerFinishedCount.get(), "# containers finished");

		assertTrue(TestCaseWithFailingAfter.testExecuted, "test executed?");
	}

	@org.junit.Test
	public void nestedTestsAreExecuted() {
		TrackingEngineExecutionListener listener = executeTestsForClass(TestCaseWithNesting.class, 5);

		assertEquals(3, listener.testStartedCount.get(), "# tests started");
		assertEquals(2, listener.testSucceededCount.get(), "# tests succeeded");
		assertEquals(1, listener.testFailedCount.get(), "# tests failed");

		assertEquals(3, listener.containerStartedCount.get(), "# containers started");
		assertEquals(3, listener.containerFinishedCount.get(), "# containers finished");
	}

	@org.junit.Test
	public void doublyNestedTestsAreExecuted() {
		TrackingEngineExecutionListener listener = executeTestsForClass(TestCaseWithDoubleNesting.class, 8);

		assertEquals(5, listener.testStartedCount.get(), "# tests started");
		assertEquals(3, listener.testSucceededCount.get(), "# tests succeeded");
		assertEquals(2, listener.testFailedCount.get(), "# tests failed");

		assertEquals(4, listener.containerStartedCount.get(), "# containers started");
		assertEquals(4, listener.containerFinishedCount.get(), "# containers finished");

		assertAll("before each counts", //
			() -> assertEquals(5, TestCaseWithDoubleNesting.beforeTopCount),
			() -> assertEquals(4, TestCaseWithDoubleNesting.beforeNestedCount),
			() -> assertEquals(2, TestCaseWithDoubleNesting.beforeDoublyNestedCount));
	}

}

class MyStandardTestCase {

	static int countBefore1 = 0;
	static int countBefore2 = 0;
	static int countAfter = 0;

	@BeforeEach
	void before1() {
		countBefore1++;
	}

	@BeforeEach
	void before2() {
		countBefore2++;
	}

	@AfterEach
	void after() {
		countAfter++;
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

class FirstOfTwoTestCases {

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

class SecondOfTwoTestCases {

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

class TestCaseWithFailingBefore {

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

class TestCaseWithFailingAfter {

	static boolean testExecuted = false;

	@AfterEach
	void after() {
		throw new RuntimeException("Problem during setup");
	}

	@Test
	void test1() {
		testExecuted = true;
	}

}

class TestCaseWithNesting {

	@Test
	void someTest() {
	}

	@Nested
	class NestedTestCase {

		@Test
		void successful() {
		}

		@Test
		void failing() {
			Assertions.fail("Something went horribly wrong");
		}
	}
}

class TestCaseWithDoubleNesting {

	static int beforeTopCount = 0;
	static int beforeNestedCount = 0;
	static int beforeDoublyNestedCount = 0;

	@BeforeEach
	void top() {
		beforeTopCount++;
	}

	@Test
	void someTest() {
	}

	@Nested
	class NestedTestCase {

		@BeforeEach
		void nested() {
			beforeNestedCount++;
		}

		@Test
		void successful() {
		}

		@Test
		void failing() {
			Assertions.fail("Something went horribly wrong");
		}

		@Nested
		class DoublyNestedTestCase {

			@BeforeEach
			void doublyNested() {
				beforeDoublyNestedCount++;
			}

			@Test
			void successful() {
			}

			@Test
			void failing() {
				Assertions.fail("Something went horribly wrong");
			}
		}
	}
}