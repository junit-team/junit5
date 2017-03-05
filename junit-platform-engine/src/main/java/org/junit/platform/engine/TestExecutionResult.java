/*
 * Copyright 2015-2017 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.junit.platform.engine;

import static org.junit.platform.commons.meta.API.Usage.Experimental;
import static org.junit.platform.engine.TestExecutionResult.Status.ABORTED;
import static org.junit.platform.engine.TestExecutionResult.Status.FAILED;
import static org.junit.platform.engine.TestExecutionResult.Status.SUCCESSFUL;

import java.util.Optional;

import org.junit.platform.commons.meta.API;
import org.junit.platform.commons.util.Preconditions;
import org.junit.platform.commons.util.ToStringBuilder;

/**
 * Result of executing a single test or container.
 *
 * <p>A {@code TestExecutionResult} consists of a mandatory {@link Status} and
 * an optional {@link Throwable}.
 *
 * @since 1.0
 */
@API(Experimental)
public class TestExecutionResult {

	private static final TestExecutionResult SUCCESSFUL_RESULT = new TestExecutionResult(SUCCESSFUL, null);

	/**
	 * Status of executing a single test or container.
	 */
	public enum Status {

		/**
		 * Indicates that the execution of a test or container was
		 * <em>successful</em>.
		 */
		SUCCESSFUL,

		/**
		 * Indicates that the execution of a test or container was
		 * <em>aborted</em> (started but not finished).
		 */
		ABORTED,

		/**
		 * Indicates that the execution of a test or container has
		 * <em>failed</em>.
		 */
		FAILED
	}

	private final Status status;
	private final Throwable throwable;

	/**
	 * Obtain a {@code TestExecutionResult} for a <em>successful</em> execution
	 * of a test or container.
	 */
	public static TestExecutionResult successful() {
		return SUCCESSFUL_RESULT;
	}

	/**
	 * Obtain a {@code TestExecutionResult} for an <em>aborted</em> execution
	 * of a test or container with the supplied {@link Throwable throwable}.
	 *
	 * @param throwable the throwable that caused the aborted execution; may be null
	 */
	public static TestExecutionResult aborted(Throwable throwable) {
		return new TestExecutionResult(ABORTED, throwable);
	}

	/**
	 * Obtain a {@code TestExecutionResult} for a <em>failed</em> execution
	 * of a test or container with the supplied {@link Throwable throwable}.
	 *
	 * @param throwable the throwable that caused the failed execution; may be null
	 */
	public static TestExecutionResult failed(Throwable throwable) {
		return new TestExecutionResult(FAILED, throwable);
	}

	private TestExecutionResult(Status status, Throwable throwable) {
		this.status = Preconditions.notNull(status, "Status must not be null");
		this.throwable = throwable;
	}

	/**
	 * Get the status of this result.
	 */
	public Status getStatus() {
		return status;
	}

	/**
	 * Get the throwable that caused this result, if available.
	 */
	public Optional<Throwable> getThrowable() {
		return Optional.ofNullable(throwable);
	}

	@Override
	public String toString() {
		// @formatter:off
		return new ToStringBuilder(this)
				.append("status", status)
				.append("throwable", throwable)
				.toString();
		// @formatter:on
	}

}
