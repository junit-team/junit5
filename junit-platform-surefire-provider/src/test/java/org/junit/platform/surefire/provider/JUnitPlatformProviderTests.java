/*
 * Copyright 2015-2017 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.junit.platform.surefire.provider;

import static java.util.Collections.emptyMap;
import static java.util.Collections.singletonMap;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;

import org.apache.maven.surefire.providerapi.ProviderParameters;
import org.apache.maven.surefire.report.ReportEntry;
import org.apache.maven.surefire.report.ReporterFactory;
import org.apache.maven.surefire.report.RunListener;
import org.apache.maven.surefire.testset.TestSetFailedException;
import org.apache.maven.surefire.util.RunOrderCalculator;
import org.apache.maven.surefire.util.ScanResult;
import org.apache.maven.surefire.util.TestsToRun;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.platform.commons.util.PreconditionViolationException;
import org.junit.platform.launcher.Launcher;
import org.junit.platform.launcher.TestPlan;
import org.junit.platform.launcher.core.LauncherFactory;
import org.junit.platform.launcher.listeners.SummaryGeneratingListener;
import org.junit.platform.launcher.listeners.TestExecutionSummary;
import org.mockito.ArgumentCaptor;

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

	@Test
	void bothGroupsAndIncludeTagsThrowsException() {
		Map<String, String> properties = new HashMap<>();
		properties.put(JUnitPlatformProvider.INCLUDE_GROUPS, "groupOne, groupTwo");
		properties.put(JUnitPlatformProvider.INCLUDE_TAGS, "tagOne, tagTwo");
		verifyPreconditionViolationException(properties);
	}

	@Test
	void bothExcludedGroupsAndExcludeTagsThrowsException() {
		Map<String, String> properties = new HashMap<>();
		properties.put(JUnitPlatformProvider.EXCLUDE_GROUPS, "groupOne, groupTwo");
		properties.put(JUnitPlatformProvider.EXCLUDE_TAGS, "tagOne, tagTwo");
		verifyPreconditionViolationException(properties);
	}

	@Test
	void onlyGroupsIsDeclared() throws Exception {
		Map<String, String> properties = new HashMap<>();
		properties.put(JUnitPlatformProvider.INCLUDE_GROUPS, "groupOne, groupTwo");

		ProviderParameters providerParameters = providerParametersMock(TestClass1.class);
		when(providerParameters.getProviderProperties()).thenReturn(properties);

		JUnitPlatformProvider provider = new JUnitPlatformProvider(providerParameters);

		assertEquals(1, provider.includeAndExcludeFilters.length);
	}

	@Test
	void onlyExcludeTagsIsDeclared() throws Exception {
		Map<String, String> properties = new HashMap<>();
		properties.put(JUnitPlatformProvider.EXCLUDE_TAGS, "tagOne, tagTwo");

		ProviderParameters providerParameters = providerParametersMock(TestClass1.class);
		when(providerParameters.getProviderProperties()).thenReturn(properties);

		JUnitPlatformProvider provider = new JUnitPlatformProvider(providerParameters);

		assertEquals(1, provider.includeAndExcludeFilters.length);
	}

	@Test
	void bothIncludeAndExcludeAreAllowed() throws Exception {
		Map<String, String> properties = new HashMap<>();
		properties.put(JUnitPlatformProvider.INCLUDE_TAGS, "tagOne, tagTwo");
		properties.put(JUnitPlatformProvider.EXCLUDE_TAGS, "tagThree, tagFour");

		ProviderParameters providerParameters = providerParametersMock(TestClass1.class);
		when(providerParameters.getProviderProperties()).thenReturn(properties);

		JUnitPlatformProvider provider = new JUnitPlatformProvider(providerParameters);

		assertEquals(2, provider.includeAndExcludeFilters.length);
	}

	@Test
	void noFiltersAreCreatedIfNoPropertiesAreDeclared() throws Exception {
		ProviderParameters providerParameters = providerParametersMock(TestClass1.class);

		JUnitPlatformProvider provider = new JUnitPlatformProvider(providerParameters);

		assertEquals(0, provider.includeAndExcludeFilters.length);
	}

	@Test
	void defaultConfigurationParametersAreEmpty() {
		ProviderParameters providerParameters = providerParametersMock(TestClass1.class);
		when(providerParameters.getProviderProperties()).thenReturn(emptyMap());

		JUnitPlatformProvider provider = new JUnitPlatformProvider(providerParameters);

		assertTrue(provider.configurationParameters.isEmpty());
	}

	@Test
	void parsesConfigurationParameters() {
		ProviderParameters providerParameters = providerParametersMock(TestClass1.class);
		when(providerParameters.getProviderProperties()).thenReturn( //
			singletonMap(JUnitPlatformProvider.CONFIGURATION_PARAMETERS, "foo = true\nbar 42\rbaz: *\r\nqux: EOF"));

		JUnitPlatformProvider provider = new JUnitPlatformProvider(providerParameters);

		assertEquals(4, provider.configurationParameters.size());
		assertEquals("true", provider.configurationParameters.get("foo"));
		assertEquals("42", provider.configurationParameters.get("bar"));
		assertEquals("*", provider.configurationParameters.get("baz"));
		assertEquals("EOF", provider.configurationParameters.get("qux"));
	}

	private void verifyPreconditionViolationException(Map<String, String> properties) {
		ProviderParameters providerParameters = providerParametersMock(TestClass1.class);
		when(providerParameters.getProviderProperties()).thenReturn(properties);

		Throwable throwable = assertThrows(PreconditionViolationException.class,
			() -> new JUnitPlatformProvider(providerParameters));

		assertEquals(JUnitPlatformProvider.EXCEPTION_MESSAGE_BOTH_NOT_ALLOWED, throwable.getMessage());
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

	@Test
	void usesClassNamesForXmlReport() throws TestSetFailedException, InvocationTargetException {
		String[] classNames = { "org.junit.platform.surefire.provider.JUnitPlatformProviderTests$Sub1Tests",
				"org.junit.platform.surefire.provider.JUnitPlatformProviderTests$Sub2Tests" };
		ProviderParameters providerParameters = providerParametersMock(Sub1Tests.class, Sub2Tests.class);

		JUnitPlatformProvider jUnitPlatformProvider = new JUnitPlatformProvider(providerParameters);
		TestsToRun testsToRun = newTestsToRun(Sub1Tests.class, Sub2Tests.class);

		jUnitPlatformProvider.invoke(testsToRun);
		RunListener reporter = providerParameters.getReporterFactory().createReporter();

		ArgumentCaptor<ReportEntry> reportEntryArgumentCaptor = ArgumentCaptor.forClass(ReportEntry.class);
		verify(reporter, times(2)).testSucceeded(reportEntryArgumentCaptor.capture());

		List<ReportEntry> allValues = reportEntryArgumentCaptor.getAllValues();
		assertThat(allValues).extracting(ReportEntry::getSourceName).containsExactly(classNames);
	}

	static class AbstractTestClass {
		@Test
		void test() {
		}
	}

	static class Sub1Tests extends AbstractTestClass {
	}

	static class Sub2Tests extends AbstractTestClass {
	}
}
