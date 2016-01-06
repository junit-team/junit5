/*
 * Copyright 2015-2016 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.junit.gen5.engine;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * {@link EngineExecutionListener} that records all events and makes them available to tests.
 *
 * @see ExecutionEvent
 */
public class ExecutionEventRecordingEngineExecutionListener implements EngineExecutionListener {

	public static List<ExecutionEvent> execute(TestEngine testEngine, TestPlanSpecification testPlanSpecification) {
		TestDescriptor engineTestDescriptor = testEngine.discoverTests(testPlanSpecification);
		ExecutionEventRecordingEngineExecutionListener listener = new ExecutionEventRecordingEngineExecutionListener();
		testEngine.execute(new ExecutionRequest(engineTestDescriptor, listener));
		return listener.getExecutionEvents();
	}

	public final List<ExecutionEvent> executionEvents = new CopyOnWriteArrayList<>();

	@Override
	public void dynamicTestRegistered(TestDescriptor testDescriptor) {
		addEvent(ExecutionEvent.dynamicTestRegistered(testDescriptor));
	}

	@Override
	public void executionSkipped(TestDescriptor testDescriptor, String reason) {
		addEvent(ExecutionEvent.executionSkipped(testDescriptor, reason));
	}

	@Override
	public void executionStarted(TestDescriptor testDescriptor) {
		addEvent(ExecutionEvent.executionStarted(testDescriptor));
	}

	@Override
	public void executionFinished(TestDescriptor testDescriptor, TestExecutionResult result) {
		addEvent(ExecutionEvent.executionFinished(testDescriptor, result));
	}

	public List<ExecutionEvent> getExecutionEvents() {
		return executionEvents;
	}

	private void addEvent(ExecutionEvent event) {
		executionEvents.add(event);
	}

}
