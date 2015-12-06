
package org.junit.gen5.engine.junit5ext;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.gen5.engine.TestPlanSpecification.build;
import static org.junit.gen5.engine.TestPlanSpecification.forClass;
import static org.junit.gen5.engine.junit5ext.ExtensibleJUnit5TestEngine.DISPLAY_NAME;
import static org.junit.gen5.engine.junit5ext.ExtensibleJUnit5TestEngine.ENGINE_ID;

import org.junit.Before;
import org.junit.Test;
import org.junit.gen5.api.Assertions;
import org.junit.gen5.engine.TestDescriptor;
import org.junit.gen5.engine.TestPlanSpecification;
import org.junit.gen5.engine.junit5ext.samples.EmptyTestSampleClass;
import org.junit.gen5.engine.junit5ext.testdoubles.TestResolverRegistrySpy;

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
