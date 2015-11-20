/*
 * Copyright 2015 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.junit.gen5.launcher.listeners;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.StringJoiner;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicLong;

import lombok.Value;

import org.junit.gen5.engine.TestDescriptor;

// TODO Give it a REAL interface and make it threadsafe
public class TestExecutionSummary {

	final AtomicLong testsStarted = new AtomicLong();
	final AtomicLong testsFound = new AtomicLong();
	final AtomicLong testsSkipped = new AtomicLong();
	final AtomicLong testsAborted = new AtomicLong();
	final AtomicLong testsSucceeded = new AtomicLong();
	final AtomicLong testsFailed = new AtomicLong();

	long timeStarted;
	long timePaused;
	long timeFinished;

	private String message;
	private List<Failure> failures = new ArrayList<>();

	void finishTestRun(String message) {
		this.timeFinished = System.currentTimeMillis();
		this.message = message;
	}

	public void printOn(PrintWriter writer) {

		// @formatter:off
		writer.println(String.format(
			"%n%s after %d ms\n"
			+ "[%10d tests found     ]\n"
			+ "[%10d tests started   ]\n"
			+ "[%10d tests skipped   ]\n"
			+ "[%10d tests aborted   ]\n"
			+ "[%10d tests successful]\n"
			+ "[%10d tests failed    ]\n",
			this.message, (this.timeFinished - this.timeStarted), this.testsFound.get(), this.testsStarted.get(),
			this.testsSkipped.get(), this.testsAborted.get(), this.testsSucceeded.get(), this.testsFailed.get()));
		// @formatter:on

		//		if (countFailedTests() > 0) {
		//			writer.println("Test failures: ");
		//		}

		writer.flush();
	}

	public long countFailedTests() {
		return testsFailed.get();
	}

	public void addFailure(TestDescriptor testDescriptor, Throwable throwable) {
		failures.add(new Failure(testDescriptor, throwable));
	}

	@Value
	static class Failure {

		final private TestDescriptor descriptor;
		final private Throwable exception;
	}
}
