/*
 * Copyright 2015-2025 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * https://www.eclipse.org/legal/epl-v20.html
 */

package org.junit.jupiter.params;

import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.extension.Extension;
import org.junit.jupiter.api.extension.TestTemplateInvocationContext;
import org.junit.jupiter.params.provider.Arguments;

/**
 * @since 5.0
 */
class ParameterizedTestInvocationContext implements TestTemplateInvocationContext {

	private final ParameterizedTestNameFormatter formatter;
	private final ParameterizedTestMethodContext methodContext;
	private final EvaluatedArgumentSet arguments;
	private final int invocationIndex;

	ParameterizedTestInvocationContext(ParameterizedTestNameFormatter formatter,
			ParameterizedTestMethodContext methodContext, Arguments arguments, int invocationIndex) {

		this.formatter = formatter;
		this.methodContext = methodContext;
		this.arguments = EvaluatedArgumentSet.of(arguments, this::determineConsumedArgumentCount);
		this.invocationIndex = invocationIndex;
	}

	@Override
	public String getDisplayName(int invocationIndex) {
		return this.formatter.format(invocationIndex, this.arguments);
	}

	@Override
	public List<Extension> getAdditionalExtensions() {
		return Arrays.asList(
			new ParameterizedTestParameterResolver(this.methodContext, this.arguments, this.invocationIndex),
			new ArgumentCountValidator(this.methodContext, this.arguments));
	}

	private int determineConsumedArgumentCount(int totalLength) {
		return methodContext.hasAggregator() //
				? totalLength //
				: Math.min(totalLength, methodContext.getParameterCount());
	}

}
