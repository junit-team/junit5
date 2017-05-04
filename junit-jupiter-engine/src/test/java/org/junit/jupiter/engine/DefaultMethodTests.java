/*
 * Copyright 2015-2017 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.junit.jupiter.engine;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.platform.engine.discovery.DiscoverySelectors.selectMethod;
import static org.junit.platform.launcher.core.LauncherDiscoveryRequestBuilder.request;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.platform.engine.test.event.ExecutionEventRecorder;
import org.junit.platform.launcher.LauncherDiscoveryRequest;

/**
 * Integration tests that verify support for selecting and executing default
 * methods from interfaces in conjunction with the {@link JupiterTestEngine}.
 *
 * @since 5.0
 */
class DefaultMethodTests extends AbstractJupiterTestEngineTests {

	private static boolean beforeAllInvoked = false;
	private static boolean afterAllInvoked = false;

	@Test
	void executeTestCaseWithDefaultMethodFromInterfaceSelectedByFullyQualifedMethodName() {
		String fqmn = TestCaseWithDefaultMethod.class.getName() + "#test";
		LauncherDiscoveryRequest request = request().selectors(selectMethod(fqmn)).build();
		ExecutionEventRecorder eventRecorder = executeTests(request);

		// @formatter:off
		assertAll(
				() -> assertEquals(1, eventRecorder.getTestStartedCount(), "# tests started"),
				() -> assertEquals(1, eventRecorder.getTestSuccessfulCount(), "# tests succeeded"),
				() -> assertEquals(0, eventRecorder.getTestFailedCount(), "# tests failed"),
				() -> assertTrue(beforeAllInvoked, "@BeforeAll invoked from interface"),
				() -> assertTrue(afterAllInvoked, "@AfterAll invoked from interface")
		);
		// @formatter:on
	}

	interface TestInterface {

		@BeforeAll
		static void beforeAll() {
			beforeAllInvoked = true;
		}

		@Test
		default void test() {
		}

		@AfterAll
		static void afterAll() {
			afterAllInvoked = true;
		}

	}

	static class TestCaseWithDefaultMethod implements TestInterface {
	}

}
