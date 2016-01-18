/*
 * Copyright 2015-2016 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.junit.gen5.engine;

import java.util.Optional;

import org.junit.gen5.commons.util.ToStringBuilder;

/**
 * The result of evaluating a {@link GenericFilter}.
 *
 * @since 5.0
 */
public class FilterResult {
	/**
	 * Factory for creating <em>included</em> results.
	 *
	 * @param reason the reason why the result was included
	 * @return an included {@code FilterResult} with the given reason
	 */
	public static FilterResult included(String reason) {
		return new FilterResult(true, reason);
	}

	/**
	 * Factory for creating <em>excluded</em> results.
	 *
	 * @param reason the reason why the result was excluded
	 * @return a excluded {@code FilterResult} with the given reason
	 */
	public static FilterResult excluded(String reason) {
		return new FilterResult(false, reason);
	}

	/**
	 * Factory for creating filter results based on the condition given.
	 *
	 * @param included whether or not the returned {@code FilterResult} should be included
	 * @return a valid {@code FilterResult} for the given condition
	 */
	public static FilterResult includedIf(boolean included) {
		return includedIf(included, null);
	}

	/**
	 * Factory for creating filter results based on the condition given.
	 *
	 * @param included whether or not the returned {@code FilterResult} should be included
	 * @param reason the reason for the result
	 * @return a valid {@code FilterResult} for the given condition
	 */
	public static FilterResult includedIf(boolean included, String reason) {
		return included ? included(reason) : excluded(reason);
	}

	private final boolean included;

	private final Optional<String> reason;

	private FilterResult(boolean included, String reason) {
		this.included = included;
		this.reason = Optional.ofNullable(reason);
	}

	/**
	 * @return {@code true} if the filtered object should be included in the test plan
	 */
	public boolean included() {
		return included;
	}

	/**
	 * @return {@code true} if the filtered object should be excluded from the test plan
	 */
	public boolean excluded() {
		return !included();
	}

	public Optional<String> getReason() {
		return reason;
	}

	@Override
	public String toString() {
		// @formatter:off
        return new ToStringBuilder(this)
                .append("included", this.included)
                .append("reason", this.reason)
                .toString();
        // @formatter:on
	}
}
