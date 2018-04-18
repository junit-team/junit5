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
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.platform.engine.discovery.DiscoverySelectors.selectClass;
import static org.junit.platform.launcher.core.LauncherDiscoveryRequestBuilder.request;

import java.util.HashMap;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestReporter;
import org.junit.platform.commons.util.PreconditionViolationException;
import org.junit.platform.launcher.LauncherDiscoveryRequest;
import org.junit.platform.testkit.ExecutionGraph;

/**
 * @since 5.0
 */
class ReportingTests extends AbstractJupiterTestEngineTests {

	@Test
	void threeReportEntriesArePublished() {
		LauncherDiscoveryRequest request = request().selectors(selectClass(MyReportingTestCase.class)).build();

		ExecutionGraph executionGraph = executeTests(request).getExecutionGraph();

		assertEquals(2, executionGraph.getTestStartedCount(), "# tests started");
		assertEquals(2, executionGraph.getTestSuccessfulCount(), "# tests succeeded");
		assertEquals(0, executionGraph.getTestFailedCount(), "# tests failed");
		assertEquals(6, executionGraph.getReportingEntryPublishedCount(), "# report entries published");
	}

	static class MyReportingTestCase {

		@BeforeEach
		void before(TestReporter reporter) {
			reporter.publishEntry(new HashMap<>());
		}

		@AfterEach
		void after(TestReporter reporter) {
			reporter.publishEntry(new HashMap<>());
		}

		@Test
		void succeedingTest(TestReporter reporter) {
			reporter.publishEntry(new HashMap<>());
			reporter.publishEntry("userName", "dk38");
		}

		@Test
		void testWithNullReportData(TestReporter reporter) {
			assertThrows(PreconditionViolationException.class, () -> reporter.publishEntry(null));
		}

	}

}
