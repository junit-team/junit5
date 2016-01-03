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

import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.gen5.api.extension.ExtensionContext;
import org.junit.gen5.api.extension.MethodInvocationContext;
import org.junit.gen5.api.extension.MethodParameterResolver;
import org.junit.gen5.api.extension.ParameterResolutionException;
import org.junit.gen5.commons.util.ReflectionUtils;
import org.junit.gen5.engine.junit5.execution.TestExtensionRegistry.ApplicationOrder;

/**
 * {@code MethodInvoker} encapsulates the invocation of a method, including
 * support for dynamic resolution of method parameters via
 * {@link MethodParameterResolver MethodParameterResolvers}.
 *
 * @since 5.0
 */
public class MethodInvoker {

	private final ExtensionContext extensionContext;

	private final TestExtensionRegistry extensionRegistry;

	public MethodInvoker(ExtensionContext extensionContext, TestExtensionRegistry extensionRegistry) {
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

		try {
			final List<MethodParameterResolver> matchingResolvers = new ArrayList<>();
			extensionRegistry.stream(MethodParameterResolver.class, ApplicationOrder.FORWARD).forEach(
				registeredExtensionPoint -> {
					if (registeredExtensionPoint.getExtensionPoint().supports(parameter, methodInvocationContext,
						extensionContext))
						matchingResolvers.add(registeredExtensionPoint.getExtensionPoint());
				});

			if (matchingResolvers.size() == 0) {
				throw new ParameterResolutionException(
					String.format("No MethodParameterResolver registered for parameter [%s] in method [%s].", parameter,
						methodInvocationContext.getMethod().toGenericString()));
			}
			if (matchingResolvers.size() > 1) {
				// @formatter:off
				String resolverNames = matchingResolvers.stream()
						.map(resolver -> resolver.getClass().getName())
						.collect(joining(", "));
				// @formatter:on
				throw new ParameterResolutionException(String.format(
					"Discovered multiple competing MethodParameterResolvers for parameter [%s] in method [%s]: %s",
					parameter, methodInvocationContext.getMethod().toGenericString(), resolverNames));
			}
			return matchingResolvers.get(0).resolve(parameter, methodInvocationContext, extensionContext);
		}
		catch (Throwable ex) {
			if (ex instanceof ParameterResolutionException) {
				throw (ParameterResolutionException) ex;
			}
			throw new ParameterResolutionException(String.format("Failed to resolve parameter [%s] in method [%s]",
				parameter, methodInvocationContext.getMethod().toGenericString()), ex);
		}
	}

}
