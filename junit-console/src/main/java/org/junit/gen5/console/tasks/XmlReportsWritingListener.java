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
import static org.junit.gen5.engine.TestExecutionResult.Status.ABORTED;
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
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

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

	private TestPlan testPlan;
	private Map<TestIdentifier, TestExecutionResult> finishedTests = new ConcurrentHashMap<>();
	private Map<TestIdentifier, String> skippedTests = new ConcurrentHashMap<>();

	public XmlReportsWritingListener(String reportsDir, PrintWriter out) {
		this.reportsDir = new File(reportsDir);
		this.out = out;
	}

	@Override
	public void testPlanExecutionStarted(TestPlan testPlan) {
		this.testPlan = testPlan;
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
		// TODO #86 write file for roots
		skippedTests.put(testIdentifier, reason == null ? "" : reason);
	}

	@Override
	public void executionStarted(TestIdentifier testIdentifier) {
		// TODO #86 start timer
	}

	@Override
	public void executionFinished(TestIdentifier testIdentifier, TestExecutionResult result) {
		// TODO #86 stop timer
		if (result.getStatus() == ABORTED) {
			String reason = result.getThrowable().map(this::readStackTrace).orElse("");
			skippedTests.put(testIdentifier, reason);
		}
		else {
			finishedTests.put(testIdentifier, result);
		}
		if (isARoot(testIdentifier)) {
			// TODO #86 consider skipped/failed/aborted containers
			// @formatter:off
			List<TestIdentifier> tests = testPlan.getDescendants(testIdentifier).stream()
					.filter(TestIdentifier::isTest)
					.collect(toList());
			// @formatter:on
			if (!tests.isEmpty()) {
				File xmlFile = new File(reportsDir, "TEST-" + testIdentifier.getUniqueId() + ".xml");
				writeXmlReport(testIdentifier, tests, xmlFile);
			}
		}
	}

	private boolean isARoot(TestIdentifier testIdentifier) {
		return !testIdentifier.getParentId().isPresent();
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
			if (skippedTests.containsKey(test)) {
				skipped++;
			}
			else if (finishedTests.containsKey(test)) {
				TestExecutionResult result = finishedTests.get(test);
				if (result.getStatus() == FAILED) {
					failures++;
				}
			}
		}

		writer.writeAttribute("name", testIdentifier.getDisplayName());
		writer.writeAttribute("tests", String.valueOf(tests.size()));
		// TODO #86 compute skipped/failure/error counts
		writer.writeAttribute("skipped", String.valueOf(skipped));
		writer.writeAttribute("failures", String.valueOf(failures));
		writer.writeAttribute("errors", "0");
		// TODO #86 measure time
		writer.writeAttribute("time", "0.0");

		writer.writeComment("Unique ID: " + testIdentifier.getUniqueId().toString());

		for (TestIdentifier test : tests) {
			writeTestcase(test, writer);
		}

		writer.writeEndElement();
	}

	private void writeTestcase(TestIdentifier test, XMLStreamWriter writer) throws XMLStreamException {
		writer.writeStartElement("testcase");
		writer.writeAttribute("name", test.getDisplayName());
		Optional<TestIdentifier> parent = testPlan.getParent(test);
		if (parent.isPresent()) {
			writer.writeAttribute("classname", parent.get().getName());
		}
		// TODO #86 measure time
		writer.writeAttribute("time", "0.0");
		// TODO #86 write skipped/error/failure elements
		writer.writeComment("Unique ID: " + test.getUniqueId().toString());
		if (skippedTests.containsKey(test)) {
			writeSkipped(skippedTests.get(test), writer);
		}
		else if (finishedTests.containsKey(test)) {
			TestExecutionResult result = finishedTests.get(test);
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

	private String readStackTrace(Throwable throwable) {
		StringWriter stringWriter = new StringWriter();
		try (PrintWriter printWriter = new PrintWriter(stringWriter)) {
			throwable.printStackTrace(printWriter);
		}
		return stringWriter.toString();
	}

}
