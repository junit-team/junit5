/*
 * Copyright 2015-2020 the original author or authors.
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

	public RepetitionInfoParameterResolver(int currentRepetition, int totalRepetitions) {
		this.currentRepetition = currentRepetition;
		this.totalRepetitions = totalRepetitions;
	}

	@Override
	public boolean supportsParameter(ParameterContext parameterContext, ExtensionContext extensionContext) {
		return (parameterContext.getParameter().getType() == RepetitionInfo.class);
	}

	@Override
	public RepetitionInfo resolveParameter(ParameterContext parameterContext, ExtensionContext extensionContext) {
		return new DefaultRepetitionInfo(this.currentRepetition, this.totalRepetitions);
	}

	private static class DefaultRepetitionInfo implements RepetitionInfo {

		private final int currentRepetition;
		private final int totalRepetitions;

		DefaultRepetitionInfo(int currentRepetition, int totalRepetitions) {
			this.currentRepetition = currentRepetition;
			this.totalRepetitions = totalRepetitions;
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
