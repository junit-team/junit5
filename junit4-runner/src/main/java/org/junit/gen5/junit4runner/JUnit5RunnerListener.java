/*
 * Copyright 2015-2016 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.junit.gen5.junit4runner;

import static org.junit.gen5.engine.TestExecutionResult.Status.*;

import org.junit.gen5.engine.TestExecutionResult;
import org.junit.gen5.launcher.TestExecutionListener;
import org.junit.gen5.launcher.TestIdentifier;
import org.junit.gen5.launcher.TestPlan;
import org.junit.runner.Description;
import org.junit.runner.Result;
import org.junit.runner.notification.Failure;
import org.junit.runner.notification.RunNotifier;

class JUnit5RunnerListener implements TestExecutionListener {

	private final JUnit5TestTree testTree;
	private final RunNotifier notifier;
	private final Result result;

	JUnit5RunnerListener(JUnit5TestTree testTree, RunNotifier notifier, Result result) {
		this.testTree = testTree;
		this.notifier = notifier;
		this.result = result;
	}

	@Override
	public void testPlanExecutionStarted(TestPlan testPlan) {
		notifier.fireTestRunStarted(testTree.getSuiteDescription());
	}

	@Override
	public void testPlanExecutionFinished(TestPlan testPlan) {
		notifier.fireTestRunFinished(result);
	}

	@Override
	public void dynamicTestRegistered(TestIdentifier testIdentifier) {
		System.out.println("JUnit5 test runner cannot handle dynamic tests");
	}

	@Override
	public void executionSkipped(TestIdentifier testIdentifier, String reason) {
		Description description = findJUnit4Description(testIdentifier);
		notifier.fireTestIgnored(description);
		notifier.fireTestFinished(description);
	}

	@Override
	public void executionStarted(TestIdentifier testIdentifier) {
		Description description = findJUnit4Description(testIdentifier);
		notifier.fireTestStarted(description);
	}

	@Override
	public void executionFinished(TestIdentifier testIdentifier, TestExecutionResult testExecutionResult) {
		Description description = findJUnit4Description(testIdentifier);
		if (testExecutionResult.getStatus() == ABORTED) {
			notifier.fireTestAssumptionFailed(toFailure(testExecutionResult, description));
		}
		else if (testExecutionResult.getStatus() == FAILED) {
			notifier.fireTestFailure(toFailure(testExecutionResult, description));
		}
		notifier.fireTestFinished(description);
	}

	private Failure toFailure(TestExecutionResult testExecutionResult, Description description) {
		return new Failure(description, testExecutionResult.getThrowable().orElse(null));
	}

	private Description findJUnit4Description(TestIdentifier testIdentifier) {
		return testTree.getDescription(testIdentifier);
	}

}
