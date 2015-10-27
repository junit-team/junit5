package org.junit.gen5.engine;

/**
 * @author Stefan Bechtold
 * @since 5.0
 */
public interface TestListener {
  default void testExecutionStarted() {};
  default void testExecutionPaused() {};
  default void testExecutionRestarted() {};
  default void testExecutionStopped() {};
  default void testExecutionFinished() {};

  default void testFound(TestDescriptor testDescriptor) {};
  default void testStarted(TestDescriptor testDescriptor) {};
  default void testSkipped(TestDescriptor testDescriptor, Throwable t) {};
  default void testAborted(TestDescriptor testDescriptor, Throwable t) {};
  default void testFailed(TestDescriptor testDescriptor, Throwable t) {};
  default void testSucceeded(TestDescriptor testDescriptor) {};
}