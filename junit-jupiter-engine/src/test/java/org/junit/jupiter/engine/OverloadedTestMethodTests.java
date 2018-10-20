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
import org.junit.platform.launcher.LauncherDiscoveryRequest;
import org.junit.platform.launcher.TestIdentifier;
import org.junit.platform.testkit.ExecutionEvent;
import org.junit.platform.testkit.ExecutionResults;

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
		ExecutionResults executionResults1 = executeTests(request).getExecutionResults();

		// @formatter:off
		assertAll(
				() -> assertEquals(2, executionResults1.getTestsStartedCount(), "# tests started"),
				() -> assertEquals(2, executionResults1.getTestsSuccessfulCount(), "# tests succeeded"),
				() -> assertEquals(0, executionResults1.getTestsFailedCount(), "# tests failed"));
		// @formatter:on

		Optional<ExecutionEvent> first = executionResults1.getTestsSuccessfulEvents().stream().filter(
			event -> event.getTestDescriptor().getUniqueId().toString().contains(TestInfo.class.getName())).findFirst();
		assertTrue(first.isPresent());
		TestIdentifier testIdentifier = TestIdentifier.from(first.get().getTestDescriptor());
		String uniqueId = testIdentifier.getUniqueId();

		request = request().selectors(selectUniqueId(uniqueId)).build();
		ExecutionResults executionResults2 = executeTests(request).getExecutionResults();

		// @formatter:off
		assertAll(
				() -> assertEquals(1, executionResults2.getTestsStartedCount(), "# tests started"),
				() -> assertEquals(1, executionResults2.getTestsSuccessfulCount(), "# tests succeeded"),
				() -> assertEquals(0, executionResults2.getTestsFailedCount(), "# tests failed"));
		// @formatter:on

		first = executionResults2.getTestsSuccessfulEvents().stream().filter(
			event -> event.getTestDescriptor().getUniqueId().toString().contains(TestInfo.class.getName())).findFirst();
		assertTrue(first.isPresent());
	}

	@Test
	void executeTestCaseWithOverloadedMethodsWithSingleMethodThatAcceptsArgumentsSelectedByFullyQualifedMethodName() {
		String fqmn = TestCase.class.getName() + "#test(" + TestInfo.class.getName() + ")";
		LauncherDiscoveryRequest request = request().selectors(selectMethod(fqmn)).build();
		ExecutionResults executionResults = executeTests(request).getExecutionResults();

		// @formatter:off
		assertAll(
				() -> assertEquals(1, executionResults.getTestsStartedCount(), "# tests started"),
				() -> assertEquals(1, executionResults.getTestsSuccessfulCount(), "# tests succeeded"),
				() -> assertEquals(0, executionResults.getTestsFailedCount(), "# tests failed"));
		// @formatter:on

		Optional<ExecutionEvent> first = executionResults.getTestsSuccessfulEvents().stream().filter(
			event -> event.getTestDescriptor().getUniqueId().toString().contains(TestInfo.class.getName())).findFirst();
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
