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

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.platform.engine.discovery.DiscoverySelectors.selectClass;
import static org.junit.platform.engine.discovery.DiscoverySelectors.selectMethod;
import static org.junit.platform.engine.discovery.DiscoverySelectors.selectUniqueId;
import static org.junit.platform.launcher.core.LauncherDiscoveryRequestBuilder.request;

import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;
import org.junit.platform.engine.TestExecutionResult;
import org.junit.platform.launcher.LauncherDiscoveryRequest;
import org.junit.platform.launcher.TestIdentifier;
import org.junit.platform.tck.ExecutionGraph;
import org.junit.platform.tck.TestExecution;

/**
 * Integration tests for support of overloaded test methods in conjunction with
 * the {@link JupiterTestEngine}.
 *
 * @since 5.0
 */
class OverloadedTestMethodTests extends AbstractJupiterTestEngineTests {

	@Test
	void executeTestCaseWithOverloadedMethodsAndThenRerunOnlyOneOfTheMethodsSelectedByUniqueId() {
		LauncherDiscoveryRequest request = request().selectors(selectClass(TestCase.class)).build();
		ExecutionGraph executionGraph1 = executeTests(request).getExecutionGraph();

		// @formatter:off
		assertAll(
			() -> assertEquals(2, executionGraph1.getTestExecutionsFinished().size(), "# tests started"),
			() -> assertEquals(2,
					executionGraph1.getTestExecutionsFinished(TestExecutionResult.Status.SUCCESSFUL).size(),
					"# tests succeeded"),
			() -> assertEquals(0,
					executionGraph1.getTestExecutionsFinished(TestExecutionResult.Status.FAILED).size(),
					"# tests failed"));
		// @formatter:on

		Optional<TestExecution> first = executionGraph1.getTestExecutionsFinished(
			TestExecutionResult.Status.SUCCESSFUL).stream().filter(
				testExecution -> testExecution.getTestDescriptor().getUniqueId().toString().contains(
					TestInfo.class.getName())).findFirst();
		assertTrue(first.isPresent());
		TestIdentifier testIdentifier = TestIdentifier.from(first.get().getTestDescriptor());
		String uniqueId = testIdentifier.getUniqueId();

		request = request().selectors(selectUniqueId(uniqueId)).build();
		ExecutionGraph executionGraph2 = executeTests(request).getExecutionGraph();

		// @formatter:off
		assertAll(
			() -> assertEquals(1, executionGraph2.getTestExecutionsFinished().size(), "# tests started"),
			() -> assertEquals(1,
					executionGraph2.getTestExecutionsFinished(TestExecutionResult.Status.SUCCESSFUL).size(),
					"# tests succeeded"),
			() -> assertEquals(0,
					executionGraph2.getTestExecutionsFinished(TestExecutionResult.Status.FAILED).size(),
					"# tests failed"));
		// @formatter:on

		first = executionGraph2.getTestExecutionsFinished(TestExecutionResult.Status.SUCCESSFUL).stream().filter(
			testExecution -> testExecution.getTestDescriptor().getUniqueId().toString().contains(
				TestInfo.class.getName())).findFirst();
		assertTrue(first.isPresent());
	}

	@Test
	void executeTestCaseWithOverloadedMethodsWithSingleMethodThatAcceptsArgumentsSelectedByFullyQualifedMethodName() {
		String fqmn = TestCase.class.getName() + "#test(" + TestInfo.class.getName() + ")";
		LauncherDiscoveryRequest request = request().selectors(selectMethod(fqmn)).build();
		ExecutionGraph executionGraph = executeTests(request).getExecutionGraph();

		// @formatter:off
		assertAll(
			() -> assertEquals(1, executionGraph.getTestExecutionsFinished().size(), "# tests started"),
			() -> assertEquals(1,
					executionGraph.getTestExecutionsFinished(TestExecutionResult.Status.SUCCESSFUL).size(),
					"# tests succeeded"),
			() -> assertEquals(0,
					executionGraph.getTestExecutionsFinished(TestExecutionResult.Status.FAILED).size(),
					"# tests failed"));
		// @formatter:on

		Optional<TestExecution> first = executionGraph.getTestExecutionsFinished(
			TestExecutionResult.Status.SUCCESSFUL).stream().filter(
				testExecution -> testExecution.getTestDescriptor().getUniqueId().toString().contains(
					TestInfo.class.getName())).findFirst();
		assertTrue(first.isPresent());
	}

	static class TestCase {

		@Test
		void test() {
		}

		@Test
		void test(TestInfo testInfo) {
		}

	}

}
