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
		// TODO #86 consider skipped/failed/aborted containers
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

		long skipped = 0;
		long failures = 0;
		for (TestIdentifier test : tests) {
			if (reportData.wasSkipped(test)) {
				skipped++;
			}
			else if (reportData.wasFinished(test)) {
				TestExecutionResult result = reportData.getResult(test);
				if (result.getStatus() == FAILED) {
					failures++;
				}
			}
		}

		writer.writeAttribute("name", testIdentifier.getDisplayName());
		writer.writeAttribute("tests", String.valueOf(tests.size()));
		// TODO #86 compute error count
		writer.writeAttribute("skipped", String.valueOf(skipped));
		writer.writeAttribute("failures", String.valueOf(failures));
		writer.writeAttribute("errors", "0");

		NumberFormat numberFormat = NumberFormat.getInstance(Locale.US);
		writer.writeAttribute("time", numberFormat.format(reportData.getDurationInSeconds(testIdentifier)));

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
		writer.writeAttribute("time", numberFormat.format(reportData.getDurationInSeconds(test)));
		// TODO #86 write error elements
		writer.writeComment("Unique ID: " + test.getUniqueId().toString());
		if (reportData.wasSkipped(test)) {
			writeSkipped(reportData.getSkipReason(test), writer);
		}
		else if (reportData.wasFinished(test)) {
			TestExecutionResult result = reportData.getResult(test);
			if (result.getStatus() == FAILED) {
				writeFailure(result.getThrowable(), writer);
			}
		}
		writer.writeEndElement();
	}

	private void writeSkipped(String reason, XMLStreamWriter writer) throws XMLStreamException {
		if (isNotBlank(reason)) {
			writer.writeStartElement("skipped");
			writer.writeCharacters(reason);
			writer.writeEndElement();
		}
		else {
			writer.writeEmptyElement("skipped");
		}
	}

	private void writeFailure(Optional<Throwable> throwable, XMLStreamWriter writer) throws XMLStreamException {
		writer.writeStartElement("failure");
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

}
