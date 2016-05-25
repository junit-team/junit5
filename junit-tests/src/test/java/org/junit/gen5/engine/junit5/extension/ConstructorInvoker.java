/*
 * Copyright 2015-2016 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.junit.gen5.engine.junit5.extension;

import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toList;
import static org.junit.gen5.commons.util.ReflectionUtils.isAssignableTo;

import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Parameter;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Logger;

import org.junit.gen5.api.extension.ExtensionContext;
import org.junit.gen5.api.extension.ParameterResolutionException;
import org.junit.gen5.api.extension.ParameterResolver;
import org.junit.gen5.commons.util.ExceptionUtils;
import org.junit.gen5.engine.junit5.execution.MethodInvoker;

/**
 * This class serves as a proof of concept for adding constructor injection
 * support to JUnit 5.
 *
 * <p>It therefore intentionally duplicates {@link MethodInvoker} until
 * {@code MethodInvoker} is converted into a general purpose invoker
 * for {@link java.lang.reflect.Executable}.
 *
 * @since 5.0
 */
class ConstructorInvoker {

	private static final Logger LOG = Logger.getLogger(ConstructorInvoker.class.getName());

	private final ExtensionContext extensionContext;
	private final ExtensionRegistry extensionRegistry;

	ConstructorInvoker(ExtensionContext extensionContext, ExtensionRegistry extensionRegistry) {
		this.extensionContext = extensionContext;
		this.extensionRegistry = extensionRegistry;
	}

	Object invoke(Constructor<?> constructor) {
		try {
			return makeAccessible(constructor).newInstance(resolveParameters(constructor));
		}
		catch (Throwable t) {
			throw ExceptionUtils.throwAsUncheckedException(getUnderlyingCause(t));
		}
	}

	private Object[] resolveParameters(Constructor<?> constructor) {
		// @formatter:off
		return Arrays.stream(constructor.getParameters())
				.map(param -> resolveParameter(param, constructor))
				.toArray(Object[]::new);
		// @formatter:on
	}

	private Object resolveParameter(Parameter parameter, Constructor<?> constructor) {
		try {
			// @formatter:off
			List<ParameterResolver> matchingResolvers = this.extensionRegistry.stream(ParameterResolver.class)
					.filter(resolver -> resolver.supports(parameter, null, this.extensionContext))
					.collect(toList());
			// @formatter:on

			if (matchingResolvers.size() == 0) {
				throw new ParameterResolutionException(
					String.format("No ParameterResolver registered for parameter [%s] in constructor [%s].", parameter,
						constructor.toGenericString()));
			}

			if (matchingResolvers.size() > 1) {
				// @formatter:off
				String resolverNames = matchingResolvers.stream()
						.map(resolver -> resolver.getClass().getName())
						.collect(joining(", "));
				// @formatter:on
				throw new ParameterResolutionException(String.format(
					"Discovered multiple competing ParameterResolvers for parameter [%s] in constructor [%s]: %s",
					parameter, constructor.toGenericString(), resolverNames));
			}

			ParameterResolver resolver = matchingResolvers.get(0);
			Object value = resolver.resolve(parameter, null, this.extensionContext);
			validateResolvedType(parameter, value, constructor, resolver);

			LOG.finer(() -> String.format(
				"ParameterResolver [%s] resolved a value of type [%s] for parameter [%s] in constructor [%s].",
				resolver.getClass().getName(), (value != null ? value.getClass().getName() : null), parameter,
				constructor.toGenericString()));

			return value;
		}
		catch (Throwable ex) {
			if (ex instanceof ParameterResolutionException) {
				throw (ParameterResolutionException) ex;
			}
			throw new ParameterResolutionException(String.format("Failed to resolve parameter [%s] in constructor [%s]",
				parameter, constructor.toGenericString()), ex);
		}
	}

	private void validateResolvedType(Parameter parameter, Object value, Constructor<?> constructor,
			ParameterResolver resolver) {

		Class<?> type = parameter.getType();

		// Note: null is permissible as a resolved value but only for non-primitive types.
		if (!isAssignableTo(value, type)) {
			String message;
			if (value == null && type.isPrimitive()) {
				message = String.format(
					"ParameterResolver [%s] resolved a null value for parameter [%s] "
							+ "in constructor [%s], but a primitive of type [%s] is required.",
					resolver.getClass().getName(), parameter, constructor.toGenericString(), type.getName());
			}
			else {
				message = String.format(
					"ParameterResolver [%s] resolved a value of type [%s] for parameter [%s] "
							+ "in constructor [%s], but a value assignment compatible with [%s] is required.",
					resolver.getClass().getName(), (value != null ? value.getClass().getName() : null), parameter,
					constructor.toGenericString(), type.getName());
			}

			throw new ParameterResolutionException(message);
		}
	}

	private static <T extends AccessibleObject> T makeAccessible(T object) {
		if (!object.isAccessible()) {
			object.setAccessible(true);
		}
		return object;
	}

	private static Throwable getUnderlyingCause(Throwable t) {
		if (t instanceof InvocationTargetException) {
			return getUnderlyingCause(((InvocationTargetException) t).getTargetException());
		}
		return t;
	}

}
