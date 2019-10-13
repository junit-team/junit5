/*
 * Copyright 2015-2019 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * https://www.eclipse.org/legal/epl-v20.html
 */

package org.junit.platform.engine;

import java.util.Optional;

import org.apiguardian.api.API;
import org.junit.platform.commons.util.ToStringBuilder;

@API(status = API.Status.EXPERIMENTAL, since = "1.6")
public class SelectorResolutionResult {

	public enum Status {
		RESOLVED, UNRESOLVED, FAILED
	}

	private static final SelectorResolutionResult RESOLVED_RESULT = new SelectorResolutionResult(Status.RESOLVED, null);
	private static final SelectorResolutionResult UNRESOLVED_RESULT = new SelectorResolutionResult(Status.UNRESOLVED,
		null);

	public static SelectorResolutionResult resolved() {
		return RESOLVED_RESULT;
	}

	public static SelectorResolutionResult unresolved() {
		return UNRESOLVED_RESULT;
	}

	public static SelectorResolutionResult failed(Throwable throwable) {
		return new SelectorResolutionResult(Status.FAILED, throwable);
	}

	private final Status status;
	private final Throwable throwable;

	private SelectorResolutionResult(Status status, Throwable throwable) {
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
		// @formatter:off
        return new ToStringBuilder(this)
                .append("status", status)
                .append("throwable", throwable)
                .toString();
        // @formatter:on
	}

}
