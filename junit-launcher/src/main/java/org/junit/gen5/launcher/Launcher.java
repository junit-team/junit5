/*
 * Copyright 2015 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.junit.gen5.launcher;

import static java.util.stream.Collectors.toSet;
import static java.util.stream.StreamSupport.stream;
import static org.junit.gen5.launcher.TestEngineRegistry.lookupAllTestEngines;

import java.util.Optional;
import java.util.Set;
import java.util.logging.Logger;
import java.util.stream.Stream;

import org.junit.gen5.engine.*;

/**
 * @since 5.0
 */
public class Launcher {

	private static final Logger LOG = Logger.getLogger(Launcher.class.getName());

	private final TestListenerRegistry listenerRegistry = new TestListenerRegistry();

	public void registerTestPlanExecutionListeners(TestExecutionListener... testListeners) {
		listenerRegistry.registerListener(testListeners);
	}

	public TestPlan discover(TestPlanSpecification specification) {
		TestPlan testPlan = new TestPlan();
		for (TestEngine testEngine : lookupAllTestEngines()) {
			LOG.info("Discovering tests in engine " + testEngine.getId());
			TestDescriptor rootTestDescriptor = testEngine.discoverTests(specification);
			testPlan.addTestDescriptorForEngine(testEngine, rootTestDescriptor);
		}
		testPlan.applyFilters(specification);
		testPlan.prune();
		return testPlan;
	}

	public void execute(TestPlanSpecification specification) {
		execute(discover(specification));
	}

	public void execute(TestPlan testPlan) {
		TestPlanExecutionListener testPlanExecutionListener = listenerRegistry.getCompositeTestPlanExecutionListener();
		TestExecutionListener testExecutionListener = listenerRegistry.getCompositeTestExecutionListener();

		testPlanExecutionListener.testPlanExecutionStarted(testPlan);
		for (TestEngine testEngine : getAvailableEngines()) {
			Optional<TestDescriptor> testDescriptorOptional = testPlan.getTestDescriptorFor(testEngine);
			testDescriptorOptional.ifPresent(testDescriptor -> {
				testPlanExecutionListener.testPlanExecutionStartedOnEngine(testPlan, testEngine);
				testEngine.execute(new ExecutionRequest(testDescriptor, testExecutionListener));
				testPlanExecutionListener.testPlanExecutionFinishedOnEngine(testPlan, testEngine);
			});
		}
		testPlanExecutionListener.testPlanExecutionFinished(testPlan);
	}

	public Set<TestEngine> getAvailableEngines() {
		return getTestEngineStream().collect(toSet());
	}

	private Stream<TestEngine> getTestEngineStream() {
		return stream(lookupAllTestEngines().spliterator(), false);
	}

}
