/*
 * Copyright 2015-2016 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.junit.gen5.launcher.main;

import java.util.HashSet;
import java.util.Set;
import java.util.logging.Logger;

import org.junit.gen5.commons.JUnitException;
import org.junit.gen5.engine.ExecutionRequest;
import org.junit.gen5.engine.FilterResult;
import org.junit.gen5.engine.TestDescriptor;
import org.junit.gen5.engine.TestEngine;
import org.junit.gen5.launcher.Launcher;
import org.junit.gen5.launcher.TestDiscoveryRequest;
import org.junit.gen5.launcher.TestExecutionListener;
import org.junit.gen5.launcher.TestPlan;

/**
 * Default implementation of the {@link Launcher} API using dynamically
 * registered test engines.
 *
 * <p>External clients can obtain an instance by invoking {@link #create()}.
 *
 * <p>Test engines are registered at runtime using the
 * {@link java.util.ServiceLoader ServiceLoader} facility. For that purpose, a
 * text file named {@code META-INF/services/org.junit.gen5.engine.TestEngine}
 * has to be added to the engine's JAR file in which the fully qualified name
 * of the implementation class of the {@link org.junit.gen5.engine.TestEngine}
 * interface is stated.
 *
 * @since 5.0
 * @see Launcher
 * @see DiscoveryRequest
 * @see TestPlan
 * @see TestExecutionListener
 */
class DefaultLauncher implements Launcher {

	private static final Logger LOG = Logger.getLogger(DefaultLauncher.class.getName());

	private final TestEngineRegistry testEngineRegistry;
	private final TestExecutionListenerRegistry testExecutionListenerRegistry;

	DefaultLauncher() {
		this(new ServiceLoaderTestEngineRegistry(), new TestExecutionListenerRegistry());
	}

	// For tests only
	DefaultLauncher(TestEngineRegistry testEngineRegistry) {
		this(testEngineRegistry, new TestExecutionListenerRegistry());
	}

	// For tests only
	DefaultLauncher(TestEngineRegistry testEngineRegistry,
			TestExecutionListenerRegistry testExecutionListenerRegistry) {
		this.testEngineRegistry = testEngineRegistry;
		this.testExecutionListenerRegistry = testExecutionListenerRegistry;
	}

	@Override
	public void registerTestExecutionListeners(TestExecutionListener... listeners) {
		testExecutionListenerRegistry.registerListener(listeners);
	}

	@Override
	public TestPlan discover(TestDiscoveryRequest discoveryRequest) {
		return TestPlan.from(discoverRoot(discoveryRequest, "discovery").getEngineDescriptors());
	}

	@Override
	public void execute(TestDiscoveryRequest discoveryRequest) {
		execute(discoverRoot(discoveryRequest, "execution"));
	}

	private Root discoverRoot(TestDiscoveryRequest discoveryRequest, String phase) {
		Root root = new Root();

		Set<String> uniqueEngineIds = new HashSet<>();

		for (TestEngine testEngine : testEngineRegistry.getTestEngines()) {
			final String engineId = testEngine.getId();

			if (discoveryRequest.getEngineIdFilters().stream().map(
				engineIdFilter -> engineIdFilter.filter(engineId)).anyMatch(FilterResult::excluded)) {
				LOG.fine(() -> String.format(
					"Test discovery for engine '%s' was skipped due to a filter in phase '%s'.", engineId, phase));
				continue;
			}

			if (!uniqueEngineIds.add(engineId)) {
				throw new JUnitException(String.format(
					"Failure in launcher: TestEngineRegistry [%s] returned multiple engines with the same ID [%s].",
					testEngineRegistry.getClass().getName(), engineId));
			}

			LOG.fine(
				() -> String.format("Discovering tests during launcher %s phase in engine '%s'.", phase, engineId));
			TestDescriptor engineRoot = testEngine.discoverTests(discoveryRequest);
			root.add(testEngine, engineRoot);
		}
		root.applyPostDiscoveryFilters(discoveryRequest);
		root.prune();
		return root;
	}

	private void execute(Root root) {
		TestPlan testPlan = TestPlan.from(root.getEngineDescriptors());
		TestExecutionListener testExecutionListener = testExecutionListenerRegistry.getCompositeTestExecutionListener();
		testExecutionListener.testPlanExecutionStarted(testPlan);
		ExecutionListenerAdapter engineExecutionListener = new ExecutionListenerAdapter(testPlan,
			testExecutionListener);
		for (TestEngine testEngine : root.getTestEngines()) {
			TestDescriptor testDescriptor = root.getTestDescriptorFor(testEngine);
			testEngine.execute(new ExecutionRequest(testDescriptor, engineExecutionListener));
		}
		testExecutionListener.testPlanExecutionFinished(testPlan);
	}

}
