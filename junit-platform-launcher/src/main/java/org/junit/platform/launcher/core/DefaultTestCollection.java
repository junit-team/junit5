/*
 * Copyright 2015-2017 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v20.html
 */

package org.junit.platform.launcher.core;

import java.util.Arrays;

import org.junit.platform.commons.logging.Logger;
import org.junit.platform.commons.logging.LoggerFactory;
import org.junit.platform.commons.util.BlacklistedExceptions;
import org.junit.platform.commons.util.Preconditions;
import org.junit.platform.engine.ConfigurationParameters;
import org.junit.platform.engine.ExecutionRequest;
import org.junit.platform.engine.TestDescriptor;
import org.junit.platform.engine.TestEngine;
import org.junit.platform.launcher.Launcher;
import org.junit.platform.launcher.PostDiscoveryFilter;
import org.junit.platform.launcher.TestCollection;
import org.junit.platform.launcher.TestExecutionListener;
import org.junit.platform.launcher.TestPlan;

/**
 * Default implementation of the {@link TestCollection} API.
 *
 * @since 1.1
 * @see Launcher
 */
class DefaultTestCollection implements TestCollection {

	private static final Logger logger = LoggerFactory.getLogger(DefaultTestCollection.class);

	private final Root root;
	private final TestExecutionListenerRegistry listenerRegistry;
	private final ConfigurationParameters configurationParameters;

	DefaultTestCollection(Root root, TestExecutionListenerRegistry listenerRegistry,
			ConfigurationParameters configurationParameters) {
		this.root = root;
		this.listenerRegistry = new TestExecutionListenerRegistry(listenerRegistry);
		this.configurationParameters = configurationParameters;
	}

	@Override
	public TestPlan testPlan() {
		return TestPlan.from(root.getEngineDescriptors());
	}

	@Override
	public void applyPostDiscoveryFilters(PostDiscoveryFilter... filters) {
		root.applyPostDiscoveryFilters(Arrays.asList(filters));
	}

	@Override
	public void execute(TestExecutionListener... listeners) {
		Preconditions.notNull(listeners, "TestExecutionListener array must not be null");
		Preconditions.containsNoNullElements(listeners, "individual listeners must not be null");
		TestExecutionListener testExecutionListener = buildListenerRegistryForExecution(
			listeners).getCompositeTestExecutionListener();
		TestPlan testPlan = testPlan();
		ExecutionListenerAdapter engineExecutionListener = new ExecutionListenerAdapter(testPlan,
			testExecutionListener);

		testExecutionListener.testPlanExecutionStarted(testPlan);
		for (TestEngine testEngine : root.getTestEngines()) {
			TestDescriptor testDescriptor = root.getTestDescriptorFor(testEngine);
			execute(testEngine, new ExecutionRequest(testDescriptor, engineExecutionListener, configurationParameters));
		}
		testExecutionListener.testPlanExecutionFinished(testPlan);
	}

	private TestExecutionListenerRegistry buildListenerRegistryForExecution(TestExecutionListener... listeners) {
		if (listeners.length == 0) {
			return this.listenerRegistry;
		}
		TestExecutionListenerRegistry registry = new TestExecutionListenerRegistry(this.listenerRegistry);
		registry.registerListeners(listeners);
		return registry;
	}

	private static void execute(TestEngine testEngine, ExecutionRequest executionRequest) {
		try {
			testEngine.execute(executionRequest);
		}
		catch (Throwable throwable) {
			handleThrowable(testEngine, throwable);
		}
	}

	private static void handleThrowable(TestEngine testEngine, Throwable throwable) {
		logger.warn(throwable,
			() -> String.format("TestEngine with ID '%s' failed to execute tests", testEngine.getId()));
		BlacklistedExceptions.rethrowIfBlacklisted(throwable);
	}
}
