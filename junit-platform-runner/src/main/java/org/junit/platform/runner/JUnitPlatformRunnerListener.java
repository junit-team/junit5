/*
 * Copyright 2015-2024 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * https://www.eclipse.org/legal/epl-v20.html
 */

package org.junit.platform.runner;

import static org.junit.platform.engine.TestExecutionResult.Status.ABORTED;
import static org.junit.platform.engine.TestExecutionResult.Status.FAILED;

import org.junit.platform.engine.TestExecutionResult;
import org.junit.platform.engine.TestExecutionResult.Status;
import org.junit.platform.engine.UniqueId;
import org.junit.platform.engine.reporting.ReportEntry;
import org.junit.platform.launcher.TestExecutionListener;
import org.junit.platform.launcher.TestIdentifier;
import org.junit.runner.Description;
import org.junit.runner.notification.Failure;
import org.junit.runner.notification.RunNotifier;

/**
 * @since 1.0
 */
class JUnitPlatformRunnerListener implements TestExecutionListener {

	private final JUnitPlatformTestTree testTree;
	private final RunNotifier notifier;

	JUnitPlatformRunnerListener(JUnitPlatformTestTree testTree, RunNotifier notifier) {
		this.testTree = testTree;
		this.notifier = notifier;
	}

	@Override
	public void dynamicTestRegistered(TestIdentifier testIdentifier) {
		UniqueId parentId = testIdentifier.getParentIdObject().get();
		testTree.addDynamicDescription(testIdentifier, parentId);
	}

	@Override
	public void executionSkipped(TestIdentifier testIdentifier, String reason) {
		if (testIdentifier.isTest()) {
			fireTestIgnored(testIdentifier);
		}
		else {
			testTree.getTestsInSubtree(testIdentifier).forEach(this::fireTestIgnored);
		}
	}

	private void fireTestIgnored(TestIdentifier testIdentifier) {
		Description description = findJUnit4Description(testIdentifier);
		this.notifier.fireTestIgnored(description);
	}

	@Override
	public void executionStarted(TestIdentifier testIdentifier) {
		Description description = findJUnit4Description(testIdentifier);
		if (description.isTest()) {
			this.notifier.fireTestStarted(description);
		}
	}

	@Override
	public void executionFinished(TestIdentifier testIdentifier, TestExecutionResult testExecutionResult) {
		Description description = findJUnit4Description(testIdentifier);
		Status status = testExecutionResult.getStatus();
		if (status == ABORTED) {
			this.notifier.fireTestAssumptionFailed(toFailure(testExecutionResult, description));
		}
		else if (status == FAILED) {
			this.notifier.fireTestFailure(toFailure(testExecutionResult, description));
		}
		if (description.isTest()) {
			this.notifier.fireTestFinished(description);
		}
	}

	@Override
	public void reportingEntryPublished(TestIdentifier testIdentifier, ReportEntry entry) {
		System.out.println(entry);
	}

	private Failure toFailure(TestExecutionResult testExecutionResult, Description description) {
		return new Failure(description, testExecutionResult.getThrowable().orElse(null));
	}

	private Description findJUnit4Description(TestIdentifier testIdentifier) {
		return this.testTree.getDescription(testIdentifier);
	}

}
