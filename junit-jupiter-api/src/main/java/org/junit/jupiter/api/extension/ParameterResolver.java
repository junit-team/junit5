/*
 * Copyright 2015-2017 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.junit.jupiter.api.extension;

import static org.junit.platform.commons.meta.API.Usage.Experimental;

import java.lang.reflect.Parameter;

import org.junit.platform.commons.meta.API;

/**
 * {@code ParameterResolver} defines the API for {@link Extension Extensions}
 * that wish to dynamically resolve {@linkplain Parameter parameters} at runtime.
 *
 * <p>If a constructor for a test class or a
 * {@link org.junit.jupiter.api.Test @Test},
 * {@link org.junit.jupiter.api.BeforeEach @BeforeEach},
 * {@link org.junit.jupiter.api.AfterEach @AfterEach},
 * {@link org.junit.jupiter.api.BeforeAll @BeforeAll}, or
 * {@link org.junit.jupiter.api.AfterAll @AfterAll} method accepts a parameter,
 * the parameter must be resolved at runtime by a {@code ParameterResolver}.
 *
 * <p>Implementations must provide a no-args constructor.
 *
 * @since 5.0
 * @see #supports(ParameterContext, ExtensionContext)
 * @see #resolve(ParameterContext, ExtensionContext)
 * @see ParameterContext
 */
@API(Experimental)
public interface ParameterResolver extends Extension {

	/**
	 * Determine if this resolver supports resolution of the {@link Parameter}
	 * in the supplied {@link ParameterContext} for the supplied
	 * {@link ExtensionContext}.
	 *
	 * <p>The {@link java.lang.reflect.Method} or {@link java.lang.reflect.Constructor}
	 * in which the parameter is declared can be retrieved via
	 * {@link ParameterContext#getDeclaringExecutable()}.
	 *
	 * @param parameterContext the context for the parameter to be resolved; never
	 * {@code null}
	 * @param extensionContext the extension context for the {@code Executable}
	 * about to be invoked; never {@code null}
	 * @return {@code true} if this resolver can resolve the parameter
	 * @see #resolve
	 * @see ParameterContext
	 */
	boolean supports(ParameterContext parameterContext, ExtensionContext extensionContext)
			throws ParameterResolutionException;

	/**
	 * Resolve the {@link Parameter} in the supplied {@link ParameterContext}
	 * for the supplied {@link ExtensionContext}.
	 *
	 * <p>This method is only called by the framework if {@link #supports} has
	 * previously returned {@code true} for the same {@link ParameterContext}
	 * and {@link ExtensionContext}.
	 *
	 * <p>The {@link java.lang.reflect.Method} or {@link java.lang.reflect.Constructor}
	 * in which the parameter is declared can be retrieved via
	 * {@link ParameterContext#getDeclaringExecutable()}.
	 *
	 * @param parameterContext the context for the parameter to be resolved; never
	 * {@code null}
	 * @param extensionContext the extension context for the {@code Executable}
	 * about to be invoked; never {@code null}
	 * @return the resolved parameter object; may only be {@code null} if the
	 * parameter type is not a primitive
	 * @see #supports
	 * @see ParameterContext
	 */
	Object resolve(ParameterContext parameterContext, ExtensionContext extensionContext)
			throws ParameterResolutionException;

}
