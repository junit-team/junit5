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

import java.util.Map;

import org.junit.gen5.engine.EngineExecutionListener;
import org.junit.gen5.engine.TestDescriptor;
import org.junit.gen5.engine.TestExecutionResult;
import org.junit.gen5.launcher.TestExecutionListener;
import org.junit.gen5.launcher.TestId;
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
	public void reportingEntryPublished(TestDescriptor testDescriptor, Map<String, String> entry) {
		testExecutionListener.reportingEntryPublished(getTestIdentifier(testDescriptor), entry);
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

	private TestIdentifier getTestIdentifier(TestDescriptor testDescriptor) {
		return testPlan.getTestIdentifier(new TestId(testDescriptor.getUniqueId()));
	}
}
