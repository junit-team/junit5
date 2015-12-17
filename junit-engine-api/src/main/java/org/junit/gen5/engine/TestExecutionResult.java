/*
 * Copyright 2015 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.junit.gen5.engine;

import static org.junit.gen5.engine.TestExecutionResult.Status.*;

import java.util.Optional;

import org.junit.gen5.commons.util.Preconditions;

/**
 * @since 5.0
 */
public class TestExecutionResult {

	private static final TestExecutionResult SUCCESSFUL_RESULT = new TestExecutionResult(SUCCESSFUL, null);

	public enum Status {
		SUCCESSFUL, ABORTED, FAILED
	}

	private final Status status;
	private final Throwable throwable;

	public static TestExecutionResult successful() {
		return SUCCESSFUL_RESULT;
	}

	public static TestExecutionResult aborted(Throwable throwable) {
		return new TestExecutionResult(ABORTED, throwable);
	}

	public static TestExecutionResult failed(Throwable throwable) {
		return new TestExecutionResult(FAILED, throwable);
	}

	public TestExecutionResult(Status status, Throwable throwable) {
		Preconditions.notNull(status, "status must not be null");
		this.status = status;
		this.throwable = throwable;
	}

	public Status getStatus() {
		return status;
	}

	public Optional<Throwable> getThrowable() {
		return Optional.ofNullable(throwable);
	}

	@Override
	public String toString() {
		StringBuilder result = new StringBuilder();
		result.append(status);
		if (throwable != null) {
			result.append(": ").append(throwable.getMessage()).append(" [").append(
				throwable.getClass().getName()).append(']');
		}
		return result.toString();
	}

}
