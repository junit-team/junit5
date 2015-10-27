package org.junit.gen5.engine;

import org.junit.Test;

import java.util.HashMap;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for {@link TestPlanConfiguration.Builder}.
 *
 * @author Sam Brannen
 * @since 5.0
 */
public class TestPlanConfigurationBuilderTests {
  @Test
  public void testPlanBuilderDemo() {
    TestPlanConfiguration testPlanConfiguration = TestPlanConfiguration.builder()
        .parameters(new HashMap<String, String>() {{
          put("category", "smoke");
        }})
        .packageNames("org.example.service.impl")
        .includePatterns("*Tests")
        .uniqueIds("junit5:org.example.UserTests#fullname()")
        .build();

    assertThat(testPlanConfiguration).isNotNull();
  }
}