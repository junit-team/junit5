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

import java.util.List;

import org.junit.gen5.engine.*;

/**
 * @author Stefan Bechtold
 * @author Sam Brannen
 * @since 5.0
 */
public class Launcher {

	private final TestListenerRegistry listenerRegistry = new TestListenerRegistry();

	public void registerListeners(TestExecutionListener... testListeners) {
		listenerRegistry.registerListener(testListeners);
	}

	public TestPlan discover(TestPlanSpecification specification) {
		TestPlan testPlan = new TestPlan();
		for (TestEngine testEngine : lookupAllTestEngines()) {
			testPlan.addTests(testEngine.discoverTests(specification));
		}
		return testPlan;
	}

	public void execute(TestPlanSpecification specification) {
		TestPlanExecutionListener testPlanExecutionListener = listenerRegistry.getCompositeTestPlanExecutionListener();
		TestExecutionListener testExecutionListener = listenerRegistry.getCompositeTestExecutionListener();

		TestPlan plan = discover(specification);

		testPlanExecutionListener.testPlanExecutionStarted(plan);
		for (TestEngine testEngine : lookupAllTestEngines()) {
			List<TestDescriptor> testDescriptors = plan.getAllTestsForTestEngine(testEngine);
			testEngine.execute(new TestExecutionContext(testDescriptors, testExecutionListener));
		}
		testPlanExecutionListener.testPlanExecutionFinished(plan);
	}
}
