
package org.junit.gen5.engine;

/**
 * @author Stefan Bechtold
 * @author Sam Brannen
 * @since 5.0
 */
public interface TestPlanExecutionListener extends TestExecutionListener {

	default void testPlanExecutionStarted(int numberOfStaticTests) {
	};

	default void testPlanExecutionPaused() {
	};

	default void testPlanExecutionRestarted() {
	};

	default void testPlanExecutionStopped() {
	};

	default void testPlanExecutionFinished() {
	};

}