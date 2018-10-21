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

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.Assume;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.platform.testkit.ExecutionResults;

/**
 * Integration tests that verify support for failed assumptions in the
 * {@link JupiterTestEngine}.
 *
 * @since 5.4
 */
class FailedAssumptionsTests extends AbstractJupiterTestEngineTests {

	@Test
	void testAbortedExceptionInBeforeAll() {
		ExecutionResults executionResults = executeTestsForClass(TestAbortedExceptionInBeforeAllTestCase.class);

		assertEquals(1, executionResults.getContainersAbortedCount(), "# containers aborted");
		assertEquals(0, executionResults.getTestsStartedCount(), "# tests started");
	}

	@Test
	void assumptionViolatedExceptionInBeforeAll() {
		ExecutionResults executionResults = executeTestsForClass(AssumptionViolatedExceptionInBeforeAllTestCase.class);

		assertEquals(1, executionResults.getContainersAbortedCount(), "# containers aborted");
		assertEquals(0, executionResults.getTestsStartedCount(), "# tests started");
	}

	// -------------------------------------------------------------------

	static class TestAbortedExceptionInBeforeAllTestCase {

		@BeforeAll
		static void beforeAll() {
			Assumptions.assumeTrue(false);
		}

		@Test
		void test() {
		}
	}

	static class AssumptionViolatedExceptionInBeforeAllTestCase {

		@BeforeAll
		static void beforeAll() {
			Assume.assumeTrue(false);
		}

		@Test
		void test() {
		}
	}

}
