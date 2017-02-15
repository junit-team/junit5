package org.junit.platform.surefire.provider;

import org.apache.maven.surefire.providerapi.ProviderParameters;
import org.apache.maven.surefire.report.ReportEntry;
import org.apache.maven.surefire.report.RunListener;
import org.apache.maven.surefire.testset.TestSetFailedException;
import org.apache.maven.surefire.util.TestsToRun;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.lang.reflect.InvocationTargetException;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.platform.surefire.provider.JUnitPlatformProviderTests.newTestsToRun;
import static org.junit.platform.surefire.provider.JUnitPlatformProviderTests.providerParametersMock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

public class LegacyReportingTests {

	@Test
	public void testLegacyXmlReport() throws TestSetFailedException, InvocationTargetException {
		ProviderParameters providerParameters = providerParametersMock(Sub1Tests.class, Sub2Tests.class);

		final JUnitPlatformProvider jUnitPlatformProvider = new JUnitPlatformProvider(providerParameters);
		TestsToRun testsToRun = newTestsToRun(Sub1Tests.class, Sub2Tests.class);

		jUnitPlatformProvider.invoke(testsToRun);
		final RunListener reporter = providerParameters.getReporterFactory().createReporter();

		ArgumentCaptor<ReportEntry> reportEntryArgumentCaptor = ArgumentCaptor.forClass(ReportEntry.class);
		verify(reporter, times(2)).testSucceeded(reportEntryArgumentCaptor.capture());
		final ReportEntry value = reportEntryArgumentCaptor.getValue();
		assertTrue(value.getSourceName().endsWith("Sub2Tests"));
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
