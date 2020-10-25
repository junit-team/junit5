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

import static java.text.MessageFormat.format;
import static java.time.format.DateTimeFormatter.ISO_LOCAL_DATE_TIME;
import static java.util.stream.Collectors.toList;
import static org.junit.platform.commons.util.ExceptionUtils.readStackTrace;
import static org.junit.platform.commons.util.StringUtils.isNotBlank;
import static org.junit.platform.engine.TestExecutionResult.Status.FAILED;
import static org.junit.platform.launcher.LauncherConstants.STDERR_REPORT_ENTRY_KEY;
import static org.junit.platform.launcher.LauncherConstants.STDOUT_REPORT_ENTRY_KEY;

import java.io.Writer;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.text.NumberFormat;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;
import java.util.TreeSet;
import java.util.regex.Pattern;

import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import org.junit.platform.engine.TestExecutionResult;
import org.junit.platform.engine.reporting.ReportEntry;
import org.junit.platform.launcher.TestIdentifier;
import org.junit.platform.reporting.legacy.LegacyReportingUtils;

/**
 * {@code XmlReportWriter} writes an XML report whose format is compatible
 * with the de facto standard for JUnit 4 based test reports that was made
 * popular by the Ant build system.
 *
 * @since 1.4
 */
class XmlReportWriter {

	// Using zero-width assertions in the split pattern simplifies the splitting process: All split parts
	// (including the first and last one) can be used directly, without having to re-add separator characters.
	private static final Pattern CDATA_SPLIT_PATTERN = Pattern.compile("(?<=]])(?=>)");

	private final XmlReportData reportData;

	XmlReportWriter(XmlReportData reportData) {
		this.reportData = reportData;
	}

	void writeXmlReport(TestIdentifier testIdentifier, Writer out) throws XMLStreamException {
		// @formatter:off
		List<TestIdentifier> tests = this.reportData.getTestPlan().getDescendants(testIdentifier)
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

		writeOutputElement("system-out", formatNonStandardAttributesAsString(testIdentifier), writer);

		writer.writeEndElement();
		newLine(writer);
	}

	private void writeSuiteAttributes(TestIdentifier testIdentifier, List<TestIdentifier> tests,
			NumberFormat numberFormat, XMLStreamWriter writer) throws XMLStreamException {

		writeAttributeSafely(writer, "name", testIdentifier.getDisplayName());
		writeTestCounts(tests, writer);
		writeAttributeSafely(writer, "time", getTime(testIdentifier, numberFormat));
		writeAttributeSafely(writer, "hostname", getHostname().orElse("<unknown host>"));
		writeAttributeSafely(writer, "timestamp", ISO_LOCAL_DATE_TIME.format(getCurrentDateTime()));
	}

	private void writeTestCounts(List<TestIdentifier> tests, XMLStreamWriter writer) throws XMLStreamException {
		TestCounts testCounts = TestCounts.from(this.reportData, tests);
		writeAttributeSafely(writer, "tests", String.valueOf(testCounts.getTotal()));
		writeAttributeSafely(writer, "skipped", String.valueOf(testCounts.getSkipped()));
		writeAttributeSafely(writer, "failures", String.valueOf(testCounts.getFailures()));
		writeAttributeSafely(writer, "errors", String.valueOf(testCounts.getErrors()));
	}

	private void writeSystemProperties(XMLStreamWriter writer) throws XMLStreamException {
		writer.writeStartElement("properties");
		newLine(writer);
		Properties systemProperties = System.getProperties();
		for (String propertyName : new TreeSet<>(systemProperties.stringPropertyNames())) {
			writer.writeEmptyElement("property");
			writeAttributeSafely(writer, "name", propertyName);
			writeAttributeSafely(writer, "value", systemProperties.getProperty(propertyName));
			newLine(writer);
		}
		writer.writeEndElement();
		newLine(writer);
	}

	private void writeTestcase(TestIdentifier testIdentifier, NumberFormat numberFormat, XMLStreamWriter writer)
			throws XMLStreamException {

		writer.writeStartElement("testcase");

		writeAttributeSafely(writer, "name", getName(testIdentifier));
		writeAttributeSafely(writer, "classname", getClassName(testIdentifier));
		writeAttributeSafely(writer, "time", getTime(testIdentifier, numberFormat));
		newLine(writer);

		writeSkippedOrErrorOrFailureElement(testIdentifier, writer);

		List<String> systemOutElements = new ArrayList<>();
		List<String> systemErrElements = new ArrayList<>();
		systemOutElements.add(formatNonStandardAttributesAsString(testIdentifier));
		collectReportEntries(testIdentifier, systemOutElements, systemErrElements);
		writeOutputElements("system-out", systemOutElements, writer);
		writeOutputElements("system-err", systemErrElements, writer);

		writer.writeEndElement();
		newLine(writer);
	}

	private String getName(TestIdentifier testIdentifier) {
		return testIdentifier.getLegacyReportingName();
	}

	private String getClassName(TestIdentifier testIdentifier) {
		return LegacyReportingUtils.getClassName(this.reportData.getTestPlan(), testIdentifier);
	}

	private void writeSkippedOrErrorOrFailureElement(TestIdentifier testIdentifier, XMLStreamWriter writer)
			throws XMLStreamException {

		if (this.reportData.wasSkipped(testIdentifier)) {
			writeSkippedElement(this.reportData.getSkipReason(testIdentifier), writer);
		}
		else {
			Optional<TestExecutionResult> result = this.reportData.getResult(testIdentifier);
			if (result.isPresent() && result.get().getStatus() == FAILED) {
				writeErrorOrFailureElement(result.get(), writer);
			}
		}
	}

