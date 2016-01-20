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

import java.util.logging.Logger;

import org.junit.gen5.engine.ExecutionRequest;
import org.junit.gen5.engine.FilterResult;
import org.junit.gen5.engine.TestDescriptor;
import org.junit.gen5.engine.TestEngine;
import org.junit.gen5.launcher.*;

/**
 * The currently only implementation of {@link Launcher}.
 *
 * <p>External clients get hold of an instance by calling {@link #get}</p>
 *
 * @since 5.0
 * @see Launcher
 * @see DiscoveryRequest
 * @see TestPlan
 * @see TestExecutionListener
 */
public class JUnit5Launcher implements Launcher {

	public static Launcher get() {
		return new JUnit5Launcher();
	}

	private static final Logger LOG = Logger.getLogger(JUnit5Launcher.class.getName());

	private final TestEngineRegistry testEngineRegistry;
	private final TestExecutionListenerRegistry testExecutionListenerRegistry;

	JUnit5Launcher() {
		this(new ServiceLoaderTestEngineRegistry(), new TestExecutionListenerRegistry());
	}

	// For tests only
	JUnit5Launcher(TestEngineRegistry testEngineRegistry) {
		this(testEngineRegistry, new TestExecutionListenerRegistry());
	}

	// For tests only
	JUnit5Launcher(TestEngineRegistry testEngineRegistry, TestExecutionListenerRegistry testExecutionListenerRegistry) {
		this.testEngineRegistry = testEngineRegistry;
		this.testExecutionListenerRegistry = testExecutionListenerRegistry;
	}

	/**
	 * Register one or more listeners for test execution.
	 *
	 * @param listeners the listeners to be notified of test execution events
	 */
	public void registerTestExecutionListeners(TestExecutionListener... listeners) {
		testExecutionListenerRegistry.registerListener(listeners);
	}

	/**
	 * Discover tests and build a {@link TestPlan} according to the supplied
	 * {@link DiscoveryRequest} by querying all registered engines and
	 * collecting their results.
	 *
	 * @param discoveryRequest the discovery request
	 * @return a {@code TestPlan} that contains all resolved
	 *         {@linkplain TestIdentifier identifiers} from all registered engines
	 */
	public TestPlan discover(TestDiscoveryRequest discoveryRequest) {
		return TestPlan.from(discoverRoot(discoveryRequest, "discovery").getEngineDescriptors());
	}

	/**
	 * Execute a {@link TestPlan} which is built according to the supplied
	 * {@link DiscoveryRequest} by querying all registered engines and
	 * collecting their results, and notify {@linkplain #registerTestExecutionListeners
	 * registered listeners} about the progress and results of the execution.
	 *
	 * @param discoveryRequest the discovery request to be executed
	 */
	public void execute(TestDiscoveryRequest discoveryRequest) {
		execute(discoverRoot(discoveryRequest, "execution"));
	}

	private Root discoverRoot(TestDiscoveryRequest discoveryRequest, String phase) {
		Root root = new Root();
		for (TestEngine testEngine : testEngineRegistry.getTestEngines()) {
			if (discoveryRequest.getEngineIdFilters().stream().map(
				engineIdFilter -> engineIdFilter.filter(testEngine.getId())).anyMatch(FilterResult::excluded)) {
				LOG.fine(
					() -> String.format("Test discovery for engine '%s' was skipped due to a filter in phase '%s'.",
						testEngine.getId(), phase));
				continue;
			}

			LOG.fine(() -> String.format("Discovering tests during launcher %s phase in engine '%s'.", phase,
				testEngine.getId()));
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
