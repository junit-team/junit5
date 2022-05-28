/*
 * Copyright 2015-2022 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * https://www.eclipse.org/legal/epl-v20.html
 */

package org.junit.jupiter.engine.extension;

import org.junit.jupiter.api.RepetitionInfo;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.ParameterContext;
import org.junit.jupiter.api.extension.ParameterResolver;
import org.junit.platform.commons.util.ToStringBuilder;

/**
 * {@link ParameterResolver} that resolves the {@link RepetitionInfo} for
 * the currently executing {@code @RepeatedTest}.
 *
 * @since 5.0
 */
class RepetitionInfoParameterResolver implements ParameterResolver {

	private final int currentRepetition;
	private final int totalRepetitions;
	// CS304 Issue link: https://github.com/junit-team/junit5/issues/2925
	private final int stopAfterFailure;

	// CS304 Issue link: https://github.com/junit-team/junit5/issues/2925
	public RepetitionInfoParameterResolver(int currentRepetition, int totalRepetitions, int stopAfterFailure) {
		this.currentRepetition = currentRepetition;
		this.totalRepetitions = totalRepetitions;
		this.stopAfterFailure = stopAfterFailure;
	}

	public RepetitionInfoParameterResolver(int currentRepetition, int totalRepetitions) {
		this.currentRepetition = currentRepetition;
		this.totalRepetitions = totalRepetitions;
		// CS304 Issue link: https://github.com/junit-team/junit5/issues/2925
		this.stopAfterFailure = 0;
	}

	@Override
	public boolean supportsParameter(ParameterContext parameterContext, ExtensionContext extensionContext) {
		return (parameterContext.getParameter().getType() == RepetitionInfo.class);
	}

	@Override
	public RepetitionInfo resolveParameter(ParameterContext parameterContext, ExtensionContext extensionContext) {
		// CS304 Issue link: https://github.com/junit-team/junit5/issues/2925
		return new DefaultRepetitionInfo(this.currentRepetition, this.totalRepetitions, this.stopAfterFailure);
	}

	// CS304 Issue link: https://github.com/junit-team/junit5/issues/2925
	public int getStopAfterFailure() {
		return this.stopAfterFailure;
	}

	private static class DefaultRepetitionInfo implements RepetitionInfo {

		private final int currentRepetition;
		private final int totalRepetitions;
		// CS304 Issue link: https://github.com/junit-team/junit5/issues/2925
		private final int stopAfterFailure;

		DefaultRepetitionInfo(int currentRepetition, int totalRepetitions) {
			this.currentRepetition = currentRepetition;
			this.totalRepetitions = totalRepetitions;
			// CS304 Issue link: https://github.com/junit-team/junit5/issues/2925
			this.stopAfterFailure = 0;
		}

		// CS304 Issue link: https://github.com/junit-team/junit5/issues/2925
		DefaultRepetitionInfo(int currentRepetition, int totalRepetitions, int stopAfterFailure) {
			this.currentRepetition = currentRepetition;
			this.totalRepetitions = totalRepetitions;
			this.stopAfterFailure = stopAfterFailure;
		}

		@Override
		public int getCurrentRepetition() {
			return this.currentRepetition;
		}

		@Override
		public int getTotalRepetitions() {
			return this.totalRepetitions;
		}

		// CS304 Issue link: https://github.com/junit-team/junit5/issues/2925
		@Override
		public int getStopAfterFailure() {
			return this.stopAfterFailure;
		}

		@Override
		public String toString() {
			// @formatter:off
			return new ToStringBuilder(this)
					.append("currentRepetition", this.currentRepetition)
					.append("totalRepetitions", this.totalRepetitions)
					.toString();
			// @formatter:on
		}

	}
}
