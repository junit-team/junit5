/*
 * Copyright 2015-2018 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v20.html
 */

package org.junit.platform.testkit;

import static org.apiguardian.api.API.Status.EXPERIMENTAL;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

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
 * {@code ExecutionRecorder} is an {@link EngineExecutionListener} that records
 * data from every event that occurs during the engine execution lifecycle and
 * provides functionality for retrieving execution state via
 * {@link ExecutionResults}.
 *
 * @see ExecutionResults
 * @see ExecutionEvent
 * @see Execution
 * @since 1.4
 */
@API(status = EXPERIMENTAL, since = "1.4")
public class ExecutionRecorder implements EngineExecutionListener {

	/**
	 * Execute tests for a given {@link EngineDiscoveryRequest} using the
	 * provided {@link TestEngine}.
	 *
	 * @param testEngine the {@code TestEngine} to use
	 * @param discoveryRequest the {@code EngineDiscoveryRequest} to use
	 * @return the recorded {@code ExecutionResults}
	 */
	public static ExecutionResults execute(TestEngine testEngine, EngineDiscoveryRequest discoveryRequest) {
		ExecutionRecorder executionRecorder = new ExecutionRecorder();
		execute(testEngine, discoveryRequest, executionRecorder);
		return executionRecorder.getExecutionResults();
	}

	private static void execute(TestEngine testEngine, EngineDiscoveryRequest discoveryRequest,
			EngineExecutionListener listener) {

		UniqueId engineUniqueId = UniqueId.forEngine(testEngine.getId());
		TestDescriptor engineTestDescriptor = testEngine.discover(discoveryRequest, engineUniqueId);
		ExecutionRequest request = new ExecutionRequest(engineTestDescriptor, listener,
			discoveryRequest.getConfigurationParameters());
		testEngine.execute(request);
	}

	// -------------------------------------------------------------------------

	private final List<ExecutionEvent> events = new CopyOnWriteArrayList<>();

	/**
	 * Record an {@link ExecutionEvent} for a dynamically registered container
	 * or test.
	 */
	@Override
	public void dynamicTestRegistered(TestDescriptor testDescriptor) {
		this.events.add(ExecutionEvent.dynamicTestRegistered(testDescriptor));
	}

	/**
	 * Record an {@link ExecutionEvent} for a container or test that was skipped.
	 */
	@Override
	public void executionSkipped(TestDescriptor testDescriptor, String reason) {
		this.events.add(ExecutionEvent.executionSkipped(testDescriptor, reason));
	}

	/**
	 * Record an {@link ExecutionEvent} for a container or test that started.
	 */
	@Override
	public void executionStarted(TestDescriptor testDescriptor) {
		this.events.add(ExecutionEvent.executionStarted(testDescriptor));
	}

	/**
	 * Record an {@link ExecutionEvent} for a container or test that completed
	 * with the provided {@link TestExecutionResult}.
	 */
	@Override
	public void executionFinished(TestDescriptor testDescriptor, TestExecutionResult testExecutionResult) {
		this.events.add(ExecutionEvent.executionFinished(testDescriptor, testExecutionResult));
	}

	/**
	 * Record an {@link ExecutionEvent} for a published {@link ReportEntry}.
	 */
	@Override
	public void reportingEntryPublished(TestDescriptor testDescriptor, ReportEntry entry) {
		this.events.add(ExecutionEvent.reportingEntryPublished(testDescriptor, entry));
	}

	/**
	 * Get the state of the engine's execution in the form of {@link ExecutionResults}.
	 *
	 * @return the {@code ExecutionResults} containing all current state information
	 */
	public ExecutionResults getExecutionResults() {
		return new ExecutionResults(this.events);
	}

}
