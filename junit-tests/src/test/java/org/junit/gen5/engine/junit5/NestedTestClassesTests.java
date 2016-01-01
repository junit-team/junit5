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
import static org.junit.gen5.engine.TestPlanSpecification.*;

import org.junit.gen5.api.AfterEach;
import org.junit.gen5.api.BeforeEach;
import org.junit.gen5.api.Nested;
import org.junit.gen5.api.Test;
import org.junit.gen5.engine.TestPlanSpecification;

/**
 * Integration tests that verify support for {@linkplain Nested nested contexts}
 * in the {@link JUnit5TestEngine}.
 *
 * @since 5.0
 */
public class NestedTestClassesTests extends AbstractJUnit5TestEngineTests {

	@org.junit.Test
	public void executeTestCaseWithNestedTests() {
		TestCaseWithNesting.countAfterInvoked = 0;

		TestPlanSpecification spec = build(forClass(TestCaseWithNesting.class));
		TrackingEngineExecutionListener listener = executeTests(spec, 6);

		assertEquals(3, listener.testStartedCount.get(), "# tests started");
		assertEquals(3, listener.testSucceededCount.get(), "# tests succeeded");
		assertEquals(0, listener.testSkippedCount.get(), "# tests skipped");
		assertEquals(0, listener.testAbortedCount.get(), "# tests aborted");
		assertEquals(0, listener.testFailedCount.get(), "# tests failed");

		assertEquals(3, TestCaseWithNesting.countAfterInvoked, "# after calls");
	}

	// -------------------------------------------------------------------

	private static class TestCaseWithNesting {

		boolean beforeInvoked = false;
		boolean innerBeforeInvoked = false;

		static int countAfterInvoked = 0;

		@BeforeEach
		void init() {
			beforeInvoked = true;
		}

		@AfterEach
		void after() {
			countAfterInvoked++;
		}

		@Test
		void enabledTest() {
		}

		@Nested
		class InnerTestCase {

			@BeforeEach
			void innerInit() {
				innerBeforeInvoked = true;
			}

			@Test
			void innerTest() {
				assertTrue(beforeInvoked, "beforeEach of parent context was not invoked");
				assertTrue(innerBeforeInvoked, "beforeEach of nested test was not invoked");
			}

			@Nested
			class InnerInnerTestCase {

				@Test
				void innerInnerTest() {
					assertTrue(beforeInvoked, "beforeEach of parent context was not invoked");
				}
			}
		}
	}

}
