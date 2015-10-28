
package org.junit.gen5.engine;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import lombok.Value;

/**
 * @author Sam Brannen
 * @author Stefan Bechtold
 * @since 5.0
 */
@Value
public final class TestPlanSpecification {
  private List<Class<?>> classes = new LinkedList<>();
  private List<String> classNames = new LinkedList<>();
  private List<String> uniqueIds = new LinkedList<>();

  private TestPlanSpecification() { /* no-op */ }

  public static Builder builder() {
    return new Builder();
  }

  public static final class Builder {

    private TestPlanSpecification specification = new TestPlanSpecification();

    private Builder() {
      /* no-op */
    }

    public Builder classes(Class<?>... classes) {
      Arrays.stream(classes).forEach(testClass -> specification.classes.add(testClass));
      return this;
    }

    public Builder classNames(String... classNames) {
      Arrays.stream(classNames).forEach(className -> specification.classNames.add(className));
      return this;
    }

    public Builder uniqueIds(String... uniqueIds) {
      Arrays.stream(uniqueIds).forEach(uniqueId -> specification.uniqueIds.add(uniqueId));
      return this;
    }

    public TestPlanSpecification build() {
      return specification;
    }
  }
}