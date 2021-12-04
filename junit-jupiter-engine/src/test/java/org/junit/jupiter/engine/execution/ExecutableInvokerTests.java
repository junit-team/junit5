/*
 * Copyright 2015-2022 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * https://www.eclipse.org/legal/epl-v20.html
 */

package org.junit.jupiter.engine.execution;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.engine.execution.ParameterResolutionUtilsTest.ConfigurableParameterResolver;
import static org.junit.jupiter.engine.execution.ParameterResolutionUtilsTest.ConstructorInjectionTestCase;
import static org.junit.jupiter.engine.execution.ParameterResolutionUtilsTest.MethodSource;
import static org.junit.jupiter.engine.execution.ParameterResolutionUtilsTest.NumberParameterResolver;
import static org.junit.jupiter.engine.execution.ParameterResolutionUtilsTest.StringParameterResolver;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import java.lang.reflect.Constructor;
import java.lang.reflect.Executable;
import java.lang.reflect.Method;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.ParameterResolutionException;
import org.junit.jupiter.api.extension.ParameterResolver;
import org.junit.jupiter.engine.config.JupiterConfiguration;
import org.junit.jupiter.engine.execution.ExecutableInvoker.ReflectiveInterceptorCall;
import org.junit.jupiter.engine.extension.MutableExtensionRegistry;
import org.junit.platform.commons.util.ReflectionUtils;

/**
 * Unit tests for {@link ExecutableInvoker}.
 *
 * @since 5.0
 */
class ExecutableInvokerTests {

	private static final String ENIGMA = "enigma";

	private final MethodSource instance = mock(MethodSource.class);
	private Method method;

	private final ExtensionContext extensionContext = mock(ExtensionContext.class);

	private final JupiterConfiguration configuration = mock(JupiterConfiguration.class);

	private final MutableExtensionRegistry extensionRegistry = MutableExtensionRegistry.createRegistryWithDefaultExtensions(
		configuration);

	@Test
	void constructorInjection() {
		register(new StringParameterResolver(), new NumberParameterResolver());

		Class<ConstructorInjectionTestCase> outerClass = ConstructorInjectionTestCase.class;
		Constructor<ConstructorInjectionTestCase> constructor = ReflectionUtils.getDeclaredConstructor(outerClass);
		ConstructorInjectionTestCase outer = newInvoker().invoke(constructor, Optional.empty(), extensionContext,
			extensionRegistry, passthroughInterceptor());

		assertNotNull(outer);
		assertEquals(ENIGMA, outer.str);

		Class<ConstructorInjectionTestCase.NestedTestCase> innerClass = ConstructorInjectionTestCase.NestedTestCase.class;
		Constructor<ConstructorInjectionTestCase.NestedTestCase> innerConstructor = ReflectionUtils.getDeclaredConstructor(
			innerClass);
		ConstructorInjectionTestCase.NestedTestCase inner = newInvoker().invoke(innerConstructor, Optional.of(outer),
			extensionContext, extensionRegistry, passthroughInterceptor());

		assertNotNull(inner);
		assertEquals(42, inner.num);
	}

	@Test
	void resolveArgumentsViaParameterResolver() {
		testMethodWithASingleStringParameter();
		thereIsAParameterResolverThatResolvesTheParameterTo("argument");

		invokeMethod();

		verify(instance).singleStringParameter("argument");
	}

	@Test
	void propagatesParameterResolutionException() {
		testMethodWithASingleStringParameter();
		ParameterResolutionException cause = new ParameterResolutionException("custom message");
		throwDuringParameterResolution(cause);

		ParameterResolutionException caught = assertThrows(ParameterResolutionException.class, this::invokeMethod);

		assertSame(cause, caught);
	}

	private void throwDuringParameterResolution(RuntimeException parameterResolutionException) {
		register(ConfigurableParameterResolver.onAnyCallThrow(parameterResolutionException));
	}

	private void thereIsAParameterResolverThatResolvesTheParameterTo(Object argument) {
		register(ConfigurableParameterResolver.supportsAndResolvesTo(parameterContext -> argument));
	}

	private void testMethodWithASingleStringParameter() {
		this.method = ReflectionUtils.findMethod(this.instance.getClass(), "singleStringParameter", String.class).get();
	}

	private void register(ParameterResolver... resolvers) {
		for (ParameterResolver resolver : resolvers) {
			extensionRegistry.registerExtension(resolver, this);
		}
	}

	private ExecutableInvoker newInvoker() {
		return new ExecutableInvoker();
	}

	private void invokeMethod() {
		newInvoker().invoke(this.method, this.instance, this.extensionContext, this.extensionRegistry,
			passthroughInterceptor());
	}

	static <E extends Executable, T> ReflectiveInterceptorCall<E, T> passthroughInterceptor() {
		return (interceptor, invocation, invocationContext, extensionContext) -> invocation.proceed();
	}

}
