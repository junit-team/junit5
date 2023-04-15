/*
 * Copyright 2015-2023 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * https://www.eclipse.org/legal/epl-v20.html
 */

package org.junit.jupiter.engine.extension;

import java.util.concurrent.atomic.AtomicInteger;

import org.junit.jupiter.api.RepetitionInfo;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.ParameterContext;
import org.junit.jupiter.api.extension.ParameterResolver;
import org.junit.jupiter.api.extension.TestWatcher;
import org.junit.platform.commons.util.ToStringBuilder;

/**
 * {@link ParameterResolver} that resolves the {@link RepetitionInfo} for
 * the currently executing {@code @RepeatedTest}.
 *
 * @since 5.0
 */
class RepetitionInfoParameterResolver implements ParameterResolver, TestWatcher {

	private final int currentRepetition;
	private final int totalRepetitions;
	private final int failureThreshold;
	private final AtomicInteger failureCount;

	public RepetitionInfoParameterResolver(int currentRepetition, int totalRepetitions, int failureThreshold,
			AtomicInteger failureCount) {
		this.currentRepetition = currentRepetition;
		this.totalRepetitions = totalRepetitions;
		this.failureThreshold = failureThreshold;
		this.failureCount = failureCount;
	}

	@Override
	public boolean supportsParameter(ParameterContext parameterContext, ExtensionContext extensionContext) {
		return (parameterContext.getParameter().getType() == RepetitionInfo.class);
	}

	@Override
	public RepetitionInfo resolveParameter(ParameterContext parameterContext, ExtensionContext extensionContext) {
		return new DefaultRepetitionInfo(this.currentRepetition, this.totalRepetitions, this.failureThreshold,
			this.failureCount.get());
	}

	@Override
	public void testFailed(ExtensionContext context, Throwable cause) {
		this.failureCount.incrementAndGet();
	}

	private static class DefaultRepetitionInfo implements RepetitionInfo {

		private final int currentRepetition;
		private final int totalRepetitions;
		private final int failureThreshold;
		private final int failureCount;

		public DefaultRepetitionInfo(int currentRepetition, int totalRepetitions, int failureThreshold,
				int failureCount) {
			this.currentRepetition = currentRepetition;
			this.totalRepetitions = totalRepetitions;
			this.failureThreshold = failureThreshold;
			this.failureCount = failureCount;
		}

		@Override
		public int getCurrentRepetition() {
			return this.currentRepetition;
		}

		@Override
		public int getTotalRepetitions() {
			return this.totalRepetitions;
		}

		@Override
		public int getFailureThreshold() {
			return this.failureThreshold;
		}

		@Override
		public int getFailureCount() {
			return this.failureCount;
		}

		@Override
		public String toString() {
			// @formatter:off
			return new ToStringBuilder(this)
					.append("currentRepetition", this.currentRepetition)
					.append("totalRepetitions", this.totalRepetitions)
					.append("failureThreshold", this.failureThreshold)
					.append("failureCount", this.failureCount)
					.toString();
			// @formatter:on
		}

	}

}
