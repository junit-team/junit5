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
import java.lang.reflect.Executable;

import org.junit.jupiter.api.extension.ExtensionContext;

/**
 * @since 5.13
 */
class ContainerTemplateConstructorParameterResolver extends ParameterizedInvocationParameterResolver {

	ContainerTemplateConstructorParameterResolver(ParameterizedDeclarationContext<?> declarationContext,
			EvaluatedArgumentSet arguments, int invocationIndex) {
		super(declarationContext, arguments, invocationIndex);
	}

	@Override
	protected boolean isSupportedOnConstructorOrMethod(Executable declaringExecutable,
			ExtensionContext extensionContext) {
		if (declaringExecutable instanceof Constructor) {
			Class<?> declaringClass = ((Constructor<?>) declaringExecutable).getDeclaringClass();
			// TODO #878 see ParameterizedTestParameterResolver
			return declaringClass.equals(this.declarationContext.getAnnotatedElement());
		}
		return false;
	}

}
