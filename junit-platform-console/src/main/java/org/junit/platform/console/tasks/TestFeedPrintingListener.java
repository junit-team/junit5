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

import static org.junit.platform.console.tasks.Color.NONE;

import java.io.PrintWriter;
import java.util.regex.Pattern;

import org.junit.platform.commons.util.ExceptionUtils;
import org.junit.platform.engine.TestExecutionResult;
import org.junit.platform.launcher.TestExecutionListener;
import org.junit.platform.launcher.TestIdentifier;

public class TestFeedPrintingListener implements TestExecutionListener {

	private static final Pattern LINE_START_PATTERN = Pattern.compile("(?m)^");

	static final String INDENTATION = "\t";

	private final PrintWriter out;
	private final ColorPalette colorPalette;

	public TestFeedPrintingListener(PrintWriter out, ColorPalette colorPalette) {
		this.out = out;
		this.colorPalette = colorPalette;
	}

	@Override
	public void executionSkipped(TestIdentifier testIdentifier, String reason) {
		if (testIdentifier.isContainer())
			return;
		String msg = printTestIdenfifier(testIdentifier);
		println(Color.SKIPPED, "%s SKIPPED\n\tReason: %s", msg, reason);
	}

	@Override
	public void executionStarted(TestIdentifier testIdentifier) {
		if (testIdentifier.isContainer())
			return;
		String msg = printTestIdenfifier(testIdentifier);
		println(Color.valueOf(testIdentifier), "%s > STARTED", msg);
	}

	@Override
	public void executionFinished(TestIdentifier testIdentifier, TestExecutionResult testExecutionResult) {
		if (testIdentifier.isContainer())
			return;
		TestExecutionResult.Status status = testExecutionResult.getStatus();
		String msg = printTestIdenfifier(testIdentifier);
		if (testExecutionResult.getThrowable().isPresent()) {
			Throwable throwable = testExecutionResult.getThrowable().get();
			println(Color.valueOf(testIdentifier), "%s > %s\n\t%s", msg, status.toString(),
				indented(ExceptionUtils.readStackTrace(throwable)));
		}
		else {
			println(Color.valueOf(testIdentifier), "%s > %s", msg, status.toString());
		}
	}

	private String printTestIdenfifier(TestIdentifier testIdentifier) {
		String msg = generateMessageFromUniqueId(testIdentifier.getUniqueId());
		msg += " > " + testIdentifier.getDisplayName();
		return msg;
	}

	private void println(Color color, String format, Object... args) {
		println(color, String.format(format, args));
	}

	private void println(Color color, String message) {
		if (this.disableAnsiColors) {
			this.out.println(message);
		}
		else {
			// Use string concatenation to avoid ANSI disruption on console
			this.out.println(color + message + NONE);
		}
	}

	private String generateMessageFromUniqueId(String uniqueId) {
		System.out.println(uniqueId);
		String[] messages = uniqueId.split("/");
		String engine = parseEngine(messages[0]);
		String output = engine + "";
		for (int i = 1; i < messages.length; i++) {
			String message = messages[i];
			if (message.indexOf("class:") != -1) {
				output += " > " + parseMessage(message, "class:");
			}
			else if (message.indexOf("method:") != -1) {
				output += " > " + parseMessage(message, "method:");
			}
		}
		return output;
	}

	private String parseEngine(String uniqueId) {
		if (uniqueId.contains("junit-jupiter")) {
			return "JUnit Jupiter";
		}
		else if (uniqueId.contains("junit-vintage")) {
			return "JUnit Vintage";
		}
		return uniqueId;
	}

	private String parseMessage(String className, String prefix) {
		return className.replaceAll("\\[", "").replaceAll("]", "").replaceAll(prefix, "");
	}

	private static String indented(String message) {
		return LINE_START_PATTERN.matcher(message).replaceAll(INDENTATION).trim();
	}
}