	private void writeSkippedElement(String reason, XMLStreamWriter writer) throws XMLStreamException {
		if (isNotBlank(reason)) {
			writer.writeStartElement("skipped");
			writeCDataSafely(writer, reason);
			writer.writeEndElement();
		}
		else {
			writer.writeEmptyElement("skipped");
		}
		newLine(writer);
	}

	private void writeErrorOrFailureElement(TestExecutionResult result, XMLStreamWriter writer)
			throws XMLStreamException {

		Optional<Throwable> throwable = result.getThrowable();
		if (throwable.isPresent()) {
			writer.writeStartElement(isFailure(result) ? "failure" : "error");
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
			writeAttributeSafely(writer, "message", throwable.getMessage());
		}
		writeAttributeSafely(writer, "type", throwable.getClass().getName());
		writeCDataSafely(writer, readStackTrace(throwable));
	}

	private void collectReportEntries(TestIdentifier testIdentifier, List<String> systemOutElements,
			List<String> systemErrElements) {
		List<ReportEntry> entries = this.reportData.getReportEntries(testIdentifier);
		if (!entries.isEmpty()) {
			List<String> systemOutElementsForCapturedOutput = new ArrayList<>();
			StringBuilder formattedReportEntries = new StringBuilder();
			for (int i = 0; i < entries.size(); i++) {
				ReportEntry reportEntry = entries.get(i);
				Map<String, String> keyValuePairs = new LinkedHashMap<>(reportEntry.getKeyValuePairs());
				removeIfPresentAndAddAsSeparateElement(keyValuePairs, STDOUT_REPORT_ENTRY_KEY,
					systemOutElementsForCapturedOutput);
				removeIfPresentAndAddAsSeparateElement(keyValuePairs, STDERR_REPORT_ENTRY_KEY, systemErrElements);
				if (!keyValuePairs.isEmpty()) {
					buildReportEntryDescription(reportEntry.getTimestamp(), keyValuePairs, i + 1,
						formattedReportEntries);
				}
			}
			systemOutElements.add(formattedReportEntries.toString().trim());
			systemOutElements.addAll(systemOutElementsForCapturedOutput);
		}
	}

	private void removeIfPresentAndAddAsSeparateElement(Map<String, String> keyValuePairs, String key,
			List<String> elements) {
		String value = keyValuePairs.remove(key);
		if (value != null) {
			elements.add(value);
		}
	}

	private void buildReportEntryDescription(LocalDateTime timestamp, Map<String, String> keyValuePairs,
			int entryNumber, StringBuilder result) {
		result.append(
			format("Report Entry #{0} (timestamp: {1})\n", entryNumber, ISO_LOCAL_DATE_TIME.format(timestamp)));
		keyValuePairs.forEach((key, value) -> result.append(format("\t- {0}: {1}\n", key, value)));
	}

	private String getTime(TestIdentifier testIdentifier, NumberFormat numberFormat) {
		return numberFormat.format(this.reportData.getDurationInSeconds(testIdentifier));
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
		return LocalDateTime.now(this.reportData.getClock()).withNano(0);
	}

	private String formatNonStandardAttributesAsString(TestIdentifier testIdentifier) {
		return "unique-id: " + testIdentifier.getUniqueId() //
				+ "\ndisplay-name: " + testIdentifier.getDisplayName();
	}

	private void writeOutputElements(String elementName, List<String> elements, XMLStreamWriter writer)
			throws XMLStreamException {
		for (String content : elements) {
			writeOutputElement(elementName, content, writer);
		}
	}

	private void writeOutputElement(String elementName, String content, XMLStreamWriter writer)
			throws XMLStreamException {
		writer.writeStartElement(elementName);
		writeCDataSafely(writer, "\n" + content + "\n");
		writer.writeEndElement();
		newLine(writer);
	}

	private void writeAttributeSafely(XMLStreamWriter writer, String name, String value) throws XMLStreamException {
		writer.writeAttribute(name, escapeIllegalChars(value));
	}

	private void writeCDataSafely(XMLStreamWriter writer, String data) throws XMLStreamException {
		for (String safeDataPart : CDATA_SPLIT_PATTERN.split(escapeIllegalChars(data))) {
			writer.writeCData(safeDataPart);
		}
	}

	static String escapeIllegalChars(String text) {
		if (text.codePoints().allMatch(XmlReportWriter::isAllowedXmlCharacter)) {
			return text;
		}
		StringBuilder result = new StringBuilder(text.length() * 2);
		text.codePoints().forEach(codePoint -> {
			if (isAllowedXmlCharacter(codePoint)) {
				result.appendCodePoint(codePoint);
			}
			else { // use a Character Reference (cf. https://www.w3.org/TR/xml/#NT-CharRef)
				result.append("&#").append(codePoint).append(';');
			}
		});
		return result.toString();
	}

	private static boolean isAllowedXmlCharacter(int codePoint) {
		// source: https://www.w3.org/TR/xml/#charsets
		return codePoint == 0x9 //
				|| codePoint == 0xA //
				|| codePoint == 0xD //
				|| (codePoint >= 0x20 && codePoint <= 0xD7FF) //
				|| (codePoint >= 0xE000 && codePoint <= 0xFFFD) //
				|| (codePoint >= 0x10000 && codePoint <= 0x10FFFF);
	}

	private void newLine(XMLStreamWriter xmlWriter) throws XMLStreamException {
		xmlWriter.writeCharacters("\n");
	}

	private static boolean isFailure(TestExecutionResult result) {
		Optional<Throwable> throwable = result.getThrowable();
		return throwable.isPresent() && throwable.get() instanceof AssertionError;
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
						if (isFailure(result.get())) {
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

		TestCounts(long total) {
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
