/*
 * Copyright 2015-2017 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.junit.jupiter.api.extension;

import static org.junit.platform.commons.meta.API.Usage.Experimental;

import java.util.Optional;

import org.junit.platform.commons.meta.API;
import org.junit.platform.commons.util.ToStringBuilder;

/**
 * The result of evaluating a {@link ContainerExecutionCondition} or
 * {@linkplain TestExecutionCondition}.
 *
 * @since 5.0
 */
@API(Experimental)
public class ConditionEvaluationResult {

	/**
	 * Factory for creating <em>enabled</em> results.
	 *
	 * @param reason the reason why the container or test should be enabled
	 * @return an enabled {@code ConditionEvaluationResult} with the given reason
	 */
	public static ConditionEvaluationResult enabled(String reason) {
		return new ConditionEvaluationResult(true, reason);
	}

	/**
	 * Factory for creating <em>disabled</em> results.
	 *
	 * @param reason the reason why the container or test should be disabled
	 * @return a disabled {@code ConditionEvaluationResult} with the given reason
	 */
	public static ConditionEvaluationResult disabled(String reason) {
		return new ConditionEvaluationResult(false, reason);
	}

	private final boolean enabled;

	private final Optional<String> reason;

	private ConditionEvaluationResult(boolean enabled, String reason) {
		this.enabled = enabled;
		this.reason = Optional.ofNullable(reason);
	}

	/**
	 * Whether the container or test should be disabled.
	 *
	 * @return {@code true} if the container or test should be disabled
	 */
	public boolean isDisabled() {
		return !this.enabled;
	}

	/**
	 * Get the reason why the container or test should be enabled or disabled,
	 * if available.
	 */
	public Optional<String> getReason() {
		return this.reason;
	}

	@Override
	public String toString() {
		// @formatter:off
		return new ToStringBuilder(this)
				.append("enabled", this.enabled)
				.append("reason", this.reason.orElse("<unknown>"))
				.toString();
		// @formatter:on
	}

}
