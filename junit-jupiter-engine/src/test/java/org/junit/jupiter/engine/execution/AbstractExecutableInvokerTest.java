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
import static org.junit.jupiter.engine.execution.ParameterResolutionUtilsTest.ConfigurableParameterResolver;
import static org.junit.jupiter.engine.execution.ParameterResolutionUtilsTest.ConstructorInjectionTestCase;
import static org.junit.jupiter.engine.execution.ParameterResolutionUtilsTest.MethodSource;
import static org.junit.jupiter.engine.execution.ParameterResolutionUtilsTest.NumberParameterResolver;
import static org.junit.jupiter.engine.execution.ParameterResolutionUtilsTest.StringParameterResolver;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.ParameterResolver;
import org.junit.jupiter.engine.config.JupiterConfiguration;
import org.junit.jupiter.engine.extension.MutableExtensionRegistry;
import org.junit.platform.commons.util.ReflectionUtils;

/**
 * @since 5.9
 */
abstract class AbstractExecutableInvokerTest {

	private static final String ENIGMA = "enigma";

	protected final MethodSource instance = mock(MethodSource.class);
	protected Method method;

	protected final ExtensionContext extensionContext = mock(ExtensionContext.class);

	private final JupiterConfiguration configuration = mock(JupiterConfiguration.class);

	protected final MutableExtensionRegistry extensionRegistry = MutableExtensionRegistry.createRegistryWithDefaultExtensions(
		configuration);

	@Test
	void constructorInjection() {
		register(new StringParameterResolver(), new NumberParameterResolver());

		Class<ConstructorInjectionTestCase> outerClass = ConstructorInjectionTestCase.class;
		Constructor<ConstructorInjectionTestCase> constructor = ReflectionUtils.getDeclaredConstructor(outerClass);
		ConstructorInjectionTestCase outer = invokeConstructor(constructor, Optional.empty());

		assertNotNull(outer);
		assertEquals(ENIGMA, outer.str);

		Class<ConstructorInjectionTestCase.NestedTestCase> innerClass = ConstructorInjectionTestCase.NestedTestCase.class;
		Constructor<ConstructorInjectionTestCase.NestedTestCase> innerConstructor = ReflectionUtils.getDeclaredConstructor(
			innerClass);
		ConstructorInjectionTestCase.NestedTestCase inner = invokeConstructor(innerConstructor, Optional.of(outer));

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

	abstract void invokeMethod();

	abstract <T> T invokeConstructor(Constructor<T> constructor, Optional<Object> outerInstance);

}
