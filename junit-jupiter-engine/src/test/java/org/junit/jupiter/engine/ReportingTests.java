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

import static java.util.Collections.emptyMap;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.platform.engine.discovery.DiscoverySelectors.selectClass;
import static org.junit.platform.launcher.core.LauncherDiscoveryRequestBuilder.request;

import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestReporter;
import org.junit.platform.commons.util.PreconditionViolationException;
import org.junit.platform.engine.test.event.ExecutionEventRecorder;
import org.junit.platform.launcher.LauncherDiscoveryRequest;

/**
 * @since 5.0
 */
class ReportingTests extends AbstractJupiterTestEngineTests {

	@Test
	void reportEntriesArePublished() {
		LauncherDiscoveryRequest request = request().selectors(selectClass(MyReportingTestCase.class)).build();

		ExecutionEventRecorder eventRecorder = executeTests(request);

		assertEquals(2, eventRecorder.getTestStartedCount(), "# tests started");
		assertEquals(2, eventRecorder.getTestSuccessfulCount(), "# tests succeeded");
		assertEquals(0, eventRecorder.getTestFailedCount(), "# tests failed");

		assertEquals(7, eventRecorder.getReportingEntryPublishedCount(), "# report entries published");
	}

	static class MyReportingTestCase {

		@BeforeEach
		void beforeEach(TestReporter reporter) {
			reporter.publishEntry("@BeforeEach");
		}

		@AfterEach
		void afterEach(TestReporter reporter) {
			reporter.publishEntry("@AfterEach");
		}

		@Test
		void succeedingTest(TestReporter reporter) {
			reporter.publishEntry(emptyMap());
			reporter.publishEntry("user name", "dk38");
			reporter.publishEntry("message");
		}

		@Test
		void invalidReportData(TestReporter reporter) {

			// Maps
			Map<String, String> map = new HashMap<>();

			map.put("key", null);
			assertThrows(PreconditionViolationException.class, () -> reporter.publishEntry(map));

			map.clear();
			map.put(null, "value");
			assertThrows(PreconditionViolationException.class, () -> reporter.publishEntry(map));

			assertThrows(PreconditionViolationException.class, () -> reporter.publishEntry((Map<String, String>) null));

			// Key-Value pair
			assertThrows(PreconditionViolationException.class, () -> reporter.publishEntry(null, "bar"));
			assertThrows(PreconditionViolationException.class, () -> reporter.publishEntry("foo", null));

			// Value
			assertThrows(PreconditionViolationException.class, () -> reporter.publishEntry((String) null));
		}

	}

}
