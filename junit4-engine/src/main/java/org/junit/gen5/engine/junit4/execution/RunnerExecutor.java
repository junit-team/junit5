/*
 * Copyright 2015-2016 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.junit.gen5.engine.junit4.execution;

import static org.junit.gen5.engine.TestExecutionResult.failed;

import org.junit.gen5.engine.EngineExecutionListener;
import org.junit.gen5.engine.TestExecutionResult;
import org.junit.gen5.engine.junit4.descriptor.RunnerTestDescriptor;
import org.junit.runner.JUnitCore;
import org.junit.runner.Request;
import org.junit.runner.Runner;

public class RunnerExecutor {

	private final EngineExecutionListener engineExecutionListener;

	public RunnerExecutor(EngineExecutionListener engineExecutionListener) {
		this.engineExecutionListener = engineExecutionListener;
	}

	public void execute(RunnerTestDescriptor runnerTestDescriptor) {
		TestRun testRun = new TestRun(runnerTestDescriptor);
		JUnitCore core = new JUnitCore();
		core.addListener(new RunListenerAdapter(testRun, engineExecutionListener));
		try {
			core.run(new Request() {

				@Override
				public Runner getRunner() {
					return runnerTestDescriptor.getRunner();
				}

			});
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
