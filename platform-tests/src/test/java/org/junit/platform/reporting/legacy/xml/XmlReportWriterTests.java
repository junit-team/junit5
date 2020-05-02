/*
 * Copyright 2015-2020 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * https://www.eclipse.org/legal/epl-v20.html
 */

package org.junit.platform.reporting.legacy.xml;

import static java.util.Collections.singleton;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.params.provider.Arguments.arguments;
import static org.junit.platform.commons.util.CollectionUtils.getOnlyElement;
import static org.junit.platform.engine.TestExecutionResult.failed;
import static org.junit.platform.engine.TestExecutionResult.successful;
import static org.junit.platform.launcher.LauncherConstants.STDERR_REPORT_ENTRY_KEY;
import static org.junit.platform.launcher.LauncherConstants.STDOUT_REPORT_ENTRY_KEY;
import static org.junit.platform.reporting.legacy.xml.XmlReportAssertions.assertValidAccordingToJenkinsSchema;

import java.io.StringWriter;
import java.io.Writer;
import java.time.Clock;
import java.util.Map;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.platform.engine.UniqueId;
import org.junit.platform.engine.reporting.ReportEntry;
import org.junit.platform.engine.support.descriptor.EngineDescriptor;
import org.junit.platform.fakes.TestDescriptorStub;
import org.junit.platform.launcher.TestIdentifier;
import org.junit.platform.launcher.TestPlan;

/**
 * @since 1.0
 */
class XmlReportWriterTests {

	private EngineDescriptor engineDescriptor = new EngineDescriptor(UniqueId.forEngine("engine"), "Engine");

	@Test
	void writesTestsuiteElementsWithoutTestcaseElementsWithoutAnyTests() throws Exception {
		TestPlan testPlan = TestPlan.from(singleton(engineDescriptor));

		XmlReportData reportData = new XmlReportData(testPlan, Clock.systemDefaultZone());

		String content = writeXmlReport(testPlan, reportData);

		assertValidAccordingToJenkinsSchema(content);
		//@formatter:off
		assertThat(content)
			.containsSubsequence(
				"<testsuite name=\"Engine\" tests=\"0\"",
				"</testsuite>")
			.doesNotContain("<testcase");
		//@formatter:on
	}

	@Test
	void writesReportEntry() throws Exception {
		UniqueId uniqueId = engineDescriptor.getUniqueId().append("test", "test");
		TestDescriptorStub testDescriptor = new TestDescriptorStub(uniqueId, "successfulTest");
		engineDescriptor.addChild(testDescriptor);
		TestPlan testPlan = TestPlan.from(singleton(engineDescriptor));

		XmlReportData reportData = new XmlReportData(testPlan, Clock.systemDefaultZone());
		reportData.addReportEntry(TestIdentifier.from(testDescriptor), ReportEntry.from("myKey", "myValue"));
		reportData.markFinished(testPlan.getTestIdentifier(uniqueId.toString()), successful());

		String content = writeXmlReport(testPlan, reportData);

		assertValidAccordingToJenkinsSchema(content);
		//@formatter:off
		assertThat(content)
			.containsSubsequence(
				"<system-out>",
					"Report Entry #1 (timestamp: ",
					"- myKey: myValue",
				"</system-out>");
		//@formatter:on
	}

