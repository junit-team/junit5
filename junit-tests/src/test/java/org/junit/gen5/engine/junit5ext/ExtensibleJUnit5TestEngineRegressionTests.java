/*
 * Copyright 2015 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.junit.gen5.engine.junit5ext;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.gen5.engine.TestPlanSpecification.build;
import static org.junit.gen5.engine.TestPlanSpecification.forClass;

import org.junit.Before;
import org.junit.Test;
import org.junit.gen5.engine.ExecutionRequest;
import org.junit.gen5.engine.TestDescriptor;
import org.junit.gen5.engine.junit5ext.executor.GroupExecutor;
import org.junit.gen5.engine.junit5ext.executor.MethodExecutor;
import org.junit.gen5.engine.junit5ext.executor.TestExecutorRegistryImpl;
import org.junit.gen5.engine.junit5ext.resolver.ClassResolver;
import org.junit.gen5.engine.junit5ext.resolver.MethodResolver;
import org.junit.gen5.engine.junit5ext.resolver.TestResolverRegistryImpl;
import org.junit.gen5.engine.junit5ext.samples.EmptyTestSampleClass;
import org.junit.gen5.engine.junit5ext.samples.SinglePassingTestSampleClass;
import org.junit.gen5.engine.junit5ext.testdoubles.TestExecutionListenerSpy;

public class ExtensibleJUnit5TestEngineRegressionTests {
	private TestResolverRegistryImpl testResolverRegistry = new TestResolverRegistryImpl();
	private TestExecutorRegistryImpl testExecutorRegistry = new TestExecutorRegistryImpl();
	private ExtensibleJUnit5TestEngine testEngine = new ExtensibleJUnit5TestEngine();

	@Before
	public void setUp() throws Exception {
		testResolverRegistry.register(new ClassResolver());
		testResolverRegistry.register(new MethodResolver());

		testExecutorRegistry.register(new GroupExecutor());
		testExecutorRegistry.register(new MethodExecutor());

		testEngine.setTestResolverRegistry(testResolverRegistry);
		testEngine.setTestExecutorRegistry(testExecutorRegistry);
	}

	@Test
	public void noTests() throws Exception {
		TestExecutionListenerSpy testExecutionListener = new TestExecutionListenerSpy();

		TestDescriptor testDescriptor = testEngine.discoverTests(build(forClass(EmptyTestSampleClass.class)));
		testEngine.execute(new ExecutionRequest(testDescriptor, testExecutionListener));

		assertThat(testExecutionListener.foundAbortedTests).hasSize(0);
		assertThat(testExecutionListener.foundDynamicTests).hasSize(0);
		assertThat(testExecutionListener.foundFailedTests).hasSize(0);
		assertThat(testExecutionListener.foundSkippedTests).hasSize(0);
		assertThat(testExecutionListener.foundStartedTests).hasSize(0);
		assertThat(testExecutionListener.foundSucceededTests).hasSize(0);
	}

	@Test
	public void singlePassingTest() throws Exception {
		TestExecutionListenerSpy testExecutionListener = new TestExecutionListenerSpy();

		TestDescriptor testDescriptor = testEngine.discoverTests(build(forClass(SinglePassingTestSampleClass.class)));
		testEngine.execute(new ExecutionRequest(testDescriptor, testExecutionListener));

		assertThat(testExecutionListener.foundAbortedTests).hasSize(0);
		assertThat(testExecutionListener.foundDynamicTests).hasSize(0);
		assertThat(testExecutionListener.foundFailedTests).hasSize(0);
		assertThat(testExecutionListener.foundSkippedTests).hasSize(0);
		assertThat(testExecutionListener.foundStartedTests).hasSize(1);
		assertThat(testExecutionListener.foundSucceededTests).hasSize(1);
	}
}
