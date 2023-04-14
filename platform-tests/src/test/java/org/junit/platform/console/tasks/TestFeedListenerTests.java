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

import static org.junit.jupiter.api.Assertions.assertLinesMatch;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.platform.engine.TestExecutionResult;
import org.junit.platform.engine.UniqueId;
import org.junit.platform.fakes.TestDescriptorStub;
import org.junit.platform.launcher.TestIdentifier;

public class TestFeedListenerTests {

	private static final String EOL = System.lineSeparator();

	@Test
	public void testExecutionSkipped() {
		StringWriter stringWriter = new StringWriter();
		listener(stringWriter).executionSkipped(newTestIdentifier(), "Test disabled");
		String[] lines = lines(stringWriter);
		assertLinesMatch(List.of("[engine:demo-engine] > %c ool test SKIPPED\n Test disabled"), List.of(lines));
	}

	@Test
	public void testExecutionFailed() {
		StringWriter stringWriter = new StringWriter();
		listener(stringWriter).executionFinished(newTestIdentifier(),
			TestExecutionResult.failed(new AssertionError("Boom!")));
		String[] lines = lines(stringWriter);
		assertLinesMatch(List.of("[engine:demo-engine] > %c ool test > FAILED \n Boom!"), List.of(lines));
	}

	@Test
	public void testExecutionSucceeded() {
		StringWriter stringWriter = new StringWriter();
		listener(stringWriter).executionFinished(newTestIdentifier(), TestExecutionResult.successful());
		String[] lines = lines(stringWriter);
		assertLinesMatch(
			List.of("[engine:demo-engine] > %c ool test > " + TestExecutionResult.Status.SUCCESSFUL.toString()),
			List.of(lines));
	}

	private TestFeedPrintingListener listener(StringWriter stringWriter) {
		return new TestFeedPrintingListener(new PrintWriter(stringWriter), ColorPalette.NONE);
	}

	private static TestIdentifier newTestIdentifier() {
		TestDescriptorStub testDescriptor = new TestDescriptorStub(UniqueId.forEngine("demo-engine"), "%c ool test");
		return TestIdentifier.from(testDescriptor);
	}

	private String[] lines(StringWriter stringWriter) {
		return stringWriter.toString().split(EOL);
	}

}
