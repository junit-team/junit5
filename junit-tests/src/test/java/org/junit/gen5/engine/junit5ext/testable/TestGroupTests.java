
package org.junit.gen5.engine.junit5ext.testable;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.gen5.engine.TestPlanSpecification.build;

import org.junit.Test;

public class TestGroupTests {
	@Test
	public void ensureThat_isTest_alwaysEvaluatesTo_False_ForATestGroup() throws Exception {
		TestGroup testGroup = new TestGroup(null, null);
		assertThat(testGroup.isTest()).isFalse();
	}

	@Test
	public void givenAnUniqueTestId_TestGroupReturnsTestId() throws Exception {
		TestGroup testGroup = new TestGroup("testID", null);
		assertThat(testGroup.getUniqueId()).isEqualTo("testID");
	}

	@Test
	public void givenADisplayName_TestGroupReturnsDisplayName() throws Exception {
		TestGroup testGroup = new TestGroup(null, "A descriptive display name");
		assertThat(testGroup.getDisplayName()).isEqualTo("A descriptive display name");
	}
}
