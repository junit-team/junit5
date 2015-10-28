package org.junit.gen5.engine;

import java.util.Collection;

public interface TestEngine {
  default String getId() {
    return getClass().getCanonicalName();
  }

  Collection<TestDescriptor> discoverTests(TestPlanSpecification configuration);

  default boolean supports(TestDescriptor testDescriptor) {
    return testDescriptor.getUniqueId().startsWith(getId());
  }

  default boolean supportsAll(Collection<TestDescriptor> testDescriptors) {
    return testDescriptors.stream()
        .allMatch(testDescriptor -> supports(testDescriptor));
  }

  void execute(Collection<TestDescriptor> testDescriptions, TestExecutionListener testExecutionListener);
}
