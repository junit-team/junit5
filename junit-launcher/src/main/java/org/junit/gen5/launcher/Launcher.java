
package org.junit.gen5.launcher;

import static org.junit.gen5.engine.TestListenerRegistry.*;
import static org.junit.gen5.launcher.TestEngineRegistry.*;

import org.junit.gen5.engine.TestEngine;
import org.junit.gen5.engine.TestListenerRegistry;
import org.junit.gen5.engine.TestPlanExecutionListener;
import org.junit.gen5.engine.TestPlanSpecification;

/**
 * @author Stefan Bechtold
 * @author Sam Brannen
 * @since 5.0
 */

public class Launcher {

	public void registerTestPlanExecutionListener(TestPlanExecutionListener testListener) {
		TestListenerRegistry.registerTestPlanExecutionListener(testListener);
	}

	public TestPlan createTestPlanWithConfiguration(TestPlanSpecification specification) {
		TestPlan testPlan = new TestPlan();
		for (TestEngine testEngine : lookupAllTestEngines()) {
			testPlan.addTests(testEngine.discoverTests(specification));
		}
		return testPlan;
	}

	public void execute(TestPlan testPlan) {
		notifyTestPlanExecutionListeners(TestPlanExecutionListener::testPlanExecutionStarted);

		for (TestEngine testEngine : lookupAllTestEngines()) {
			testEngine.execute(testPlan.getAllTestsForTestEngine(testEngine));
		}

		notifyTestPlanExecutionListeners(TestPlanExecutionListener::testPlanExecutionFinished);
	}

}
