/*
 * Copyright 2015-2017 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.junit.vintage.engine;

import static org.junit.platform.commons.meta.API.Usage.Experimental;
import static org.junit.platform.engine.TestExecutionResult.successful;
import static org.junit.vintage.engine.descriptor.VintageTestDescriptor.ENGINE_ID;

import java.util.Optional;
import java.util.logging.Logger;

import org.junit.platform.commons.meta.API;
import org.junit.platform.engine.EngineDiscoveryRequest;
import org.junit.platform.engine.EngineExecutionListener;
import org.junit.platform.engine.ExecutionRequest;
import org.junit.platform.engine.TestDescriptor;
import org.junit.platform.engine.TestEngine;
import org.junit.platform.engine.UniqueId;
import org.junit.vintage.engine.descriptor.RunnerTestDescriptor;
import org.junit.vintage.engine.discovery.VintageDiscoverer;
import org.junit.vintage.engine.execution.RunnerExecutor;

/**
 * The JUnit Vintage {@link TestEngine}.
 *
 * @since 4.12
 */
@API(Experimental)
public class VintageTestEngine implements TestEngine {

	private static final Logger logger = Logger.getLogger(VintageTestEngine.class.getName());

	@Override
	public String getId() {
		return ENGINE_ID;
	}

	/**
	 * Returns {@code org.junit.vintage} as the group ID.
	 */
	@Override
	public Optional<String> getGroupId() {
		return Optional.of("org.junit.vintage");
	}

	/**
	 * Returns {@code junit-vintage-engine} as the artifact ID.
	 */
	@Override
	public Optional<String> getArtifactId() {
		return Optional.of("junit-vintage-engine");
	}

	@Override
	public TestDescriptor discover(EngineDiscoveryRequest discoveryRequest, UniqueId uniqueId) {
		return new VintageDiscoverer(logger).discover(discoveryRequest, uniqueId);
	}

	@Override
	public void execute(ExecutionRequest request) {
		EngineExecutionListener engineExecutionListener = request.getEngineExecutionListener();
		TestDescriptor engineTestDescriptor = request.getRootTestDescriptor();
		engineExecutionListener.executionStarted(engineTestDescriptor);
		RunnerExecutor runnerExecutor = new RunnerExecutor(engineExecutionListener, logger);
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
