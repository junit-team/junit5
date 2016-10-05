/*
 * Copyright 2015-2016 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.junit.platform.launcher.core;

import java.util.HashSet;
import java.util.Set;
import java.util.logging.Logger;

import org.junit.platform.commons.JUnitException;
import org.junit.platform.commons.util.Preconditions;
import org.junit.platform.engine.ConfigurationParameters;
import org.junit.platform.engine.ExecutionRequest;
import org.junit.platform.engine.FilterResult;
import org.junit.platform.engine.TestDescriptor;
import org.junit.platform.engine.TestEngine;
import org.junit.platform.engine.UniqueId;
import org.junit.platform.launcher.Launcher;
import org.junit.platform.launcher.LauncherDiscoveryRequest;
import org.junit.platform.launcher.TestExecutionListener;
import org.junit.platform.launcher.TestPlan;

/**
 * Default implementation of the {@link Launcher} API.
 *
 * <p>External clients can obtain an instance by invoking {@link LauncherFactory#create()}.
 *
 * @since 1.0
 * @see Launcher
 * @see LauncherFactory
 */
class DefaultLauncher implements Launcher {

	private static final Logger LOG = Logger.getLogger(DefaultLauncher.class.getName());

	private final TestExecutionListenerRegistry listenerRegistry = new TestExecutionListenerRegistry();
	private final Iterable<TestEngine> testEngines;

	/**
	 * Construct a new {@code DefaultLauncher} with the supplied test engines.
	 *
	 * @param testEngines the test engines to delegate to; never {@code null} or empty
	 */
	DefaultLauncher(Iterable<TestEngine> testEngines) {
		Preconditions.condition(testEngines != null && testEngines.iterator().hasNext(),
			() -> "Cannot create Launcher without at least one TestEngine; "
					+ "consider adding an engine implementation JAR to the classpath");
		this.testEngines = validateUniqueIds(testEngines);
	}

	private static Iterable<TestEngine> validateUniqueIds(Iterable<TestEngine> testEngines) {
		Set<String> ids = new HashSet<>();
		for (TestEngine testEngine : testEngines) {
			if (!ids.add(testEngine.getId())) {
				throw new JUnitException(String.format(
					"Cannot create Launcher for multiple engines with the same ID '%s'.", testEngine.getId()));
			}
		}
		return testEngines;
	}

	@Override
	public void registerTestExecutionListeners(TestExecutionListener... listeners) {
		Preconditions.notEmpty(listeners, "listeners array must not be null or empty");
		Preconditions.containsNoNullElements(listeners, "individual listeners must not be null");
		this.listenerRegistry.registerListeners(listeners);
	}

	@Override
	public TestPlan discover(LauncherDiscoveryRequest discoveryRequest) {
		Preconditions.notNull(discoveryRequest, "LauncherDiscoveryRequest must not be null");
		return TestPlan.from(discoverRoot(discoveryRequest, "discovery").getEngineDescriptors());
	}

	@Override
	public void execute(LauncherDiscoveryRequest discoveryRequest) {
		Preconditions.notNull(discoveryRequest, "LauncherDiscoveryRequest must not be null");
		execute(discoverRoot(discoveryRequest, "execution"), discoveryRequest.getConfigurationParameters());
	}

	private Root discoverRoot(LauncherDiscoveryRequest discoveryRequest, String phase) {
		Root root = new Root();

		for (TestEngine testEngine : this.testEngines) {
			// @formatter:off
			boolean engineIsExcluded = discoveryRequest.getEngineFilters().stream()
					.map(engineFilter -> engineFilter.apply(testEngine))
					.anyMatch(FilterResult::excluded);
			// @formatter:on

			if (engineIsExcluded) {
				LOG.fine(() -> String.format(
					"Test discovery for engine '%s' was skipped due to an EngineFilter in phase '%s'.",
					testEngine.getId(), phase));
				continue;
			}

			LOG.fine(() -> String.format("Discovering tests during Launcher %s phase in engine '%s'.", phase,
				testEngine.getId()));

			UniqueId uniqueEngineId = UniqueId.forEngine(testEngine.getId());
			TestDescriptor engineRoot = testEngine.discover(discoveryRequest, uniqueEngineId);
			Preconditions.notNull(engineRoot,
				() -> String.format(
					"The discover() method for TestEngine with ID '%s' must return a non-null root TestDescriptor.",
					testEngine.getId()));
			root.add(testEngine, engineRoot);
		}
		root.applyPostDiscoveryFilters(discoveryRequest);
		root.prune();
		return root;
	}

	private void execute(Root root, ConfigurationParameters configurationParameters) {
		TestPlan testPlan = TestPlan.from(root.getEngineDescriptors());
		TestExecutionListener testExecutionListener = this.listenerRegistry.getCompositeTestExecutionListener();
		testExecutionListener.testPlanExecutionStarted(testPlan);
		ExecutionListenerAdapter engineExecutionListener = new ExecutionListenerAdapter(testPlan,
			testExecutionListener);
		for (TestEngine testEngine : root.getTestEngines()) {
			TestDescriptor testDescriptor = root.getTestDescriptorFor(testEngine);
			testEngine.execute(new ExecutionRequest(testDescriptor, engineExecutionListener, configurationParameters));
		}
		testExecutionListener.testPlanExecutionFinished(testPlan);
	}

}
