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

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.junit.gen5.commons.util.ExceptionUtils;
import org.junit.gen5.engine.TestExecutionResult;
import org.junit.gen5.launcher.TestIdentifier;
import org.junit.gen5.launcher.TestPlan;

class XmlReportData {

	private static final int MILLIS_PER_SECOND = 1000;

	private final Map<TestIdentifier, TestExecutionResult> finishedTests = new ConcurrentHashMap<>();
	private final Map<TestIdentifier, String> skippedTests = new ConcurrentHashMap<>();
	private final Map<TestIdentifier, Instant> startInstants = new ConcurrentHashMap<>();
	private final Map<TestIdentifier, Instant> endInstants = new ConcurrentHashMap<>();

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
		startInstants.put(testIdentifier, clock.instant());
	}

	void markFinished(TestIdentifier testIdentifier, TestExecutionResult result) {
		endInstants.put(testIdentifier, clock.instant());
		if (result.getStatus() == ABORTED) {
			String reason = result.getThrowable().map(ExceptionUtils::readStackTrace).orElse("");
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
		Instant startInstant = startInstants.getOrDefault(testIdentifier, Instant.EPOCH);
		Instant endInstant = endInstants.getOrDefault(testIdentifier, startInstant);
		return Duration.between(startInstant, endInstant).toMillis() / (double) MILLIS_PER_SECOND;
	}

	String getSkipReason(TestIdentifier test) {
		return skippedTests.get(test);
	}

	TestExecutionResult getResult(TestIdentifier testIdentifier) {
		return finishedTests.get(testIdentifier);
	}

}
