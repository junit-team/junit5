/*
 * Copyright 2015-2016 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.junit.platform.launcher.listeners;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

import org.junit.platform.launcher.TestIdentifier;
import org.junit.platform.launcher.TestPlan;

/**
 * Mutable, internal implementation of the {@link TestExecutionSummary} API.
 *
 * @since 1.0
 */
class MutableTestExecutionSummary implements TestExecutionSummary {

	private static final String TAB = "  ";
	private static final String DOUBLE_TAB = TAB + TAB;

	final AtomicLong testsFound = new AtomicLong();
	final AtomicLong testsStarted = new AtomicLong();
	final AtomicLong testsSkipped = new AtomicLong();
	final AtomicLong testsAborted = new AtomicLong();
	final AtomicLong testsSucceeded = new AtomicLong();
	final AtomicLong testsFailed = new AtomicLong();
	final AtomicLong containersFailed = new AtomicLong();

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

	@Override
	public long getTestsFoundCount() {
		return this.testsFound.get();
	}

	@Override
	public long getTestsStartedCount() {
		return this.testsStarted.get();
	}

	@Override
	public long getTestsSkippedCount() {
		return this.testsSkipped.get();
	}

	@Override
	public long getTestsAbortedCount() {
		return this.testsAborted.get();
	}

	@Override
	public long getTestsSucceededCount() {
		return this.testsSucceeded.get();
	}

	@Override
	public long getTestsFailedCount() {
		return this.testsFailed.get();
	}

	@Override
	public long getContainersFailedCount() {
		return this.containersFailed.get();
	}

	@Override
	public long getTotalFailureCount() {
		return getTestsFailedCount() + getContainersFailedCount();
	}

	@Override
	public long getTimeStarted() {
		return this.timeStarted;
	}

	@Override
	public long getTimeFinished() {
		return this.timeFinished;
	}

	@Override
	public void printTo(PrintWriter writer) {
		// @formatter:off
		writer.println(String.format(
			"%nTest run finished after %d ms%n"
			+ "[%10d tests found      ]%n"
			+ "[%10d tests skipped    ]%n"
			+ "[%10d tests started    ]%n"
			+ "[%10d tests aborted    ]%n"
			+ "[%10d tests successful ]%n"
			+ "[%10d tests failed     ]%n"
			+ "[%10d containers failed]%n",
			(this.timeFinished - this.timeStarted),
			this.testsFound.get(),
			this.testsSkipped.get(),
			this.testsStarted.get(),
			this.testsAborted.get(),
			this.testsSucceeded.get(),
			this.testsFailed.get(),
			this.containersFailed.get()
		));
		// @formatter:on

		writer.flush();
	}

	@Override
	public void printFailuresTo(PrintWriter writer) {
		if (getTotalFailureCount() > 0) {
			writer.println();
			writer.println(String.format("Failures (%d):", getTotalFailureCount()));
			this.failures.forEach(failure -> {
				writer.println(TAB + describeTest(failure.getTestIdentifier()));
				failure.getTestIdentifier().getSource().ifPresent(source -> writer.println(DOUBLE_TAB + source));
				writer.println(String.format("%s=> %s", DOUBLE_TAB, failure.getException()));
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
