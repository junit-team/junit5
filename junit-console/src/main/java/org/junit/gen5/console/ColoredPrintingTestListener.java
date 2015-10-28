package org.junit.gen5.console;


import org.junit.gen5.engine.TestDescriptor;
import org.junit.gen5.engine.TestListener;

import java.io.PrintStream;

/**
 * @author Stefan Bechtold
 * @since 5.0
 */
public class ColoredPrintingTestListener implements TestListener {
  public static final String ANSI_RESET = "\u001B[0m";
  public static final String ANSI_BLACK = "\u001B[30m";
  public static final String ANSI_RED = "\u001B[31m";
  public static final String ANSI_GREEN = "\u001B[32m";
  public static final String ANSI_YELLOW = "\u001B[33m";
  public static final String ANSI_BLUE = "\u001B[34m";
  public static final String ANSI_PURPLE = "\u001B[35m";
  public static final String ANSI_CYAN = "\u001B[36m";
  public static final String ANSI_WHITE = "\u001B[37m";

  private final PrintStream out;

  public ColoredPrintingTestListener(PrintStream out) {
    this.out = out;
  }

  @Override
  public void testPlanExecutionStarted() {
    out.println("Test execution started.");
  }

  @Override
  public void testPlanExecutionPaused() {
    out.println("Test execution paused.");
  }

  @Override
  public void testPlanExecutionRestarted() {
    out.println("Test execution continued.");
  }

  @Override
  public void testPlanExecutionStopped() {
    out.println("Test execution canceled.");
  }

  @Override
  public void testPlanExecutionFinished() {
    out.println(ANSI_RESET);
  }

  @Override
  public void testFound(TestDescriptor testDescriptor) {
    out.print(ANSI_GREEN);
    out.format("Test found:     %s", testDescriptor.toString());
    out.println(ANSI_RESET);
  }

  @Override
  public void testStarted(TestDescriptor testDescriptor) {
    out.print(ANSI_GREEN);
    out.format("Test started:   %s", testDescriptor.toString());
    out.println(ANSI_RESET);
  }

  @Override
  public void testSkipped(TestDescriptor testDescriptor, Throwable t) {
    out.print(ANSI_YELLOW);
    out.format("Test skipped:   %s\n=> Exception:   %s", testDescriptor.toString(), (t != null) ? t.getLocalizedMessage() : "none");
    out.println(ANSI_RESET);
  }

  @Override
  public void testAborted(TestDescriptor testDescriptor, Throwable t) {
    out.print(ANSI_YELLOW);
    out.format("Test aborted:   %s\n=> Exception:   %s", testDescriptor.toString(), (t != null) ? t.getLocalizedMessage() : "none");
    out.println(ANSI_RESET);
  }

  @Override
  public void testFailed(TestDescriptor testDescriptor, Throwable t) {
    out.print(ANSI_RED);
    out.format("Test failed:    %s\n=> Exception:   %s", testDescriptor.toString(), t.getLocalizedMessage());
    out.println(ANSI_RESET);
  }

  @Override
  public void testSucceeded(TestDescriptor testDescriptor) {
    out.print(ANSI_GREEN);
    out.format("Test succeeded: %s", testDescriptor.toString());
    out.println(ANSI_RESET);
  }
}
