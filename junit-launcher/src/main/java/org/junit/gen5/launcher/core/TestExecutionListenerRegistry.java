/*
 * Copyright 2015-2016 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.junit.gen5.launcher.core;

import java.util.LinkedList;
import java.util.List;
import java.util.function.Consumer;

import org.junit.gen5.engine.TestExecutionResult;
import org.junit.gen5.engine.reporting.ReportEntry;
import org.junit.gen5.launcher.TestExecutionListener;
import org.junit.gen5.launcher.TestIdentifier;
import org.junit.gen5.launcher.TestPlan;

/**
 * @since 5.0
 */
class TestExecutionListenerRegistry {

	private final List<TestExecutionListener> testExecutionListeners = new LinkedList<>();

	void registerListeners(TestExecutionListener... listeners) {
		for (TestExecutionListener listener : listeners) {
			this.testExecutionListeners.add(listener);
		}
	}

	private void notifyTestExecutionListeners(Consumer<TestExecutionListener> consumer) {
		this.testExecutionListeners.forEach(consumer);
	}

	TestExecutionListener getCompositeTestExecutionListener() {
		return new CompositeTestExecutionListener();
	}

	private class CompositeTestExecutionListener implements TestExecutionListener {

		@Override
		public void dynamicTestRegistered(TestIdentifier testIdentifier) {
			notifyTestExecutionListeners(listener -> listener.dynamicTestRegistered(testIdentifier));
		}

		@Override
		public void executionSkipped(TestIdentifier testIdentifier, String reason) {
			notifyTestExecutionListeners(listener -> listener.executionSkipped(testIdentifier, reason));
		}

		@Override
		public void executionStarted(TestIdentifier testIdentifier) {
			notifyTestExecutionListeners(listener -> listener.executionStarted(testIdentifier));
		}

		@Override
		public void executionFinished(TestIdentifier testIdentifier, TestExecutionResult testExecutionResult) {
			notifyTestExecutionListeners(listener -> listener.executionFinished(testIdentifier, testExecutionResult));
		}

		@Override
		public void testPlanExecutionStarted(TestPlan testPlan) {
			notifyTestExecutionListeners(listener -> listener.testPlanExecutionStarted(testPlan));
		}

		@Override
		public void testPlanExecutionFinished(TestPlan testPlan) {
			notifyTestExecutionListeners(listener -> listener.testPlanExecutionFinished(testPlan));
		}

		@Override
		public void reportingEntryPublished(TestIdentifier testIdentifier, ReportEntry entry) {
			notifyTestExecutionListeners(listener -> listener.reportingEntryPublished(testIdentifier, entry));
		}

	}

}
