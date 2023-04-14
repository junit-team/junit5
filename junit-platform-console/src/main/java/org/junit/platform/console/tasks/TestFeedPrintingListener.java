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

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import org.junit.platform.commons.util.ExceptionUtils;
import org.junit.platform.engine.TestExecutionResult;
import org.junit.platform.engine.UniqueId;
import org.junit.platform.launcher.TestIdentifier;
import org.junit.platform.launcher.TestPlan;

class TestFeedPrintingListener implements DetailsPrintingListener {

	private static final Pattern LINE_START_PATTERN = Pattern.compile("(?m)^");

	static final String INDENTATION = "\t";

	private final PrintWriter out;
	private final ColorPalette colorPalette;
	private TestPlan testPlan;

	TestFeedPrintingListener(PrintWriter out, ColorPalette colorPalette) {
		this.out = out;
		this.colorPalette = colorPalette;
	}

	@Override
	public void testPlanExecutionStarted(TestPlan testPlan) {
		this.testPlan = testPlan;
	}

	@Override
	public void testPlanExecutionFinished(TestPlan testPlan) {
		this.testPlan = null;
	}

	@Override
	public void executionSkipped(TestIdentifier testIdentifier, String reason) {
		if (testIdentifier.isContainer())
			return;
		String msg = formatTestIdentifier(testIdentifier);
		println(Style.SKIPPED, "%s > SKIPPED%n\tReason: %s", msg, reason);
	}

	@Override
	public void executionStarted(TestIdentifier testIdentifier) {
		if (testIdentifier.isContainer())
			return;
		String msg = formatTestIdentifier(testIdentifier);
		println(Style.valueOf(testIdentifier), "%s > STARTED", msg);
	}

	@Override
	public void executionFinished(TestIdentifier testIdentifier, TestExecutionResult testExecutionResult) {
		if (testIdentifier.isContainer())
			return;
		TestExecutionResult.Status status = testExecutionResult.getStatus();
		String msg = formatTestIdentifier(testIdentifier);
		if (testExecutionResult.getThrowable().isPresent()) {
			Throwable throwable = testExecutionResult.getThrowable().get();
			String format = "%s > %s%n" + INDENTATION + "%s";
			String stacktrace = indented(ExceptionUtils.readStackTrace(throwable));
			println(Style.valueOf(testIdentifier), format, msg, status, stacktrace);
		}
		else {
			println(Style.valueOf(testIdentifier), "%s > %s", msg, status);
		}
	}

	private String formatTestIdentifier(TestIdentifier testIdentifier) {
		return String.join(" > ", collectDisplayNames(testIdentifier.getUniqueIdObject()));
	}

	private void println(Style style, String format, Object... args) {
		println(style, String.format(format, args));
	}

	private void println(Style color, String message) {
		this.out.println(colorPalette.paint(color, message));
	}

	private List<String> collectDisplayNames(UniqueId uniqueId) {
		int size = uniqueId.getSegments().size();
		List<String> displayNames = new ArrayList<>(size);
		for (int i = 0; i < size; i++) {
			displayNames.add(0, testPlan.getTestIdentifier(uniqueId).getDisplayName());
			if (i < size - 1) {
				uniqueId = uniqueId.removeLastSegment();
			}
		}
		return displayNames;
	}

	private static String indented(String message) {
		return LINE_START_PATTERN.matcher(message).replaceAll(INDENTATION).trim();
	}

	@Override
	public void listTests(TestPlan testPlan) {
		this.testPlan = testPlan;
		try {
			testPlan.accept(new TestPlan.Visitor() {
				@Override
				public void visit(TestIdentifier testIdentifier) {
					if (testIdentifier.isContainer())
						return;
					println(Style.valueOf(testIdentifier), formatTestIdentifier(testIdentifier));
				}
			});
		}
		finally {
			this.testPlan = null;
		}
	}
}
