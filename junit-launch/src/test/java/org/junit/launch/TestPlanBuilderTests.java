
package org.junit.launch;

import java.util.HashMap;

import org.junit.Test;
import org.junit.core.TestDescriptor;
import org.junit.launch.TestPlan.TestPlanBuilder;

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
				.listeners(new ConsoleLoggingListener())
				.build();

		testPlan.start();
		System.out.println("\tTest plan is active: " + testPlan.isActive());

		testPlan.pause();
		System.out.println("\tTest plan is paused: " + testPlan.isPaused());

		testPlan.restart();
		System.out.println("\tTest plan is active: " + testPlan.isActive());
		System.out.println("\tTest plan is paused: " + testPlan.isPaused());

		testPlan.stop();
		System.out.println("\tTest plan is stopped: " + testPlan.isStopped());
	}


	static class ConsoleLoggingListener implements TestPlanListener {

		@Override
		public void testCompleted(TestDescriptor testDescriptor) throws Exception {
			System.out.println("Test completed for " + testDescriptor);
		}
	}

}
