/*
 * Copyright 2015-2018 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v20.html
 */

package org.junit.platform.engine.test;

import org.apiguardian.api.API;
import org.junit.platform.engine.EngineDiscoveryRequest;
import org.junit.platform.engine.EngineExecutionListener;
import org.junit.platform.engine.ExecutionRequest;
import org.junit.platform.engine.TestDescriptor;
import org.junit.platform.engine.TestEngine;
import org.junit.platform.engine.TestExecutionResult;
import org.junit.platform.engine.UniqueId;
import org.junit.platform.engine.reporting.ReportEntry;

/**
 * {@link EngineExecutionListener} that records data from every event that occurs during the engine
 * execution lifecycle and provides functionality for retrieving execution state via {@link ExecutionGraph}.
 *
 * @see ExecutionEvent
 * @see ExecutionGraph
 * @since 1.1.1
 */
@API(status = API.Status.EXPERIMENTAL, since = "1.1.1")
public class ExecutionRecorder implements EngineExecutionListener {

	private ExecutionGraph.Builder graphBuilder;

	public ExecutionRecorder() {
		this.graphBuilder = ExecutionGraph.builder();
	}

	public static ExecutionGraph execute(TestEngine testEngine, EngineDiscoveryRequest discoveryRequest) {
		ExecutionRecorder executionReporter = new ExecutionRecorder();
		execute(testEngine, discoveryRequest, executionReporter);
		return executionReporter.getExecutionGraph();
	}

	public static void execute(TestEngine testEngine, EngineDiscoveryRequest discoveryRequest,
			EngineExecutionListener listener) {
		TestDescriptor engineTestDescriptor = testEngine.discover(discoveryRequest,
			UniqueId.forEngine(testEngine.getId()));
		testEngine.execute(
			new ExecutionRequest(engineTestDescriptor, listener, discoveryRequest.getConfigurationParameters()));
	}

	@Override
	public void dynamicTestRegistered(TestDescriptor testDescriptor) {
		graphBuilder.addEvent(ExecutionEvent.dynamicTestRegistered(testDescriptor));
	}

	@Override
	public void executionSkipped(TestDescriptor testDescriptor, String reason) {
		graphBuilder.addEvent(ExecutionEvent.executionSkipped(testDescriptor, reason));
	}

	@Override
	public void executionStarted(TestDescriptor testDescriptor) {
		graphBuilder.addEvent(ExecutionEvent.executionStarted(testDescriptor));
	}

	@Override
	public void executionFinished(TestDescriptor testDescriptor, TestExecutionResult testExecutionResult) {
		graphBuilder.addEvent(ExecutionEvent.executionFinished(testDescriptor, testExecutionResult));
	}

	@Override
	public void reportingEntryPublished(TestDescriptor testDescriptor, ReportEntry entry) {
		graphBuilder.addEvent(ExecutionEvent.reportingEntryPublished(testDescriptor, entry));
	}

	/**
	 * Gets the state of the engine's execution in the form of a {@link ExecutionGraph}.
	 *
	 * @return the {@link ExecutionGraph} containing all current state information from the engine
	 */
	public ExecutionGraph getExecutionGraph() {
		return graphBuilder.build();
	}

}
