package org.junit.gen5.console;

import org.junit.gen5.engine.TestDescriptor;
import org.junit.gen5.engine.TestListener;

import java.io.PrintStream;

/**
 * @author Stefan Bechtold
 * @since 5.0
 */
public class TestSummaryReportingTestListener implements TestListener {
  private final PrintStream out;

  int testsFound;
  int testsSkipped;
  int testsAborted;
  int testsSucceeded;
  int testsFailed;

  private long timeStarted;
  private long timePaused;
  private long timeFinished;

  public TestSummaryReportingTestListener(PrintStream out) {
    this.out = out;
  }

  @Override
  public void testExecutionStarted() {
    timeStarted = System.currentTimeMillis();
  }

  @Override
  public void testExecutionPaused() {
    timePaused = System.currentTimeMillis();
  }

  @Override
  public void testExecutionRestarted() {
    timeStarted += System.currentTimeMillis() - timePaused;
    timePaused = 0;
  }

  @Override
  public void testExecutionStopped() {
    reportSummary("Test run stopped");
  }

  @Override
  public void testExecutionFinished() {
    reportSummary("Test run finished");
  }

  private void reportSummary(String msg) {
    timeFinished = System.currentTimeMillis();

    out.println(String.format(
        "%s after %d ms\n" +
            "[%10d tests found     ]\n" +
            "[%10d tests skipped   ]\n" +
            "[%10d tests aborted   ]\n" +
            "[%10d tests failed    ]\n" +
            "[%10d tests successful]\n",
        msg,
        timeFinished - timeStarted,
        testsFound,
        testsSkipped,
        testsAborted,
        testsFailed,
        testsSucceeded
    ));
  }

  @Override
  public void testFound(TestDescriptor testDescriptor) {
    testsFound++;
  }

  @Override
  public void testStarted(TestDescriptor testDescriptor) {
  }

  @Override
  public void testSkipped(TestDescriptor testDescriptor, Throwable t) {
    testsSkipped++;
  }

  @Override
  public void testAborted(TestDescriptor testDescriptor, Throwable t) {
    testsAborted++;
  }

  @Override
  public void testFailed(TestDescriptor testDescriptor, Throwable t) {
    testsFailed++;
  }

  @Override
  public void testSucceeded(TestDescriptor testDescriptor) {
    testsSucceeded++;
  }
}