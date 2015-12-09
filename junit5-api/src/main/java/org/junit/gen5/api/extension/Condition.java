/*
 * Copyright 2015 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.junit.gen5.api.extension;

import java.util.Optional;

/**
 * {@code Condition} defines the {@link TestExtension} API for programmatic,
 * <em>conditional test execution</em>.
 *
 * <p>A {@code Condition} is {@linkplain #evaluate evaluated} to determine
 * if a given test (e.g., class or method) should be executed based on the
 * supplied {@link ExtensionContext}. When evaluated at the class level,
 * a condition applies to all test methods within that class.
 *
 * <p>Implementations must provide a no-args constructor.
 *
 * @since 5.0
 * @see org.junit.gen5.api.Disabled
 */
@FunctionalInterface
public interface Condition extends TestExtension {

	/**
	 * Evaluate this condition for the supplied {@link ExtensionContext}.
	 *
	 * <p>An {@linkplain Result#enabled enabled} result indicates that the
	 * test should be executed; whereas, a {@linkplain Result#disabled disabled}
	 * result indicates that the test should not be executed.
	 *
	 * @param context the current {@code ExtensionContext}
	 */
	Result evaluate(ExtensionContext context);

	/**
	 * The result of evaluating a {@code Condition}.
	 */
	public class Result {

		/**
		 * Factory for creating <em>enabled</em> results.
		 */
		public static Result enabled(String reason) {
			return new Result(true, reason);
		}

		/**
		 * Factory for creating <em>disabled</em> results.
		 */
		public static Result disabled(String reason) {
			return new Result(false, reason);
		}

		private final boolean enabled;

		private final Optional<String> reason;

		private Result(boolean enabled, String reason) {
			this.enabled = enabled;
			this.reason = Optional.ofNullable(reason);
		}

		public boolean isDisabled() {
			return !enabled;
		}

		public Optional<String> getReason() {
			return reason;
		}

		@Override
		public String toString() {
			// @formatter:off
			return new StringBuilder(getClass().getSimpleName()).append(" ")
				.append("[")
				.append("enabled = ").append(this.enabled).append(", ")
				.append("reason = ").append(this.reason)
				.append("]")
				.toString();
			// @formatter:on
		}

	}

}
