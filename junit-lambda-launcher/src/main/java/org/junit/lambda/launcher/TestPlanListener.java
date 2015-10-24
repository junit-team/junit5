package org.junit.lambda.launcher;

/**
 * @author Sam Brannen
 * @since 5.0
 */
public interface TestPlanListener {

	default void planStarted() throws Exception {
	}

	default void planPaused() throws Exception {
	}

	default void planRestarted() throws Exception {
	}

	default void planStopped() throws Exception {
	}

	default void planCompleted() throws Exception {
	}

	default void testAdded() throws Exception {
	}

	default void testStarted() throws Exception {
	}

	default void testFailed() throws Exception {
	}

	default void testSkipped() throws Exception {
	}

	default void testAborted() throws Exception {
	}

	default void testCompleted() throws Exception {
	}

}
