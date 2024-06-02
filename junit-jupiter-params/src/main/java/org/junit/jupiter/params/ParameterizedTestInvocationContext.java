/*
 * Copyright 2015-2024 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * https://www.eclipse.org/legal/epl-v20.html
 */

package org.junit.jupiter.params;

import static java.util.Collections.singletonList;

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
	private final Arguments arguments;
	private final Object[] consumedArguments;
	private final int invocationIndex;

	ParameterizedTestInvocationContext(ParameterizedTestNameFormatter formatter,
			ParameterizedTestMethodContext methodContext, Arguments arguments, int invocationIndex) {

		this.formatter = formatter;
		this.methodContext = methodContext;
		this.arguments = arguments;
		this.consumedArguments = consumedArguments(methodContext, arguments.get());
		this.invocationIndex = invocationIndex;
	}

	@Override
	public String getDisplayName(int invocationIndex) {
		return this.formatter.format(invocationIndex, this.arguments, this.consumedArguments);
	}

	@Override
	public List<Extension> getAdditionalExtensions() {
		return singletonList(
			new ParameterizedTestParameterResolver(this.methodContext, this.consumedArguments, this.invocationIndex));
	}

	private static Object[] consumedArguments(ParameterizedTestMethodContext methodContext, Object[] arguments) {
		if (methodContext.hasAggregator()) {
			return arguments;
		}
		int parameterCount = methodContext.getParameterCount();
		return arguments.length > parameterCount ? Arrays.copyOf(arguments, parameterCount) : arguments;
	}

}
