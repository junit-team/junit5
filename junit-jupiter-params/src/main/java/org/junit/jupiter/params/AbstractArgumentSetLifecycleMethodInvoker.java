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

import org.junit.jupiter.api.extension.ExecutableInvoker;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.ParameterContext;
import org.junit.jupiter.api.extension.ParameterResolutionException;
import org.junit.jupiter.api.extension.ParameterResolver;

/**
 * @since 5.13
 */
abstract class AbstractArgumentSetLifecycleMethodInvoker implements ParameterResolver {

	private final ParameterizedClassContext declarationContext;
	private final EvaluatedArgumentSet arguments;
	private final int invocationIndex;
	private final ResolutionCache resolutionCache;
	private final ArgumentSetLifecycleMethod lifecycleMethod;

	AbstractArgumentSetLifecycleMethodInvoker(ParameterizedClassContext declarationContext,
			EvaluatedArgumentSet arguments, int invocationIndex, ResolutionCache resolutionCache,
			ArgumentSetLifecycleMethod lifecycleMethod) {
		this.declarationContext = declarationContext;
		this.arguments = arguments;
		this.invocationIndex = invocationIndex;
		this.resolutionCache = resolutionCache;
		this.lifecycleMethod = lifecycleMethod;
	}

	@Override
	public boolean supportsParameter(ParameterContext parameterContext, ExtensionContext extensionContext)
			throws ParameterResolutionException {
		// TODO Check for parameter index
		return this.lifecycleMethod.injectArguments //
				&& parameterContext.getDeclaringExecutable().equals(this.lifecycleMethod.method);
	}

	@Override
	public Object resolveParameter(ParameterContext parameterContext, ExtensionContext extensionContext)
			throws ParameterResolutionException {
		return this.declarationContext.getResolverFacade() //
				.resolve(parameterContext, extensionContext, this.arguments, this.invocationIndex,
					this.resolutionCache);
	}

	protected void invoke(ExtensionContext context) {
		if (isCorrectTestClass(context)) {
			ExecutableInvoker executableInvoker = context.getExecutableInvoker();
			Object testInstance = context.getTestInstance().orElse(null);
			executableInvoker.invoke(this.lifecycleMethod.method, testInstance);
		}
	}

	private boolean isCorrectTestClass(ExtensionContext context) {
		return this.declarationContext.getAnnotatedElement().equals(context.getTestClass().orElse(null));
	}

}
