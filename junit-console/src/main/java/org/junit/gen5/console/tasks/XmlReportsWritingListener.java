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
import static org.junit.gen5.commons.util.StringUtils.isNotBlank;
import static org.junit.gen5.engine.TestExecutionResult.Status.FAILED;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.nio.file.Files;
import java.text.NumberFormat;
import java.time.Clock;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import org.junit.gen5.engine.TestExecutionResult;
import org.junit.gen5.launcher.TestExecutionListener;
import org.junit.gen5.launcher.TestIdentifier;
import org.junit.gen5.launcher.TestPlan;

class XmlReportsWritingListener implements TestExecutionListener {

	private final File reportsDir;
	private final PrintWriter out;
	private final Clock clock;

	private XmlReportData reportData;

	public XmlReportsWritingListener(String reportsDir, PrintWriter out) {
		this(reportsDir, out, Clock.systemDefaultZone());
	}

	// For tests only
	XmlReportsWritingListener(String reportsDir, PrintWriter out, Clock clock) {
		this.reportsDir = new File(reportsDir);
		this.out = out;
		this.clock = clock;
	}

	@Override
	public void testPlanExecutionStarted(TestPlan testPlan) {
		this.reportData = new XmlReportData(testPlan, clock);
		try {
			Files.createDirectories(reportsDir.toPath());
		}
		catch (IOException e) {
			out.println("Could not create report directory: " + reportsDir);
			e.printStackTrace(out);
		}
	}

	@Override
	public void executionSkipped(TestIdentifier testIdentifier, String reason) {
		reportData.markSkipped(testIdentifier, reason);
		// TODO #86 write file for roots
	}

	@Override
	public void executionStarted(TestIdentifier testIdentifier) {
		reportData.markStarted(testIdentifier);
	}

	@Override
	public void executionFinished(TestIdentifier testIdentifier, TestExecutionResult result) {
		reportData.markFinished(testIdentifier, result);
		if (isARoot(testIdentifier)) {
			// TODO #86 consider skipped/failed/aborted containers
			// @formatter:off
			File xmlFile = new File(reportsDir, "TEST-" + testIdentifier.getUniqueId() + ".xml");
			writeXmlReport(testIdentifier, xmlFile);
		}
	}

	private boolean isARoot(TestIdentifier testIdentifier) {
		return !testIdentifier.getParentId().isPresent();
	}

	private void writeXmlReport(TestIdentifier testIdentifier, File xmlFile) {
		List<TestIdentifier> tests = reportData.getTestPlan().getDescendants(testIdentifier).stream()
				.filter(TestIdentifier::isTest)
				.collect(toList());
		// @formatter:on
		if (!tests.isEmpty()) {
			writeXmlReport(testIdentifier, tests, xmlFile);
		}
	}

	private void writeXmlReport(TestIdentifier testIdentifier, List<TestIdentifier> tests, File xmlFile) {
		try (Writer fileWriter = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(xmlFile), UTF_8))) {
			XMLOutputFactory factory = XMLOutputFactory.newInstance();
			XMLStreamWriter xmlWriter = factory.createXMLStreamWriter(fileWriter);
			xmlWriter.writeStartDocument();
			writeTestsuite(testIdentifier, tests, xmlWriter);
			xmlWriter.writeEndDocument();
			xmlWriter.flush();
			xmlWriter.close();
		}
		catch (XMLStreamException | IOException e) {
			out.println("Could not write file: " + xmlFile);
			e.printStackTrace(out);
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

	// TODO #86 Move to ExceptionUtils
	private String readStackTrace(Throwable throwable) {
		StringWriter stringWriter = new StringWriter();
		try (PrintWriter printWriter = new PrintWriter(stringWriter)) {
			throwable.printStackTrace(printWriter);
		}
		return stringWriter.toString();
	}

}
