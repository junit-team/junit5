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

import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toList;
import static org.junit.platform.commons.util.ReflectionUtils.isAssignableTo;

import java.lang.reflect.Constructor;
import java.lang.reflect.Executable;
import java.lang.reflect.Parameter;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.ParameterContext;
import org.junit.jupiter.api.extension.ParameterResolutionException;
import org.junit.jupiter.api.extension.ParameterResolver;
import org.junit.jupiter.engine.extension.ExtensionRegistry;
import org.junit.platform.commons.logging.Logger;
import org.junit.platform.commons.logging.LoggerFactory;
import org.junit.platform.commons.util.Preconditions;

public class ParametersResolver {

	private static final Logger logger = LoggerFactory.getLogger(ParametersResolver.class);

	/**
	 * Resolve the array of parameters for the supplied executable and target.
	 *
	 * @param executable the executable for which to resolve parameters
	 * @param target an {@code Optional} containing the target on which the
	 * executable will be invoked; never {@code null} but should be empty for
	 * static methods and constructors
	 * @param extensionContext the current {@code ExtensionContext}
	 * @param extensionRegistry the {@code ExtensionRegistry} to retrieve
	 * {@code ParameterResolvers} from
	 * @return the array of Objects to be used as parameters in the executable
	 * invocation; never {@code null} though potentially empty
	 */
	public Object[] resolveParameters(Executable executable, Optional<Object> target, ExtensionContext extensionContext,
			ExtensionRegistry extensionRegistry) {

		return resolveParameters(executable, target, null, extensionContext, extensionRegistry);
	}

	/**
	 * Resolve the array of parameters for the supplied executable, target, and
	 * outer instance.
	 *
	 * @param executable the executable for which to resolve parameters
	 * @param target an {@code Optional} containing the target on which the
	 * executable will be invoked; never {@code null} but should be empty for
	 * static methods and constructors
	 * @param outerInstance the outer instance that will be supplied as the
	 * first argument to a constructor for an inner class; should be {@code null}
	 * for methods and constructors for top-level or static classes
	 * @param extensionContext the current {@code ExtensionContext}
	 * @param extensionRegistry the {@code ExtensionRegistry} to retrieve
	 * {@code ParameterResolvers} from
	 * @return the array of Objects to be used as parameters in the executable
	 * invocation; never {@code null} though potentially empty
	 */
	Object[] resolveParameters(Executable executable, Optional<Object> target, Object outerInstance,
			ExtensionContext extensionContext, ExtensionRegistry extensionRegistry) {

		Preconditions.notNull(target, "target must not be null");

		Parameter[] parameters = executable.getParameters();
		Object[] values = new Object[parameters.length];
		int start = 0;

		// Ensure that the outer instance is resolved as the first parameter if
		// the executable is a constructor for an inner class.
		if (outerInstance != null) {
			values[0] = outerInstance;
			start = 1;
		}

		// Resolve remaining parameters dynamically
		for (int i = start; i < parameters.length; i++) {
			ParameterContext parameterContext = new DefaultParameterContext(parameters[i], i, target);
			values[i] = resolveParameter(parameterContext, executable, extensionContext, extensionRegistry);
		}
		return values;
	}

	private Object resolveParameter(ParameterContext parameterContext, Executable executable,
			ExtensionContext extensionContext, ExtensionRegistry extensionRegistry) {

		try {
			// @formatter:off
   			List<ParameterResolver> matchingResolvers = extensionRegistry.stream(ParameterResolver.class)
   					.filter(resolver -> resolver.supportsParameter(parameterContext, extensionContext))
   					.collect(toList());
   			// @formatter:on

			if (matchingResolvers.isEmpty()) {
				throw new ParameterResolutionException(
					String.format("No ParameterResolver registered for parameter [%s] in %s [%s].",
						parameterContext.getParameter(), asLabel(executable), executable.toGenericString()));
			}

			if (matchingResolvers.size() > 1) {
				// @formatter:off
   				String resolverNames = matchingResolvers.stream()
   						.map(resolver -> resolver.getClass().getName())
   						.collect(joining(", "));
   				// @formatter:on
				throw new ParameterResolutionException(String.format(
					"Discovered multiple competing ParameterResolvers for parameter [%s] in %s [%s]: %s",
					parameterContext.getParameter(), asLabel(executable), executable.toGenericString(), resolverNames));
			}

			ParameterResolver resolver = matchingResolvers.get(0);
			Object value = resolver.resolveParameter(parameterContext, extensionContext);
			validateResolvedType(parameterContext.getParameter(), value, executable, resolver);

			logger.trace(() -> String.format(
				"ParameterResolver [%s] resolved a value of type [%s] for parameter [%s] in %s [%s].",
				resolver.getClass().getName(), (value != null ? value.getClass().getName() : null),
				parameterContext.getParameter(), asLabel(executable), executable.toGenericString()));

			return value;
		}
		catch (ParameterResolutionException ex) {
			throw ex;
		}
		catch (Throwable ex) {
			throw new ParameterResolutionException(String.format("Failed to resolve parameter [%s] in %s [%s]",
				parameterContext.getParameter(), asLabel(executable), executable.toGenericString()), ex);
		}
	}

	private void validateResolvedType(Parameter parameter, Object value, Executable executable,
			ParameterResolver resolver) {

		Class<?> type = parameter.getType();

		// Note: null is permissible as a resolved value but only for non-primitive types.
		if (!isAssignableTo(value, type)) {
			String message;
			if (value == null && type.isPrimitive()) {
				message = String.format(
					"ParameterResolver [%s] resolved a null value for parameter [%s] "
							+ "in %s [%s], but a primitive of type [%s] is required.",
					resolver.getClass().getName(), parameter, asLabel(executable), executable.toGenericString(),
					type.getName());
			}
			else {
				message = String.format(
					"ParameterResolver [%s] resolved a value of type [%s] for parameter [%s] "
							+ "in %s [%s], but a value assignment compatible with [%s] is required.",
					resolver.getClass().getName(), (value != null ? value.getClass().getName() : null), parameter,
					asLabel(executable), executable.toGenericString(), type.getName());
			}

			throw new ParameterResolutionException(message);
		}
	}

	private static String asLabel(Executable executable) {
		return executable instanceof Constructor ? "constructor" : "method";
	}
}
