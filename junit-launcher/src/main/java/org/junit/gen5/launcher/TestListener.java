package org.junit.gen5.launcher;

/**
 * @author Stefan Bechtold
 * @since 5.0
 */
// TODO use default methods...
public interface TestListener {
  void testFound(TestIdentifier testIdentifier);
  void testStarted(TestIdentifier testIdentifier);
  void testSkipped(TestIdentifier testIdentifier, Throwable t);
  void testAborted(TestIdentifier testIdentifier, Throwable t);
  void testFailed(TestIdentifier testIdentifier, Throwable t);
  void testSucceeded(TestIdentifier testIdentifier);
}
