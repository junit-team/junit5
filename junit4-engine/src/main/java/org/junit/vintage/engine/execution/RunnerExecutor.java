/*
 * Copyright 2015-2016 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.junit.vintage.engine.execution;

import static org.junit.gen5.commons.meta.API.Usage.Internal;
import static org.junit.gen5.engine.TestExecutionResult.failed;

import java.util.logging.Logger;

import org.junit.gen5.commons.meta.API;
import org.junit.gen5.engine.EngineExecutionListener;
import org.junit.gen5.engine.TestExecutionResult;
import org.junit.runner.JUnitCore;
import org.junit.vintage.engine.descriptor.RunnerTestDescriptor;

/**
 * @since 5.0
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
		TestRun testRun = new TestRun(runnerTestDescriptor, logger);
		JUnitCore core = new JUnitCore();
		core.addListener(new RunListenerAdapter(testRun, engineExecutionListener));
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
