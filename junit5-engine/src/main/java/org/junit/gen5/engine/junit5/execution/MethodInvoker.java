/*
 * Copyright 2015-2016 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.junit.gen5.engine.junit5.execution;

import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toList;
import static org.junit.gen5.commons.meta.API.Usage.Internal;
import static org.junit.gen5.commons.util.ReflectionUtils.isAssignableTo;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Logger;

import org.junit.gen5.api.extension.ExtensionContext;
import org.junit.gen5.api.extension.MethodInvocationContext;
import org.junit.gen5.api.extension.MethodParameterResolver;
import org.junit.gen5.api.extension.ParameterResolutionException;
import org.junit.gen5.commons.meta.API;
import org.junit.gen5.commons.util.ReflectionUtils;
import org.junit.gen5.engine.junit5.extension.ExtensionRegistry;

/**
 * {@code MethodInvoker} encapsulates the invocation of a method, including
 * support for dynamic resolution of method parameters via
 * {@link MethodParameterResolver MethodParameterResolvers}.
 *
 * @since 5.0
 */
@API(Internal)
public class MethodInvoker {

	private static final Logger LOG = Logger.getLogger(MethodInvoker.class.getName());

	private final ExtensionContext extensionContext;

	private final ExtensionRegistry extensionRegistry;

	public MethodInvoker(ExtensionContext extensionContext, ExtensionRegistry extensionRegistry) {
		this.extensionContext = extensionContext;
		this.extensionRegistry = extensionRegistry;
	}

	public Object invoke(MethodInvocationContext methodInvocationContext) {
		return ReflectionUtils.invokeMethod(methodInvocationContext.getMethod(), methodInvocationContext.getInstance(),
			resolveParameters(methodInvocationContext));
	}

	/**
	 * Resolve the array of parameters for the configured method.
	 *
	 * @return the array of Objects to be used as parameters in the method
	 * invocation; never {@code null} though potentially empty
	 */
	private Object[] resolveParameters(MethodInvocationContext methodInvocationContext)
			throws ParameterResolutionException {
		// @formatter:off
		return Arrays.stream(methodInvocationContext.getMethod().getParameters())
				.map(param -> resolveParameter(param, methodInvocationContext))
				.toArray(Object[]::new);
		// @formatter:on
	}

	private Object resolveParameter(Parameter parameter, MethodInvocationContext methodInvocationContext)
			throws ParameterResolutionException {

		Method method = methodInvocationContext.getMethod();

		try {
			// @formatter:off
			List<MethodParameterResolver> matchingResolvers = this.extensionRegistry.stream(MethodParameterResolver.class)
					.filter(resolver -> resolver.supports(parameter, methodInvocationContext, this.extensionContext))
					.collect(toList());
			// @formatter:on

			if (matchingResolvers.size() == 0) {
				throw new ParameterResolutionException(
					String.format("No MethodParameterResolver registered for parameter [%s] in method [%s].", parameter,
						method.toGenericString()));
			}

			if (matchingResolvers.size() > 1) {
				// @formatter:off
				String resolverNames = matchingResolvers.stream()
						.map(resolver -> resolver.getClass().getName())
						.collect(joining(", "));
				// @formatter:on
				throw new ParameterResolutionException(String.format(
					"Discovered multiple competing MethodParameterResolvers for parameter [%s] in method [%s]: %s",
					parameter, method.toGenericString(), resolverNames));
			}

			MethodParameterResolver resolver = matchingResolvers.get(0);
			Object value = resolver.resolve(parameter, methodInvocationContext, this.extensionContext);
			validateResolvedType(parameter, value, method, resolver);

			LOG.finer(() -> String.format(
				"MethodParameterResolver [%s] resolved a value of type [%s] for parameter [%s] in method [%s].",
				resolver.getClass().getName(), (value != null ? value.getClass().getName() : null), parameter,
				method.toGenericString()));

			return value;
		}
		catch (Throwable ex) {
			if (ex instanceof ParameterResolutionException) {
				throw (ParameterResolutionException) ex;
			}
			throw new ParameterResolutionException(
				String.format("Failed to resolve parameter [%s] in method [%s]", parameter, method.toGenericString()),
				ex);
		}
	}

	private void validateResolvedType(Parameter parameter, Object value, Method method,
			MethodParameterResolver resolver) {

		final Class<?> type = parameter.getType();

		// Note: null is permissible as a resolved value but only for non-primitive types.
		if (!isAssignableTo(value, type)) {
			String message;
			if (value == null && type.isPrimitive()) {
				message = String.format(
					"MethodParameterResolver [%s] resolved a null value for parameter [%s] "
							+ "in method [%s], but a primitive of type [%s] is required.",
					resolver.getClass().getName(), parameter, method.toGenericString(), type.getName());
			}
			else {
				message = String.format(
					"MethodParameterResolver [%s] resolved a value of type [%s] for parameter [%s] "
							+ "in method [%s], but a value assignment compatible with [%s] is required.",
					resolver.getClass().getName(), (value != null ? value.getClass().getName() : null), parameter,
					method.toGenericString(), type.getName());
			}

			throw new ParameterResolutionException(message);
		}
	}

}
