package org.junit.gen5.launcher;

import org.junit.gen5.engine.TestDescriptor;
import org.junit.gen5.engine.TestEngine;
import org.junit.gen5.engine.TestPlanConfiguration;

import java.util.List;
import java.util.ServiceLoader;

public class Launcher {
  private volatile ServiceLoader<TestEngine> testEngines;

  public TestPlan createTestPlanWithConfiguration(TestPlanConfiguration configuration) {
    TestPlan testPlan = new TestPlan();
    for (TestEngine testEngine : lookupAllTestEngines()) {
      testPlan.addTests(testEngine.discoverTests(configuration));
    }
    return testPlan;
  }

  public void execute(TestPlan testPlan) {
    for (TestEngine testEngine : lookupAllTestEngines()) {
      List<TestDescriptor> tests = testPlan.getAllTestsForTestEngine(testEngine);
      testEngine.execute(tests);
    }
  }

  private Iterable<TestEngine> lookupAllTestEngines() {
    if (testEngines == null) {
      testEngines = ServiceLoader.load(TestEngine.class);
    }
    return testEngines;
  }
}