/*
 * Copyright 2015-2023 the original author or authors.
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

import java.io.PrintWriter;
import java.io.StringWriter;

import org.junit.jupiter.api.Test;
import org.junit.platform.engine.TestExecutionResult;
import org.junit.platform.engine.UniqueId;
import org.junit.platform.fakes.TestDescriptorStub;
import org.junit.platform.launcher.TestIdentifier;

class TestFeedPrintingListenerTest {

	private static final String EOL = System.lineSeparator();

	@Test
	void executionSkipped() {
		StringWriter writer = new StringWriter();
		TestFeedPrintingListener testFeedPrintingListener = createTestFeedPrintingListener(writer);
		testFeedPrintingListener.executionSkipped(createTestIdentifier(), "Test skipped");

		String[] lines = writer.toString().split(EOL);

		assertEquals(2, lines.length);

		assertAll("lines in output", () -> assertEquals("[engine:demo-engine] > demo-test SKIPPED", lines[0]),
			() -> assertEquals("\tReason: Test skipped", lines[1]));
	}

	@Test
	void executionStarted() {
		StringWriter writer = new StringWriter();
		TestFeedPrintingListener testFeedPrintingListener = createTestFeedPrintingListener(writer);
		testFeedPrintingListener.executionStarted(createTestIdentifier());

		String[] lines = writer.toString().split(EOL);

		assertEquals(1, lines.length);
		assertEquals("[engine:demo-engine] > demo-test > STARTED", lines[0]);
	}

	@Test
	void executionFinishedSuccessfully() {
		StringWriter writer = new StringWriter();
		TestFeedPrintingListener testFeedPrintingListener = createTestFeedPrintingListener(writer);
		testFeedPrintingListener.executionFinished(createTestIdentifier(), TestExecutionResult.successful());

		String[] lines = writer.toString().split(EOL);

		assertEquals(1, lines.length);
		assertEquals("[engine:demo-engine] > demo-test > SUCCESSFUL", lines[0]);
	}

	@Test
	void executionFinishedWithFailure() {
		StringWriter writer = new StringWriter();
		TestFeedPrintingListener testFeedPrintingListener = createTestFeedPrintingListener(writer);
		testFeedPrintingListener.executionFinished(createTestIdentifier(),
			TestExecutionResult.failed(new AssertionError("test failed")));

		String[] lines = writer.toString().split(EOL);

		assertAll("lines in test failure output",
			() -> assertEquals("[engine:demo-engine] > demo-test > FAILED", lines[0]),
			() -> assertEquals("\tjava.lang.AssertionError: test failed", lines[1]));
	}

	@Test
	void executionAborted() {
		StringWriter writer = new StringWriter();
		TestFeedPrintingListener testFeedPrintingListener = createTestFeedPrintingListener(writer);
		testFeedPrintingListener.executionFinished(createTestIdentifier(),
			TestExecutionResult.aborted(new AssertionError("test aborted")));

		String[] lines = writer.toString().split(EOL);

		assertAll("lines in test aborted output",
			() -> assertEquals("[engine:demo-engine] > demo-test > ABORTED", lines[0]),
			() -> assertEquals("\tjava.lang.AssertionError: test aborted", lines[1]));
	}

	private TestFeedPrintingListener createTestFeedPrintingListener(StringWriter stringWriter) {
		return new TestFeedPrintingListener(new PrintWriter(stringWriter), ColorPalette.NONE);
	}

	private TestIdentifier createTestIdentifier() {
		return TestIdentifier.from(new TestDescriptorStub(UniqueId.forEngine("demo-engine"), "demo-test"));
	}
}
