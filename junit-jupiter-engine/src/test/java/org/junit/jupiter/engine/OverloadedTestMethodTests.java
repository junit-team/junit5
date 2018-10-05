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
import org.junit.platform.testkit.ExecutionsResult;

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
		ExecutionsResult executionsResult1 = executeTests(request).getExecutionsResult();

		// @formatter:off
		assertAll(
				() -> assertEquals(2, executionsResult1.getTestStartedCount(), "# tests started"),
				() -> assertEquals(2, executionsResult1.getTestSuccessfulCount(), "# tests succeeded"),
				() -> assertEquals(0, executionsResult1.getTestFailedCount(), "# tests failed"));
		// @formatter:on

		Optional<ExecutionEvent> first = executionsResult1.getSuccessfulTestFinishedEvents().stream().filter(
			event -> event.getTestDescriptor().getUniqueId().toString().contains(TestInfo.class.getName())).findFirst();
		assertTrue(first.isPresent());
		TestIdentifier testIdentifier = TestIdentifier.from(first.get().getTestDescriptor());
		String uniqueId = testIdentifier.getUniqueId();

		request = request().selectors(selectUniqueId(uniqueId)).build();
		ExecutionsResult executionsResult2 = executeTests(request).getExecutionsResult();

		// @formatter:off
		assertAll(
				() -> assertEquals(1, executionsResult2.getTestStartedCount(), "# tests started"),
				() -> assertEquals(1, executionsResult2.getTestSuccessfulCount(), "# tests succeeded"),
				() -> assertEquals(0, executionsResult2.getTestFailedCount(), "# tests failed"));
		// @formatter:on

		first = executionsResult2.getSuccessfulTestFinishedEvents().stream().filter(
			event -> event.getTestDescriptor().getUniqueId().toString().contains(TestInfo.class.getName())).findFirst();
		assertTrue(first.isPresent());
	}

	@Test
	void executeTestCaseWithOverloadedMethodsWithSingleMethodThatAcceptsArgumentsSelectedByFullyQualifedMethodName() {
		String fqmn = TestCase.class.getName() + "#test(" + TestInfo.class.getName() + ")";
		LauncherDiscoveryRequest request = request().selectors(selectMethod(fqmn)).build();
		ExecutionsResult executionsResult = executeTests(request).getExecutionsResult();

		// @formatter:off
		assertAll(
				() -> assertEquals(1, executionsResult.getTestStartedCount(), "# tests started"),
				() -> assertEquals(1, executionsResult.getTestSuccessfulCount(), "# tests succeeded"),
				() -> assertEquals(0, executionsResult.getTestFailedCount(), "# tests failed"));
		// @formatter:on

		Optional<ExecutionEvent> first = executionsResult.getSuccessfulTestFinishedEvents().stream().filter(
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
