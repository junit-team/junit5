/*
 * Copyright 2015-2019 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * https://www.eclipse.org/legal/epl-v20.html
 */

package org.junit.jupiter.api.extension.support;

import static org.apiguardian.api.API.Status.EXPERIMENTAL;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

import org.apiguardian.api.API;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.ParameterContext;
import org.junit.jupiter.api.extension.ParameterResolutionException;
import org.junit.jupiter.api.extension.ParameterResolver;

/**
 * {@link ParameterResolver} adapter which resolve a parameter based on its exact type.
 *
 * @param <T> the type of the parameter to resolve
 * @since 5.6
 */
@API(status = EXPERIMENTAL)
public abstract class TypeBasedParameterResolver<T> implements ParameterResolver {

	private final Type supportedParameterType;

	public TypeBasedParameterResolver() {
		supportedParameterType = enclosedTypeOfParameterResolver();
	}

	@Override
	public abstract T resolveParameter(ParameterContext parameterContext, ExtensionContext extensionContext)
			throws ParameterResolutionException;

	@Override
	public boolean supportsParameter(ParameterContext parameterContext, ExtensionContext extensionContext)
			throws ParameterResolutionException {
		return supportedParameterType.equals(getParameterType(parameterContext));
	}

	private Type getParameterType(ParameterContext parameterContext) {
		return parameterContext.getParameter().getParameterizedType();
	}

	private Type enclosedTypeOfParameterResolver() {
		return ((ParameterizedType) findTypeBasedParameterResolverSuperclass(
			this.getClass())).getActualTypeArguments()[0];
	}

	private Type findTypeBasedParameterResolverSuperclass(Class<?> subClass) {
		Type genericSuperclass = subClass.getGenericSuperclass();
		if (genericSuperclass instanceof ParameterizedType) {
			Type rawType = ((ParameterizedType) genericSuperclass).getRawType();
			if (rawType == TypeBasedParameterResolver.class) {
				return genericSuperclass;
			}
		}
		return findTypeBasedParameterResolverSuperclass(subClass.getSuperclass());
	}
}
