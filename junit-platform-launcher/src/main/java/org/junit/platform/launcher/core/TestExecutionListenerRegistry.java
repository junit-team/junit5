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

	CompositeTestExecutionListener getCompositeTestExecutionListener() {
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
					rethrowIfBlacklistedAndLogTest(throwable, listener, testIdentifier, "dynamicTestRegistered");
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
					rethrowIfBlacklistedAndLogTest(throwable, listener, testIdentifier, "executionSkipped");
				}
			});
		}

		@Override
		public void executionStarted(TestIdentifier testIdentifier) {
			notifyEagerTestExecutionListeners(listener -> {
				try {
					listener.executionJustStarted(testIdentifier);
				}
				catch (Throwable throwable) {
					rethrowIfBlacklistedAndLogTest(throwable, listener, testIdentifier, "executionJustStarted");
				}
			});
			notifyTestExecutionListeners(listener -> {
				try {
					listener.executionStarted(testIdentifier);
				}
				catch (Throwable throwable) {
					rethrowIfBlacklistedAndLogTest(throwable, listener, testIdentifier, "executionStarted");
				}
			});
		}

		@Override
		public void executionFinished(TestIdentifier testIdentifier, TestExecutionResult testExecutionResult) {
			notifyEagerTestExecutionListeners(listener -> {
				try {
					listener.executionJustFinished(testIdentifier, testExecutionResult);
				}
				catch (Throwable throwable) {
					rethrowIfBlacklistedAndLogTest(throwable, listener, testIdentifier, "executionJustFinished");
				}
			});
			notifyTestExecutionListeners(listener -> {
				try {
					listener.executionFinished(testIdentifier, testExecutionResult);
				}
				catch (Throwable throwable) {
					rethrowIfBlacklistedAndLogTest(throwable, listener, testIdentifier, "executionFinished");
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
					rethrowIfBlacklistedAndLogPlan(throwable, testPlan, listener, "testPlanExecutionStarted");
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
					rethrowIfBlacklistedAndLogPlan(throwable, testPlan, listener, "testPlanExecutionFinished");
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
					rethrowIfBlacklistedAndLogTest(throwable, listener, testIdentifier, "reportingEntryPublished");
				}
			});
		}

		private void rethrowIfBlacklistedAndLogTest(Throwable throwable, TestExecutionListener listener,
				TestIdentifier testIdentifier, String methodName) {
			BlacklistedExceptions.rethrowIfBlacklisted(throwable);
			logger.warn(throwable,
				() -> String.format(
					"Failed to invoke ExecutionListener [%s] for method [%s] with test display name [%s]",
					listener.getClass().getName(), methodName, testIdentifier.getDisplayName()));
		}

		private void rethrowIfBlacklistedAndLogPlan(Throwable throwable, TestPlan testPlan,
				TestExecutionListener listener, String method) {
			BlacklistedExceptions.rethrowIfBlacklisted(throwable);
			logger.warn(throwable,
				() -> String.format("Failed to invoke ExecutionListener [%s] for method [%s] for test plan [%s]",
					listener.getClass().getName(), method, testPlan.getClass().getName()));
		}

	}

	interface EagerTestExecutionListener extends TestExecutionListener {
		default void executionJustStarted(TestIdentifier testIdentifier) {
		}

		default void executionJustFinished(TestIdentifier testIdentifier, TestExecutionResult testExecutionResult) {
		}
	}

}
