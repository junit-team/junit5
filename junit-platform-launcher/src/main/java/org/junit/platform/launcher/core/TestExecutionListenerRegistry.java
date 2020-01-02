/*
 * Copyright 2015-2020 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * https://www.eclipse.org/legal/epl-v20.html
 */

package org.junit.platform.launcher.core;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;

import org.junit.platform.commons.logging.Logger;
import org.junit.platform.commons.logging.LoggerFactory;
import org.junit.platform.commons.util.BlacklistedExceptions;
import org.junit.platform.engine.TestExecutionResult;
import org.junit.platform.engine.reporting.ReportEntry;
import org.junit.platform.launcher.TestExecutionListener;
import org.junit.platform.launcher.TestIdentifier;
import org.junit.platform.launcher.TestPlan;

/**
 * @since 1.0
 */
class TestExecutionListenerRegistry {
	private static final Logger logger = LoggerFactory.getLogger(TestExecutionListenerRegistry.class);

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

	private <T extends TestExecutionListener> void notifyEach(List<T> listeners, Consumer<T> consumer,
			Supplier<String> description) {
		listeners.forEach(listener -> {
			try {
				consumer.accept(listener);
			}
			catch (Throwable throwable) {
				BlacklistedExceptions.rethrowIfBlacklisted(throwable);
				logger.warn(throwable, () -> String.format("TestExecutionListener [%s] threw exception for method: %s",
					listener.getClass().getName(), description.get()));
			}
		});
	}

	TestExecutionListener getCompositeTestExecutionListener() {
		return new CompositeTestExecutionListener();
	}

	private class CompositeTestExecutionListener implements TestExecutionListener {

		@Override
		public void dynamicTestRegistered(TestIdentifier testIdentifier) {
			notifyEach(testExecutionListeners, listener -> listener.dynamicTestRegistered(testIdentifier),
				() -> "dynamicTestRegistered(" + testIdentifier + ")");
		}

		@Override
		public void executionSkipped(TestIdentifier testIdentifier, String reason) {
			notifyEach(testExecutionListeners, listener -> listener.executionSkipped(testIdentifier, reason),
				() -> "executionSkipped(" + testIdentifier + ", " + reason + ")");
		}

		@Override
		public void executionStarted(TestIdentifier testIdentifier) {
			notifyEach(eagerTestExecutionListeners, listener -> listener.executionJustStarted(testIdentifier),
				() -> "executionJustStarted(" + testIdentifier + ")");
			notifyEach(testExecutionListeners, listener -> listener.executionStarted(testIdentifier),
				() -> "executionStarted(" + testIdentifier + ")");
		}

		@Override
		public void executionFinished(TestIdentifier testIdentifier, TestExecutionResult testExecutionResult) {
			notifyEach(eagerTestExecutionListeners,
				listener -> listener.executionJustFinished(testIdentifier, testExecutionResult),
				() -> "executionJustFinished(" + testIdentifier + ", " + testExecutionResult + ")");
			notifyEach(testExecutionListeners,
				listener -> listener.executionFinished(testIdentifier, testExecutionResult),
				() -> "executionFinished(" + testIdentifier + ", " + testExecutionResult + ")");
		}

		@Override
		public void testPlanExecutionStarted(TestPlan testPlan) {
			notifyEach(testExecutionListeners, listener -> listener.testPlanExecutionStarted(testPlan),
				() -> "testPlanExecutionStarted(" + testPlan + ")");
		}

		@Override
		public void testPlanExecutionFinished(TestPlan testPlan) {
			notifyEach(testExecutionListeners, listener -> listener.testPlanExecutionFinished(testPlan),
				() -> "testPlanExecutionFinished(" + testPlan + ")");
		}

		@Override
		public void reportingEntryPublished(TestIdentifier testIdentifier, ReportEntry entry) {
			notifyEach(testExecutionListeners, listener -> listener.reportingEntryPublished(testIdentifier, entry),
				() -> "reportingEntryPublished(" + testIdentifier + ", " + entry + ")");
		}

	}

	interface EagerTestExecutionListener extends TestExecutionListener {
		default void executionJustStarted(TestIdentifier testIdentifier) {
		}

		default void executionJustFinished(TestIdentifier testIdentifier, TestExecutionResult testExecutionResult) {
		}
	}

}
