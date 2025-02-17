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

import java.lang.reflect.Constructor;

import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.ParameterContext;
import org.junit.jupiter.api.extension.ParameterResolutionException;
import org.junit.jupiter.api.extension.ParameterResolver;

class ParameterizedContainerParameterResolver implements ParameterResolver {

	private final ParameterizedContainerClassContext declarationContext;
	private final EvaluatedArgumentSet arguments;

	ParameterizedContainerParameterResolver(ParameterizedContainerClassContext declarationContext,
			EvaluatedArgumentSet arguments) {
		this.declarationContext = declarationContext;
		this.arguments = arguments;
	}

	@Override
	public ExtensionContextScope getTestInstantiationExtensionContextScope(ExtensionContext rootContext) {
		return ExtensionContextScope.TEST_METHOD;
	}

	@Override
	public boolean supportsParameter(ParameterContext parameterContext, ExtensionContext extensionContext)
			throws ParameterResolutionException {
		if (parameterContext.getDeclaringExecutable() instanceof Constructor) {
			Class<?> declaringClass = ((Constructor<?>) parameterContext.getDeclaringExecutable()).getDeclaringClass();
			// TODO #878 see ParameterizedTestParameterResolver
			return declaringClass.equals(declarationContext.getAnnotatedElement());
		}
		return false;
	}

	@Override
	public Object resolveParameter(ParameterContext parameterContext, ExtensionContext extensionContext)
			throws ParameterResolutionException {
		// TODO #878 see ParameterizedTestParameterResolver
		return this.arguments.getConsumedPayloads()[parameterContext.getIndex()];
	}
}
