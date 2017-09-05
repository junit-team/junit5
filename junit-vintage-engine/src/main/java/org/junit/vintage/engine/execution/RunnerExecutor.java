/*
 * Copyright 2015-2017 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.junit.vintage.engine.execution;

import static org.junit.platform.commons.meta.API.Usage.Internal;
import static org.junit.platform.engine.TestExecutionResult.failed;

import org.junit.platform.commons.logging.Logger;
import org.junit.platform.commons.meta.API;
import org.junit.platform.engine.EngineExecutionListener;
import org.junit.platform.engine.TestExecutionResult;
import org.junit.runner.JUnitCore;
import org.junit.vintage.engine.descriptor.RunnerTestDescriptor;

/**
 * @since 4.12
 */
@API(Internal)
public class RunnerExecutor {

	private final EngineExecutionListener engineExecutionListener;
	private final Logger logger;

	public RunnerExecutor(EngineExecutionListener engineExecutionListener, Logger logger) {
		this.engineExecutionListener = engineExecutionListener;
		this.logger = logger;
	}

	public void execute(RunnerTestDescriptor runnerTestDescriptor) {
		TestRun testRun = new TestRun(runnerTestDescriptor);
		JUnitCore core = new JUnitCore();
		core.addListener(new RunListenerAdapter(testRun, logger, engineExecutionListener));
		try {
			core.run(runnerTestDescriptor.toRequest());
		}
		catch (Throwable t) {
			reportUnexpectedFailure(testRun, runnerTestDescriptor, failed(t));
		}
	}

	private void reportUnexpectedFailure(TestRun testRun, RunnerTestDescriptor runnerTestDescriptor,
			TestExecutionResult result) {
		if (testRun.isNotStarted(runnerTestDescriptor)) {
			engineExecutionListener.executionStarted(runnerTestDescriptor);
		}
		engineExecutionListener.executionFinished(runnerTestDescriptor, result);
	}

}
