
package org.junit.gen5.engine.junit5ext;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.gen5.engine.TestPlanSpecification.build;
import static org.junit.gen5.engine.TestPlanSpecification.forClass;
import static org.junit.gen5.engine.junit5ext.ExtensibleJUnit5TestEngine.DISPLAY_NAME;
import static org.junit.gen5.engine.junit5ext.ExtensibleJUnit5TestEngine.ENGINE_ID;

import org.junit.Before;
import org.junit.Test;
import org.junit.gen5.api.Assertions;
import org.junit.gen5.engine.ExecutionRequest;
import org.junit.gen5.engine.TestDescriptor;
import org.junit.gen5.engine.TestExecutionListener;
import org.junit.gen5.engine.TestPlanSpecification;
import org.junit.gen5.engine.junit5ext.samples.EmptyTestSampleClass;
import org.junit.gen5.engine.junit5ext.samples.SinglePassingTestSampleClass;
import org.junit.gen5.engine.junit5ext.testdoubles.TestExecutionListenerSpy;
import org.junit.gen5.engine.junit5ext.testdoubles.TestExecutorRegistrySpy;
import org.junit.gen5.engine.junit5ext.testdoubles.TestResolverRequest;
import org.junit.gen5.engine.junit5ext.testdoubles.TestResolverRegistrySpy;

public class ExtensibleJUnit5TestEngineClassLevelTests {
	private ExtensibleJUnit5TestEngine testEngine = new ExtensibleJUnit5TestEngine();
	private TestResolverRegistrySpy testResolverRegistrySpy = new TestResolverRegistrySpy();
	private TestExecutorRegistrySpy testExecutorRegistrySpy = new TestExecutorRegistrySpy();

	@Before
	public void setUp() throws Exception {
		testEngine.setTestResolverRegistry(testResolverRegistrySpy);
		testEngine.setTestExecutorRegistry(testExecutorRegistrySpy);
	}

	@Test
	public void givenEmptyTestClass_discoverTestsReturnsOnlyTestEngineDescriptor() throws Exception {
		TestPlanSpecification testPlanSpecification = build(forClass(EmptyTestSampleClass.class));
		TestDescriptor testDescriptor = testEngine.discoverTests(testPlanSpecification);

		Assertions.assertAll(() -> assertThat(testDescriptor.getUniqueId()).isEqualTo(ENGINE_ID),
			() -> assertThat(testDescriptor.getChildren()).isEmpty());
	}

	@Test
	public void givenEmptyTestClass_discoverTestsNotifiesTestResolverRegistry() throws Exception {
		TestPlanSpecification testPlanSpecification = build(forClass(EmptyTestSampleClass.class));
		testEngine.discoverTests(testPlanSpecification);

		assertThat(testResolverRegistrySpy.notifications).hasSize(1);

		TestResolverRequest notification = testResolverRegistrySpy.notifications.get(0);
		Assertions.assertAll(
				() -> assertThat(notification.parent.getUniqueId()).isEqualTo(ENGINE_ID),
				() -> assertThat(notification.parent.getDisplayName()).isEqualTo(DISPLAY_NAME),
				() -> assertThat(notification.parent.getChildren()).isEmpty(),
				() -> assertThat(notification.testPlanSpecification).isEqualTo(testPlanSpecification)
		);
	}

	@Test
	public void givenExecutionRequest_engineNotifiesStartAndEnd() throws Exception {
		TestPlanSpecification testPlanSpecification = build(forClass(SinglePassingTestSampleClass.class));
		TestDescriptor testDescriptor = testEngine.discoverTests(testPlanSpecification);

		TestExecutionListenerSpy testExecutionListenerSpy = new TestExecutionListenerSpy();

		ExecutionRequest executionRequest = new ExecutionRequest(testDescriptor, testExecutionListenerSpy);
		testEngine.execute(executionRequest);

		assertThat(testExecutorRegistrySpy.testDescriptors).contains(testDescriptor);
	}
}
