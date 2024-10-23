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
import static org.junit.jupiter.engine.Constants.DEFAULT_TEST_INSTANCE_LIFECYCLE_PROPERTY_NAME;
import static org.junit.platform.engine.discovery.DiscoverySelectors.selectClass;
import static org.junit.platform.launcher.core.LauncherDiscoveryRequestBuilder.request;

import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance.Lifecycle;
import org.junit.jupiter.api.TestReporter;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.platform.commons.PreconditionViolationException;

/**
 * @since 5.0
 */
class ReportingTests extends AbstractJupiterTestEngineTests {

	@ParameterizedTest
	@CsvSource(textBlock = """
			PER_CLASS,  7
			PER_METHOD, 9
			""")
	void reportEntriesArePublished(Lifecycle lifecycle, int expectedReportEntryCount) {
		var request = request() //
				.selectors(selectClass(MyReportingTestCase.class)) //
				.configurationParameter(DEFAULT_TEST_INSTANCE_LIFECYCLE_PROPERTY_NAME, lifecycle.name());
		executeTests(request) //
				.testEvents() //
				.assertStatistics(stats -> stats //
						.started(2) //
						.succeeded(2) //
						.failed(0) //
						.reportingEntryPublished(expectedReportEntryCount));
	}

	@SuppressWarnings("JUnitMalformedDeclaration")
	static class MyReportingTestCase {

		public MyReportingTestCase(TestReporter reporter) {
			// Reported on class-level for PER_CLASS lifecycle and on method-level for PER_METHOD lifecycle
			reporter.publishEntry("Constructor");
		}

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
