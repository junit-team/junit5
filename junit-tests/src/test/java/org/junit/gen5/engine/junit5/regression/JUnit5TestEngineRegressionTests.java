/*
 * Copyright 2015-2016 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.junit.gen5.engine.junit5.regression;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.gen5.engine.TestPlanSpecification.build;
import static org.junit.gen5.engine.TestPlanSpecification.forClass;

import org.junit.Before;
import org.junit.Test;
import org.junit.gen5.api.Assertions;
import org.junit.gen5.engine.ExecutionRequest;
import org.junit.gen5.engine.TestDescriptor;
import org.junit.gen5.engine.junit5.JUnit5TestEngine;
import org.junit.gen5.engine.junit5.TrackingEngineExecutionListener;
import org.junit.gen5.engine.junit5.samples.EmptyTestSampleClass;
import org.junit.gen5.engine.junit5.samples.SinglePassingTestSampleClass;
import org.junit.gen5.engine.junit5.samples.TestCaseWithNesting;
import org.junit.gen5.engine.junit5.testdoubles.EngineExecutionListenerSpy;

public class JUnit5TestEngineRegressionTests {
	private JUnit5TestEngine testEngine;

	@Before
	public void setUp() throws Exception {
		testEngine = new JUnit5TestEngine();
		testEngine.initialize();
	}

	@Test
	public void noTests() throws Exception {
		TrackingEngineExecutionListener testExecutionListener = new TrackingEngineExecutionListener();

		TestDescriptor testDescriptor = testEngine.discoverTests(build(forClass(EmptyTestSampleClass.class)));
		testEngine.execute(new ExecutionRequest(testDescriptor, testExecutionListener));

		assertThat(testExecutionListener.containerStartedCount.get()).isEqualTo(1);
		assertThat(testExecutionListener.containerFinishedCount.get()).isEqualTo(1);
		assertThat(testExecutionListener.testAbortedCount.get()).isEqualTo(0);
		assertThat(testExecutionListener.testFailedCount.get()).isEqualTo(0);
		assertThat(testExecutionListener.testSkippedCount.get()).isEqualTo(0);
		assertThat(testExecutionListener.testStartedCount.get()).isEqualTo(0);
		assertThat(testExecutionListener.testSucceededCount.get()).isEqualTo(0);
	}

	@Test
	public void singlePassingTest() throws Exception {
		TrackingEngineExecutionListener testExecutionListener = new TrackingEngineExecutionListener();

		TestDescriptor testDescriptor = testEngine.discoverTests(build(forClass(SinglePassingTestSampleClass.class)));
		testEngine.execute(new ExecutionRequest(testDescriptor, testExecutionListener));

		assertThat(testExecutionListener.containerStartedCount.get()).isEqualTo(2);
		assertThat(testExecutionListener.containerFinishedCount.get()).isEqualTo(2);
		assertThat(testExecutionListener.testAbortedCount.get()).isEqualTo(0);
		assertThat(testExecutionListener.testFailedCount.get()).isEqualTo(0);
		assertThat(testExecutionListener.testSkippedCount.get()).isEqualTo(0);
		assertThat(testExecutionListener.testStartedCount.get()).isEqualTo(1);
		assertThat(testExecutionListener.testSucceededCount.get()).isEqualTo(1);
	}

	@Test
	public void nestedTestClassTest() throws Exception {
		TrackingEngineExecutionListener testExecutionListener = new TrackingEngineExecutionListener();

		TestDescriptor testDescriptor = testEngine.discoverTests(build(forClass(TestCaseWithNesting.class)));
		testEngine.execute(new ExecutionRequest(testDescriptor, testExecutionListener));

		assertThat(testExecutionListener.containerStartedCount.get()).isEqualTo(4);
		assertThat(testExecutionListener.containerFinishedCount.get()).isEqualTo(4);
		assertThat(testExecutionListener.testAbortedCount.get()).isEqualTo(0);
		assertThat(testExecutionListener.testFailedCount.get()).isEqualTo(0);
		assertThat(testExecutionListener.testSkippedCount.get()).isEqualTo(0);
		assertThat(testExecutionListener.testStartedCount.get()).isEqualTo(3);
		assertThat(testExecutionListener.testSucceededCount.get()).isEqualTo(3);
	}
}
