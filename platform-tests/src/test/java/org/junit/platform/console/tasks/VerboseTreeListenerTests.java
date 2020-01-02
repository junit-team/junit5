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

import static org.junit.jupiter.api.Assertions.assertLinesMatch;
import static org.junit.platform.engine.TestExecutionResult.failed;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.platform.console.options.Theme;
import org.junit.platform.engine.UniqueId;
import org.junit.platform.engine.reporting.ReportEntry;
import org.junit.platform.fakes.TestDescriptorStub;
import org.junit.platform.launcher.TestIdentifier;

/**
 * @since 1.3.2
 */
class VerboseTreeListenerTests {

	private static final String EOL = System.lineSeparator();

	@Test
	void executionSkipped() {
		StringWriter stringWriter = new StringWriter();
		listener(stringWriter).executionSkipped(newTestIdentifier(), "Test" + EOL + "disabled");
		String[] lines = lines(stringWriter);

		assertLinesMatch(List.of( //
			"+-- %c ool test", //
			"     tags: []", //
			" uniqueId: [engine:demo-engine]", //
			"   parent: []", //
			"   reason: Test", //
			"             disabled", //
			"   status: [S] SKIPPED"), List.of(lines));
	}

	@Test
	void reportingEntryPublished() {
		StringWriter stringWriter = new StringWriter();
		listener(stringWriter).reportingEntryPublished(newTestIdentifier(), ReportEntry.from("foo", "bar"));
		String[] lines = lines(stringWriter);

		assertLinesMatch(List.of("  reports: ReportEntry \\[timestamp = .+, foo = 'bar'\\]"), List.of(lines));
	}

	@Test
	void executionFinishedWithFailure() {
		StringWriter stringWriter = new StringWriter();
		listener(stringWriter).executionFinished(newTestIdentifier(), failed(new AssertionError("Boom!")));
		String[] lines = lines(stringWriter);

		assertLinesMatch(List.of("   caught: java.lang.AssertionError: Boom!", //
			">> STACKTRACE >>", //
			" duration: \\d+ ms", //
			"   status: [X] FAILED"), List.of(lines));
	}

	@Test
	void failureMessageWithFormatSpecifier() {
		StringWriter stringWriter = new StringWriter();
		listener(stringWriter).executionFinished(newTestIdentifier(), failed(new AssertionError("%crash")));
		String[] lines = lines(stringWriter);

		assertLinesMatch(List.of("   caught: java.lang.AssertionError: %crash", //
			">> STACKTRACE >>", //
			" duration: \\d+ ms", //
			"   status: [X] FAILED"), List.of(lines));
	}

	private VerboseTreePrintingListener listener(StringWriter stringWriter) {
		return new VerboseTreePrintingListener(new PrintWriter(stringWriter), true, 16, Theme.ASCII);
	}

	private static TestIdentifier newTestIdentifier() {
		TestDescriptorStub testDescriptor = new TestDescriptorStub(UniqueId.forEngine("demo-engine"), "%c ool test");
		return TestIdentifier.from(testDescriptor);
	}

	private String[] lines(StringWriter stringWriter) {
		return stringWriter.toString().split(EOL);
	}

}
