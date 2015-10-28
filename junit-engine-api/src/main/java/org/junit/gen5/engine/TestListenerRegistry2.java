
package org.junit.gen5.engine;

import java.util.LinkedList;
import java.util.List;
import java.util.function.Consumer;

/**
 * @author Stefan Bechtold
 * @author Sam Brannen
 * @since 5.0
 */
public class TestListenerRegistry2 {

	private static final List<TestExecutionListener> testExecutionListeners = new LinkedList<>();

	private static final List<TestPlanExecutionListener> testPlanExecutionListeners = new LinkedList<>();


	public static void registerTestExecutionListener(TestExecutionListener listener) {
		testExecutionListeners.add(listener);
	}

	public static void registerTestPlanExecutionListener(TestPlanExecutionListener listener) {
		testPlanExecutionListeners.add(listener);
	}

	public static Iterable<TestExecutionListener> lookupTestExecutionListeners() {
		return testExecutionListeners;
	}

	public static Iterable<TestPlanExecutionListener> lookupTestPlanExecutionListeners() {
		return testPlanExecutionListeners;
	}

	public static void notifyTestExecutionListeners(Consumer<TestExecutionListener> consumer) {
		lookupTestExecutionListeners().forEach(consumer);
	}

	public static void notifyTestPlanExecutionListeners(Consumer<TestPlanExecutionListener> consumer) {
		lookupTestPlanExecutionListeners().forEach(consumer);
	}

}
