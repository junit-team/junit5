/*
 * Copyright 2015-2016 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.junit.gen5.engine.junit4;

import static org.junit.gen5.engine.TestExecutionResult.successful;

import org.junit.gen5.engine.*;
import org.junit.gen5.engine.DiscoveryRequest;
import org.junit.gen5.engine.junit4.descriptor.RunnerTestDescriptor;
import org.junit.gen5.engine.junit4.discovery.JUnit4TestPlanSpecificationResolver;
import org.junit.gen5.engine.junit4.execution.RunnerExecutor;

public class JUnit4TestEngine implements TestEngine {

	@Override
	public String getId() {
		return "junit4";
	}

	@Override
	public EngineAwareTestDescriptor discoverTests(DiscoveryRequest specification) {
		EngineDescriptor engineDescriptor = new EngineDescriptor(this);
		new JUnit4TestPlanSpecificationResolver(engineDescriptor).resolve(specification);
		return engineDescriptor;
	}

	@Override
	public void execute(ExecutionRequest request) {
		EngineExecutionListener engineExecutionListener = request.getEngineExecutionListener();
		TestDescriptor engineTestDescriptor = request.getRootTestDescriptor();
		engineExecutionListener.executionStarted(engineTestDescriptor);
		RunnerExecutor runnerExecutor = new RunnerExecutor(engineExecutionListener);
		executeAllChildren(runnerExecutor, engineTestDescriptor);
		engineExecutionListener.executionFinished(engineTestDescriptor, successful());
	}

	private void executeAllChildren(RunnerExecutor runnerExecutor, TestDescriptor engineTestDescriptor) {
		// @formatter:off
		engineTestDescriptor.getChildren()
			.stream()
			.map(RunnerTestDescriptor.class::cast)
			.forEach(runnerExecutor::execute);
		// @formatter:on
	}
}
