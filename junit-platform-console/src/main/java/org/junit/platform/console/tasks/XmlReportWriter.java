/*
 * Copyright 2015-2017 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.junit.platform.console.tasks;

import static java.text.MessageFormat.format;
import static java.time.format.DateTimeFormatter.ISO_LOCAL_DATE_TIME;
import static java.util.stream.Collectors.toList;
import static org.junit.platform.commons.util.ExceptionUtils.readStackTrace;
import static org.junit.platform.commons.util.StringUtils.isNotBlank;
import static org.junit.platform.console.tasks.XmlReportData.isFailure;
import static org.junit.platform.engine.TestExecutionResult.Status.FAILED;

import java.io.Writer;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.text.NumberFormat;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.Properties;
import java.util.TreeSet;

import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import org.junit.platform.engine.TestExecutionResult;
import org.junit.platform.engine.TestSource;
import org.junit.platform.engine.reporting.ReportEntry;
import org.junit.platform.engine.support.descriptor.ClassSource;
import org.junit.platform.engine.support.descriptor.MethodSource;
import org.junit.platform.launcher.TestIdentifier;

/**
 * @since 1.0
 */
class XmlReportWriter {

	private final XmlReportData reportData;

	XmlReportWriter(XmlReportData reportData) {
		this.reportData = reportData;
	}

	void writeXmlReport(TestIdentifier testIdentifier, Writer out) throws XMLStreamException {
		// @formatter:off
		List<TestIdentifier> tests = reportData.getTestPlan().getDescendants(testIdentifier)
				.stream()
				.filter(TestIdentifier::isTest)
				.collect(toList());
		// @formatter:on
		writeXmlReport(testIdentifier, tests, out);
	}

	private void writeXmlReport(TestIdentifier testIdentifier, List<TestIdentifier> tests, Writer out)
			throws XMLStreamException {
		XMLOutputFactory factory = XMLOutputFactory.newInstance();
		XMLStreamWriter xmlWriter = factory.createXMLStreamWriter(out);
		xmlWriter.writeStartDocument("UTF-8", "1.0");
		newLine(xmlWriter);
		writeTestsuite(testIdentifier, tests, xmlWriter);
		xmlWriter.writeEndDocument();
		xmlWriter.flush();
		xmlWriter.close();
	}

	private void writeTestsuite(TestIdentifier testIdentifier, List<TestIdentifier> tests, XMLStreamWriter writer)
			throws XMLStreamException {

		// NumberFormat is not thread-safe. Thus, we instantiate it here and pass it to
		// writeTestcase instead of using a constant
		NumberFormat numberFormat = NumberFormat.getInstance(Locale.US);

		writer.writeStartElement("testsuite");

		writeSuiteAttributes(testIdentifier, tests, numberFormat, writer);

		newLine(writer);
		writeSystemProperties(writer);

		for (TestIdentifier test : tests) {
			writeTestcase(test, numberFormat, writer);
		}

		writeNonStandardAttributesToSystemOutElement(testIdentifier, writer);

		writer.writeEndElement();
		newLine(writer);
	}

	private void writeSuiteAttributes(TestIdentifier testIdentifier, List<TestIdentifier> tests,
			NumberFormat numberFormat, XMLStreamWriter writer) throws XMLStreamException {
		writer.writeAttribute("name", testIdentifier.getDisplayName());
		writeTestCounts(tests, writer);
		writer.writeAttribute("time", getTime(testIdentifier, numberFormat));
		writer.writeAttribute("hostname", getHostname().orElse("<unknown host>"));
		writer.writeAttribute("timestamp", ISO_LOCAL_DATE_TIME.format(getCurrentDateTime()));
	}

	private void writeTestCounts(List<TestIdentifier> tests, XMLStreamWriter writer) throws XMLStreamException {
		TestCounts testCounts = TestCounts.from(reportData, tests);
		writer.writeAttribute("tests", String.valueOf(testCounts.getTotal()));
		writer.writeAttribute("skipped", String.valueOf(testCounts.getSkipped()));
		writer.writeAttribute("failures", String.valueOf(testCounts.getFailures()));
		writer.writeAttribute("errors", String.valueOf(testCounts.getErrors()));
	}

	private void writeSystemProperties(XMLStreamWriter writer) throws XMLStreamException {
		writer.writeStartElement("properties");
		newLine(writer);
		Properties systemProperties = System.getProperties();
		for (String propertyName : new TreeSet<>(systemProperties.stringPropertyNames())) {
			writer.writeEmptyElement("property");
			writer.writeAttribute("name", propertyName);
			writer.writeAttribute("value", systemProperties.getProperty(propertyName));
			newLine(writer);
		}
		writer.writeEndElement();
		newLine(writer);
	}

	private void writeTestcase(TestIdentifier testIdentifier, NumberFormat numberFormat, XMLStreamWriter writer)
			throws XMLStreamException {

		writer.writeStartElement("testcase");

		writer.writeAttribute("name", getName(testIdentifier));
		writer.writeAttribute("classname", getClassName(testIdentifier));
		writer.writeAttribute("time", getTime(testIdentifier, numberFormat));
		newLine(writer);

		writeSkippedOrErrorOrFailureElement(testIdentifier, writer);
		writeReportEntriesToSystemOutElement(testIdentifier, writer);
		writeNonStandardAttributesToSystemOutElement(testIdentifier, writer);

		writer.writeEndElement();
		newLine(writer);
	}

