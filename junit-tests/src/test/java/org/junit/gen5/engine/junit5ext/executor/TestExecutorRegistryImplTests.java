/*
 * Copyright 2015 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.junit.gen5.engine.junit5ext.executor;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;
import org.junit.gen5.engine.TestPlanSpecification;
import org.junit.gen5.engine.junit5ext.descriptor.GroupDescriptor;
import org.junit.gen5.engine.junit5ext.testdoubles.AlwaysMatchingTestExecutorSpy;
import org.junit.gen5.engine.junit5ext.testdoubles.AlwaysNonMatchingTestExecutorSpy;
import org.junit.gen5.engine.junit5ext.testdoubles.ExecutionRequestDummy;

public class TestExecutorRegistryImplTests {
	private GroupDescriptor testGroup = new GroupDescriptor("testGroup", "Test Group");
	private TestPlanSpecification emptyTestPlanSpecification = TestPlanSpecification.build();
	private TestExecutorRegistryImpl testExecutorRegistry = new TestExecutorRegistryImpl();

	@Test
	public void givenAMatchingTestExecutor_lookupReturnsExecutor() throws Exception {
		AlwaysMatchingTestExecutorSpy testExecutor = new AlwaysMatchingTestExecutorSpy();
		testExecutorRegistry.register(testExecutor);

		testExecutorRegistry.executeAll(new ExecutionRequestDummy(), testGroup);

		assertThat(testExecutor.foundTestDescriptor).isEqualTo(testGroup);
		assertThat(testExecutor.foundTestDescriptorForExecution).isEqualTo(testGroup);
	}

	@Test
	public void givenNonMatchingTestExecutor_lookupReturnsAnEmptyList() throws Exception {
		AlwaysNonMatchingTestExecutorSpy testExecutor = new AlwaysNonMatchingTestExecutorSpy();
		testExecutorRegistry.register(testExecutor);

		testExecutorRegistry.executeAll(new ExecutionRequestDummy(), testGroup);

		assertThat(testExecutor.foundTestDescriptor).isEqualTo(testGroup);
		assertThat(testExecutor.foundTestDescriptorForExecution).isNull();
	}
}
