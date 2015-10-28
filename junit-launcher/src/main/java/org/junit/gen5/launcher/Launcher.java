
package org.junit.gen5.launcher;

import org.junit.gen5.engine.*;

import static org.junit.gen5.launcher.TestEngineRegistry.lookupAllTestEngines;

/**
 * @author Stefan Bechtold
 * @author Sam Brannen
 * @since 5.0
 */

public class Launcher {

	private TestListenerRegistry listenerRegistry = new TestListenerRegistry();

	public void registerTestPlanExecutionListener(TestPlanExecutionListener testListener) {
		listenerRegistry.registerTestPlanExecutionListener(testListener);
		listenerRegistry.registerTestExecutionListener((TestExecutionListener) testListener);
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
		listenerRegistry.notifyTestPlanExecutionListeners(
				testPlanExecutionListener -> testPlanExecutionListener.testPlanExecutionStarted(testPlan.getTests().size())
		);

		TestExecutionListener compositeListener = new TestExecutionListener() {
			@Override
			public void dynamicTestFound(TestDescriptor testDescriptor) {
				listenerRegistry.notifyTestExecutionListeners(
						testExecutionListener -> testExecutionListener.dynamicTestFound(testDescriptor)
				);
			}

			@Override
			public void testStarted(TestDescriptor testDescriptor) {
				listenerRegistry.notifyTestExecutionListeners(
						testExecutionListener -> testExecutionListener.testStarted(testDescriptor)
				);

			}

			@Override
			public void testSkipped(TestDescriptor testDescriptor, Throwable t) {
				listenerRegistry.notifyTestExecutionListeners(
						testExecutionListener -> testExecutionListener.testSkipped(testDescriptor, t)
				);

			}

			@Override
			public void testAborted(TestDescriptor testDescriptor, Throwable t) {
				listenerRegistry.notifyTestExecutionListeners(
						testExecutionListener -> testExecutionListener.testAborted(testDescriptor, t)
				);

			}

			@Override
			public void testFailed(TestDescriptor testDescriptor, Throwable t) {
				listenerRegistry.notifyTestExecutionListeners(
						testExecutionListener -> testExecutionListener.testFailed(testDescriptor, t)
				);

			}

			@Override
			public void testSucceeded(TestDescriptor testDescriptor) {
				listenerRegistry.notifyTestExecutionListeners(
						testExecutionListener -> testExecutionListener.testSucceeded(testDescriptor)
				);

			}
		};

		for (TestEngine testEngine : lookupAllTestEngines()) {
			testEngine.execute(testPlan.getAllTestsForTestEngine(testEngine), compositeListener);
		}

		listenerRegistry.notifyTestPlanExecutionListeners(TestPlanExecutionListener::testPlanExecutionFinished);
	}

}
