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

import static org.junit.gen5.api.Assertions.assertEquals;
import static org.junit.gen5.engine.TestPlanSpecification.*;

import java.util.List;

import org.junit.gen5.engine.ExecutionEvent;
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

	protected TrackingEngineExecutionListener executeTestsForClass(Class<?> testClass, int expectedDescriptorCount) {
		TestPlanSpecification spec = build(forClass(testClass));
		return executeTests(spec, expectedDescriptorCount);
	}

	protected TrackingEngineExecutionListener executeTests(TestPlanSpecification spec, int expectedDescriptorCount) {
		TestDescriptor testDescriptor = discoverTests(spec);
		assertEquals(expectedDescriptorCount, testDescriptor.allDescendants().size());
		TrackingEngineExecutionListener listener = new TrackingEngineExecutionListener();
		engine.execute(new ExecutionRequest(testDescriptor, listener));
		return listener;
	}

	protected List<ExecutionEvent> executeTestsAndRecordEvents(TestPlanSpecification spec,
			int expectedDescriptorCount) {
		TestDescriptor testDescriptor = discoverTests(spec);
		assertEquals(expectedDescriptorCount, testDescriptor.allDescendants().size());
		ExecutionEventRecordingEngineExecutionListener listener = new ExecutionEventRecordingEngineExecutionListener();
		engine.execute(new ExecutionRequest(testDescriptor, listener));
		return listener.getExecutionEvents();
	}

	private TestDescriptor discoverTests(TestPlanSpecification spec) {
		return engine.discoverTests(spec);
	}

}
