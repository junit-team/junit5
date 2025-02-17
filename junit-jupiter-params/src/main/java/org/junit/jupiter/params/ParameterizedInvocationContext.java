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

import org.junit.jupiter.params.provider.Arguments;

class ParameterizedInvocationContext<T extends ParameterizedDeclarationContext<?>> {

	private final ParameterizedInvocationNameFormatter formatter;
	protected final T declarationContext;
	protected final EvaluatedArgumentSet arguments;
	protected final int invocationIndex;

	ParameterizedInvocationContext(ParameterizedInvocationNameFormatter formatter, T declarationContext,
			Arguments arguments, int invocationIndex) {

		this.formatter = formatter;
		this.declarationContext = declarationContext;
		this.arguments = EvaluatedArgumentSet.of(arguments, this::determineConsumedArgumentCount);
		this.invocationIndex = invocationIndex;
	}

	public String getDisplayName(int invocationIndex) {
		return this.formatter.format(invocationIndex, this.arguments);
	}

	ArgumentCountValidator createArgumentCountValidator() {
		return new ArgumentCountValidator(this.declarationContext, this.arguments);
	}

	private int determineConsumedArgumentCount(int totalLength) {
		return this.declarationContext.hasAggregator() //
				? totalLength //
				: Math.min(totalLength, this.declarationContext.getParameterCount());
	}
}
