/*
 * Copyright 2015-2019 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * https://www.eclipse.org/legal/epl-v20.html
 */

package org.junit.platform.launcher.core;

import org.junit.platform.engine.TestExecutionResult;
import org.junit.platform.engine.reporting.ReportEntry;
import org.junit.platform.launcher.TestExecutionListener;
import org.junit.platform.launcher.TestIdentifier;
import org.junit.platform.launcher.TestPlan;

public class ThrowingTestExecutionListener implements TestExecutionListener {
	@Override
	public void testPlanExecutionStarted(TestPlan testPlan) {
		throw new RuntimeException("failed to invoke listener");
	}

	@Override
	public void testPlanExecutionFinished(TestPlan testPlan) {
		throw new RuntimeException("failed to invoke listener");
	}

	@Override
	public void dynamicTestRegistered(TestIdentifier testIdentifier) {
		throw new RuntimeException("failed to invoke listener");
	}

	@Override
	public void executionStarted(TestIdentifier testIdentifier) {
		throw new RuntimeException("failed to invoke listener");
	}

	@Override
	public void executionSkipped(TestIdentifier testIdentifier, String reason) {
		throw new RuntimeException("failed to invoke listener");
	}

	@Override
	public void executionFinished(TestIdentifier testIdentifier, TestExecutionResult testExecutionResult) {
		throw new RuntimeException("failed to invoke listener");
	}

	@Override
	public void reportingEntryPublished(TestIdentifier testIdentifier, ReportEntry entry) {
		throw new RuntimeException("failed to invoke listener");
	}
}
