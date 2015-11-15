/*
 * Copyright 2015 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.junit.gen5.api;

import java.util.Optional;

import lombok.Data;

import org.junit.gen5.api.extension.TestExecutionContext;

/**
 * A {@code Condition} can be {@linkplain #evaluate evaluated} to determine
 * if a given test (e.g., class or method) should be executed based on the
 * supplied {@link TestExecutionContext}.
 *
 * @author Sam Brannen
 * @since 5.0
 * @see Conditional
 */
@FunctionalInterface
public interface Condition {

	/**
	 * Evaluate this condition for the supplied {@link TestExecutionContext}.
	 *
	 * <p>A {@linkplain Result#success successful} result implies that the
	 * test should be executed; whereas, a {@linkplain Result#failure failed}
	 * result implies that the condition failed and the test should not be
	 * executed.
	 */
	Result evaluate(TestExecutionContext context);

	/**
	 * The result of evaluating a condition.
	 */
	@Data(staticConstructor = "of")
	public static class Result {

		/**
		 * Factory for creating <em>success</em> results.
		 */
		public static Result success(String reason) {
			return of(true, Optional.ofNullable(reason));
		}

		/**
		 * Factory for creating <em>failure</em> results.
		 */
		public static Result failure(String reason) {
			return of(false, Optional.ofNullable(reason));
		}

		private final boolean success;
		private final Optional<String> reason;

	}

}
