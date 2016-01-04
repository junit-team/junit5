/*
 * Copyright 2015-2016 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.junit.gen5.engine.junit5;

import java.util.HashMap;

import org.junit.Assert;
import org.junit.gen5.api.AfterEach;
import org.junit.gen5.api.BeforeEach;
import org.junit.gen5.api.Test;
import org.junit.gen5.engine.TestPlanSpecification;
import org.junit.gen5.engine.TrackingEngineExecutionListener;
import org.junit.gen5.engine.junit5.extension.TestReporter;

public class ReportingTest extends AbstractJUnit5TestEngineTests {

	@org.junit.Test
	public void threeReportEntriesArePublished() {
		TestPlanSpecification testPlanSpecification = TestPlanSpecification.build(
			TestPlanSpecification.forClass(MyReportingTestCase.class));

		TrackingEngineExecutionListener listener = executeTests(testPlanSpecification, 2);

		Assert.assertEquals("# tests started", 1, listener.testStartedCount.get());
		Assert.assertEquals("# tests succeeded", 1, listener.testSucceededCount.get());
		Assert.assertEquals("# tests failed", 0, listener.testFailedCount.get());

		Assert.assertEquals("# report entries published", 3, listener.reportEntriesCount.get());

	}

}

class MyReportingTestCase {

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
	}

}
