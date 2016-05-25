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

import static org.junit.gen5.commons.meta.API.Usage.Experimental;

import java.lang.reflect.Parameter;
import java.util.Optional;

import org.junit.gen5.commons.meta.API;

/**
 * {@code ParameterResolver} defines the API for {@link Extension Extensions}
 * that wish to dynamically resolve {@linkplain Parameter parameters} at runtime.
 *
 * <p>If a {@link org.junit.gen5.api.Test @Test},
 * {@link org.junit.gen5.api.BeforeEach @BeforeEach},
 * {@link org.junit.gen5.api.AfterEach @AfterEach},
 * {@link org.junit.gen5.api.BeforeAll @BeforeAll}, or
 * {@link org.junit.gen5.api.AfterAll @AfterAll} method accepts a parameter,
 * the parameter must be resolved at runtime by a {@code ParameterResolver}.
 *
 * <p>Implementations must provide a no-args constructor.
 *
 * <p>Note: as of JUnit 5.0.0-M1, the {@code ParameterResolver} API is only
 * applied to method invocations. Support for constructor invocations
 * may come at a later date.
 *
 * @since 5.0
 */
@API(Experimental)
public interface ParameterResolver extends Extension {

	/**
	 * Determine if this resolver supports resolution of the given {@link Parameter}
	 * for the supplied {@code target} and {@link ExtensionContext}.
	 *
	 * <p>The {@link java.lang.reflect.Method} or {@link java.lang.reflect.Constructor}
	 * in which the {@code parameter} is declared can be retrieved via
	 * {@link Parameter#getDeclaringExecutable()}.
	 *
	 * @param parameter the parameter to be resolved
	 * @param target the container for the target on which the {@code java.lang.reflect.Executable}
	 * will be invoked; may be <em>empty</em> if the {@code Executable} is a constructor
	 * or {@code static} method
	 * @param extensionContext the extension context for the {@code Executable}
	 * about to be invoked
	 * @return {@code true} if this resolver can resolve the parameter
	 * @see #resolve
	 * @see java.lang.reflect.Parameter
	 * @see java.lang.reflect.Executable
	 * @see java.lang.reflect.Method
	 * @see java.lang.reflect.Constructor
	 */
	boolean supports(Parameter parameter, Optional<Object> target, ExtensionContext extensionContext)
			throws ParameterResolutionException;

	/**
	 * Resolve the given {@link Parameter} for the supplied {@code target} and
	 * {@link ExtensionContext}.
	 *
	 * <p>The {@link java.lang.reflect.Method} or {@link java.lang.reflect.Constructor}
	 * in which the {@code parameter} is declared can be retrieved via
	 * {@link Parameter#getDeclaringExecutable()}.
	 *
	 * @param parameter the parameter to be resolved
	 * @param target the container for the target on which the {@code java.lang.reflect.Executable}
	 * will be invoked; may be <em>empty</em> if the {@code Executable} is a constructor
	 * or {@code static} method
	 * @param extensionContext the extension context for the {@code Executable}
	 * about to be invoked
	 * @return the resolved parameter object
	 * @see #supports
	 * @see java.lang.reflect.Parameter
	 * @see java.lang.reflect.Executable
	 * @see java.lang.reflect.Method
	 * @see java.lang.reflect.Constructor
	 */
	Object resolve(Parameter parameter, Optional<Object> target, ExtensionContext extensionContext)
			throws ParameterResolutionException;

}
