/*
 * Copyright 2015-2019 the original author or authors.
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

	private void notifyTestExecutionListeners(Consumer<TestExecutionListener> consumer) {
		this.testExecutionListeners.forEach(consumer);
	}

	private void notifyEagerTestExecutionListeners(Consumer<EagerTestExecutionListener> consumer) {
		this.eagerTestExecutionListeners.forEach(consumer);
	}

	TestExecutionListener getCompositeTestExecutionListener() {
		return new CompositeTestExecutionListener();
	}

	class CompositeTestExecutionListener implements TestExecutionListener {
		public List<TestExecutionListener> getAll() {
			return testExecutionListeners;
		}

		@Override
		public void dynamicTestRegistered(TestIdentifier testIdentifier) {
			notifyTestExecutionListeners(listener -> {
				try {
					listener.dynamicTestRegistered(testIdentifier);
				}
				catch (Throwable throwable) {
					BlacklistedExceptions.rethrowIfBlacklisted(throwable);
					logger.error(throwable,
						() -> String.format(
							"Failed to invoke ExecutionListener [%s] for method [%s] with test display name [%s]",
							listener.getClass().getName(), "dynamicTestRegistered", testIdentifier.getDisplayName()));
				}
			});
		}

		@Override
		public void executionSkipped(TestIdentifier testIdentifier, String reason) {
			notifyTestExecutionListeners(listener -> {
				try {
					listener.executionSkipped(testIdentifier, reason);
				}
				catch (Throwable throwable) {
					BlacklistedExceptions.rethrowIfBlacklisted(throwable);
					logger.error(throwable,
						() -> String.format(
							"Failed to invoke ExecutionListener [%s] for method [%s] with test display name [%s]",
							listener.getClass().getName(), "executionSkipped", testIdentifier.getDisplayName()));
				}
			});
		}

		@Override
		public void executionStarted(TestIdentifier testIdentifier) {
			notifyEagerTestExecutionListeners(listener -> listener.executionJustStarted(testIdentifier));
			notifyTestExecutionListeners(listener -> {
				try {
					listener.executionStarted(testIdentifier);
				}
				catch (Throwable throwable) {
					BlacklistedExceptions.rethrowIfBlacklisted(throwable);
					logger.error(throwable,
						() -> String.format(
							"Failed to invoke ExecutionListener [%s] for method [%s] with test display name [%s]",
							listener.getClass().getName(), "executionStarted", testIdentifier.getDisplayName()));
				}
			});
		}

		@Override
		public void executionFinished(TestIdentifier testIdentifier, TestExecutionResult testExecutionResult) {
			notifyEagerTestExecutionListeners(
				listener -> listener.executionJustFinished(testIdentifier, testExecutionResult));
			notifyTestExecutionListeners(listener -> {
				try {
					listener.executionFinished(testIdentifier, testExecutionResult);
				}
				catch (Throwable throwable) {
					BlacklistedExceptions.rethrowIfBlacklisted(throwable);
					logger.error(throwable,
						() -> String.format(
							"Failed to invoke ExecutionListener [%s] for method [%s] with test display name [%s]",
							listener.getClass().getName(), "executionFinished", testIdentifier.getDisplayName()));
				}
			});
		}

		@Override
		public void testPlanExecutionStarted(TestPlan testPlan) {
			notifyTestExecutionListeners(listener -> {
				try {
					listener.testPlanExecutionStarted(testPlan);
				}
				catch (Throwable throwable) {
					BlacklistedExceptions.rethrowIfBlacklisted(throwable);
					logger.error(throwable,
						() -> String.format(
							"Failed to invoke ExecutionListener [%s] for method [%s] for test plan [%s]",
							listener.getClass().getName(), "testPlanExecutionStarted", testPlan.getClass().getName()));
				}
			});
		}

		@Override
		public void testPlanExecutionFinished(TestPlan testPlan) {
			notifyTestExecutionListeners(listener -> {
				try {
					listener.testPlanExecutionFinished(testPlan);
				}
				catch (Throwable throwable) {
					BlacklistedExceptions.rethrowIfBlacklisted(throwable);
					logger.error(throwable,
						() -> String.format(
							"Failed to invoke ExecutionListener [%s] for method [%s] for test plan [%s]",
							listener.getClass().getName(), "testPlanExecutionFinished", testPlan.getClass().getName()));
				}
			});
		}

		@Override
		public void reportingEntryPublished(TestIdentifier testIdentifier, ReportEntry entry) {
			notifyTestExecutionListeners(listener -> {
				try {
					listener.reportingEntryPublished(testIdentifier, entry);
				}
				catch (Throwable throwable) {
					BlacklistedExceptions.rethrowIfBlacklisted(throwable);
					logger.error(throwable,
						() -> String.format(
							"Failed to invoke ExecutionListener [%s] for method [%s] with test display name [%s]",
							listener.getClass().getName(), "reportingEntryPublished", testIdentifier.getDisplayName()));
				}
			});
		}

	}

	interface EagerTestExecutionListener extends TestExecutionListener {
		default void executionJustStarted(TestIdentifier testIdentifier) {
		}

		default void executionJustFinished(TestIdentifier testIdentifier, TestExecutionResult testExecutionResult) {
		}
	}

}
