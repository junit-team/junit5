/*
 * Copyright 2015 the original author or authors.
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
 * {@code MethodParameterResolver} is a {@link TestExtension} strategy for
 * dynamically resolving method parameters at runtime.
 *
 * <p>If a {@link org.junit.gen5.api.Test @Test},
 * {@link org.junit.gen5.api.BeforeEach @BeforeEach}, or
 * {@link org.junit.gen5.api.AfterEach @AfterEach} method accepts a parameter,
 * the parameter must be resolved at runtime by a {@code MethodParameterResolver}.
 *
 * <p>Implementations must provide a no-args constructor.
 *
 * @author Matthias Merdes
 * @author Sam Brannen
 * @since 5.0
 */
public interface MethodParameterResolver extends TestExtension {

	/**
	 * Determine if this resolver supports resolution of the given {@link Parameter}
	 * for the supplied {@link TestExecutionContext}.
	 */
	boolean supports(Parameter parameter, TestExecutionContext testExecutionContext);

	/**
	 * Resolve the given {@link Parameter} for the supplied {@link TestExecutionContext}.
	 *
	 * <p>The default implementation uses reflection to instantiate the
	 * required {@link Parameter#getType type} via its default constructor.
	 *
	 * @see ReflectionUtils#newInstance(Class, Object...)
	 */
	default Object resolve(Parameter parameter, TestExecutionContext testExecutionContext)
			throws ParameterResolutionException {

		return ReflectionUtils.newInstance(parameter.getType());
	}

}
