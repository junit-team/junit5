/*
 * Copyright 2015-2016 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.junit.gen5.console.tasks;

import static org.junit.gen5.engine.TestExecutionResult.Status.ABORTED;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.time.Clock;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.junit.gen5.engine.TestExecutionResult;
import org.junit.gen5.launcher.TestIdentifier;
import org.junit.gen5.launcher.TestPlan;

class XmlReportData {

	private static final int MILLIS_PER_SECOND = 1000;

	private final Map<TestIdentifier, TestExecutionResult> finishedTests = new ConcurrentHashMap<>();
	private final Map<TestIdentifier, String> skippedTests = new ConcurrentHashMap<>();
	private final Map<TestIdentifier, Long> startTimes = new ConcurrentHashMap<>();
	private final Map<TestIdentifier, Long> endTimes = new ConcurrentHashMap<>();

	private final TestPlan testPlan;
	private final Clock clock;

	XmlReportData(TestPlan testPlan, Clock clock) {
		this.testPlan = testPlan;
		this.clock = clock;
	}

	TestPlan getTestPlan() {
		return testPlan;
	}

	void markSkipped(TestIdentifier testIdentifier, String reason) {
		skippedTests.put(testIdentifier, reason == null ? "" : reason);
	}

	void markStarted(TestIdentifier testIdentifier) {
		startTimes.put(testIdentifier, clock.millis());
	}

	void markFinished(TestIdentifier testIdentifier, TestExecutionResult result) {
		endTimes.put(testIdentifier, clock.millis());
		if (result.getStatus() == ABORTED) {
			String reason = result.getThrowable().map(XmlReportData::readStackTrace).orElse("");
			skippedTests.put(testIdentifier, reason);
		}
		else {
			finishedTests.put(testIdentifier, result);
		}
	}

	boolean wasSkipped(TestIdentifier testIdentifier) {
		return skippedTests.containsKey(testIdentifier);
	}

	boolean wasFinished(TestIdentifier testIdentifier) {
		return finishedTests.containsKey(testIdentifier);
	}

	double getDurationInSeconds(TestIdentifier testIdentifier) {
		long startMillis = startTimes.getOrDefault(testIdentifier, 0L);
		long endMillis = endTimes.getOrDefault(testIdentifier, startMillis);
		return (endMillis - startMillis) / (double) MILLIS_PER_SECOND;
	}

	String getSkipReason(TestIdentifier test) {
		return skippedTests.get(test);
	}

	TestExecutionResult getResult(TestIdentifier testIdentifier) {
		return finishedTests.get(testIdentifier);
	}

	// TODO #86 Move to ExceptionUtils
	static String readStackTrace(Throwable throwable) {
		StringWriter stringWriter = new StringWriter();
		try (PrintWriter printWriter = new PrintWriter(stringWriter)) {
			throwable.printStackTrace(printWriter);
		}
		return stringWriter.toString();
	}

}
