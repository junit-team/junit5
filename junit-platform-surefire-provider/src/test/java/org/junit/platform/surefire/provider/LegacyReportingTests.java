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

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.platform.surefire.provider.JUnitPlatformProviderTests.newTestsToRun;
import static org.junit.platform.surefire.provider.JUnitPlatformProviderTests.providerParametersMock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.lang.reflect.InvocationTargetException;
import java.util.List;

import org.apache.maven.surefire.providerapi.ProviderParameters;
import org.apache.maven.surefire.report.ReportEntry;
import org.apache.maven.surefire.report.RunListener;
import org.apache.maven.surefire.testset.TestSetFailedException;
import org.apache.maven.surefire.util.TestsToRun;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

public class LegacyReportingTests {
	private static final String[] classNames = { "org.junit.platform.surefire.provider.LegacyReportingTests$Sub1Tests",
			"org.junit.platform.surefire.provider.LegacyReportingTests$Sub2Tests" };

	@Test
	public void testLegacyXmlReport() throws TestSetFailedException, InvocationTargetException {
		ProviderParameters providerParameters = providerParametersMock(Sub1Tests.class, Sub2Tests.class);

		final JUnitPlatformProvider jUnitPlatformProvider = new JUnitPlatformProvider(providerParameters);
		TestsToRun testsToRun = newTestsToRun(Sub1Tests.class, Sub2Tests.class);

		jUnitPlatformProvider.invoke(testsToRun);
		final RunListener reporter = providerParameters.getReporterFactory().createReporter();

		ArgumentCaptor<ReportEntry> reportEntryArgumentCaptor = ArgumentCaptor.forClass(ReportEntry.class);
		verify(reporter, times(2)).testSucceeded(reportEntryArgumentCaptor.capture());

		final List<ReportEntry> allValues = reportEntryArgumentCaptor.getAllValues();
		assertThat(allValues).extracting(ReportEntry::getSourceName).containsExactly(classNames);
	}

	public static class AbstractTestClass {
		@Test
		void test() {
		}
	}

	public static class Sub1Tests extends AbstractTestClass {
	}

	public static class Sub2Tests extends AbstractTestClass {
	}
}
