/*
 * Copyright 2015-2018 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v20.html
 */

package org.junit.platform.testkit;

import static org.apiguardian.api.API.Status.EXPERIMENTAL;

import java.time.Duration;
import java.time.Instant;

import org.apiguardian.api.API;
import org.junit.platform.engine.TestDescriptor;
import org.junit.platform.engine.TestExecutionResult;

/**
 * {@code Execution} encapsulates metadata for the execution of a single
 * {@link TestDescriptor}.
 *
 * @since 1.4
 */
@API(status = EXPERIMENTAL, since = "1.4")
public class Execution {

	private final TestDescriptor descriptor;
	private final Instant startInstant;
	private final Instant endInstant;
	private final Duration duration;
	private final TerminationInfo termination;

	private Execution(TestDescriptor descriptor, Instant startInstant, Instant endInstant,
			TerminationInfo termination) {

		this.descriptor = descriptor;
		this.startInstant = startInstant;
		this.endInstant = endInstant;
		this.duration = Duration.between(startInstant, endInstant);
		this.termination = termination;
	}

	/**
	 * Construct a new instance of an {@code Execution} that finished with the
	 * provided {@link TestExecutionResult}.
	 *
	 * @param descriptor the {@code TestDescriptor} that finished
	 * @param startInstant the {@code Instant} that the {@code Execution} started
	 * @param endInstant the {@code Instant} that the {@code Execution} completed
	 * @param executionResult the {@code TestExecutionResult} of the finished test
	 * @return the newly constructed {@code Execution} instance
	 */
	public static Execution finished(TestDescriptor descriptor, Instant startInstant, Instant endInstant,
			TestExecutionResult executionResult) {

		return new Execution(descriptor, startInstant, endInstant, TerminationInfo.executed(executionResult));
	}

	/**
	 * Construct a new instance of an {@code Execution} that was skipped with the provided
	 * {@code skipReason}.
	 *
	 * @param descriptor the {@code TestDescriptor} that finished
	 * @param startInstant the {@code Instant} that the {@code Execution} started
	 * @param endInstant the {@code Instant} that the {@code Execution} completed
	 * @param skipReason the {@code String} reason for the test being skipped
	 * @return the newly constructed {@code Execution} instance
	 */
	public static Execution skipped(TestDescriptor descriptor, Instant startInstant, Instant endInstant,
			String skipReason) {

		return new Execution(descriptor, startInstant, endInstant, TerminationInfo.skipped(skipReason));
	}

	/**
	 * Get the {@link TestDescriptor} for this {@code Execution}.
	 *
	 * @return the {@code TestDescriptor} for this {@code Execution}
	 */
	public TestDescriptor getTestDescriptor() {
		return this.descriptor;
	}

	/**
	 * Get the start {@link Instant} of this {@code Execution}.
	 *
	 * @return the start {@code Instant} of this {@code Execution}
	 */
	public Instant getStartInstant() {
		return this.startInstant;
	}

	/**
	 * Get the end {@link Instant} of this {@code Execution}.
	 *
	 * @return the end {@code Instant} of this {@code Execution}
	 */
	public Instant getEndInstant() {
		return this.endInstant;
	}

	/**
	 * Get the {@link Duration} of this {@code Execution}.
	 *
	 * @return the {@code Duration} of this {@code Execution}
	 */
	public Duration getDuration() {
		return this.duration;
	}

	/**
	 * Get the {@link TerminationInfo} for this {@code Execution}.
	 *
	 * @return the {@code TerminationInfo} for this {@code Execution}
	 */
	public TerminationInfo getTerminationInfo() {
		return this.termination;
	}

}
