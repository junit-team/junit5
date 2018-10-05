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
 * execution lifecycle and provides functionality for retrieving execution state via {@link ExecutionsResult}.
 *
 * @see ExecutionEvent
 * @see ExecutionsResult
 * @since 1.4.0
 */
@API(status = API.Status.EXPERIMENTAL, since = "1.4.0")
public class ExecutionRecorder implements EngineExecutionListener {

	private ExecutionsResult.Builder graphBuilder;

	public ExecutionRecorder() {
		this.graphBuilder = ExecutionsResult.builder();
	}

	/**
	 * Execute tests via a {@link EngineDiscoveryRequest} using the provided {@link TestEngine},
	 * then return the recorded {@link ExecutionsResult} to the caller.
	 *
	 * @param testEngine the {@link TestEngine} to use when running the discovered tests
	 * @param discoveryRequest the {@link EngineDiscoveryRequest} to use to discover tests to execute
	 * @return the recorded {@link ExecutionsResult} of the executed tests
	 */
	public static ExecutionsResult execute(TestEngine testEngine, EngineDiscoveryRequest discoveryRequest) {
		ExecutionRecorder executionReporter = new ExecutionRecorder();
		execute(testEngine, discoveryRequest, executionReporter);
		return executionReporter.getExecutionsResult();
	}

	public static void execute(TestEngine testEngine, EngineDiscoveryRequest discoveryRequest,
			EngineExecutionListener listener) {
		TestDescriptor engineTestDescriptor = testEngine.discover(discoveryRequest,
			UniqueId.forEngine(testEngine.getId()));
		testEngine.execute(
			new ExecutionRequest(engineTestDescriptor, listener, discoveryRequest.getConfigurationParameters()));
	}

	/**
	 * Records an {@link ExecutionEvent} where a dynamic test was registered.
	 *
	 * @param testDescriptor the descriptor of the newly registered test
	 */
	@Override
	public void dynamicTestRegistered(TestDescriptor testDescriptor) {
		graphBuilder.addEvent(ExecutionEvent.dynamicTestRegistered(testDescriptor));
	}

	/**
	 * Records an {@link ExecutionEvent} for a test (via a {@link TestDescriptor}) that was skipped.
	 *
	 * @param testDescriptor the descriptor of the skipped test or container
	 * @param reason a human-readable message describing why the execution was skipped
	 */
	@Override
	public void executionSkipped(TestDescriptor testDescriptor, String reason) {
		graphBuilder.addEvent(ExecutionEvent.executionSkipped(testDescriptor, reason));
	}

	/**
	 * Records an {@link ExecutionEvent} for a test (via a {@link TestDescriptor}) that started.
	 *
	 * @param testDescriptor the descriptor of the started test or container
	 */
	@Override
	public void executionStarted(TestDescriptor testDescriptor) {
		graphBuilder.addEvent(ExecutionEvent.executionStarted(testDescriptor));
	}

	/**
	 * Records an {@link ExecutionEvent} for a test (via a {@link TestDescriptor}) that completed
	 * with the provided {@link TestExecutionResult}.
	 *
	 * @param testDescriptor the descriptor of the finished test or container
	 * @param testExecutionResult the (unaggregated) result of the execution
	 */
	@Override
	public void executionFinished(TestDescriptor testDescriptor, TestExecutionResult testExecutionResult) {
		graphBuilder.addEvent(ExecutionEvent.executionFinished(testDescriptor, testExecutionResult));
	}

	/**
	 * Records an {@link ExecutionEvent} where a {@link ReportEntry} was published.
	 *
	 * @param testDescriptor the descriptor of the test or container to which the entry belongs
	 * @param entry a {@link ReportEntry} instance to be published
	 */
	@Override
	public void reportingEntryPublished(TestDescriptor testDescriptor, ReportEntry entry) {
		graphBuilder.addEvent(ExecutionEvent.reportingEntryPublished(testDescriptor, entry));
	}

	/**
	 * Gets the state of the engine's execution in the form of a {@link ExecutionsResult}.
	 *
	 * @return the {@link ExecutionsResult} containing all current state information from the engine
	 */
	public ExecutionsResult getExecutionsResult() {
		return graphBuilder.build();
	}

}
