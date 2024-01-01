/*
 * Copyright 2015-2024 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * https://www.eclipse.org/legal/epl-v20.html
 */

package org.junit.platform.launcher.core;

import org.junit.platform.engine.EngineExecutionListener;
import org.junit.platform.engine.TestDescriptor;
import org.junit.platform.engine.TestExecutionResult;

/**
 * Delays reporting of engine skipped/finished events so that exceptions thrown
 * by engines can be reported to listeners.
 *
 * @since 1.6
 */
class OutcomeDelayingEngineExecutionListener extends DelegatingEngineExecutionListener {

	private final TestDescriptor engineDescriptor;

	private volatile boolean engineStarted;
	private volatile Outcome outcome;
	private volatile String skipReason;
	private volatile TestExecutionResult executionResult;

	OutcomeDelayingEngineExecutionListener(EngineExecutionListener delegate, TestDescriptor engineDescriptor) {
		super(delegate);
		this.engineDescriptor = engineDescriptor;
	}

	@Override
	public void executionSkipped(TestDescriptor testDescriptor, String reason) {
		if (testDescriptor == engineDescriptor) {
			outcome = Outcome.SKIPPED;
			skipReason = reason;
		}
		else {
			super.executionSkipped(testDescriptor, reason);
		}
	}

	@Override
	public void executionStarted(TestDescriptor testDescriptor) {
		if (testDescriptor == engineDescriptor) {
			engineStarted = true;
		}
		super.executionStarted(testDescriptor);
	}

	@Override
	public void executionFinished(TestDescriptor testDescriptor, TestExecutionResult executionResult) {
		if (testDescriptor == engineDescriptor) {
			outcome = Outcome.FINISHED;
			this.executionResult = executionResult;
		}
		else {
			super.executionFinished(testDescriptor, executionResult);
		}
	}

	void reportEngineOutcome() {
		if (outcome == Outcome.FINISHED) {
			super.executionFinished(engineDescriptor, executionResult);
		}
		else if (outcome == Outcome.SKIPPED) {
			super.executionSkipped(engineDescriptor, skipReason);
		}
	}

	void reportEngineFailure(Throwable throwable) {
		if (!engineStarted) {
			super.executionStarted(engineDescriptor);
		}
		if (executionResult != null && executionResult.getThrowable().isPresent()) {
			throwable.addSuppressed(executionResult.getThrowable().get());
		}
		super.executionFinished(engineDescriptor, TestExecutionResult.failed(throwable));
	}

	private enum Outcome {
		SKIPPED, FINISHED
	}

}
