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
 * {@link ParameterResolver} adapter which resolves a parameter based on its exact type.
 *
 * @param <T> the type of the parameter supported by this {@code ParameterResolver}
 * @since 5.6
 */
@API(status = EXPERIMENTAL, since = "5.6")
public abstract class TypeBasedParameterResolver<T> implements ParameterResolver {

	private final Type supportedParameterType;

	public TypeBasedParameterResolver() {
		this.supportedParameterType = enclosedTypeOfParameterResolver();
	}

	@Override
	public final boolean supportsParameter(ParameterContext parameterContext, ExtensionContext extensionContext) {
		return this.supportedParameterType.equals(getParameterType(parameterContext));
	}

	@Override
	public abstract T resolveParameter(ParameterContext parameterContext, ExtensionContext extensionContext)
			throws ParameterResolutionException;

	private Type getParameterType(ParameterContext parameterContext) {
		return parameterContext.getParameter().getParameterizedType();
	}

	private Type enclosedTypeOfParameterResolver() {
		return findTypeBasedParameterResolverSuperclass(getClass()).getActualTypeArguments()[0];
	}

	private ParameterizedType findTypeBasedParameterResolverSuperclass(Class<?> subclass) {
		Type genericSuperclass = subclass.getGenericSuperclass();
		if (genericSuperclass instanceof ParameterizedType) {
			Type rawType = ((ParameterizedType) genericSuperclass).getRawType();
			if (rawType == TypeBasedParameterResolver.class) {
				return (ParameterizedType) genericSuperclass;
			}
		}
		return findTypeBasedParameterResolverSuperclass(subclass.getSuperclass());
	}

}
