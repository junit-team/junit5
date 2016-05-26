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
 * Summary of test execution.
 *
 * @since 5.0
 * @see SummaryGeneratingListener
 */
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
	private final List<Failure> failures = new ArrayList<>();

	TestExecutionSummary(TestPlan testPlan) {
		this.testPlan = testPlan;
	}

	/**
	 * Prints the summary to the supplied {@link PrintWriter}.
	 *
	 * This method does not print failure messages.
	 *
	 * @see #printFailuresOn
	 */
	public void printOn(PrintWriter writer) {

		// @formatter:off
		writer.println(String.format(
			"%nTest run finished after %d ms\n"
			+ "[%10d tests found     ]\n"
			+ "[%10d tests skipped   ]\n"
			+ "[%10d tests started   ]\n"
			+ "[%10d tests aborted   ]\n"
			+ "[%10d tests successful]\n"
			+ "[%10d tests failed    ]\n",
			(this.timeFinished - this.timeStarted), this.testsFound.get(), this.testsSkipped.get(),
			this.testsStarted.get(), this.testsAborted.get(), this.testsSucceeded.get(), this.testsFailed.get()));
		// @formatter:on

		writer.flush();
	}

	/**
	 * Prints failed tests including source and exception message to the
	 * supplied {@link PrintWriter}.
	 */
	public void printFailuresOn(PrintWriter writer) {
		if (countFailedTests() > 0) {
			writer.println();
			writer.println(String.format("Test failures (%d):", testsFailed.get()));
			failures.forEach(failure -> {
				writer.println("  " + describeTest(failure.getTestIdentifier()));
				failure.getTestIdentifier().getSource().ifPresent(source -> writer.println("    " + source));
				writer.println("    => Exception: " + failure.getException().getLocalizedMessage());
			});
			writer.flush();
		}
	}

	/**
	 * Returns the number of failed tests.
	 */
	public long countFailedTests() {
		return testsFailed.get();
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

	void addFailure(TestIdentifier testIdentifier, Throwable throwable) {
		failures.add(new Failure(testIdentifier, throwable));
	}

	private static class Failure {

		private final TestIdentifier testIdentifier;
		private final Throwable exception;

		Failure(TestIdentifier testIdentifier, Throwable exception) {
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
