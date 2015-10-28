package org.junit.gen5.console;

import org.junit.gen5.engine.TestDescriptor;
import org.junit.gen5.engine.TestExecutionListener;
import org.junit.gen5.engine.TestPlanExecutionListener;

import java.io.PrintStream;

/**
 * @author Stefan Bechtold
 * @since 5.0
 */
public class TestSummaryReportingTestListener implements TestPlanExecutionListener, TestExecutionListener {

  private final PrintStream out;

  private int testsFound;
  private int testsSkipped;
  private int testsAborted;
  private int testsSucceeded;
  private int testsFailed;

  private long timeStarted;
  private long timePaused;
  private long timeFinished;

  public TestSummaryReportingTestListener(PrintStream out) {
    this.out = out;
  }

  @Override
  public void testPlanExecutionStarted(int numberOfStaticTests) {
    testsFound = numberOfStaticTests;
    timeStarted = System.currentTimeMillis();
  }

  @Override
  public void testPlanExecutionPaused() {
    timePaused = System.currentTimeMillis();
  }

  @Override
  public void testPlanExecutionRestarted() {
    timeStarted += System.currentTimeMillis() - timePaused;
    timePaused = 0;
  }

  @Override
  public void testPlanExecutionStopped() {
    reportSummary("Test run stopped");
  }

  @Override
  public void testPlanExecutionFinished() {
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
  public void dynamicTestFound(TestDescriptor testDescriptor) {
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
