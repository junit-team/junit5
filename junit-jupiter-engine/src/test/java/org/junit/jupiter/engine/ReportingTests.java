/*
 * Copyright 2015-2024 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * https://www.eclipse.org/legal/epl-v20.html
 */

package org.junit.jupiter.engine;

import static java.util.Collections.emptyMap;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestReporter;
import org.junit.platform.commons.PreconditionViolationException;

/**
 * @since 5.0
 */
class ReportingTests extends AbstractJupiterTestEngineTests {

	@Test
	void reportEntriesArePublished() {
		executeTestsForClass(MyReportingTestCase.class).testEvents().assertStatistics(stats -> stats //
				.started(2) //
				.succeeded(2) //
				.failed(0) //
				.reportingEntryPublished(7));
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
