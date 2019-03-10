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

	CompositeTestExecutionListener getCompositeTestExecutionListener() {
		return new CompositeTestExecutionListener();
	}

	private void notifyTestExecutionListeners(TestIdentifier testIdentifier, String listenerMethodName,
			Consumer<TestExecutionListener> consumer) {
		this.testExecutionListeners.forEach(testExecutionListener -> {
			try {
				consumer.accept(testExecutionListener);
			}
			catch (Throwable throwable) {
				rethrowIfBlacklistedAndLogTest(throwable, testExecutionListener, testIdentifier, listenerMethodName);
			}
		});
	}

	private void notifyTestExecutionListeners(TestPlan testPlan, String listenerMethodName,
			Consumer<TestExecutionListener> consumer) {
		this.testExecutionListeners.forEach(testExecutionListener -> {
			try {
				consumer.accept(testExecutionListener);
			}
			catch (Throwable throwable) {
				rethrowIfBlacklistedAndLogPlan(throwable, testExecutionListener, testPlan, listenerMethodName);
			}
		});
	}

	private void notifyEagerTestExecutionListeners(TestIdentifier testIdentifier, String listenerMethodName,
			Consumer<EagerTestExecutionListener> consumer) {
		this.eagerTestExecutionListeners.forEach(eagerTestExecutionListener -> {
			try {
				consumer.accept(eagerTestExecutionListener);
			}
			catch (Throwable throwable) {
				rethrowIfBlacklistedAndLogTest(throwable, eagerTestExecutionListener, testIdentifier,
					listenerMethodName);
			}
		});
	}

	private void rethrowIfBlacklistedAndLogTest(Throwable throwable, TestExecutionListener listener,
			TestIdentifier testIdentifier, String methodName) {
		BlacklistedExceptions.rethrowIfBlacklisted(throwable);
		logger.warn(throwable,
			() -> String.format("Failed to invoke ExecutionListener [%s] for method [%s] with test display name [%s]",
				listener.getClass().getName(), methodName, testIdentifier.getDisplayName()));
	}

	private void rethrowIfBlacklistedAndLogPlan(Throwable throwable, TestExecutionListener listener, TestPlan testPlan,
			String method) {
		BlacklistedExceptions.rethrowIfBlacklisted(throwable);
		logger.warn(throwable,
			() -> String.format("Failed to invoke ExecutionListener [%s] for method [%s] for test plan [%s]",
				listener.getClass().getName(), method, testPlan.getClass().getName()));
	}

	class CompositeTestExecutionListener implements TestExecutionListener {
		public List<TestExecutionListener> getAll() {
			return testExecutionListeners;
		}

		@Override
		public void dynamicTestRegistered(TestIdentifier testIdentifier) {
			notifyTestExecutionListeners(testIdentifier, "dynamicTestRegistered",
				listener -> listener.dynamicTestRegistered(testIdentifier));
		}

		@Override
		public void executionSkipped(TestIdentifier testIdentifier, String reason) {
			notifyTestExecutionListeners(testIdentifier, "executionSkipped",
				listener -> listener.executionSkipped(testIdentifier, reason));
		}

		@Override
		public void executionStarted(TestIdentifier testIdentifier) {
			notifyEagerTestExecutionListeners(testIdentifier, "executionJustStarted",
				listener -> listener.executionJustStarted(testIdentifier));
			notifyTestExecutionListeners(testIdentifier, "executionStarted",
				listener -> listener.executionStarted(testIdentifier));
		}

		@Override
		public void executionFinished(TestIdentifier testIdentifier, TestExecutionResult testExecutionResult) {
			notifyEagerTestExecutionListeners(testIdentifier, "executionJustFinished",
				listener -> listener.executionJustFinished(testIdentifier, testExecutionResult));
			notifyTestExecutionListeners(testIdentifier, "executionFinished",
				listener -> listener.executionFinished(testIdentifier, testExecutionResult));
		}

		@Override
		public void testPlanExecutionStarted(TestPlan testPlan) {
			notifyTestExecutionListeners(testPlan, "testPlanExecutionStarted",
				listener -> listener.testPlanExecutionStarted(testPlan));
		}

		@Override
		public void testPlanExecutionFinished(TestPlan testPlan) {
			notifyTestExecutionListeners(testPlan, "testPlanExecutionFinished",
				listener -> listener.testPlanExecutionFinished(testPlan));
		}

		@Override
		public void reportingEntryPublished(TestIdentifier testIdentifier, ReportEntry entry) {
			notifyTestExecutionListeners(testIdentifier, "reportingEntryPublished",
				listener -> listener.reportingEntryPublished(testIdentifier, entry));
		}
	}
	interface EagerTestExecutionListener extends TestExecutionListener {
		default void executionJustStarted(TestIdentifier testIdentifier) {
		}

		default void executionJustFinished(TestIdentifier testIdentifier, TestExecutionResult testExecutionResult) {
		}
	}

}
