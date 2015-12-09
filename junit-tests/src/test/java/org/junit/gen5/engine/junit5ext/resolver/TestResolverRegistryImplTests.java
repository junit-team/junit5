/*
 * Copyright 2015 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.junit.gen5.engine.junit5ext.resolver;

import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.junit.Test;
import org.junit.gen5.engine.MutableTestDescriptor;
import org.junit.gen5.engine.TestPlanSpecification;
import org.junit.gen5.engine.junit5ext.descriptor.GroupDescriptor;
import org.junit.gen5.engine.junit5ext.testdoubles.TestResolverRequest;
import org.junit.gen5.engine.junit5ext.testdoubles.TestResolverSpy;
import org.junit.gen5.engine.junit5ext.testdoubles.TestResolverSpyWithTestsForRoot;

public class TestResolverRegistryImplTests {
	private GroupDescriptor testGroup = new GroupDescriptor("testGroup", "Test Group");
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

		assertTestResolverWasNotified(testResolverSpy, asList(testGroup), emptyTestPlanSpecification);
	}

	@Test
	public void givenMultipleTestResolvers_allTestResolversGetNotified() throws Exception {
		TestResolverSpy testResolverSpy1 = new TestResolverSpy();
		TestResolverSpy testResolverSpy2 = new TestResolverSpy();

		testResolverRegistry.register(testResolverSpy1);
		testResolverRegistry.register(testResolverSpy2);
		testResolverRegistry.notifyResolvers(testGroup, emptyTestPlanSpecification);

		assertTestResolverWasNotified(testResolverSpy1, asList(testGroup), emptyTestPlanSpecification);
		assertTestResolverWasNotified(testResolverSpy2, asList(testGroup), emptyTestPlanSpecification);
	}

	@Test
	public void givenTestResolverThatReturnsNewTests_TestResolversAreCalledForAllTests() throws Exception {
		TestResolverSpyWithTestsForRoot testResolverSpy = new TestResolverSpyWithTestsForRoot(testGroup);

		testResolverRegistry.register(testResolverSpy);
		testResolverRegistry.notifyResolvers(testGroup, emptyTestPlanSpecification);

		assertTestResolverWasNotified(testResolverSpy, asList(testGroup, testResolverSpy.getResolvedTest()),
			emptyTestPlanSpecification);
	}

	private void assertTestResolverWasNotified(TestResolverSpy testResolverSpy, List<MutableTestDescriptor> parents,
			TestPlanSpecification testPlanSpecification) {
		assertThat(testResolverSpy.resolvedFor).hasSize(parents.size());

		for (int i = 0; i < parents.size(); i++) {
			TestResolverRequest request = testResolverSpy.resolvedFor.get(i);
			assertThat(request.parent).isEqualTo(parents.get(i));
			assertThat(request.testPlanSpecification).isEqualTo(testPlanSpecification);
		}
	}
}
