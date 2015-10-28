
package org.junit.gen5.engine;

/**
 * @author Stefan Bechtold
 * @author Sam Brannen
 * @since 5.0
 */
public interface TestExecutionListener {

	default void testFound(TestDescriptor testDescriptor) {
	};

	default void testStarted(TestDescriptor testDescriptor) {
	};

	default void testSkipped(TestDescriptor testDescriptor, Throwable t) {
	};

	default void testAborted(TestDescriptor testDescriptor, Throwable t) {
	};

	default void testFailed(TestDescriptor testDescriptor, Throwable t) {
	};

	default void testSucceeded(TestDescriptor testDescriptor) {
	};
}