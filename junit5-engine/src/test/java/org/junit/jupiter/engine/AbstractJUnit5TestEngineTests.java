/*
 * Copyright 2015-2016 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.junit.jupiter.engine;

import static org.junit.gen5.engine.discovery.ClassSelector.selectClass;
import static org.junit.gen5.launcher.main.TestDiscoveryRequestBuilder.request;

import org.junit.gen5.engine.ExecutionRequest;
import org.junit.gen5.engine.TestDescriptor;
import org.junit.gen5.engine.UniqueId;
import org.junit.gen5.engine.test.event.ExecutionEventRecorder;
import org.junit.gen5.launcher.TestDiscoveryRequest;

/**
 * Abstract base class for tests involving the {@link JUnit5TestEngine}.
 *
 * @since 5.0
 */
public abstract class AbstractJUnit5TestEngineTests {

	private final JUnit5TestEngine engine = new JUnit5TestEngine();

	protected ExecutionEventRecorder executeTestsForClass(Class<?> testClass) {
		return executeTests(request().selectors(selectClass(testClass)).build());
	}

	protected ExecutionEventRecorder executeTests(TestDiscoveryRequest request) {
		TestDescriptor testDescriptor = discoverTests(request);
		ExecutionEventRecorder eventRecorder = new ExecutionEventRecorder();
		engine.execute(new ExecutionRequest(testDescriptor, eventRecorder, request.getConfigurationParameters()));
		return eventRecorder;
	}

	protected TestDescriptor discoverTests(TestDiscoveryRequest request) {
		return engine.discover(request, UniqueId.forEngine(engine.getId()));
	}

}
