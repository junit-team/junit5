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

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

import org.junit.gen5.launcher.TestIdentifier;
import org.junit.gen5.launcher.TestPlan;

/**
 * Mutable, internal implementation of the {@link TestExecutionSummary} API.
 *
 * @since 5.0
 */
class MutableTestExecutionSummary implements TestExecutionSummary {

	final AtomicLong testsStarted = new AtomicLong();
	final AtomicLong testsFound = new AtomicLong();
	final AtomicLong testsSkipped = new AtomicLong();
	final AtomicLong testsAborted = new AtomicLong();
	final AtomicLong testsSucceeded = new AtomicLong();
	final AtomicLong testsFailed = new AtomicLong();

	private final TestPlan testPlan;
	private final List<Failure> failures = new ArrayList<>();
	private final long timeStarted;
	long timeFinished;

	MutableTestExecutionSummary(TestPlan testPlan) {
		this.testPlan = testPlan;
		this.testsFound.set(testPlan.countTestIdentifiers(TestIdentifier::isTest));
		this.timeStarted = System.currentTimeMillis();
	}

	void addFailure(TestIdentifier testIdentifier, Throwable throwable) {
		this.failures.add(new Failure(testIdentifier, throwable));
	}

	/**
	 * Get the number of tests started.
	 */
	@Override
	public long getTestsStartedCount() {
		return this.testsStarted.get();
	}

	/**
	 * Get the number of tests started.
	 */
	@Override
	public long getTestsFoundCount() {
		return this.testsFound.get();
	}

	/**
	 * Get the number of tests started.
	 */
	@Override
	public long getTestsSkippedCount() {
		return this.testsSkipped.get();
	}

	/**
	 * Get the number of tests started.
	 */
	@Override
	public long getTestsAbortedCount() {
		return this.testsAborted.get();
	}

	/**
	 * Get the number of tests started.
	 */
	@Override
	public long getTestsSucceededCount() {
		return this.testsSucceeded.get();
	}

	/**
	 * Get the number of tests started.
	 */
	@Override
	public long getTestsFailedCount() {
		return this.testsFailed.get();
	}

	/**
	 * Get the number of tests started.
	 */
	@Override
	public long getTimeStarted() {
		return this.timeStarted;
	}

	/**
	 * Get the number of tests started.
	 */
	@Override
	public long getTimeFinished() {
		return this.timeFinished;
	}

	/**
	 * Print the summary to the supplied {@link PrintWriter}.
	 *
	 * <p>This method does not print failure messages.
	 *
	 * @see #printFailuresTo(PrintWriter)
	 */
	@Override
	public void printTo(PrintWriter writer) {
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
	 * Print failed tests including sources and exception messages to the
	 * supplied {@link PrintWriter}.
	 *
	 * @see #printTo(PrintWriter)
	 */
	@Override
	public void printFailuresTo(PrintWriter writer) {
		if (getTestsFailedCount() > 0) {
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

	private String describeTest(TestIdentifier testIdentifier) {
		List<String> descriptionParts = new ArrayList<>();
		collectTestDescription(Optional.of(testIdentifier), descriptionParts);
		return descriptionParts.stream().collect(Collectors.joining(":"));
	}

	private void collectTestDescription(Optional<TestIdentifier> optionalIdentifier, List<String> descriptionParts) {
		optionalIdentifier.ifPresent(testIdentifier -> {
			descriptionParts.add(0, testIdentifier.getDisplayName());
			collectTestDescription(this.testPlan.getParent(testIdentifier), descriptionParts);
		});
	}

	private static class Failure {

		private final TestIdentifier testIdentifier;
		private final Throwable exception;

		Failure(TestIdentifier testIdentifier, Throwable exception) {
			this.testIdentifier = testIdentifier;
			this.exception = exception;
		}

		TestIdentifier getTestIdentifier() {
			return testIdentifier;
		}

		Throwable getException() {
			return exception;
		}
	}

}
