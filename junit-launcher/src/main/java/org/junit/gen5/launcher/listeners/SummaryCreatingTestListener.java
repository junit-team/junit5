/*
 * Copyright 2015 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.junit.gen5.launcher.listeners;

import static java.util.stream.Stream.concat;
import static org.junit.gen5.engine.TestExecutionResult.Status.*;

import java.util.stream.Stream;

import org.junit.gen5.engine.TestExecutionResult;
import org.junit.gen5.launcher.TestExecutionListener;
import org.junit.gen5.launcher.TestIdentifier;
import org.junit.gen5.launcher.TestPlan;

/**
 * @since 5.0
 */
public class SummaryCreatingTestListener implements TestExecutionListener {

	private TestPlan testPlan;
	private TestExecutionSummary summary;

	public TestExecutionSummary getSummary() {
		return summary;
	}

	@Override
	public void testPlanExecutionStarted(TestPlan testPlan) {
		this.testPlan = testPlan;
		this.summary = new TestExecutionSummary(testPlan);
		summary.testsFound.set(testPlan.countTestIdentifiers(TestIdentifier::isTest));
		summary.timeStarted = System.currentTimeMillis();
	}

	@Override
	public void testPlanExecutionFinished(TestPlan testPlan) {
		summary.finishTestRun("Test run finished");
	}

	@Override
	public void dynamicTestRegistered(TestIdentifier testIdentifier) {
		summary.testsFound.incrementAndGet();
	}

	@Override
	public void testSkipped(TestIdentifier testIdentifier, String reason) {
		// @formatter:off
		long skippedTests = concat(Stream.of(testIdentifier), testPlan.getDescendants(testIdentifier).stream())
				.filter(TestIdentifier::isTest)
				.count();
		// @formatter:on
		summary.testsSkipped.addAndGet(skippedTests);
	}

	@Override
	public void testStarted(TestIdentifier testIdentifier) {
		summary.testsStarted.incrementAndGet();
	}

	@Override
	public void testFinished(TestIdentifier testIdentifier, TestExecutionResult testExecutionResult) {
		if (testExecutionResult.getStatus() == SUCCESSFUL) {
			summary.testsSucceeded.incrementAndGet();
		}
		else if (testExecutionResult.getStatus() == ABORTED) {
			summary.testsAborted.incrementAndGet();
		}
		else if (testExecutionResult.getStatus() == FAILED) {
			summary.testsFailed.incrementAndGet();
			testExecutionResult.getThrowable().ifPresent(throwable -> summary.addFailure(testIdentifier, throwable));
		}
	}

}
