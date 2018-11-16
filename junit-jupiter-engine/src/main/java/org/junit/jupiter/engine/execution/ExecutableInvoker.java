/*
 * Copyright 2015-2018 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v20.html
 */

package org.junit.jupiter.engine.execution;

import static org.apiguardian.api.API.Status.INTERNAL;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.Optional;

import org.apiguardian.api.API;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.ParameterResolver;
import org.junit.jupiter.engine.extension.ExtensionRegistry;
import org.junit.platform.commons.util.ReflectionUtils;

/**
 * {@code ExecutableInvoker} encapsulates the invocation of a
 * {@link java.lang.reflect.Executable} (i.e., method or constructor),
 * including support for dynamic resolution of method parameters via
 * {@link ParameterResolver ParameterResolvers}.
 *
 * @since 5.0
 */
@API(status = INTERNAL, since = "5.0")
public class ExecutableInvoker {

	private static final ParametersResolver parameterResolver = new ParametersResolver();

	/**
	 * Invoke the supplied constructor with dynamic parameter resolution.
	 *
	 * @param constructor the constructor to invoke and resolve parameters for
	 * @param extensionContext the current {@code ExtensionContext}
	 * @param extensionRegistry the {@code ExtensionRegistry} to retrieve
	 * {@code ParameterResolvers} from
	 */
	public <T> T invoke(Constructor<T> constructor, ExtensionContext extensionContext,
			ExtensionRegistry extensionRegistry) {

		return ReflectionUtils.newInstance(constructor,
			parameterResolver.resolveParameters(constructor, Optional.empty(), extensionContext, extensionRegistry));
	}

	/**
	 * Invoke the supplied constructor with the supplied outer instance and
	 * dynamic parameter resolution.
	 *
	 * <p>This method should only be used to invoke the constructor for
	 * an inner class.
	 *
	 * @param constructor the constructor to invoke and resolve parameters for
	 * @param outerInstance the outer instance to supply as the first argument
	 * to the constructor
	 * @param extensionContext the current {@code ExtensionContext}
	 * @param extensionRegistry the {@code ExtensionRegistry} to retrieve
	 * {@code ParameterResolvers} from
	 */
	public <T> T invoke(Constructor<T> constructor, Object outerInstance, ExtensionContext extensionContext,
			ExtensionRegistry extensionRegistry) {

		return ReflectionUtils.newInstance(constructor, parameterResolver.resolveParameters(constructor,
			Optional.empty(), outerInstance, extensionContext, extensionRegistry));
	}

	/**
	 * Invoke the supplied {@code static} method with dynamic parameter resolution.
	 *
	 * @param method the method to invoke and resolve parameters for
	 * @param extensionContext the current {@code ExtensionContext}
	 * @param extensionRegistry the {@code ExtensionRegistry} to retrieve
	 * {@code ParameterResolvers} from
	 */
	public Object invoke(Method method, ExtensionContext extensionContext, ExtensionRegistry extensionRegistry) {
		return ReflectionUtils.invokeMethod(method, null,
			parameterResolver.resolveParameters(method, Optional.empty(), extensionContext, extensionRegistry));
	}

	/**
	 * Invoke the supplied method on the supplied target object with dynamic parameter
	 * resolution.
	 *
	 * @param method the method to invoke and resolve parameters for
	 * @param target the object on which the method will be invoked; should be
	 * {@code null} for static methods
	 * @param extensionContext the current {@code ExtensionContext}
	 * @param extensionRegistry the {@code ExtensionRegistry} to retrieve
	 * {@code ParameterResolvers} from
	 */
	public Object invoke(Method method, Object target, ExtensionContext extensionContext,
			ExtensionRegistry extensionRegistry) {

		@SuppressWarnings("unchecked")
		Optional<Object> optionalTarget = (target instanceof Optional ? (Optional<Object>) target
				: Optional.ofNullable(target));
		return ReflectionUtils.invokeMethod(method, target,
			parameterResolver.resolveParameters(method, optionalTarget, extensionContext, extensionRegistry));
	}

	/**
	 * Invoke the supplied method on the supplied target object with the supplied arguments
	 *
	 * @param method the method to invoke and resolve parameters for
	 * @param target the object on which the method will be invoked; should be
	 * {@code null} for static methods
	 * @param arguments the arguments with which to call the method
	 * {@code ParameterResolvers} from
	 */
	public Object invoke(Method method, Object target, Object... arguments) {
		return ReflectionUtils.invokeMethod(method, target, arguments);
	}
}
