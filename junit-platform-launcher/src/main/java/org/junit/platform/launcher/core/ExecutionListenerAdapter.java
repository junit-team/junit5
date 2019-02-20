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

import org.junit.platform.commons.logging.Logger;
import org.junit.platform.commons.logging.LoggerFactory;
import org.junit.platform.commons.util.BlacklistedExceptions;
import org.junit.platform.engine.EngineExecutionListener;
import org.junit.platform.engine.TestDescriptor;
import org.junit.platform.engine.TestExecutionResult;
import org.junit.platform.engine.reporting.ReportEntry;
import org.junit.platform.launcher.TestExecutionListener;
import org.junit.platform.launcher.TestIdentifier;
import org.junit.platform.launcher.TestPlan;

/**
 * An {@code ExecutionListenerAdapter} adapts a {@link TestPlan} and a corresponding
 * {@link TestExecutionListener} to the {@link EngineExecutionListener} API.
 *
 * @since 1.0
 */
class ExecutionListenerAdapter implements EngineExecutionListener {
	private static final Logger logger = LoggerFactory.getLogger(ExecutionListenerAdapter.class);

	private final InternalTestPlan testPlan;
	private final TestExecutionListener testExecutionListener;

	ExecutionListenerAdapter(InternalTestPlan testPlan, TestExecutionListener testExecutionListener) {
		this.testPlan = testPlan;
		this.testExecutionListener = testExecutionListener;
	}

	@Override
	public void dynamicTestRegistered(TestDescriptor testDescriptor) {
		TestIdentifier testIdentifier = TestIdentifier.from(testDescriptor);
		this.testPlan.addInternal(testIdentifier);
		try {
			this.testExecutionListener.dynamicTestRegistered(testIdentifier);
		}
		catch (Throwable throwable) {
			BlacklistedExceptions.rethrowIfBlacklisted(throwable);
			logger.error(throwable,
				() -> String.format(
					"Failed to invoke ExecutionListener [%s] for method [%s] with test display name [%s]",
					this.testExecutionListener.getClass().getName(), "dynamicTestRegistered",
					testIdentifier.getDisplayName()));
		}
	}

	@Override
	public void executionStarted(TestDescriptor testDescriptor) {
		TestIdentifier testIdentifier = getTestIdentifier(testDescriptor);
		try {
			this.testExecutionListener.executionStarted(testIdentifier);
		}
		catch (Throwable throwable) {
			BlacklistedExceptions.rethrowIfBlacklisted(throwable);
			logger.error(throwable,
				() -> String.format(
					"Failed to invoke ExecutionListener [%s] for method [%s] with test display name [%s]",
					this.testExecutionListener.getClass().getName(), "executionStarted",
					testIdentifier.getDisplayName()));
		}
	}

	@Override
	public void executionSkipped(TestDescriptor testDescriptor, String reason) {
		TestIdentifier testIdentifier = getTestIdentifier(testDescriptor);
		try {
			this.testExecutionListener.executionSkipped(testIdentifier, reason);
		}
		catch (Throwable throwable) {
			BlacklistedExceptions.rethrowIfBlacklisted(throwable);
			logger.error(throwable,
				() -> String.format(
					"Failed to invoke ExecutionListener [%s] for method [%s] with test display name [%s]",
					this.testExecutionListener.getClass().getName(), "executionSkipped",
					testIdentifier.getDisplayName()));
		}
	}

	@Override
	public void executionFinished(TestDescriptor testDescriptor, TestExecutionResult testExecutionResult) {
		TestIdentifier testIdentifier = getTestIdentifier(testDescriptor);
		try {
			this.testExecutionListener.executionFinished(testIdentifier, testExecutionResult);
		}
		catch (Throwable throwable) {
			BlacklistedExceptions.rethrowIfBlacklisted(throwable);
			logger.error(throwable,
				() -> String.format(
					"Failed to invoke ExecutionListener [%s] for method [%s] with test display name [%s]",
					this.testExecutionListener.getClass().getName(), "executionFinished",
					testIdentifier.getDisplayName()));
		}
	}

	@Override
	public void reportingEntryPublished(TestDescriptor testDescriptor, ReportEntry entry) {
		TestIdentifier testIdentifier = getTestIdentifier(testDescriptor);
		try {
			this.testExecutionListener.reportingEntryPublished(testIdentifier, entry);
		}
		catch (Throwable throwable) {
			BlacklistedExceptions.rethrowIfBlacklisted(throwable);
			logger.error(throwable,
				() -> String.format(
					"Failed to invoke ExecutionListener [%s] for method [%s] with test display name [%s]",
					this.testExecutionListener.getClass().getName(), "reportingEntryPublished",
					testIdentifier.getDisplayName()));
		}
	}

	private TestIdentifier getTestIdentifier(TestDescriptor testDescriptor) {
		return this.testPlan.getTestIdentifier(testDescriptor.getUniqueId().toString());
	}

}
