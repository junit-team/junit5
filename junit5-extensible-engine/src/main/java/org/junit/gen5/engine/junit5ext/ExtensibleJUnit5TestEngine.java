package org.junit.gen5.engine.junit5ext;

import org.junit.gen5.engine.*;

public class ExtensibleJUnit5TestEngine implements TestEngine {
  @Override public String getId() {
    return "junit5ext";
  }

  @Override public String toString() {
    return "JUnit5 Engine (extensible)";
  }

  @Override
  public TestDescriptor discoverTests(TestPlanSpecification specification) {
    return new GroupingTestDescriptor(getId(), toString());
  }

  @Override
  public void execute(ExecutionRequest request) {
    throw new UnsupportedOperationException("Method has not been implemented, yet!");
  }
}
