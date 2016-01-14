/*
 * Copyright 2015-2016 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.junit.gen5.engine.junit5;

import static org.junit.gen5.engine.TestPlanSpecification.*;

import org.junit.gen5.api.BeforeEach;
import org.junit.gen5.engine.EngineDescriptor;
import org.junit.gen5.engine.EngineExecutionListener;
import org.junit.gen5.engine.ExecutionEventRecordingEngineExecutionListener;
import org.junit.gen5.engine.ExecutionRequest;
import org.junit.gen5.engine.TestDescriptor;
import org.junit.gen5.engine.TestPlanSpecification;
import org.junit.gen5.engine.TrackingEngineExecutionListener;

/**
 * Abstract base class for tests involving the {@link JUnit5TestEngine}.
 *
 * @since 5.0
 */
abstract class AbstractJUnit5TestEngineTests {

	protected final JUnit5TestEngine engine = new JUnit5TestEngine();

	protected TrackingEngineExecutionListener tracker;
	protected ExecutionEventRecordingEngineExecutionListener eventRecorder;

	private EngineExecutionListener listener;

	@BeforeEach
	void initListeners() {
		tracker = new TrackingEngineExecutionListener();
		eventRecorder = new ExecutionEventRecordingEngineExecutionListener();
		listener = EngineExecutionListener.combine(tracker, eventRecorder);
	}

	protected void executeTestsForClass(Class<?> testClass) {
		executeTests(build(forClass(testClass)));
	}

	protected void executeTests(TestPlanSpecification spec) {
		TestDescriptor testDescriptor = discoverTests(spec);
		engine.execute(new ExecutionRequest(testDescriptor, listener));
	}

	protected EngineDescriptor discoverTests(TestPlanSpecification spec) {
		return engine.discoverTests(spec);
	}

}
