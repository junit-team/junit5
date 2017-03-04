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

import static java.lang.String.format;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.function.Consumer;

import org.junit.platform.console.options.Theme;
import org.junit.platform.engine.TestExecutionResult;
import org.junit.platform.engine.reporting.ReportEntry;
import org.junit.platform.launcher.TestExecutionListener;
import org.junit.platform.launcher.TestIdentifier;

/**
 * @since 1.0
 */
public class TreePrintingListenerTests {

	private final TestExecutionListenerSupport support = new TestExecutionListenerSupport();
	private final TestExecutionListener listener = new TreePrintingListener(support.out(), true, Theme.UNICODE);

	void executionSkipped() {
		Consumer<TestExecutionListener> singleLineReason = listener -> listener.executionSkipped(
			support.createTest("skipped-1"), "Test disabled");
		Consumer<TestExecutionListener> multiLineReason = listener -> listener.executionSkipped(
			support.createTest("skipped-4"), "Test\ndis\n\r\nab\rled");
		List<String> lines = support.execute(listener, singleLineReason.andThen(multiLineReason));
		assertEquals("│  │  ├─ skipped-1 ↷ Test disabled", lines.get(6));
		assertEquals("│  │  ├─ skipped-4 ↷ Test", lines.get(7));
		assertEquals("│  │  │       dis", lines.get(8));
		assertEquals("│  │  │  ", lines.get(9));
		assertEquals("│  │  │       ab", lines.get(10));
		assertEquals("│  │  │       led", lines.get(11));
	}

	void reportingEntryPublished() {
		Consumer<TestExecutionListener> singleLineReports = listener -> {
			TestIdentifier id = support.createTest("report-foo-bars");
			listener.executionStarted(id);
			listener.reportingEntryPublished(id, ReportEntry.from("foo", "bar-1"));
			listener.reportingEntryPublished(id, ReportEntry.from("foo", "bar-2"));
			listener.executionFinished(id, TestExecutionResult.successful());
		};
		Consumer<TestExecutionListener> multiLineReport = listener -> {
			TestIdentifier id = support.createTest("report-multi-line");
			listener.executionStarted(id);
			listener.reportingEntryPublished(id, ReportEntry.from("foo", "b\na\nr"));
			listener.executionFinished(id, TestExecutionResult.successful());
		};
		List<String> lines = support.execute(listener, singleLineReports.andThen(multiLineReport));
		assertEquals("│  │  ├─ report-foo-bars ✔ reported:", lines.get(6));
		assertFrames("│  │  │     ReportEntry [timestamp = ", ", foo = 'bar-1']", lines.get(7));
		assertFrames("│  │  │     ReportEntry [timestamp = ", ", foo = 'bar-2']", lines.get(8));
		assertEquals("│  │  ├─ report-multi-line ✔ reported:", lines.get(9));
		assertFrames("│  │  │     ReportEntry [timestamp = ", ", foo = 'b", lines.get(10));
		assertEquals("│  │  │       a", lines.get(11));
		assertEquals("│  │  │       r']", lines.get(12));
	}

	private static void assertFrames(String leftFrame, String rightFrame, String actual) {
		assertTrue(actual.startsWith(leftFrame), format("'%s' doesn't start with: %s", actual, leftFrame));
		assertTrue(actual.endsWith(rightFrame), format("'%s' doesn't end with: %s", actual, rightFrame));
	}

	void executionFinishedWithFailure() {
		List<String> lines = support.execute(listener, l -> l.executionFinished(support.createTest("oops"),
			TestExecutionResult.failed(new AssertionError("B\no\n\nom\r\n!"))));
		assertEquals("│  │  ├─ oops ✘ B", lines.get(6));
		assertEquals("│  │  │       o", lines.get(7));
		assertEquals("│  │  │  ", lines.get(8));
		assertEquals("│  │  │       om", lines.get(9));
		assertEquals("│  │  │       !", lines.get(10));
	}

	void executionReportAndFail() {
		Consumer<TestExecutionListener> reportAndFail = listener -> {
			TestIdentifier id = support.createTest("report-and-fail");
			listener.executionStarted(id);
			listener.reportingEntryPublished(id, ReportEntry.from("foo", "bar"));
			listener.executionFinished(id, TestExecutionResult.failed(new AssertionError("Boom!")));
		};
		List<String> lines = support.execute(listener, reportAndFail);
		assertEquals("│  │  ├─ report-and-fail ✘ reported:", lines.get(6));
		assertFrames("│  │  │     ReportEntry [timestamp = ", ", foo = 'bar']", lines.get(7));
		assertEquals("│  │  │     exception message: Boom!", lines.get(8));
	}

}
