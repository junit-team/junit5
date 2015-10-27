
package org.junit.gen5.engine;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;
import org.junit.gen5.engine.TestPlan.TestPlanBuilder;

/**
 * Unit tests for {@link TestPlanBuilder}.
 *
 * @author Sam Brannen
 * @since 5.0
 */
public class TestPlanBuilderTests {

	@Test
	public void testPlanBuilderDemo() {
		TestPlan testPlan = TestPlan.builder()
				// TODO support generic configuration
				// .configuration(new HashMap<String, String>(){{
				// put("category", "smoke");
				// }})
				.packageNames("org.example.service.impl").include("*Tests")
				// TODO Support engine/test ids or unique ids
				// .descriptorIds("junit5:org.example.UserTests#fullname()")
				.build();

		assertThat(testPlan).isNotNull();
	}

}