	private String getName(TestIdentifier testIdentifier) {
		Optional<TestSource> optionalSource = testIdentifier.getSource();
		if (optionalSource.isPresent()) {
			TestSource source = optionalSource.get();
			if (source instanceof ClassSource) {
				return ((ClassSource) source).getJavaClass().getName();
			}
			else if (source instanceof MethodSource) {
				MethodSource methodSource = (MethodSource) source;
				return String.format("%s(%s)", methodSource.getMethodName(), methodSource.getMethodParameterTypes());
			}
		}

		// Else fall back to display name
		return testIdentifier.getDisplayName();
	}

	private String getClassName(TestIdentifier testIdentifier) {
		Optional<TestSource> optionalSource = testIdentifier.getSource();
		if (optionalSource.isPresent()) {
			TestSource source = optionalSource.get();
			if (source instanceof ClassSource) {
				return ((ClassSource) source).getClassName();
			}
			else if (source instanceof MethodSource) {
				return ((MethodSource) source).getClassName();
			}
		}

		// Else fall back to display name of parent
		// @formatter:off
		return reportData.getTestPlan().getParent(testIdentifier)
				.map(TestIdentifier::getDisplayName)
				.orElse("<unrooted>");
		// @formatter:on
	}

	private void writeSkippedOrErrorOrFailureElement(TestIdentifier testIdentifier, XMLStreamWriter writer)
			throws XMLStreamException {
		if (reportData.wasSkipped(testIdentifier)) {
			writeSkippedElement(reportData.getSkipReason(testIdentifier), writer);
		}
		else {
			Optional<TestExecutionResult> result = reportData.getResult(testIdentifier);
			if (result.isPresent() && result.get().getStatus() == FAILED) {
				writeErrorOrFailureElement(result.get().getThrowable(), writer);
			}
		}
	}

	private void writeSkippedElement(String reason, XMLStreamWriter writer) throws XMLStreamException {
		if (isNotBlank(reason)) {
			writer.writeStartElement("skipped");
			writer.writeCData(reason);
			writer.writeEndElement();
		}
		else {
			writer.writeEmptyElement("skipped");
		}
		newLine(writer);
	}

	private void writeErrorOrFailureElement(Optional<Throwable> throwable, XMLStreamWriter writer)
			throws XMLStreamException {
		if (throwable.isPresent()) {
			writer.writeStartElement(isFailure(throwable) ? "failure" : "error");
			writeFailureAttributesAndContent(throwable.get(), writer);
			writer.writeEndElement();
		}
		else {
			writer.writeEmptyElement("error");
		}
		newLine(writer);
	}

	private void writeFailureAttributesAndContent(Throwable throwable, XMLStreamWriter writer)
			throws XMLStreamException {
		if (throwable.getMessage() != null) {
			writer.writeAttribute("message", throwable.getMessage());
		}
		writer.writeAttribute("type", throwable.getClass().getName());
		writer.writeCData(readStackTrace(throwable));
	}

	private void writeReportEntriesToSystemOutElement(TestIdentifier testIdentifier, XMLStreamWriter writer)
			throws XMLStreamException {
		List<ReportEntry> entries = reportData.getReportEntries(testIdentifier);
		if (!entries.isEmpty()) {
			writer.writeStartElement("system-out");
			newLine(writer);
			for (int i = 0; i < entries.size(); i++) {
				writer.writeCharacters(buildReportEntryDescription(entries.get(i), i + 1));
			}
			writer.writeEndElement();
			newLine(writer);
		}
	}

	private String buildReportEntryDescription(ReportEntry reportEntry, int entryNumber) {
		StringBuilder builder = new StringBuilder(format("Report Entry #{0} (timestamp: {1})\n", entryNumber,
			ISO_LOCAL_DATE_TIME.format(reportEntry.getTimestamp())));

		reportEntry.getKeyValuePairs().entrySet().forEach(
			entry -> builder.append(format("\t- {0}: {1}\n", entry.getKey(), entry.getValue())));

		return builder.toString();
	}

	private String getTime(TestIdentifier testIdentifier, NumberFormat numberFormat) {
		return numberFormat.format(reportData.getDurationInSeconds(testIdentifier));
	}

	private Optional<String> getHostname() {
		try {
			return Optional.ofNullable(InetAddress.getLocalHost().getHostName());
		}
		catch (UnknownHostException e) {
			return Optional.empty();
		}
	}

	private LocalDateTime getCurrentDateTime() {
		return LocalDateTime.now(reportData.getClock()).withNano(0);
	}

	private void writeNonStandardAttributesToSystemOutElement(TestIdentifier testIdentifier, XMLStreamWriter writer)
			throws XMLStreamException {

		String cData = "\nunique-id: " + testIdentifier.getUniqueId() //
				+ "\ndisplay-name: " + testIdentifier.getDisplayName() + "\n";

		writer.writeStartElement("system-out");
		writer.writeCData(cData);
		writer.writeEndElement();
		newLine(writer);
	}

	private void newLine(XMLStreamWriter xmlWriter) throws XMLStreamException {
		xmlWriter.writeCharacters("\n");
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
