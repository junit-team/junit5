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

import static org.junit.gen5.api.Assertions.assertTrue;
import static org.junit.gen5.engine.TestPlanSpecification.build;
import static org.junit.gen5.engine.TestPlanSpecification.forClass;

import org.junit.Assert;
import org.junit.Ignore;
import org.junit.gen5.api.AfterEach;
import org.junit.gen5.api.BeforeEach;
import org.junit.gen5.api.Nested;
import org.junit.gen5.api.Test;
import org.junit.gen5.engine.TestPlanSpecification;

/**
 * Integration tests that verify support for {@linkplain Nested nested contexts}
 * in the {@link JUnit5TestEngine}.
 *
 * @author Sam Brannen
 * @since 5.0
 */
@Ignore("https://github.com/junit-team/junit-lambda/issues/39")
public class NestedTestClassesTests extends AbstractJUnit5TestEngineTestCase {

	@org.junit.Test
	public void executeTestCaseWithNestedTests() {
		TestCaseWithNesting.countAfterInvoked = 0;

		TestPlanSpecification spec = build(forClass(TestCaseWithNesting.class));
		TrackingTestExecutionListener listener = executeTests(spec, 6);

		Assert.assertEquals("# tests started", 3, listener.testStartedCount.get());
		Assert.assertEquals("# tests succeeded", 3, listener.testSucceededCount.get());
		Assert.assertEquals("# tests skipped", 0, listener.testSkippedCount.get());
		Assert.assertEquals("# tests aborted", 0, listener.testAbortedCount.get());
		Assert.assertEquals("# tests failed", 0, listener.testFailedCount.get());

		Assert.assertEquals("# after calls", 3, TestCaseWithNesting.countAfterInvoked);
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
