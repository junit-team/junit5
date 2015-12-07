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
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

import org.junit.gen5.engine.EngineDescriptor;
import org.junit.gen5.engine.TestDescriptor;
import org.junit.gen5.launcher.RootTestDescriptor;

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

		writer.flush();
	}

	public void printFailuresOn(PrintWriter writer) {

		if (countFailedTests() > 0) {
			writer.println();
			writer.println(String.format("Test failures (%d):", testsFailed.get()));
			failures.stream().forEach(failure -> {
				// TODO Add source description to text
				writer.println(String.format("  %s", describeTest(failure.getDescriptor())));
				failure.getDescriptor().getSource().ifPresent(scource -> {
					writer.println(String.format("    %s", scource.toString()));
				});
				writer.println(String.format("    => Exception: %s", failure.getException().getLocalizedMessage()));
			});
		}

		writer.flush();
	}

	private String describeTest(TestDescriptor descriptor) {
		List<String> descriptionParts = new ArrayList<>();
		collectTestDescription(Optional.of(descriptor), descriptionParts);
		return descriptionParts.stream().collect(Collectors.joining(":"));
	}

	private void collectTestDescription(Optional<? extends TestDescriptor> optionalDescriptor,
			List<String> descriptionParts) {
		optionalDescriptor.ifPresent(descriptor -> {
			if (descriptor instanceof RootTestDescriptor) {
			}
			else if (descriptor instanceof EngineDescriptor) {
				descriptionParts.add(0, descriptor.getUniqueId());
			}
			else {
				descriptionParts.add(0, descriptor.getDisplayName());
			}
			collectTestDescription(descriptor.getParent(), descriptionParts);
		});
	}

	public long countFailedTests() {
		return testsFailed.get();
	}

	public void addFailure(TestDescriptor testDescriptor, Throwable throwable) {
		failures.add(new Failure(testDescriptor, throwable));
	}

	static class Failure {

		private final TestDescriptor descriptor;
		private final Throwable exception;

		public Failure(TestDescriptor descriptor, Throwable exception) {
			this.descriptor = descriptor;
			this.exception = exception;
		}

		public TestDescriptor getDescriptor() {
			return descriptor;
		}

		public Throwable getException() {
			return exception;
		}

	}
}
