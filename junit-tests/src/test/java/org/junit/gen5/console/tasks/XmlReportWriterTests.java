/*
 * Copyright 2015-2016 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.junit.gen5.console.tasks;

import static java.util.Collections.singleton;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.gen5.commons.util.CollectionUtils.getOnlyElement;
import static org.junit.gen5.console.tasks.XmlReportAssertions.ensureValidAccordingToJenkinsSchema;
import static org.junit.gen5.engine.TestExecutionResult.failed;

import java.io.StringWriter;
import java.time.Clock;

import org.junit.gen5.api.Test;
import org.junit.gen5.engine.TestDescriptorStub;
import org.junit.gen5.engine.support.descriptor.EngineDescriptor;
import org.junit.gen5.launcher.TestId;
import org.junit.gen5.launcher.TestPlan;

class XmlReportWriterTests {

	@Test
	void writesTestsuiteElementsWithoutTestcaseElementsWithoutAnyTests() throws Exception {
		TestPlan testPlan = TestPlan.from(singleton(new EngineDescriptor("emptyEngine", "Empty Engine")));
		XmlReportData reportData = new XmlReportData(testPlan, Clock.systemDefaultZone());

		StringWriter out = new StringWriter();
		new XmlReportWriter(reportData).writeXmlReport(getOnlyElement(testPlan.getRoots()), out);

		String content = ensureValidAccordingToJenkinsSchema(out.toString());
		//@formatter:off
		assertThat(content)
			.containsSequence(
				"<testsuite name=\"Empty Engine\" tests=\"0\"",
				"</testsuite>")
			.doesNotContain("<testcase");
		//@formatter:on
	}

	@Test
	void writesEmptySkippedElementForSkippedTestWithoutReason() throws Exception {
		EngineDescriptor engineDescriptor = new EngineDescriptor("engine", "Engine");
		engineDescriptor.addChild(new TestDescriptorStub("skippedTest"));

		TestPlan testPlan = TestPlan.from(singleton(engineDescriptor));
		XmlReportData reportData = new XmlReportData(testPlan, Clock.systemDefaultZone());
		reportData.markSkipped(testPlan.getTestIdentifier(new TestId("skippedTest")), null);

		StringWriter out = new StringWriter();
		new XmlReportWriter(reportData).writeXmlReport(getOnlyElement(testPlan.getRoots()), out);

		String content = ensureValidAccordingToJenkinsSchema(out.toString());
		//@formatter:off
		assertThat(content)
			.containsSequence(
				"<testsuite name=\"Engine\" tests=\"1\" skipped=\"1\" failures=\"0\" errors=\"0\"",
				"<testcase name=\"skippedTest\"",
				"<skipped/>",
				"</testcase>",
				"</testsuite>");
		//@formatter:on
	}

	@Test
	void writesEmptyErrorElementForFailedTestWithoutCause() throws Exception {
		EngineDescriptor engineDescriptor = new EngineDescriptor("engine", "Engine");
		engineDescriptor.addChild(new TestDescriptorStub("failedTest"));

		TestPlan testPlan = TestPlan.from(singleton(engineDescriptor));
		XmlReportData reportData = new XmlReportData(testPlan, Clock.systemDefaultZone());
		reportData.markFinished(testPlan.getTestIdentifier(new TestId("failedTest")), failed(null));

		StringWriter out = new StringWriter();
		new XmlReportWriter(reportData).writeXmlReport(getOnlyElement(testPlan.getRoots()), out);

		String content = ensureValidAccordingToJenkinsSchema(out.toString());
		//@formatter:off
		assertThat(content)
			.containsSequence(
				"<testsuite name=\"Engine\" tests=\"1\" skipped=\"0\" failures=\"0\" errors=\"1\"",
				"<testcase name=\"failedTest\"",
				"<error/>",
				"</testcase>",
				"</testsuite>");
		//@formatter:on
	}

	@Test
	void omitsMessageAttributeForFailedTestWithThrowableWithoutMessage() throws Exception {
		EngineDescriptor engineDescriptor = new EngineDescriptor("engine", "Engine");
		engineDescriptor.addChild(new TestDescriptorStub("failedTest"));

		TestPlan testPlan = TestPlan.from(singleton(engineDescriptor));
		XmlReportData reportData = new XmlReportData(testPlan, Clock.systemDefaultZone());
		reportData.markFinished(testPlan.getTestIdentifier(new TestId("failedTest")),
			failed(new NullPointerException()));

		StringWriter out = new StringWriter();
		new XmlReportWriter(reportData).writeXmlReport(getOnlyElement(testPlan.getRoots()), out);

		String content = ensureValidAccordingToJenkinsSchema(out.toString());
		//@formatter:off
		assertThat(content)
			.containsSequence(
				"<testcase name=\"failedTest\"",
				"<error type=\"java.lang.NullPointerException\">",
				"</testcase>");
		//@formatter:on
	}

}
