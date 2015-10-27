package com.example;

import org.junit.gen5.console.ColoredPrintingTestListener;
import org.junit.gen5.console.TestSummaryReportingTestListener;
import org.junit.gen5.engine.TestPlanConfiguration;
import org.junit.gen5.launcher.Launcher;
import org.junit.gen5.launcher.TestPlan;

public class Console {
  public static void main(String[] args) throws Throwable {
    // TODO Configure launcher?
    Launcher launcher = new Launcher();

    launcher.registerTestListener(new ColoredPrintingTestListener(System.out));
    launcher.registerTestListener(new TestSummaryReportingTestListener(System.out));

    TestPlanConfiguration testPlanConfiguration =
        TestPlanConfiguration.builder()
            .classNames(args)
            .build();

    // TODO Launch parameters: Provide configuration
    TestPlan testPlan = launcher.createTestPlanWithConfiguration(testPlanConfiguration);

    // TODO Provide means to allow manipulation of test plan?
    launcher.execute(testPlan);
  }
}