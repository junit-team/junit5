/*
 * Copyright 2015 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.junit.gen5.engine.junit5;

import static org.junit.gen5.engine.TestPlanSpecification.build;
import static org.junit.gen5.engine.TestPlanSpecification.forClass;

import org.junit.gen5.engine.ExecutionRequest;
import org.junit.gen5.engine.TestDescriptor;
import org.junit.gen5.engine.TestPlanSpecification;

/**
 * Abstract base class for tests involving the {@link JUnit5TestEngine}.
 *
 * @since 5.0
 */
abstract class AbstractJUnit5TestEngineTestCase {

	protected final JUnit5TestEngine engine = new JUnit5TestEngine();

	protected TrackingTestExecutionListener executeTestsForClass(Class<?> testClass, int expectedDescriptorCount) {
		TestPlanSpecification spec = build(forClass(testClass));
		return executeTests(spec, expectedDescriptorCount);
	}

	protected TrackingTestExecutionListener executeTests(TestPlanSpecification spec, int expectedDescriptorCount) {
		TestDescriptor testDescriptor = discoverTests(spec);
		TrackingTestExecutionListener listener = new TrackingTestExecutionListener();
		engine.execute(new ExecutionRequest(testDescriptor, listener));
		return listener;
	}

	private TestDescriptor discoverTests(TestPlanSpecification spec) {
		return engine.discoverTests(spec);
	}

}
