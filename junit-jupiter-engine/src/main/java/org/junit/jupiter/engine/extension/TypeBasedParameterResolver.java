/*
 * Copyright 2015-2019 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * https://www.eclipse.org/legal/epl-v20.html
 */

package org.junit.jupiter.engine.extension;

import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.ParameterContext;
import org.junit.jupiter.api.extension.ParameterResolutionException;
import org.junit.jupiter.api.extension.ParameterResolver;

/**
 * {@link ParameterResolver} adapter which resolve a parameter based on its type.
 * @param <T> the type of the parameter to resolve
 * @since 5.6
 */
public abstract class TypeBasedParameterResolver<T> implements ParameterResolver {
	@Override
	public boolean supportsParameter(ParameterContext parameterContext, ExtensionContext extensionContext)
			throws ParameterResolutionException {
		String enclosedType = getEnclosedType();
		return getParameterTypeName(parameterContext).equals(enclosedType);
	}

	private String getParameterTypeName(ParameterContext parameterContext) {
		return parameterContext.getParameter().getParameterizedType().getTypeName();
	}

	private String getEnclosedType() {
		String instanceEffectiveType = this.getClass().getGenericSuperclass().getTypeName();
		return instanceEffectiveType.substring(TypeBasedParameterResolver.class.getName().length() + 1,
			instanceEffectiveType.length() - 1);
	}
}
