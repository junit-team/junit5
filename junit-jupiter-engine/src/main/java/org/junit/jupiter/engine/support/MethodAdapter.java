/*
 * Copyright 2015-2025 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * https://www.eclipse.org/legal/epl-v20.html
 */

package org.junit.jupiter.engine.support;

import static org.apiguardian.api.API.Status.INTERNAL;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.lang.reflect.Type;

import org.apiguardian.api.API;
import org.junit.platform.commons.support.ModifierSupport;
import org.junit.platform.commons.support.ReflectionSupport;

@API(status = INTERNAL, since = "6.0")
public interface MethodAdapter {

	Method getMethod();

	String getName();

	Class<?> getReturnType();

	Type getGenericReturnType();

	Class<?>[] getParameterTypes();

	Parameter[] getParameters();

	Object invoke(Object target, Object... args);

	default boolean isStatic() {
		return ModifierSupport.isStatic(getMethod());
	}

	default boolean isNotStatic() {
		return ModifierSupport.isNotStatic(getMethod());
	}

	default boolean isNotPrivate() {
		return ModifierSupport.isNotPrivate(getMethod());
	}

	default boolean isNotAbstract() {
		return ModifierSupport.isNotAbstract(getMethod());
	}

	default String toGenericString() {
		return getMethod().toGenericString();
	}

	static MethodAdapter createDefault(Method method) {
		return new MethodAdapter() {

			@Override
			public Method getMethod() {
				return method;
			}

			@Override
			public String getName() {
				return method.getName();
			}

			@Override
			public Class<?> getReturnType() {
				return method.getReturnType();
			}

			@Override
			public Type getGenericReturnType() {
				return method.getGenericReturnType();
			}

			@Override
			public Class<?>[] getParameterTypes() {
				return method.getParameterTypes();
			}

			@Override
			public Parameter[] getParameters() {
				return method.getParameters();
			}

			@Override
			public Object invoke(Object target, Object... args) {
				return ReflectionSupport.invokeMethod(method, target, args);
			}
		};
	}
}
