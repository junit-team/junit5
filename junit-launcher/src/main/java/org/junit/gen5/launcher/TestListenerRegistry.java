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

import static java.util.Arrays.*;

import java.util.LinkedList;
import java.util.List;
import java.util.function.Consumer;

import org.junit.gen5.engine.TestDescriptor;
import org.junit.gen5.engine.TestExecutionListener;
import org.junit.gen5.engine.TestPlanExecutionListener;

/**
 * @author Stefan Bechtold
 * @author Sam Brannen
 * @since 5.0
 */
class TestListenerRegistry {

	private final List<TestExecutionListener> testExecutionListeners = new LinkedList<>();

	private final List<TestPlanExecutionListener> testPlanExecutionListeners = new LinkedList<>();


	void registerTestExecutionListeners(TestExecutionListener... listeners) {
		this.testExecutionListeners.addAll(asList(listeners));
	}

	void registerTestPlanExecutionListeners(TestPlanExecutionListener... listeners) {
		this.testPlanExecutionListeners.addAll(asList(listeners));
	}

	void notifyTestExecutionListeners(Consumer<TestExecutionListener> consumer) {
		this.testExecutionListeners.forEach(consumer);
	}

	void notifyTestPlanExecutionListeners(Consumer<TestPlanExecutionListener> consumer) {
		this.testPlanExecutionListeners.forEach(consumer);
	}

	TestExecutionListener getCompositeTestExecutionListener() {
		return new CompositeTestExecutionListener();
	}


	private class CompositeTestExecutionListener implements TestExecutionListener {

		@Override
		public void dynamicTestFound(TestDescriptor testDescriptor) {
			notifyTestExecutionListeners(
				testExecutionListener -> testExecutionListener.dynamicTestFound(testDescriptor));
		}

		@Override
		public void testStarted(TestDescriptor testDescriptor) {
			notifyTestExecutionListeners(testExecutionListener -> testExecutionListener.testStarted(testDescriptor));
		}

		@Override
		public void testSkipped(TestDescriptor testDescriptor, Throwable t) {
			notifyTestExecutionListeners(testExecutionListener -> testExecutionListener.testSkipped(testDescriptor, t));
		}

		@Override
		public void testAborted(TestDescriptor testDescriptor, Throwable t) {
			notifyTestExecutionListeners(testExecutionListener -> testExecutionListener.testAborted(testDescriptor, t));
		}

		@Override
		public void testFailed(TestDescriptor testDescriptor, Throwable t) {
			notifyTestExecutionListeners(testExecutionListener -> testExecutionListener.testFailed(testDescriptor, t));
		}

		@Override
		public void testSucceeded(TestDescriptor testDescriptor) {
			notifyTestExecutionListeners(testExecutionListener -> testExecutionListener.testSucceeded(testDescriptor));
		}

	}

}
