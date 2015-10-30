/*
 * Copyright 2015 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.junit.gen5.launcher;

import java.util.LinkedList;
import java.util.List;
import java.util.function.Consumer;

import org.junit.gen5.engine.TestDescriptor;
import org.junit.gen5.engine.TestExecutionListener;

/**
 * @author Stefan Bechtold
 * @author Sam Brannen
 * @since 5.0
 */
class TestListenerRegistry {

	private final List<TestPlanExecutionListener> testPlanExecutionListeners = new LinkedList<>();
	private final List<TestExecutionListener> testExecutionListeners = new LinkedList<>();

	void registerListener(TestExecutionListener... listeners) {
		for (TestExecutionListener listener : listeners) {
			this.testExecutionListeners.add(listener);
			if (listener instanceof TestPlanExecutionListener) {
				this.testPlanExecutionListeners.add((TestPlanExecutionListener) listener);
			}
		}
	}

	private void notifyTestPlanExecutionListeners(Consumer<TestPlanExecutionListener> consumer) {
		this.testPlanExecutionListeners.forEach(consumer);
	}

	private void notifyTestExecutionListeners(Consumer<TestExecutionListener> consumer) {
		this.testExecutionListeners.forEach(consumer);
	}

	TestPlanExecutionListener getCompositeTestPlanExecutionListener() {
		return new CompositeTestPlanExecutionListener();
	}

	TestExecutionListener getCompositeTestExecutionListener() {
		return new CompositeTestExecutionListener();
	}

	private class CompositeTestExecutionListener implements TestExecutionListener {

		@Override
		public void dynamicTestFound(TestDescriptor testDescriptor) {
			notifyTestExecutionListeners(listener -> listener.dynamicTestFound(testDescriptor));
		}

		@Override
		public void testStarted(TestDescriptor testDescriptor) {
			notifyTestExecutionListeners(listener -> listener.testStarted(testDescriptor));
		}

		@Override
		public void testSkipped(TestDescriptor testDescriptor, Throwable t) {
			notifyTestExecutionListeners(listener -> listener.testSkipped(testDescriptor, t));
		}

		@Override
		public void testAborted(TestDescriptor testDescriptor, Throwable t) {
			notifyTestExecutionListeners(listener -> listener.testAborted(testDescriptor, t));
		}

		@Override
		public void testFailed(TestDescriptor testDescriptor, Throwable t) {
			notifyTestExecutionListeners(listener -> listener.testFailed(testDescriptor, t));
		}

		@Override
		public void testSucceeded(TestDescriptor testDescriptor) {
			notifyTestExecutionListeners(listener -> listener.testSucceeded(testDescriptor));
		}
	}

	private class CompositeTestPlanExecutionListener implements TestPlanExecutionListener {

		@Override
		public void testPlanExecutionStarted(TestPlan testPlan) {
			notifyTestPlanExecutionListeners(listener -> listener.testPlanExecutionStarted(testPlan));
		}

		@Override
		public void testPlanExecutionPaused(TestPlan testPlan) {
			notifyTestPlanExecutionListeners(listener -> listener.testPlanExecutionPaused(testPlan));
		}

		@Override
		public void testPlanExecutionRestarted(TestPlan testPlan) {
			notifyTestPlanExecutionListeners(listener -> listener.testPlanExecutionRestarted(testPlan));
		}

		@Override
		public void testPlanExecutionStopped(TestPlan testPlan) {
			notifyTestPlanExecutionListeners(listener -> listener.testPlanExecutionStopped(testPlan));
		}

		@Override
		public void testPlanExecutionFinished(TestPlan testPlan) {
			notifyTestPlanExecutionListeners(listener -> listener.testPlanExecutionFinished(testPlan));
		}
	}
}