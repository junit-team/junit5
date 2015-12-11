/*
 * Copyright 2015 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.junit.gen5.engine.junit5;

import static org.junit.gen5.api.Assertions.*;

import org.junit.Assert;
import org.junit.Before;
import org.junit.gen5.api.AfterEach;
import org.junit.gen5.api.Assertions;
import org.junit.gen5.api.BeforeEach;
import org.junit.gen5.api.Nested;
import org.junit.gen5.api.Test;
import org.junit.gen5.engine.TestPlanSpecification;

public class StandardTestClassTest extends AbstractJUnit5TestEngineTestCase {

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

		Assert.assertEquals("# tests started", 6, listener.testStartedCount.get());
		Assert.assertEquals("# tests succeeded", 5, listener.testSucceededCount.get());
		Assert.assertEquals("# tests failed", 1, listener.testFailedCount.get());

		Assert.assertEquals("# containers started", 3, listener.containerStartedCount.get());
		Assert.assertEquals("# containers finished", 3, listener.containerFinishedCount.get());
	}

	@org.junit.Test
	public void allTestsInClassAreRunWithBeforeEach() {
		TrackingEngineExecutionListener listener = executeTestsForClass(MyStandardTestCase.class, 4);

		Assert.assertEquals("# tests started", 3, listener.testStartedCount.get());
		Assert.assertEquals("# tests succeeded", 2, listener.testSucceededCount.get());
		Assert.assertEquals("# tests failed", 1, listener.testFailedCount.get());

		Assert.assertEquals("# containers started", 2, listener.containerStartedCount.get());
		Assert.assertEquals("# containers finished", 2, listener.containerFinishedCount.get());

		Assert.assertEquals("# before1 calls", 3, MyStandardTestCase.countBefore1);
		Assert.assertEquals("# before2 calls", 3, MyStandardTestCase.countBefore2);
	}

	@org.junit.Test
	public void allTestsInClassAreRunWithAfterEach() {
		TrackingEngineExecutionListener listener = executeTestsForClass(MyStandardTestCase.class, 4);

		Assert.assertEquals("# tests started", 3, listener.testStartedCount.get());
		Assert.assertEquals("# after each calls", 3, MyStandardTestCase.countAfter);

		Assert.assertEquals("# containers started", 2, listener.containerStartedCount.get());
		Assert.assertEquals("# containers finished", 2, listener.containerFinishedCount.get());
	}

	@org.junit.Test
	public void testsFailWhenBeforeEachFails() {
		TrackingEngineExecutionListener listener = executeTestsForClass(TestCaseWithFailingBefore.class, 3);

		Assert.assertEquals("# tests started", 2, listener.testStartedCount.get());
		Assert.assertEquals("# tests succeeded", 0, listener.testSucceededCount.get());
		Assert.assertEquals("# tests failed", 2, listener.testFailedCount.get());

		Assert.assertEquals("# containers started", 2, listener.containerStartedCount.get());
		Assert.assertEquals("# containers finished", 2, listener.containerFinishedCount.get());

		Assert.assertEquals("# before each calls", 2, TestCaseWithFailingBefore.countBefore);
	}

	@org.junit.Test
	public void testsFailWhenAfterEachFails() {
		TrackingEngineExecutionListener listener = executeTestsForClass(TestCaseWithFailingAfter.class, 2);

		Assert.assertEquals("# tests started", 1, listener.testStartedCount.get());
		Assert.assertEquals("# tests succeeded", 0, listener.testSucceededCount.get());
		Assert.assertEquals("# tests failed", 1, listener.testFailedCount.get());

		Assert.assertEquals("# containers started", 2, listener.containerStartedCount.get());
		Assert.assertEquals("# containers finished", 2, listener.containerFinishedCount.get());

		Assert.assertTrue("test executed?", TestCaseWithFailingAfter.testExecuted);
	}

	@org.junit.Test
	public void nestedTestsAreExecuted() {
		TrackingEngineExecutionListener listener = executeTestsForClass(TestCaseWithNesting.class, 5);

		Assert.assertEquals("# tests started", 3, listener.testStartedCount.get());
		Assert.assertEquals("# tests succeeded", 2, listener.testSucceededCount.get());
		Assert.assertEquals("# tests failed", 1, listener.testFailedCount.get());

		Assert.assertEquals("# containers started", 3, listener.containerStartedCount.get());
		Assert.assertEquals("# containers finished", 3, listener.containerFinishedCount.get());
	}

	@org.junit.Test
	public void doublyNestedTestsAreExecuted() {
		TrackingEngineExecutionListener listener = executeTestsForClass(TestCaseWithDoubleNesting.class, 8);

		Assert.assertEquals("# tests started", 5, listener.testStartedCount.get());
		Assert.assertEquals("# tests succeeded", 3, listener.testSucceededCount.get());
		Assert.assertEquals("# tests failed", 2, listener.testFailedCount.get());

		Assert.assertEquals("# containers started", 4, listener.containerStartedCount.get());
		Assert.assertEquals("# containers finished", 4, listener.containerFinishedCount.get());

		assertAll("before each counts", //
			() -> Assert.assertEquals(5, TestCaseWithDoubleNesting.beforeTopCount),
			() -> Assert.assertEquals(4, TestCaseWithDoubleNesting.beforeNestedCount),
			() -> Assert.assertEquals(2, TestCaseWithDoubleNesting.beforeDoublyNestedCount));
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