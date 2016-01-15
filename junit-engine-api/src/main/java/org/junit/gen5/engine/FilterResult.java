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
	 */
	public static FilterResult filtered(String reason) {
		return new FilterResult(true, reason);
	}

	/**
	 * Factory for creating <em>active</em> results.
	 */
	public static FilterResult active(String reason) {
		return new FilterResult(false, reason);
	}

	public static FilterResult result(boolean result) {
		return result(result, "Condition evaluates to " + Boolean.toString(result));
	}

	public static FilterResult result(boolean result, String reason) {
		return result ? active(reason) : filtered(reason);
	}

	private final boolean filtered;

	private final Optional<String> reason;

	private FilterResult(boolean filtered, String reason) {
		this.filtered = filtered;
		this.reason = Optional.ofNullable(reason);
	}

	public boolean isFiltered() {
		return filtered;
	}

	public Optional<String> getReason() {
		return reason;
	}

	@Override
	public String toString() {
		// @formatter:off
        return new ToStringBuilder(this)
                .append("filtered", this.filtered)
                .append("reason", this.reason)
                .toString();
        // @formatter:on
	}
}
