/*
 * Copyright 2015 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.junit.gen5.junit4runner;

import org.junit.gen5.engine.TestDescriptor;
import org.junit.gen5.launcher.TestPlan;
import org.junit.gen5.launcher.TestPlanExecutionListener;
import org.junit.runner.Description;
import org.junit.runner.Result;
import org.junit.runner.notification.Failure;
import org.junit.runner.notification.RunNotifier;

class JUnit5RunnerListener implements TestPlanExecutionListener {

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
	public void dynamicTestFound(TestDescriptor testDescriptor) {
		System.out.println("JUnit5 test runner cannot handle dynamic tests");
	}

	@Override
	public void testStarted(TestDescriptor testDescriptor) {
		Description description = findJUnit4Description(testDescriptor);
		notifier.fireTestStarted(description);
	}

	@Override
	public void testSkipped(TestDescriptor testDescriptor, Throwable t) {
		Description description = findJUnit4Description(testDescriptor);
		// TODO We call this after calling fireTestStarted. This leads to a wrong test
		// count in Eclipse.
		notifier.fireTestIgnored(description);
		notifier.fireTestFinished(description);
	}

	@Override
	public void testAborted(TestDescriptor testDescriptor, Throwable t) {
		Description description = findJUnit4Description(testDescriptor);
		notifier.fireTestAssumptionFailed(new Failure(description, t));
		notifier.fireTestFinished(description);
	}

	@Override
	public void testFailed(TestDescriptor testDescriptor, Throwable t) {
		Description description = findJUnit4Description(testDescriptor);
		notifier.fireTestFailure(new Failure(description, t));
		notifier.fireTestFinished(description);
	}

	@Override
	public void testSucceeded(TestDescriptor testDescriptor) {
		Description description = findJUnit4Description(testDescriptor);
		notifier.fireTestFinished(description);
	}

	private Description findJUnit4Description(TestDescriptor testDescriptor) {
		Description description = testTree.getDescription(testDescriptor);
		if (description == null) {
			description = testTree.addDescriptionFor(testDescriptor);
		}
		return description;
	}

}