	@Test
	void writesCapturedOutput() throws Exception {
		UniqueId uniqueId = engineDescriptor.getUniqueId().append("test", "test");
		TestDescriptorStub testDescriptor = new TestDescriptorStub(uniqueId, "successfulTest");
		engineDescriptor.addChild(testDescriptor);
		TestPlan testPlan = TestPlan.from(singleton(engineDescriptor));

		XmlReportData reportData = new XmlReportData(testPlan, Clock.systemDefaultZone());
		ReportEntry reportEntry = ReportEntry.from(Map.of( //
			STDOUT_REPORT_ENTRY_KEY, "normal output", //
			STDERR_REPORT_ENTRY_KEY, "error output", //
			"foo", "bar"));
		reportData.addReportEntry(TestIdentifier.from(testDescriptor), reportEntry);
		reportData.addReportEntry(TestIdentifier.from(testDescriptor), ReportEntry.from(Map.of("baz", "qux")));
		reportData.markFinished(testPlan.getTestIdentifier(uniqueId.toString()), successful());

		String content = writeXmlReport(testPlan, reportData);

		assertValidAccordingToJenkinsSchema(content);
		//@formatter:off
		assertThat(content)
			.containsSubsequence(
				"<system-out>",
					"unique-id: ", "test:test",
					"display-name: successfulTest",
				"</system-out>",
				"<system-out>",
					"Report Entry #1 (timestamp: ",
					"- foo: bar",
					"Report Entry #2 (timestamp: ",
					"- baz: qux",
				"</system-out>",
				"<system-out>",
					"normal output",
				"</system-out>",
				"<system-err>",
					"error output",
				"</system-err>")
			.doesNotContain(STDOUT_REPORT_ENTRY_KEY, STDERR_REPORT_ENTRY_KEY);
		//@formatter:on
	}

	@Test
	void writesEmptySkippedElementForSkippedTestWithoutReason() throws Exception {
		UniqueId uniqueId = engineDescriptor.getUniqueId().append("test", "test");
		engineDescriptor.addChild(new TestDescriptorStub(uniqueId, "skippedTest"));
		TestPlan testPlan = TestPlan.from(singleton(engineDescriptor));

		XmlReportData reportData = new XmlReportData(testPlan, Clock.systemDefaultZone());
		reportData.markSkipped(testPlan.getTestIdentifier(uniqueId.toString()), null);

		String content = writeXmlReport(testPlan, reportData);

		assertValidAccordingToJenkinsSchema(content);
		//@formatter:off
		assertThat(content)
			.containsSubsequence(
				"<testcase name=\"skippedTest\"",
				"<skipped/>",
				"</testcase>");
		//@formatter:on
	}

	@Test
	void writesEmptyErrorElementForFailedTestWithoutCause() throws Exception {
		engineDescriptor = new EngineDescriptor(UniqueId.forEngine("myEngineId"), "Fancy Engine") {
			@Override
			public String getLegacyReportingName() {
				return "myEngine";
			}
		};
		UniqueId uniqueId = engineDescriptor.getUniqueId().append("test", "test");
		engineDescriptor.addChild(new TestDescriptorStub(uniqueId, "some fancy name") {
			@Override
			public String getLegacyReportingName() {
				return "failedTest";
			}
		});
		TestPlan testPlan = TestPlan.from(singleton(engineDescriptor));

		XmlReportData reportData = new XmlReportData(testPlan, Clock.systemDefaultZone());
		reportData.markFinished(testPlan.getTestIdentifier(uniqueId.toString()), failed(null));

		String content = writeXmlReport(testPlan, reportData);

		assertValidAccordingToJenkinsSchema(content);
		//@formatter:off
		assertThat(content)
			.containsSubsequence(
				"<testcase name=\"failedTest\" classname=\"myEngine\"",
				"<error/>",
				"</testcase>");
		//@formatter:on
	}

	@Test
	void omitsMessageAttributeForFailedTestWithThrowableWithoutMessage() throws Exception {
		UniqueId uniqueId = engineDescriptor.getUniqueId().append("test", "test");
		engineDescriptor.addChild(new TestDescriptorStub(uniqueId, "failedTest"));
		TestPlan testPlan = TestPlan.from(singleton(engineDescriptor));

		XmlReportData reportData = new XmlReportData(testPlan, Clock.systemDefaultZone());
		reportData.markFinished(testPlan.getTestIdentifier(uniqueId.toString()), failed(new NullPointerException()));

		String content = writeXmlReport(testPlan, reportData);

		assertValidAccordingToJenkinsSchema(content);
		//@formatter:off
		assertThat(content)
			.containsSubsequence(
				"<testcase name=\"failedTest\"",
				"<error type=\"java.lang.NullPointerException\">",
				"</testcase>");
		//@formatter:on
	}

