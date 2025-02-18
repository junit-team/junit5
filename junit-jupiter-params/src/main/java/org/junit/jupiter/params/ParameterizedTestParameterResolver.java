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

import java.lang.reflect.Executable;
import java.lang.reflect.Method;

import org.junit.jupiter.api.extension.ExtensionContext;

/**
 * @since 5.0
 */
class ParameterizedTestParameterResolver extends ParameterizedInvocationParameterResolver {

	ParameterizedTestParameterResolver(ParameterizedDeclarationContext<?> declarationContext,
			EvaluatedArgumentSet arguments, int invocationIndex) {
		super(declarationContext, arguments, invocationIndex);

	}

	@Override
	protected boolean isSupportedOnConstructorOrMethod(Executable declaringExecutable,
			ExtensionContext extensionContext) {
		// Not a @ParameterizedTest method?
		Method testMethod = extensionContext.getTestMethod().orElse(null);
		return declaringExecutable.equals(testMethod);
	}

}
