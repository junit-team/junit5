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
	 * Factory for creating <em>filtered</em> results.
	 *
	 * @param reason the reason why the result was filtered
	 * @return a filtered {@code FilterResult} with the given reason
	 */
	public static FilterResult filtered(String reason) {
		return new FilterResult(false, reason);
	}

	/**
	 * Factory for creating <em>active</em> results.
	 *
	 * @param reason the reason why the result was filtered
	 * @return an accepted {@code FilterResult} with the given reason
	 */
	public static FilterResult accepted(String reason) {
		return new FilterResult(true, reason);
	}

	/**
	 * Factory for creating filter results based on the condition given.
	 *
	 * @param isAccepted whether or not the returned {@code FilterResult} should be accepted
	 * @return a valid {@code FilterResult} for the given condition
	 */
	public static FilterResult acceptedIf(boolean isAccepted) {
		return acceptedIf(isAccepted, null);
	}

	/**
	 * Factory for creating filter results based on the condition given.
	 *
	 * @param isAccepted whether or not the returned {@code FilterResult} should be accepted
	 * @param reason     the reason why the result was filtered
	 * @return a valid {@code FilterResult} for the given condition
	 */
	public static FilterResult acceptedIf(boolean isAccepted, String reason) {
		return isAccepted ? accepted(reason) : filtered(reason);
	}

	private final boolean accepted;

	private final Optional<String> reason;

	private FilterResult(boolean isAccepted, String reason) {
		this.accepted = isAccepted;
		this.reason = Optional.ofNullable(reason);
	}

	public boolean isAccepted() {
		return accepted;
	}

	public boolean isFiltered() {
		return !isAccepted();
	}

	public Optional<String> getReason() {
		return reason;
	}

	@Override
	public String toString() {
		// @formatter:off
        return new ToStringBuilder(this)
                .append("accepted", this.accepted)
                .append("reason", this.reason)
                .toString();
        // @formatter:on
	}
}
