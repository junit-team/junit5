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

import org.junit.gen5.engine.TestDescriptor;
import org.junit.gen5.engine.TestExecutionListener;
import org.junit.gen5.launcher.TestPlan;
import org.junit.gen5.launcher.TestPlanExecutionListener;

/**
 * @author Stefan Bechtold
 * @author Sam Brannen
 * @since 5.0
 */
public class SummaryCreatingTestListener implements TestPlanExecutionListener, TestExecutionListener {

	private final TestExecutionSummary summary;

	public SummaryCreatingTestListener(TestExecutionSummary summary) {
		this.summary = summary;
	}

	@Override
	public void testPlanExecutionStarted(TestPlan testPlan) {
		summary.testsFound.set(testPlan.countStaticTests());
		summary.timeStarted = System.currentTimeMillis();
	}

	@Override
	public void testPlanExecutionPaused(TestPlan testPlan) {
		summary.timePaused = System.currentTimeMillis();
	}

	@Override
	public void testPlanExecutionRestarted(TestPlan testPlan) {
		summary.timeStarted += System.currentTimeMillis() - summary.timePaused;
		summary.timePaused = 0;
	}

	@Override
	public void testPlanExecutionStopped(TestPlan testPlan) {
		summary.finishTestRun("Test run stopped");
	}

	@Override
	public void testPlanExecutionFinished(TestPlan testPlan) {
		summary.finishTestRun("Test run finished");
	}

	@Override
	public void dynamicTestFound(TestDescriptor testDescriptor) {
		summary.testsFound.incrementAndGet();
	}

	@Override
	public void testStarted(TestDescriptor testDescriptor) {
		summary.testsStarted.incrementAndGet();
	}

	@Override
	public void testSkipped(TestDescriptor testDescriptor, Throwable t) {
		summary.testsSkipped.addAndGet(testDescriptor.countStaticTests());
	}

	@Override
	public void testAborted(TestDescriptor testDescriptor, Throwable t) {
		summary.testsAborted.incrementAndGet();
	}

	@Override
	public void testFailed(TestDescriptor testDescriptor, Throwable t) {
		summary.testsFailed.incrementAndGet();
		summary.addFailure(testDescriptor, t);
	}

	@Override
	public void testSucceeded(TestDescriptor testDescriptor) {
		summary.testsSucceeded.incrementAndGet();
	}

}
