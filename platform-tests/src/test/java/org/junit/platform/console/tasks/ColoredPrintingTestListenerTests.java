/*
 * Copyright 2015-2016 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.junit.platform.console.tasks;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.platform.console.tasks.ColoredPrintingTestListener.INDENTATION;
import static org.junit.platform.engine.TestExecutionResult.failed;

import java.io.PrintWriter;
import java.io.StringWriter;

import org.junit.jupiter.api.Test;
import org.junit.platform.engine.UniqueId;
import org.junit.platform.engine.reporting.ReportEntry;
import org.junit.platform.engine.test.TestDescriptorStub;
import org.junit.platform.launcher.TestIdentifier;

/**
 * @since 1.0
 */
public class ColoredPrintingTestListenerTests {

	private static final String EOL = System.lineSeparator();

	@Test
	public void executionSkippedMessageIndented() {
		StringWriter stringWriter = new StringWriter();
		PrintWriter out = new PrintWriter(stringWriter);
		ColoredPrintingTestListener listener = new ColoredPrintingTestListener(out, true);

		listener.executionSkipped(newTestIdentifier(), "Test" + EOL + "\tdisabled");
		String output = stringWriter.toString();

		// @formatter:off
		String expected = "Skipped:     failingTest [[engine:demo-engine]]" + EOL
				+ INDENTATION + "=> Reason: Test" + EOL
				+ INDENTATION + "\tdisabled" + EOL;
		// @formatter:on

		assertEquals(expected, output);
	}

	@Test
	public void reportingEntryPublishedMessageIndented() {
		StringWriter stringWriter = new StringWriter();
		PrintWriter out = new PrintWriter(stringWriter);
		ColoredPrintingTestListener listener = new ColoredPrintingTestListener(out, true);

		listener.reportingEntryPublished(newTestIdentifier(), ReportEntry.from("foo", "bar"));
		String[] split = stringWriter.toString().split(System.lineSeparator());

		assertEquals(2, split.length);
		assertAll("lines in the message",
			() -> assertEquals("Reported:    failingTest [[engine:demo-engine]]", split[0]),
			() -> assertTrue(split[1].startsWith(INDENTATION + "=> Reported values: ReportEntry [timestamp =")),
			() -> assertTrue(split[1].endsWith(", foo = 'bar']")));
	}

	@Test
	public void exceptionMessageIndented() {
		StringWriter stringWriter = new StringWriter();
		PrintWriter out = new PrintWriter(stringWriter);
		ColoredPrintingTestListener listener = new ColoredPrintingTestListener(out, true);

		listener.executionFinished(newTestIdentifier(),
			failed(new Throwable("Fail" + EOL + "\texpected: <foo> but was: <bar>")));
		String output = stringWriter.toString();

		// @formatter:off
		String expected = "Finished:    failingTest [[engine:demo-engine]]" + EOL
				+ INDENTATION + "=> Exception: Fail" + EOL
				+ INDENTATION + "\texpected: <foo> but was: <bar>" + EOL;
		// @formatter:on

		assertEquals(expected, output);
	}

	private static TestIdentifier newTestIdentifier() {
		TestDescriptorStub testDescriptor = new TestDescriptorStub(UniqueId.forEngine("demo-engine"), "failingTest");
		return TestIdentifier.from(testDescriptor);
	}

}
