/*
 * Copyright 2015-2020 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * https://www.eclipse.org/legal/epl-v20.html
 */

package org.junit.platform.console.tasks;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.platform.console.tasks.FlatPrintingListener.INDENTATION;
import static org.junit.platform.engine.TestExecutionResult.failed;

import java.io.PrintWriter;
import java.io.StringWriter;

import org.junit.jupiter.api.Test;
import org.junit.platform.engine.UniqueId;
import org.junit.platform.engine.reporting.ReportEntry;
import org.junit.platform.fakes.TestDescriptorStub;
import org.junit.platform.launcher.TestIdentifier;

/**
 * @since 1.0
 */
class FlatPrintingListenerTests {

	private static final String EOL = System.lineSeparator();

	@Test
	void executionSkipped() {
		StringWriter stringWriter = new StringWriter();
		listener(stringWriter).executionSkipped(newTestIdentifier(), "Test" + EOL + "disabled");
		String[] lines = lines(stringWriter);

		assertEquals(3, lines.length);
		assertAll("lines in the output", //
			() -> assertEquals("Skipped:     demo-test ([engine:demo-engine])", lines[0]), //
			() -> assertEquals(INDENTATION + "=> Reason: Test", lines[1]), //
			() -> assertEquals(INDENTATION + "disabled", lines[2]));
	}

	@Test
	void reportingEntryPublished() {
		StringWriter stringWriter = new StringWriter();
		listener(stringWriter).reportingEntryPublished(newTestIdentifier(), ReportEntry.from("foo", "bar"));
		String[] lines = lines(stringWriter);

		assertEquals(2, lines.length);
		assertAll("lines in the output", //
			() -> assertEquals("Reported:    demo-test ([engine:demo-engine])", lines[0]), //
			() -> assertTrue(lines[1].startsWith(INDENTATION + "=> Reported values: ReportEntry [timestamp =")), //
			() -> assertTrue(lines[1].endsWith(", foo = 'bar']")));
	}

	@Test
	void executionFinishedWithFailure() {
		StringWriter stringWriter = new StringWriter();
		listener(stringWriter).executionFinished(newTestIdentifier(), failed(new AssertionError("Boom!")));
		String[] lines = lines(stringWriter);

		assertAll("lines in the output", //
			() -> assertEquals("Finished:    demo-test ([engine:demo-engine])", lines[0]), //
			() -> assertEquals(INDENTATION + "=> Exception: java.lang.AssertionError: Boom!", lines[1]));
	}

	private FlatPrintingListener listener(StringWriter stringWriter) {
		return new FlatPrintingListener(new PrintWriter(stringWriter), true);
	}

	private static TestIdentifier newTestIdentifier() {
		TestDescriptorStub testDescriptor = new TestDescriptorStub(UniqueId.forEngine("demo-engine"), "demo-test");
		return TestIdentifier.from(testDescriptor);
	}

	private String[] lines(StringWriter stringWriter) {
		return stringWriter.toString().split(EOL);
	}

}
