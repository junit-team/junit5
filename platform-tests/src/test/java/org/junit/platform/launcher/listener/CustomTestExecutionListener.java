/*
 * Copyright 2015-2016 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.junit.platform.launcher.listener;

import org.junit.platform.engine.TestExecutionResult;
import org.junit.platform.engine.reporting.ReportEntry;
import org.junit.platform.launcher.TestExecutionListener;
import org.junit.platform.launcher.TestIdentifier;
import org.junit.platform.launcher.TestPlan;

public class CustomTestExecutionListener implements TestExecutionListener {

	public int testPlanExecutionStarted;
	public int testPlanExecutionFinished;
	public int dynamicTestRegistered;
	public int executionSkipped;
	public int executionStarted;
	public int executionFinished;
	public int reportingEntryPublished;

	public CustomTestExecutionListener() {
		// ServiceLoader needs no-arg constructor.
	}

	@Override
	public void testPlanExecutionStarted(TestPlan testPlan) {
		testPlanExecutionStarted++;
	}

	@Override
	public void testPlanExecutionFinished(TestPlan testPlan) {
		testPlanExecutionFinished++;
	}

	@Override
	public void dynamicTestRegistered(TestIdentifier testIdentifier) {
		dynamicTestRegistered++;
	}

	@Override
	public void executionSkipped(TestIdentifier testIdentifier, String reason) {
		executionSkipped++;
	}

	@Override
	public void executionStarted(TestIdentifier testIdentifier) {
		executionStarted++;
	}

	@Override
	public void executionFinished(TestIdentifier testIdentifier, TestExecutionResult testExecutionResult) {
		executionFinished++;
	}

	@Override
	public void reportingEntryPublished(TestIdentifier testIdentifier, ReportEntry entry) {
		reportingEntryPublished++;
	}
}
