
package org.junit.launch;

import org.junit.core.TestDescriptor;

/**
 * @author Sam Brannen
 * @since 5.0
 */
public interface TestPlanListener {

  default void planStarted() throws Exception {}

  default void planPaused() throws Exception {}

  default void planRestarted() throws Exception {}

  default void planStopped() throws Exception {}

  default void planCompleted() throws Exception {}

  default void testAdded(TestDescriptor testDescriptor) throws Exception {}

  default void testStarted(TestDescriptor testDescriptor) throws Exception {}

  // TODO Add source of failure, likely via a TestResult.
  default void testFailed(TestDescriptor testDescriptor) throws Exception {}

  // TODO Add information about why it was skipped, likely via a TestResult.
  default void testSkipped(TestDescriptor testDescriptor) throws Exception {}

  // TODO Add information about why it was aborted, likely via a TestResult.
  default void testAborted(TestDescriptor testDescriptor) throws Exception {}

  // TODO Add details about length of execution, etc., likely via a TestResult.
  default void testCompleted(TestDescriptor testDescriptor) throws Exception {}

}
