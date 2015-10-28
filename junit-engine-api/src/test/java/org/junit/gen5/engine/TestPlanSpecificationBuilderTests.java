package org.junit.gen5.engine;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;

/**
 * Unit tests for {@link TestPlanSpecification.Builder}.
 *
 * @author Sam Brannen
 * @since 5.0
 */
public class TestPlanSpecificationBuilderTests {
  @Test
  public void testPlanBuilderDemo() {
    TestPlanSpecification testPlanConfiguration = TestPlanSpecification.builder()
        .uniqueIds("junit5:org.example.UserTests#fullname()")
        .build();

    assertThat(testPlanConfiguration).isNotNull();
  }
}