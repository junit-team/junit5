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

import static org.junit.gen5.launcher.TestEngineRegistry.lookupAllTestEngines;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.junit.gen5.engine.EngineDescriptor;
import org.junit.gen5.engine.EngineExecutionContext;
import org.junit.gen5.engine.TestDescriptor;
import org.junit.gen5.engine.TestEngine;
import org.junit.gen5.engine.TestExecutionListener;
import org.junit.gen5.engine.TestPlanSpecification;

/**
 * @author Stefan Bechtold
 * @author Sam Brannen
 * @since 5.0
 */
public class Launcher {

	private final TestListenerRegistry listenerRegistry = new TestListenerRegistry();

	public void registerTestPlanExecutionListeners(TestExecutionListener... testListeners) {
		listenerRegistry.registerListener(testListeners);
	}

	public TestPlan discover(TestPlanSpecification specification) {
		TestPlan testPlan = new TestPlan();
		for (TestEngine testEngine : lookupAllTestEngines()) {
			EngineDescriptor engineDescriptor = new EngineDescriptor(testEngine);
			Collection<TestDescriptor> testDescriptors = testEngine.discoverTests(specification, engineDescriptor);
			if (!testDescriptors.isEmpty()) {
				Set<TestDescriptor> descriptorCandidates = findFilteredCandidates(specification, testDescriptors);
				descriptorCandidates.add(engineDescriptor);
				Set<TestDescriptor> prunedDescriptors = pruneAllWithoutConcreteTests(descriptorCandidates);
				testPlan.addTestDescriptors(prunedDescriptors);
			}
		}
		return testPlan;
	}

	protected Set<TestDescriptor> findFilteredCandidates(TestPlanSpecification specification,
			Collection<TestDescriptor> testDescriptors) {
		// @formatter:off
		return testDescriptors.stream()
				.filter((descriptor) -> !descriptor.isTest() || specification.acceptDescriptor(descriptor))
				.collect(Collectors.toSet());
		// @formatter:on
	}

	private Set<TestDescriptor> pruneAllWithoutConcreteTests(Set<TestDescriptor> descriptorCandidates) {
		Set<TestDescriptor> included = new HashSet<>();
		descriptorCandidates.stream().filter(
			descriptor -> descriptor.isTest() || included.contains(descriptor)).forEach(descriptor -> {
				included.add(descriptor);
				included.add(descriptor.getParent());
			});
		return included;
	}

	public void execute(TestPlanSpecification specification) {
		execute(discover(specification));
	}

	public void execute(TestPlan testPlan) {
		TestPlanExecutionListener testPlanExecutionListener = listenerRegistry.getCompositeTestPlanExecutionListener();
		TestExecutionListener testExecutionListener = listenerRegistry.getCompositeTestExecutionListener();

		testPlanExecutionListener.testPlanExecutionStarted(testPlan);
		for (TestEngine testEngine : lookupAllTestEngines()) {
			testPlanExecutionListener.testPlanExecutionStartedOnEngine(testPlan, testEngine);
			List<TestDescriptor> testDescriptors = testPlan.getAllTestDescriptorsForTestEngine(testEngine);
			testEngine.execute(new EngineExecutionContext(testDescriptors, testExecutionListener));
			testPlanExecutionListener.testPlanExecutionFinishedOnEngine(testPlan, testEngine);
		}
		testPlanExecutionListener.testPlanExecutionFinished(testPlan);
	}
}
