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

import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.stream.Collectors.toList;
import static org.junit.gen5.commons.util.ExceptionUtils.readStackTrace;
import static org.junit.gen5.commons.util.StringUtils.isNotBlank;
import static org.junit.gen5.console.tasks.XmlReportData.isFailure;
import static org.junit.gen5.engine.TestExecutionResult.Status.FAILED;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import org.junit.gen5.engine.TestExecutionResult;
import org.junit.gen5.launcher.TestIdentifier;

class XmlReportWriter {

	private final XmlReportData reportData;

	XmlReportWriter(XmlReportData reportData) {
		this.reportData = reportData;
	}

	void writeXmlReport(TestIdentifier testIdentifier, File xmlFile) throws IOException, XMLStreamException {
		// @formatter:off
		List<TestIdentifier> tests = reportData.getTestPlan().getDescendants(testIdentifier)
				.stream()
				.filter(TestIdentifier::isTest)
				.collect(toList());
		// @formatter:on
		if (!tests.isEmpty()) {
			writeXmlReport(testIdentifier, tests, xmlFile);
		}
	}

	private void writeXmlReport(TestIdentifier testIdentifier, List<TestIdentifier> tests, File xmlFile)
			throws IOException, XMLStreamException {
		try (Writer fileWriter = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(xmlFile), UTF_8))) {
			XMLOutputFactory factory = XMLOutputFactory.newInstance();
			XMLStreamWriter xmlWriter = factory.createXMLStreamWriter(fileWriter);
			xmlWriter.writeStartDocument();
			writeTestsuite(testIdentifier, tests, xmlWriter);
			xmlWriter.writeEndDocument();
			xmlWriter.flush();
			xmlWriter.close();
		}
	}

	private void writeTestsuite(TestIdentifier testIdentifier, List<TestIdentifier> tests, XMLStreamWriter writer)
			throws XMLStreamException {
		writer.writeStartElement("testsuite");

		writer.writeAttribute("name", testIdentifier.getDisplayName());

		TestCounts testCounts = TestCounts.from(reportData, tests);
		writer.writeAttribute("tests", String.valueOf(testCounts.getTotal()));
		writer.writeAttribute("skipped", String.valueOf(testCounts.getSkipped()));
		writer.writeAttribute("failures", String.valueOf(testCounts.getFailures()));
		writer.writeAttribute("errors", String.valueOf(testCounts.getErrors()));

		// NumberFormat is not thread-safe. Thus, we instantiate it here and pass it to
		// writeTestcase instead of using a constant
		NumberFormat numberFormat = NumberFormat.getInstance(Locale.US);
		writer.writeAttribute("time", getTime(testIdentifier, numberFormat));

		writer.writeComment("Unique ID: " + testIdentifier.getUniqueId().toString());

		for (TestIdentifier test : tests) {
			writeTestcase(test, numberFormat, writer);
		}

		writer.writeEndElement();
	}

	private void writeTestcase(TestIdentifier test, NumberFormat numberFormat, XMLStreamWriter writer)
			throws XMLStreamException {
		writer.writeStartElement("testcase");

		writer.writeAttribute("name", test.getDisplayName());
		Optional<TestIdentifier> parent = reportData.getTestPlan().getParent(test);
		if (parent.isPresent()) {
			writer.writeAttribute("classname", parent.get().getName());
		}
		writer.writeAttribute("time", getTime(test, numberFormat));
		writer.writeComment("Unique ID: " + test.getUniqueId().toString());

		writeSkippedOrErrorOrFailureElement(test, writer);

		writer.writeEndElement();
	}

	private void writeSkippedOrErrorOrFailureElement(TestIdentifier test, XMLStreamWriter writer)
			throws XMLStreamException {
		if (reportData.wasSkipped(test)) {
			writeSkippedElement(reportData.getSkipReason(test), writer);
		}
		else {
			Optional<TestExecutionResult> result = reportData.getResult(test);
			if (result.isPresent() && result.get().getStatus() == FAILED) {
				writeErrorOrFailureElement(result.get().getThrowable(), writer);
			}
		}
	}

	private void writeSkippedElement(String reason, XMLStreamWriter writer) throws XMLStreamException {
		if (isNotBlank(reason)) {
			writer.writeStartElement("skipped");
			writer.writeCharacters(reason);
			writer.writeEndElement();
		}
		else {
			writer.writeEmptyElement("skipped");
		}
	}

	private void writeErrorOrFailureElement(Optional<Throwable> throwable, XMLStreamWriter writer)
			throws XMLStreamException {
		writer.writeStartElement(isFailure(throwable) ? "failure" : "error");
		if (throwable.isPresent()) {
			writeFailureAttributesAndContent(throwable.get(), writer);
		}
		writer.writeEndElement();
	}

	private void writeFailureAttributesAndContent(Throwable throwable, XMLStreamWriter writer)
			throws XMLStreamException {
		if (throwable.getMessage() != null) {
			writer.writeAttribute("message", throwable.getMessage());
		}
		writer.writeAttribute("type", throwable.getClass().getName());
		writer.writeCharacters(readStackTrace(throwable));
	}

	private String getTime(TestIdentifier testIdentifier, NumberFormat numberFormat) {
		return numberFormat.format(reportData.getDurationInSeconds(testIdentifier));
	}

	private static class TestCounts {

		static TestCounts from(XmlReportData reportData, List<TestIdentifier> tests) {
			TestCounts counts = new TestCounts(tests.size());
			for (TestIdentifier test : tests) {
				if (reportData.wasSkipped(test)) {
					counts.skipped++;
				}
				else {
					Optional<TestExecutionResult> result = reportData.getResult(test);
					if (result.isPresent() && result.get().getStatus() == FAILED) {
						if (isFailure(result.get().getThrowable())) {
							counts.failures++;
						}
						else {
							counts.errors++;
						}
					}
				}
			}
			return counts;
		}

		private final long total;
		private long skipped;
		private long failures;
		private long errors;

		public TestCounts(long total) {
			this.total = total;
		}

		public long getTotal() {
			return total;
		}

		public long getSkipped() {
			return skipped;
		}

		public long getFailures() {
			return failures;
		}

		public long getErrors() {
			return errors;
		}

	}

}
