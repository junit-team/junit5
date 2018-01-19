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
import org.junit.platform.engine.test.event.ExecutionEvent;
import org.junit.platform.engine.test.event.ExecutionEventRecorder;
import org.junit.platform.launcher.LauncherDiscoveryRequest;
import org.junit.platform.launcher.TestIdentifier;

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
		ExecutionEventRecorder eventRecorder1 = executeTests(request);

		// @formatter:off
		assertAll(
				() -> assertEquals(2, eventRecorder1.getTestStartedCount(), "# tests started"),
				() -> assertEquals(2, eventRecorder1.getTestSuccessfulCount(), "# tests succeeded"),
				() -> assertEquals(0, eventRecorder1.getTestFailedCount(), "# tests failed"));
		// @formatter:on

		Optional<ExecutionEvent> first = eventRecorder1.getSuccessfulTestFinishedEvents().stream().filter(
			event -> event.getTestDescriptor().getUniqueId().toString().contains(TestInfo.class.getName())).findFirst();
		assertTrue(first.isPresent());
		TestIdentifier testIdentifier = TestIdentifier.from(first.get().getTestDescriptor());
		String uniqueId = testIdentifier.getUniqueId();

		request = request().selectors(selectUniqueId(uniqueId)).build();
		ExecutionEventRecorder eventRecorder2 = executeTests(request);

		// @formatter:off
		assertAll(
				() -> assertEquals(1, eventRecorder2.getTestStartedCount(), "# tests started"),
				() -> assertEquals(1, eventRecorder2.getTestSuccessfulCount(), "# tests succeeded"),
				() -> assertEquals(0, eventRecorder2.getTestFailedCount(), "# tests failed"));
		// @formatter:on

		first = eventRecorder2.getSuccessfulTestFinishedEvents().stream().filter(
			event -> event.getTestDescriptor().getUniqueId().toString().contains(TestInfo.class.getName())).findFirst();
		assertTrue(first.isPresent());
	}

	@Test
	void executeTestCaseWithOverloadedMethodsWithSingleMethodThatAcceptsArgumentsSelectedByFullyQualifedMethodName() {
		String fqmn = TestCase.class.getName() + "#test(" + TestInfo.class.getName() + ")";
		LauncherDiscoveryRequest request = request().selectors(selectMethod(fqmn)).build();
		ExecutionEventRecorder eventRecorder = executeTests(request);

		// @formatter:off
		assertAll(
				() -> assertEquals(1, eventRecorder.getTestStartedCount(), "# tests started"),
				() -> assertEquals(1, eventRecorder.getTestSuccessfulCount(), "# tests succeeded"),
				() -> assertEquals(0, eventRecorder.getTestFailedCount(), "# tests failed"));
		// @formatter:on

		Optional<ExecutionEvent> first = eventRecorder.getSuccessfulTestFinishedEvents().stream().filter(
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
