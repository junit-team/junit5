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
import static org.junit.jupiter.api.Assertions.fail;
import static org.junit.platform.engine.discovery.DiscoverySelectors.selectClass;
import static org.junit.platform.launcher.core.LauncherDiscoveryRequestBuilder.request;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.platform.launcher.LauncherDiscoveryRequest;
import org.junit.platform.testkit.ExecutionGraph;

/**
 * Integration tests that verify support for {@link Disabled @Disabled} in the {@link JupiterTestEngine}.
 *
 * @since 5.0
 */
class DisabledTests extends AbstractJupiterTestEngineTests {

	@Test
	void executeTestsWithDisabledTestClass() {
		LauncherDiscoveryRequest request = request().selectors(selectClass(DisabledTestClassTestCase.class)).build();
		ExecutionGraph executionGraph = executeTests(request).getExecutionGraph();

		assertEquals(1, executionGraph.getContainerSkippedCount(), "# container skipped");
		assertEquals(0, executionGraph.getTestStartedCount(), "# tests started");
	}

	@Test
	void executeTestsWithDisabledTestMethods() throws Exception {
		LauncherDiscoveryRequest request = request().selectors(selectClass(DisabledTestMethodsTestCase.class)).build();
		ExecutionGraph executionGraph = executeTests(request).getExecutionGraph();

		assertEquals(1, executionGraph.getTestStartedCount(), "# tests started");
		assertEquals(1, executionGraph.getTestSuccessfulCount(), "# tests succeeded");
		assertEquals(1, executionGraph.getTestSkippedCount(), "# tests skipped");

		String method = DisabledTestMethodsTestCase.class.getDeclaredMethod("disabledTest").toString();
		String reason = executionGraph.getSkippedTestEvents().get(0).getPayload(String.class).get();
		assertEquals(method + " is @Disabled", reason);
	}

	// -------------------------------------------------------------------

	@Disabled
	static class DisabledTestClassTestCase {

		@Test
		void disabledTest() {
			fail("this should be @Disabled");
		}
	}

	static class DisabledTestMethodsTestCase {

		@Test
		void enabledTest() {
		}

		@Test
		@Disabled
		void disabledTest() {
			fail("this should be @Disabled");
		}

	}

}
