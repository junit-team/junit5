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

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
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
	public void executionSkipped() {
		StringWriter stringWriter = new StringWriter();
		listener(stringWriter).executionSkipped(newTestIdentifier(), "Test" + EOL + "disabled");
		String[] lines = lines(stringWriter);

		assertEquals(7, lines.length);
		assertAll("lines in the output", //
			() -> assertEquals("├─ demo-test not executed", lines[0]), //
			() -> assertEquals("│       tags: []", lines[1]), //
			() -> assertEquals("│   uniqueId: [engine:demo-engine]", lines[2]), //
			() -> assertEquals("│     parent: []", lines[3]), //
			() -> assertEquals("│     reason: Test", lines[4]), //
			() -> assertEquals("│               disabled", lines[5]), //
			() -> assertEquals("│  SKIPPED", lines[6]) //
		);
	}

	@Test
	public void reportingEntryPublished() {
		StringWriter stringWriter = new StringWriter();
		listener(stringWriter).reportingEntryPublished(newTestIdentifier(), ReportEntry.from("foo", "bar"));
		String[] lines = lines(stringWriter);

		assertEquals(1, lines.length);
		assertAll("lines in the output", //
			() -> assertTrue(lines[0].startsWith("│    reports: ReportEntry [timestamp =")), //
			() -> assertTrue(lines[0].endsWith(", foo = 'bar']")) //
		);
	}

	@Test
	public void executionFinishedWithFailure() {
		StringWriter stringWriter = new StringWriter();
		listener(stringWriter).executionFinished(newTestIdentifier(), failed(new AssertionError("Boom!")));
		String[] lines = lines(stringWriter);

		assertAll("lines in the output", //
			() -> assertEquals("│     caught: java.lang.AssertionError: Boom!", lines[0]), //
			() -> assertEquals("│  FAILED", lines[lines.length - 1]));
	}

	private ColoredPrintingTestListener listener(StringWriter stringWriter) {
		return new ColoredPrintingTestListener(new PrintWriter(stringWriter), true, 50,
			ColoredPrintingTestListener.Theme.UTF_8, true);
	}

	private static TestIdentifier newTestIdentifier() {
		TestDescriptorStub testDescriptor = new TestDescriptorStub(UniqueId.forEngine("demo-engine"), "demo-test");
		return TestIdentifier.from(testDescriptor);
	}

	private String[] lines(StringWriter stringWriter) {
		return stringWriter.toString().split(EOL);
	}

}
