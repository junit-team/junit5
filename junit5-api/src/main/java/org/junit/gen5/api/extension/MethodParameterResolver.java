/*
 * Copyright 2015-2016 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.junit.gen5.api.extension;

import java.lang.reflect.Parameter;

import org.junit.gen5.commons.util.ReflectionUtils;

/**
 * {@code MethodParameterResolver} defines the API for {@link TestExtension
 * TestExtensions} that wish to dynamically resolve method parameters at runtime.
 *
 * <p>If a {@link org.junit.gen5.api.Test @Test},
 * {@link org.junit.gen5.api.BeforeEach @BeforeEach}, or
 * {@link org.junit.gen5.api.AfterEach @AfterEach} method accepts a parameter,
 * the parameter must be resolved at runtime by a {@code MethodParameterResolver}.
 *
 * <p>Implementations must provide a no-args constructor.
 *
 * @since 5.0
 */
public interface MethodParameterResolver extends ExtensionPoint {

	/**
	 * Determine if this resolver supports resolution of the given {@link Parameter}
	 * for the supplied {@link MethodContext} and {@link ExtensionContext}.
	 *
	 * @param parameter parameter to be resolved
	 * @param methodContext method context the parameter belongs to
	 * @param extensionContext extension context of the method about to be executed
	 */
	boolean supports(Parameter parameter, MethodContext methodContext, ExtensionContext extensionContext);

	/**
	 * Resolve the given {@link Parameter} for the supplied {@link MethodContext}
	 * and {@link ExtensionContext}.
	 *
	 * <p>The default implementation uses reflection to instantiate the
	 * required {@link Parameter#getType type} via its default constructor.
	 *
	 * @param parameter parameter to be resolved
	 * @param methodContext method context the parameter belongs to
	 * @param extensionContext extension context of the method about to be executed
	 *
	 * @see ReflectionUtils#newInstance(Class, Object...)
	 */
	default Object resolve(Parameter parameter, MethodContext methodContext, ExtensionContext extensionContext)
			throws ParameterResolutionException {

		return ReflectionUtils.newInstance(parameter.getType());
	}

}
