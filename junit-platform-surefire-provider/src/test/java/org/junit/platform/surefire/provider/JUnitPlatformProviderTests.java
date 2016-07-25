/*
 * Copyright 2015-2016 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.junit.platform.surefire.provider;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assumptions.assumeTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;

import org.apache.maven.surefire.providerapi.ProviderParameters;
import org.apache.maven.surefire.report.ReporterFactory;
import org.apache.maven.surefire.report.RunListener;
import org.apache.maven.surefire.util.RunOrderCalculator;
import org.apache.maven.surefire.util.ScanResult;
import org.apache.maven.surefire.util.TestsToRun;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.platform.launcher.Launcher;
import org.junit.platform.launcher.TestPlan;
import org.junit.platform.launcher.core.LauncherFactory;
import org.junit.platform.launcher.listeners.SummaryGeneratingListener;
import org.junit.platform.launcher.listeners.TestExecutionSummary;

/**
 * Unit tests for {@link JUnitPlatformProvider}.
 *
 * @since 1.0
 */
class JUnitPlatformProviderTests {

	@Test
	void getSuitesReturnsScannedClasses() throws Exception {
		ProviderParameters providerParameters = providerParametersMock(TestClass1.class, TestClass2.class);
		JUnitPlatformProvider provider = new JUnitPlatformProvider(providerParameters);

		assertThat(provider.getSuites()).containsOnly(TestClass1.class, TestClass2.class);
	}

	@Test
	void invokeThrowsForWrongForkTestSet() throws Exception {
		ProviderParameters providerParameters = providerParametersMock(Integer.class);
		JUnitPlatformProvider provider = new JUnitPlatformProvider(providerParameters);

		assertThrows(IllegalArgumentException.class, () -> provider.invoke("wrong forkTestSet"));
	}

	@Test
	void allGivenTestsToRunAreInvoked() throws Exception {
		Launcher launcher = LauncherFactory.create();
		JUnitPlatformProvider provider = new JUnitPlatformProvider(providerParametersMock(), launcher);

		TestPlanSummaryListener executionListener = new TestPlanSummaryListener();
		launcher.registerTestExecutionListeners(executionListener);

		TestsToRun testsToRun = newTestsToRun(TestClass1.class, TestClass2.class);
		provider.invoke(testsToRun);

		assertThat(executionListener.summaries).hasSize(2);
		TestClass1.verifyExecutionSummary(executionListener.summaries.get(0));
		TestClass2.verifyExecutionSummary(executionListener.summaries.get(1));
	}

	@Test
	void singleTestClassIsInvoked() throws Exception {
		Launcher launcher = LauncherFactory.create();
		JUnitPlatformProvider provider = new JUnitPlatformProvider(providerParametersMock(), launcher);

		TestPlanSummaryListener executionListener = new TestPlanSummaryListener();
		launcher.registerTestExecutionListeners(executionListener);

		provider.invoke(TestClass1.class);

		assertThat(executionListener.summaries).hasSize(1);
		TestClass1.verifyExecutionSummary(executionListener.summaries.get(0));
	}

	@Test
	void allDiscoveredTestsAreInvokedForNullArgument() throws Exception {
		ProviderParameters providerParameters = providerParametersMock(TestClass1.class, TestClass2.class);
		Launcher launcher = LauncherFactory.create();
		JUnitPlatformProvider provider = new JUnitPlatformProvider(providerParameters, launcher);

		TestPlanSummaryListener executionListener = new TestPlanSummaryListener();
		launcher.registerTestExecutionListeners(executionListener);

		provider.invoke(null);

		assertThat(executionListener.summaries).hasSize(2);
		TestClass1.verifyExecutionSummary(executionListener.summaries.get(0));
		TestClass2.verifyExecutionSummary(executionListener.summaries.get(1));
	}

	private static ProviderParameters providerParametersMock(Class<?>... testClasses) {
		TestsToRun testsToRun = newTestsToRun(testClasses);

		ScanResult scanResult = mock(ScanResult.class);
		when(scanResult.applyFilter(any(), any())).thenReturn(testsToRun);

		RunOrderCalculator runOrderCalculator = mock(RunOrderCalculator.class);
		when(runOrderCalculator.orderTestClasses(any())).thenReturn(testsToRun);

		ReporterFactory reporterFactory = mock(ReporterFactory.class);
		RunListener runListener = mock(RunListener.class);
		when(reporterFactory.createReporter()).thenReturn(runListener);

		ProviderParameters providerParameters = mock(ProviderParameters.class);
		when(providerParameters.getScanResult()).thenReturn(scanResult);
		when(providerParameters.getRunOrderCalculator()).thenReturn(runOrderCalculator);
		when(providerParameters.getReporterFactory()).thenReturn(reporterFactory);

		return providerParameters;
	}

	private static TestsToRun newTestsToRun(Class<?>... testClasses) {
		List<Class<?>> classesList = Arrays.asList(testClasses);
		return new TestsToRun(new LinkedHashSet<>(classesList));
	}

	private class TestPlanSummaryListener extends SummaryGeneratingListener {

		final List<TestExecutionSummary> summaries = new ArrayList<>();

		@Override
		public void testPlanExecutionFinished(TestPlan testPlan) {
			super.testPlanExecutionFinished(testPlan);
			summaries.add(getSummary());
		}
	}

	private static class TestClass1 {

		@Test
		void test1() {
		}

		@Test
		void test2() {
		}

		@Disabled
		@Test
		void test3() {
		}

		@Test
		void test4() {
			throw new RuntimeException();
		}

		static void verifyExecutionSummary(TestExecutionSummary summary) {
			assertEquals(4, summary.getTestsFoundCount());
			assertEquals(3, summary.getTestsStartedCount());
			assertEquals(2, summary.getTestsSucceededCount());
			assertEquals(1, summary.getTestsSkippedCount());
			assertEquals(0, summary.getTestsAbortedCount());
			assertEquals(1, summary.getTestsFailedCount());
		}
	}

	private static class TestClass2 {

		@Test
		void test1() {
		}

		@Test
		void test2() {
			throw new RuntimeException();
		}

		@Test
		void test3() {
			assumeTrue(false);
		}

		static void verifyExecutionSummary(TestExecutionSummary summary) {
			assertEquals(3, summary.getTestsFoundCount());
			assertEquals(3, summary.getTestsStartedCount());
			assertEquals(1, summary.getTestsSucceededCount());
			assertEquals(0, summary.getTestsSkippedCount());
			assertEquals(1, summary.getTestsAbortedCount());
			assertEquals(1, summary.getTestsFailedCount());
		}
	}
}
