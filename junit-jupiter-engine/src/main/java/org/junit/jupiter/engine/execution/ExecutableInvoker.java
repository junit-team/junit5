/*
 * Copyright 2015-2020 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * https://www.eclipse.org/legal/epl-v20.html
 */

package org.junit.jupiter.engine.execution;

import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toList;
import static org.apiguardian.api.API.Status.INTERNAL;
import static org.junit.platform.commons.util.ReflectionUtils.isAssignableTo;

import java.lang.reflect.Constructor;
import java.lang.reflect.Executable;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.List;
import java.util.Optional;

import org.apiguardian.api.API;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.InvocationInterceptor;
import org.junit.jupiter.api.extension.InvocationInterceptor.Invocation;
import org.junit.jupiter.api.extension.ParameterContext;
import org.junit.jupiter.api.extension.ParameterResolutionException;
import org.junit.jupiter.api.extension.ParameterResolver;
import org.junit.jupiter.api.extension.ReflectiveInvocationContext;
import org.junit.jupiter.engine.extension.ExtensionRegistry;
import org.junit.platform.commons.logging.Logger;
import org.junit.platform.commons.logging.LoggerFactory;
import org.junit.platform.commons.util.BlacklistedExceptions;
import org.junit.platform.commons.util.Preconditions;
import org.junit.platform.commons.util.StringUtils;

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

	private static final Logger logger = LoggerFactory.getLogger(ExecutableInvoker.class);
	private static final InvocationInterceptorChain interceptorChain = new InvocationInterceptorChain();

	/**
	 * Invoke the supplied constructor with the supplied outer instance and
	 * dynamic parameter resolution.
	 *
	 * <p>This method should only be used to invoke the constructor for
	 * an inner class.
	 *
	 * @param constructor the constructor to invoke and resolve parameters for
	 * @param outerInstance the outer instance to supply as the first argument
	 * to the constructor; empty, for top-level classes
	 * @param extensionContext the current {@code ExtensionContext}
	 * @param extensionRegistry the {@code ExtensionRegistry} to retrieve
	 * {@code ParameterResolvers} from
	 * @param interceptorCall the call for intercepting this constructor
	 * invocation via all registered {@linkplain InvocationInterceptor
	 * interceptors}
	 */
	public <T> T invoke(Constructor<T> constructor, Optional<Object> outerInstance, ExtensionContext extensionContext,
			ExtensionRegistry extensionRegistry, ReflectiveInterceptorCall<Constructor<T>, T> interceptorCall) {

		Object[] arguments = resolveParameters(constructor, Optional.empty(), outerInstance, extensionContext,
			extensionRegistry);
		ConstructorInvocation<T> invocation = new ConstructorInvocation<>(constructor, arguments);
		return invoke(invocation, invocation, extensionContext, extensionRegistry, interceptorCall);
	}

	/**
	 * Invoke the supplied {@code static} method with dynamic parameter resolution.
	 *
	 * @param method the method to invoke and resolve parameters for
	 * @param extensionContext the current {@code ExtensionContext}
	 * @param extensionRegistry the {@code ExtensionRegistry} to retrieve
	 * {@code ParameterResolvers} from
	 * @param interceptorCall the call for intercepting this method invocation
	 * via all registered {@linkplain InvocationInterceptor interceptors}
	 */
	public <T> T invoke(Method method, Object target, ExtensionContext extensionContext,
			ExtensionRegistry extensionRegistry, ReflectiveInterceptorCall<Method, T> interceptorCall) {

		@SuppressWarnings("unchecked")
		Optional<Object> optionalTarget = (target instanceof Optional ? (Optional<Object>) target
				: Optional.ofNullable(target));
		Object[] arguments = resolveParameters(method, optionalTarget, extensionContext, extensionRegistry);
		MethodInvocation<T> invocation = new MethodInvocation<>(method, optionalTarget, arguments);
		return invoke(invocation, invocation, extensionContext, extensionRegistry, interceptorCall);
	}

	private <E extends Executable, T> T invoke(Invocation<T> originalInvocation,
			ReflectiveInvocationContext<E> invocationContext, ExtensionContext extensionContext,
			ExtensionRegistry extensionRegistry, ReflectiveInterceptorCall<E, T> call) {
		return interceptorChain.invoke(originalInvocation, extensionRegistry, (interceptor,
				wrappedInvocation) -> call.apply(interceptor, wrappedInvocation, invocationContext, extensionContext));
	}

	public interface ReflectiveInterceptorCall<E extends Executable, T> {

		T apply(InvocationInterceptor interceptor, Invocation<T> invocation,
				ReflectiveInvocationContext<E> invocationContext, ExtensionContext extensionContext) throws Throwable;

		static ReflectiveInterceptorCall<Method, Void> ofVoidMethod(VoidMethodInterceptorCall call) {
			return ((interceptorChain, invocation, invocationContext, extensionContext) -> {
				call.apply(interceptorChain, invocation, invocationContext, extensionContext);
				return null;
			});
		}

		interface VoidMethodInterceptorCall {
			void apply(InvocationInterceptor interceptor, Invocation<Void> invocation,
					ReflectiveInvocationContext<Method> invocationContext, ExtensionContext extensionContext)
					throws Throwable;
		}

	}

	/**
	 * Resolve the array of parameters for the supplied method and target.
	 *
	 * @param method the method for which to resolve parameters
	 * @param target an {@code Optional} containing the target on which the
	 * executable will be invoked; never {@code null} but should be empty for
	 * static methods and constructors
	 * @param extensionContext the current {@code ExtensionContext}
	 * @param extensionRegistry the {@code ExtensionRegistry} to retrieve
	 * {@code ParameterResolvers} from
	 * @return the array of Objects to be used as parameters in the executable
	 * invocation; never {@code null} though potentially empty
	 */
	private Object[] resolveParameters(Method method, Optional<Object> target, ExtensionContext extensionContext,
			ExtensionRegistry extensionRegistry) {

		return resolveParameters(method, target, Optional.empty(), extensionContext, extensionRegistry);
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
	private Object[] resolveParameters(Executable executable, Optional<Object> target, Optional<Object> outerInstance,
			ExtensionContext extensionContext, ExtensionRegistry extensionRegistry) {

		Preconditions.notNull(target, "target must not be null");

		Parameter[] parameters = executable.getParameters();
		Object[] values = new Object[parameters.length];
		int start = 0;

		// Ensure that the outer instance is resolved as the first parameter if
		// the executable is a constructor for an inner class.
		if (outerInstance.isPresent()) {
			values[0] = outerInstance.get();
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
				String resolvers = matchingResolvers.stream()
						.map(StringUtils::defaultToString)
						.collect(joining(", "));
				// @formatter:on
				throw new ParameterResolutionException(
					String.format("Discovered multiple competing ParameterResolvers for parameter [%s] in %s [%s]: %s",
						parameterContext.getParameter(), asLabel(executable), executable.toGenericString(), resolvers));
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
		catch (Throwable throwable) {
			BlacklistedExceptions.rethrowIfBlacklisted(throwable);

			String message = String.format("Failed to resolve parameter [%s] in %s [%s]",
				parameterContext.getParameter(), asLabel(executable), executable.toGenericString());

			if (StringUtils.isNotBlank(throwable.getMessage())) {
				message += ": " + throwable.getMessage();
			}

			throw new ParameterResolutionException(message, throwable);
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
