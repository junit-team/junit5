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


	public void registerTestPlanExecutionListeners(TestPlanExecutionListener... testListeners) {
		listenerRegistry.registerTestPlanExecutionListeners(testListeners);
		listenerRegistry.registerTestExecutionListeners(testListeners);
	}

	public TestPlan discover(TestPlanSpecification specification) {
		TestPlan testPlan = new TestPlan();
		for (TestEngine testEngine : lookupAllTestEngines()) {
			TestDescriptor engineDescriptor = testEngine.createEngineDescriptor();
			testPlan.addTest(engineDescriptor);
			testPlan.addTests(testEngine.discoverTests(specification, engineDescriptor));
		}
		return testPlan;
	}

	public void execute(TestPlanSpecification specification) {
		TestPlan plan = discover(specification);
		execute(plan);
	}

	private void execute(TestPlan testPlan) {
		listenerRegistry.notifyTestPlanExecutionListeners(
			testPlanExecutionListener -> testPlanExecutionListener.testPlanExecutionStarted(testPlan));

		TestExecutionListener compositeListener = listenerRegistry.getCompositeTestExecutionListener();

		for (TestEngine testEngine : lookupAllTestEngines()) {
			testEngine.execute(testPlan.getAllTestsForTestEngine(testEngine), compositeListener);
		}

		listenerRegistry.notifyTestPlanExecutionListeners(TestPlanExecutionListener::testPlanExecutionFinished);
	}

}
