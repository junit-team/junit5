/*
 * Copyright 2015-2016 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.junit.gen5.launcher.listeners;

import static org.junit.gen5.commons.meta.API.Usage.Experimental;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

import org.junit.gen5.commons.meta.API;
import org.junit.gen5.launcher.TestIdentifier;
import org.junit.gen5.launcher.TestPlan;

/**
 * @since 5.0
 */
// TODO Design a real interface for TestExecutionSummary and make it threadsafe.
@API(Experimental)
public class TestExecutionSummary {

	final AtomicLong testsStarted = new AtomicLong();
	final AtomicLong testsFound = new AtomicLong();
	final AtomicLong testsSkipped = new AtomicLong();
	final AtomicLong testsAborted = new AtomicLong();
	final AtomicLong testsSucceeded = new AtomicLong();
	final AtomicLong testsFailed = new AtomicLong();

	long timeStarted;
	long timeFinished;

	private final TestPlan testPlan;
	private String message;
	private List<Failure> failures = new ArrayList<>();

	public TestExecutionSummary(TestPlan testPlan) {
		this.testPlan = testPlan;
	}

	void finishTestRun(String message) {
		this.timeFinished = System.currentTimeMillis();
		this.message = message;
	}

	public void printOn(PrintWriter writer) {

		// @formatter:off
		writer.println(String.format(
			"%n%s after %d ms\n"
			+ "[%10d tests found     ]\n"
			+ "[%10d tests skipped   ]\n"
			+ "[%10d tests started   ]\n"
			+ "[%10d tests aborted   ]\n"
			+ "[%10d tests successful]\n"
			+ "[%10d tests failed    ]\n",
			this.message, (this.timeFinished - this.timeStarted), this.testsFound.get(), this.testsSkipped.get(),
			this.testsStarted.get(), this.testsAborted.get(), this.testsSucceeded.get(), this.testsFailed.get()));
		// @formatter:on

		writer.flush();
	}

	public void printFailuresOn(PrintWriter writer) {

		if (countFailedTests() > 0) {
			writer.println();
			writer.println(String.format("Test failures (%d):", testsFailed.get()));
			failures.forEach(failure -> {
				// TODO Add source description to text
				writer.println(String.format("  %s", describeTest(failure.getTestIdentifier())));
				failure.getTestIdentifier().getSource().ifPresent(scource -> {
					writer.println(String.format("    %s", scource.toString()));
				});
				writer.println(String.format("    => Exception: %s", failure.getException().getLocalizedMessage()));
			});
		}

		writer.flush();
	}

	private String describeTest(TestIdentifier testIdentifier) {
		List<String> descriptionParts = new ArrayList<>();
		collectTestDescription(Optional.of(testIdentifier), descriptionParts);
		return descriptionParts.stream().collect(Collectors.joining(":"));
	}

	private void collectTestDescription(Optional<TestIdentifier> optionalIdentifier, List<String> descriptionParts) {
		optionalIdentifier.ifPresent(testIdentifier -> {
			descriptionParts.add(0, testIdentifier.getDisplayName());
			collectTestDescription(testPlan.getParent(testIdentifier), descriptionParts);
		});
	}

	public long countFailedTests() {
		return testsFailed.get();
	}

	public void addFailure(TestIdentifier testIdentifier, Throwable throwable) {
		failures.add(new Failure(testIdentifier, throwable));
	}

	static class Failure {

		private final TestIdentifier testIdentifier;
		private final Throwable exception;

		public Failure(TestIdentifier testIdentifier, Throwable exception) {
			this.testIdentifier = testIdentifier;
			this.exception = exception;
		}

		public TestIdentifier getTestIdentifier() {
			return testIdentifier;
		}

		public Throwable getException() {
			return exception;
		}
	}
}