	@Test
	void writesValidXmlEvenIfExceptionMessageContainsCData() throws Exception {
		UniqueId uniqueId = engineDescriptor.getUniqueId().append("test", "test");
		engineDescriptor.addChild(new TestDescriptorStub(uniqueId, "test"));
		TestPlan testPlan = TestPlan.from(singleton(engineDescriptor));

		XmlReportData reportData = new XmlReportData(testPlan, Clock.systemDefaultZone());
		AssertionError assertionError = new AssertionError("<foo><![CDATA[bar]]></foo>");
		reportData.markFinished(testPlan.getTestIdentifier(uniqueId.toString()), failed(assertionError));

		String content = writeXmlReport(testPlan, reportData);

		assertValidAccordingToJenkinsSchema(content);
		//@formatter:off
		assertThat(content)
				.containsSubsequence(
						"<![CDATA[",
						"<foo><![CDATA[bar]]]]><![CDATA[></foo>",
						"]]>")
				.doesNotContain(assertionError.getMessage());
		//@formatter:on
	}

	@Test
	void escapesInvalidCharactersInSystemPropertiesAndExceptionMessages(TestInfo testInfo) throws Exception {
		UniqueId uniqueId = engineDescriptor.getUniqueId().append("test", "test");
		engineDescriptor.addChild(new TestDescriptorStub(uniqueId, "test"));
		TestPlan testPlan = TestPlan.from(singleton(engineDescriptor));

		XmlReportData reportData = new XmlReportData(testPlan, Clock.systemDefaultZone());
		AssertionError assertionError = new AssertionError("expected: <A> but was: <B\0>");
		reportData.markFinished(testPlan.getTestIdentifier(uniqueId.toString()), failed(assertionError));

		System.setProperty("foo.bar", "\1");
		String content;
		try {
			content = writeXmlReport(testPlan, reportData);
		}
		finally {
			System.getProperties().remove("foo.bar");
		}

		assertValidAccordingToJenkinsSchema(content);
		assertThat(content) //
				.contains("<property name=\"foo.bar\" value=\"&amp;#1;\"/>") //
				.contains("failure message=\"expected: &lt;A&gt; but was: &lt;B&amp;#0;&gt;\"") //
				.contains("AssertionError: expected: <A> but was: <B&#0;>");
	}

	@Test
	void doesNotReopenCDataWithinCDataContent() throws Exception {
		UniqueId uniqueId = engineDescriptor.getUniqueId().append("test", "test");
		engineDescriptor.addChild(new TestDescriptorStub(uniqueId, "test"));
		TestPlan testPlan = TestPlan.from(singleton(engineDescriptor));

		XmlReportData reportData = new XmlReportData(testPlan, Clock.systemDefaultZone());
		AssertionError assertionError = new AssertionError("<foo><![CDATA[bar]]></foo>");
		reportData.markFinished(testPlan.getTestIdentifier(uniqueId.toString()), failed(assertionError));
		Writer assertingWriter = new StringWriter() {

			@Override
			public void write(char[] cbuf, int off, int len) {
				assertThat(new String(cbuf, off, len)).doesNotContain("]]><![CDATA[");
			}
		};

		writeXmlReport(testPlan, reportData, assertingWriter);
	}

	@ParameterizedTest
	@MethodSource("stringPairs")
	void escapesIllegalChars(String input, String output) {
		assertEquals(output, XmlReportWriter.escapeIllegalChars(input));
	}

	static Stream<Arguments> stringPairs() {
		return Stream.of( //
			arguments("\0", "&#0;"), //
			arguments("\1", "&#1;"), //
			arguments("\t", "\t"), //
			arguments("\r", "\r"), //
			arguments("\n", "\n"), //
			arguments("\u001f", "&#31;"), //
			arguments("\u0020", "\u0020"), //
			arguments("foo!", "foo!"), //
			arguments("\uD801\uDC00", "\uD801\uDC00") //
		);
	}

	private String writeXmlReport(TestPlan testPlan, XmlReportData reportData) throws Exception {
		StringWriter out = new StringWriter();
		writeXmlReport(testPlan, reportData, out);
		return out.toString();
	}

	private void writeXmlReport(TestPlan testPlan, XmlReportData reportData, Writer out) throws Exception {
		new XmlReportWriter(reportData).writeXmlReport(getOnlyElement(testPlan.getRoots()), out);
	}
}
