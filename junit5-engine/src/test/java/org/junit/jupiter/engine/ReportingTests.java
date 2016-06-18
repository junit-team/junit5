/*
 * Copyright 2015-2016 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.junit.jupiter.engine;

import static org.junit.gen5.engine.discovery.ClassSelector.selectClass;
import static org.junit.gen5.launcher.core.TestDiscoveryRequestBuilder.request;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.HashMap;

import org.junit.gen5.commons.util.PreconditionViolationException;
import org.junit.gen5.engine.test.event.ExecutionEventRecorder;
import org.junit.gen5.launcher.TestDiscoveryRequest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestReporter;

/**
 * @since 5.0
 */
public class ReportingTests extends AbstractJUnit5TestEngineTests {

	@Test
	void threeReportEntriesArePublished() {
		TestDiscoveryRequest request = request().selectors(selectClass(MyReportingTestCase.class)).build();

		ExecutionEventRecorder eventRecorder = executeTests(request);

		assertEquals(2, eventRecorder.getTestStartedCount(), "# tests started");
		assertEquals(2, eventRecorder.getTestSuccessfulCount(), "# tests succeeded");
		assertEquals(0, eventRecorder.getTestFailedCount(), "# tests failed");

		assertEquals(6, eventRecorder.getReportingEntryPublishedCount(), "# report entries published");
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
