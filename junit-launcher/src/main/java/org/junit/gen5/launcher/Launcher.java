package org.junit.gen5.launcher;

import org.junit.gen5.engine.TestEngine;
import org.junit.gen5.engine.TestListener;
import org.junit.gen5.engine.TestListenerRegistry;
import org.junit.gen5.engine.TestPlanConfiguration;

import java.util.ServiceLoader;

import static org.junit.gen5.engine.TestEngineRegistry.lookupAllTestEngines;
import static org.junit.gen5.engine.TestListenerRegistry.notifyListeners;

public class Launcher {
  private volatile ServiceLoader<TestEngine> testEngines;

  public void registerTestListener(TestListener testListener) {
    TestListenerRegistry.registerListener(testListener);
  }

  public TestPlan createTestPlanWithConfiguration(TestPlanConfiguration configuration) {
    TestPlan testPlan = new TestPlan();
    for (TestEngine testEngine : lookupAllTestEngines()) {
      testPlan.addTests(testEngine.discoverTests(configuration));
    }
    return testPlan;
  }

  public void execute(TestPlan testPlan) {
    notifyListeners(TestListener::testExecutionStarted);

    for (TestEngine testEngine : lookupAllTestEngines()) {
      testEngine.execute(testPlan.getAllTestsForTestEngine(testEngine));
    }

    notifyListeners(TestListener::testExecutionFinished);
  }
}