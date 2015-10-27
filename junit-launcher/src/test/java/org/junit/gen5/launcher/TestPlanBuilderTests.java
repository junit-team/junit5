
package org.junit.gen5.launcher;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.HashMap;

import org.junit.Test;
import org.junit.gen5.launcher.TestPlan.TestPlanBuilder;

/**
 * Unit tests for {@link TestPlanBuilder}.
 *
 * @author Sam Brannen
 * @since 5.0
 */
public class TestPlanBuilderTests {

	@Test
	public void testPlanBuilderDemo() {
		@SuppressWarnings("serial")
		TestPlan testPlan = TestPlan.builder()
				.configuration(new HashMap<String, String>(){{
					put("category", "smoke");
				}})
				.packageNames("org.example.service.impl")
				.includePatterns("*Tests")
				.descriptorIds("junit5:org.example.UserTests#fullname()")
				.build();

		assertThat(testPlan).isNotNull();
	}

}
