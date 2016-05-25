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

import static org.junit.gen5.commons.meta.API.Usage.Experimental;
import static org.junit.gen5.engine.TestExecutionResult.successful;
import static org.junit.gen5.engine.junit4.descriptor.JUnit4TestDescriptor.ENGINE_ID;

import java.util.logging.Logger;

import org.junit.gen5.commons.meta.API;
import org.junit.gen5.engine.EngineDiscoveryRequest;
import org.junit.gen5.engine.EngineExecutionListener;
import org.junit.gen5.engine.ExecutionRequest;
import org.junit.gen5.engine.TestDescriptor;
import org.junit.gen5.engine.TestEngine;
import org.junit.gen5.engine.UniqueId;
import org.junit.gen5.engine.junit4.descriptor.RunnerTestDescriptor;
import org.junit.gen5.engine.junit4.discovery.JUnit4DiscoveryRequestResolver;
import org.junit.gen5.engine.junit4.execution.RunnerExecutor;
import org.junit.gen5.engine.support.descriptor.EngineDescriptor;

/**
 * @since 5.0
 */
@API(Experimental)
public class JUnit4TestEngine implements TestEngine {

	private static final Logger LOG = Logger.getLogger(JUnit4TestEngine.class.getName());

	@Override
	public String getId() {
		return ENGINE_ID;
	}

	@Override
	public TestDescriptor discover(EngineDiscoveryRequest discoveryRequest, UniqueId uniqueId) {
		EngineDescriptor engineDescriptor = new EngineDescriptor(uniqueId, "JUnit 4");
		new JUnit4DiscoveryRequestResolver(engineDescriptor, LOG).resolve(discoveryRequest);
		return engineDescriptor;
	}

	@Override
	public void execute(ExecutionRequest request) {
		EngineExecutionListener engineExecutionListener = request.getEngineExecutionListener();
		TestDescriptor engineTestDescriptor = request.getRootTestDescriptor();
		engineExecutionListener.executionStarted(engineTestDescriptor);
		RunnerExecutor runnerExecutor = new RunnerExecutor(engineExecutionListener, LOG);
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
