/*
 * Copyright 2015-2024 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * https://www.eclipse.org/legal/epl-v20.html
 */

package org.junit.platform.console.tasks;

import static org.apiguardian.api.API.Status.INTERNAL;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;

import org.apiguardian.api.API;
import org.junit.platform.engine.reporting.ReportEntry;
import org.junit.platform.launcher.LauncherConstants;
import org.junit.platform.launcher.TestExecutionListener;
import org.junit.platform.launcher.TestIdentifier;
import org.junit.platform.launcher.TestPlan;

@API(status = INTERNAL, since = "5.11")
public class RedirectStdoutAndStderrListener implements TestExecutionListener {
	private final Path stdoutOutputPath;
	private final Path stderrOutputPath;
	private final StringWriter stdoutBuffer;
	private final StringWriter stderrBuffer;
	private final PrintWriter out;

	public RedirectStdoutAndStderrListener(Path stdoutOutputPath, Path stderrOutputPath, PrintWriter out) {
		this.stdoutOutputPath = stdoutOutputPath;
		this.stderrOutputPath = stderrOutputPath;
		this.stdoutBuffer = new StringWriter();
		this.stderrBuffer = new StringWriter();
		this.out = out;
	}

	public void reportingEntryPublished(TestIdentifier testIdentifier, ReportEntry entry) {
		if (testIdentifier.isTest()) {
			String redirectedStdoutContent = entry.getKeyValuePairs().get(LauncherConstants.STDOUT_REPORT_ENTRY_KEY);
			String redirectedStderrContent = entry.getKeyValuePairs().get(LauncherConstants.STDERR_REPORT_ENTRY_KEY);

			if (redirectedStdoutContent != null && !redirectedStdoutContent.isEmpty()) {
				this.stdoutBuffer.append(redirectedStdoutContent);
			}
			if (redirectedStderrContent != null && !redirectedStderrContent.isEmpty()) {
				this.stderrBuffer.append(redirectedStderrContent);
			}
		}
	}

	public void testPlanExecutionFinished(TestPlan testPlan) {
		if (stdoutBuffer.getBuffer().length() > 0) {
			flushBufferedOutputToFile(this.stdoutOutputPath, this.stdoutBuffer);
		}
		if (stderrBuffer.getBuffer().length() > 0) {
			flushBufferedOutputToFile(this.stderrOutputPath, this.stderrBuffer);
		}
	}

	private void flushBufferedOutputToFile(Path file, StringWriter buffer) {
		deleteFile(file);
		createFile(file);
		writeContentToFile(file, buffer.toString());
	}

	private void writeContentToFile(Path file, String buffer) {
		try (Writer fileWriter = Files.newBufferedWriter(file)) {
			fileWriter.write(buffer);
		}
		catch (IOException e) {
			printException("Failed to write content to file: " + file, e);
		}
	}

	private void deleteFile(Path file) {
		try {
			Files.deleteIfExists(file);
		}
		catch (IOException e) {
			printException("Failed to delete file: " + file, e);
		}
	}

	private void createFile(Path file) {
		try {
			Files.createFile(file);
		}
		catch (IOException e) {
			printException("Failed to create file: " + file, e);
		}
	}

	private void printException(String message, Exception exception) {
		out.println(message);
		exception.printStackTrace(out);
	}
}
