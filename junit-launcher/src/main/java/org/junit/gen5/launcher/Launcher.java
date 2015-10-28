
package org.junit.gen5.launcher;

import org.junit.gen5.engine.TestEngine;
import org.junit.gen5.engine.TestListenerRegistry;
import org.junit.gen5.engine.TestPlanExecutionListener;
import org.junit.gen5.engine.TestPlanSpecification;

import static org.junit.gen5.engine.TestListenerRegistry.notifyTestPlanExecutionListeners;
import static org.junit.gen5.launcher.TestEngineRegistry.lookupAllTestEngines;

/**
 * @author Stefan Bechtold
 * @author Sam Brannen
 * @since 5.0
 */

public class Launcher {

	public void registerTestPlanExecutionListener(TestPlanExecutionListener testListener) {
		TestListenerRegistry.registerTestPlanExecutionListener(testListener);
	}

	public TestPlan discover(TestPlanSpecification specification) {
		TestPlan testPlan = new TestPlan();
		for (TestEngine testEngine : lookupAllTestEngines()) {
			testPlan.addTests(testEngine.discoverTests(specification));
		}
		return testPlan;
	}

	public void execute(TestPlanSpecification specification) {
		TestPlan plan = discover(specification);
		execute(plan);
	}

	private void execute(TestPlan testPlan) {
		notifyTestPlanExecutionListeners(
				testPlanExecutionListener -> testPlanExecutionListener.testPlanExecutionStarted(testPlan.getTests().size())
		);

		for (TestEngine testEngine : lookupAllTestEngines()) {
			testEngine.execute(testPlan.getAllTestsForTestEngine(testEngine));
		}

		notifyTestPlanExecutionListeners(TestPlanExecutionListener::testPlanExecutionFinished);
	}

}
