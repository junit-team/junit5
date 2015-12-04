
package org.junit.gen5.engine.junit5ext;

import static org.junit.gen5.engine.TestPlanSpecification.build;
import static org.junit.gen5.engine.TestPlanSpecification.forClass;
import static org.assertj.core.api.Assertions.*;

import org.junit.Test;
import org.junit.gen5.api.Assertions;
import org.junit.gen5.engine.TestDescriptor;
import org.junit.gen5.engine.junit5ext.samples.EmptyTestSampleClass;

public class ExtensibleJUnit5TestEngineClassLevelTests {
	private ExtensibleJUnit5TestEngine testEngine = new ExtensibleJUnit5TestEngine();

	@Test
	public void givenEmptyTestClass_discoverTestsReturnsOnlyTestEngineDescriptor() throws Exception {
		TestDescriptor testDescriptor = testEngine.discoverTests(build(forClass(EmptyTestSampleClass.class)));

		Assertions.assertAll(
			() -> assertThat(testDescriptor.getUniqueId()).isEqualTo(testEngine.getId()),
			() -> assertThat(testDescriptor.getChildren()).isEmpty()
		);
	}
}
