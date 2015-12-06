
package org.junit.gen5.engine.junit5ext;

import org.junit.Test;
import org.junit.gen5.engine.MutableTestDescriptor;
import org.junit.gen5.engine.TestPlanSpecification;
import org.junit.gen5.engine.junit5ext.testdoubles.TestResolverRequest;
import org.junit.gen5.engine.junit5ext.testdoubles.TestResolverSpy;

import static org.assertj.core.api.Assertions.assertThat;

public class TestResolverRegistryImplTests {
	private TestGroup testGroup = new TestGroup("testGroup", "Test Group");
	private TestPlanSpecification emptyTestPlanSpecification = TestPlanSpecification.build();
	private TestResolverRegistryImpl testResolverRegistry = new TestResolverRegistryImpl();

	@Test
	public void givenNoTestResolvers_notifyTestResolvers_existsSilently() throws Exception {
		testResolverRegistry.notifyResolvers(testGroup, emptyTestPlanSpecification);
	}

	@Test
	public void givenATestResolver_testResolversGetsNotified() throws Exception {
		TestResolverSpy testResolverSpy = new TestResolverSpy();

		testResolverRegistry.register(testResolverSpy);
		testResolverRegistry.notifyResolvers(testGroup, emptyTestPlanSpecification);

		assertTestResolverWasNotified(testResolverSpy, testGroup, emptyTestPlanSpecification);
	}

	@Test
	public void givenMultipleTestResolvers_allTestResolversGetNotified() throws Exception {
		TestResolverSpy testResolverSpy1 = new TestResolverSpy();
		TestResolverSpy testResolverSpy2 = new TestResolverSpy();

		testResolverRegistry.register(testResolverSpy1);
		testResolverRegistry.register(testResolverSpy2);
		testResolverRegistry.notifyResolvers(testGroup, emptyTestPlanSpecification);

		assertTestResolverWasNotified(testResolverSpy1, testGroup, emptyTestPlanSpecification);
		assertTestResolverWasNotified(testResolverSpy2, testGroup, emptyTestPlanSpecification);
	}

	private void assertTestResolverWasNotified(TestResolverSpy testResolverSpy, MutableTestDescriptor parent, TestPlanSpecification testPlanSpecification) {
		assertThat(testResolverSpy.resolvedFor).hasSize(1);

		TestResolverRequest request = testResolverSpy.resolvedFor.get(0);
		assertThat(request.parent).isEqualTo(parent);
		assertThat(request.testPlanSpecification).isEqualTo(testPlanSpecification);
	}
}
