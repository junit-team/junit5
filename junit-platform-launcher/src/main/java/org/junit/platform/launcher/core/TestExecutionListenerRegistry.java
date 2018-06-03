/*
 * Copyright 2015-2018 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v20.html
 */

package org.junit.platform.launcher.core;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;

import org.junit.platform.engine.TestExecutionResult;
import org.junit.platform.engine.reporting.ReportEntry;
import org.junit.platform.launcher.TestExecutionListener;
import org.junit.platform.launcher.TestIdentifier;
import org.junit.platform.launcher.TestPlan;

/**
 * @since 1.0
 */
class TestExecutionListenerRegistry {

	private final List<TestExecutionListener> testExecutionListeners = new ArrayList<>();
	private final List<EagerTestExecutionListener> eagerTestExecutionListeners = new ArrayList<>();

	TestExecutionListenerRegistry() {
		this(null);
	}

	TestExecutionListenerRegistry(TestExecutionListenerRegistry source) {
		if (source != null) {
			this.testExecutionListeners.addAll(source.testExecutionListeners);
			this.eagerTestExecutionListeners.addAll(source.eagerTestExecutionListeners);
		}
	}

	List<TestExecutionListener> getTestExecutionListeners() {
		return testExecutionListeners;
	}

	void registerListeners(TestExecutionListener... listeners) {
		Collections.addAll(this.testExecutionListeners, listeners);
		// @formatter:off
		Arrays.stream(listeners)
				.filter(EagerTestExecutionListener.class::isInstance)
				.map(EagerTestExecutionListener.class::cast)
				.forEach(this.eagerTestExecutionListeners::add);
		// @formatter:on
	}

	private void notifyTestExecutionListeners(Consumer<TestExecutionListener> consumer) {
		this.testExecutionListeners.forEach(consumer);
	}

	private void notifyEagerTestExecutionListeners(Consumer<EagerTestExecutionListener> consumer) {
		this.eagerTestExecutionListeners.forEach(consumer);
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
			notifyEagerTestExecutionListeners(listener -> listener.executionJustStarted(testIdentifier));
			notifyTestExecutionListeners(listener -> listener.executionStarted(testIdentifier));
		}

		@Override
		public void executionFinished(TestIdentifier testIdentifier, TestExecutionResult testExecutionResult) {
			notifyEagerTestExecutionListeners(
				listener -> listener.executionJustFinished(testIdentifier, testExecutionResult));
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

	interface EagerTestExecutionListener extends TestExecutionListener {
		default void executionJustStarted(TestIdentifier testIdentifier) {
		}

		default void executionJustFinished(TestIdentifier testIdentifier, TestExecutionResult testExecutionResult) {
		}
	}

}
