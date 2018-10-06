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

import java.time.Duration;
import java.time.Instant;

import org.apiguardian.api.API;
import org.junit.platform.engine.TestDescriptor;
import org.junit.platform.engine.TestExecutionResult;

/**
 * Encapsulates metadata around the execution of a single Test,
 * this includes metadata of whether or not the Test was skipped.
 *
 * @since 1.4.0
 */
@API(status = API.Status.EXPERIMENTAL, since = "1.4.0")
public class Execution {

	private final TestDescriptor descriptor;
	private final Instant startInstant;
	private final Instant endInstant;
	private final TerminationInfo termination;
	private final Duration duration;

	private Execution(TestDescriptor descriptor, Instant startInstant, Instant endInstant,
			TerminationInfo termination) {
		this.descriptor = descriptor;
		this.startInstant = startInstant;
		this.endInstant = endInstant;
		this.termination = termination;
		this.duration = Duration.between(startInstant, endInstant);
	}

	/**
	 * Constructs a new instance of an {@link Execution} that finished with the provided {@link TestExecutionResult}.
	 *
	 * @param descriptor the {@link TestDescriptor} that finished
	 * @param startInstant the {@link Instant} that the {@link Execution} started
	 * @param endInstant the {@link Instant} that the {@link Execution} completed
	 * @param executionResult the {@link TestExecutionResult} of the finished test
	 * @return the newly constructed {@link Execution} instance
	 */
	public static Execution finished(TestDescriptor descriptor, Instant startInstant, Instant endInstant,
			TestExecutionResult executionResult) {
		return new Execution(descriptor, startInstant, endInstant, TerminationInfo.executed(executionResult));
	}

	/**
	 * Constructs a new instance of an {@link Execution} that was skipped with the provided
	 * {@link String} {@code skipReason}.
	 *
	 * @param descriptor the {@link TestDescriptor} that finished
	 * @param startInstant the {@link Instant} that the {@link Execution} started
	 * @param endInstant the {@link Instant} that the {@link Execution} completed
	 * @param skipReason the {@link String} reason for the test being skipped
	 * @return the newly constructed {@link Execution} instance
	 */
	public static Execution skipped(TestDescriptor descriptor, Instant startInstant, Instant endInstant,
			String skipReason) {
		return new Execution(descriptor, startInstant, endInstant, TerminationInfo.skipped(skipReason));
	}

	/**
	 * Gets the {@link TestDescriptor} for this {@link Execution}.
	 *
	 * @return the {@link TestDescriptor} for this {@link Execution}
	 */
	public TestDescriptor getTestDescriptor() {
		return descriptor;
	}

	/**
	 * Gets the start {@link Instant} of this {@link Execution}.
	 *
	 * @return the start {@link Instant} of this {@link Execution}.
	 */
	public Instant getStartInstant() {
		return startInstant;
	}

	/**
	 * Gets the end {@link Instant} of this {@link Execution}.
	 *
	 * @return the end {@link Instant} of this {@link Execution}.
	 */
	public Instant getEndInstant() {
		return endInstant;
	}

	/**
	 * Gets the {@link Duration} of this {@link Execution}.
	 *
	 * @return the {@link Duration} of this {@link Execution}.
	 */
	public Duration getDuration() {
		return duration;
	}

	/**
	 * Gets the {@link TerminationInfo} for this {@link Execution}.
	 *
	 * @return the {@link TerminationInfo} for this {@link Execution}.
	 */
	public TerminationInfo getTerminationInfo() {
		return termination;
	}

}
