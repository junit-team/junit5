/*
 * Copyright 2015-2016 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.junit.gen5.launcher.main;

import org.junit.gen5.engine.EngineExecutionListener;
import org.junit.gen5.engine.TestDescriptor;
import org.junit.gen5.engine.TestExecutionResult;
import org.junit.gen5.engine.reporting.ReportEntry;
import org.junit.gen5.launcher.TestExecutionListener;
import org.junit.gen5.launcher.TestIdentifier;
import org.junit.gen5.launcher.TestPlan;

class ExecutionListenerAdapter implements EngineExecutionListener {

	private final TestPlan testPlan;
	private final TestExecutionListener testExecutionListener;

	public ExecutionListenerAdapter(TestPlan testPlan, TestExecutionListener testExecutionListener) {
		this.testPlan = testPlan;
		this.testExecutionListener = testExecutionListener;
	}

	@Override
	public void dynamicTestRegistered(TestDescriptor testDescriptor) {
		TestIdentifier testIdentifier = TestIdentifier.from(testDescriptor);
		testPlan.add(testIdentifier);
		testExecutionListener.dynamicTestRegistered(testIdentifier);
	}

	@Override
	public void executionStarted(TestDescriptor testDescriptor) {
		testExecutionListener.executionStarted(getTestIdentifier(testDescriptor));
	}

	@Override
	public void executionSkipped(TestDescriptor testDescriptor, String reason) {
		testExecutionListener.executionSkipped(getTestIdentifier(testDescriptor), reason);
	}

	@Override
	public void executionFinished(TestDescriptor testDescriptor, TestExecutionResult testExecutionResult) {
		testExecutionListener.executionFinished(getTestIdentifier(testDescriptor), testExecutionResult);
	}

	@Override
	public void reportingEntryPublished(TestDescriptor testDescriptor, ReportEntry entry) {
		testExecutionListener.reportingEntryPublished(getTestIdentifier(testDescriptor), entry);
	}

	private TestIdentifier getTestIdentifier(TestDescriptor testDescriptor) {
		return testPlan.getTestIdentifier(testDescriptor.getUniqueId().toString());
	}

}
