/*
 * Copyright 2015-2019 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * https://www.eclipse.org/legal/epl-v20.html
 */

package org.junit.jupiter.api.extension.support;

import static java.util.Arrays.asList;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.ParameterContext;
import org.junit.jupiter.api.extension.ParameterResolutionException;
import org.junit.jupiter.api.extension.ParameterResolver;
import org.junit.platform.commons.util.ReflectionUtils;
import org.mockito.Mockito;

class TypeBasedParameterResolverTest {

	private ParameterResolver basicTypeParameterResolver = new BasicTypeParameterResolver();
	private ParameterResolver subClassedBasicTypeParameterResolver = new SubClassedBasicTypeParameterResolver();
	private ParameterResolver parametrizedTypeParameterResolver = new ParametrizedTypeParameterResolver();

	@Test
	void supportsParameterForBasicTypes() {
		Parameter parameter1 = findParameterOfMethod("methodWithBasicTypeParameter", String.class);
		assertTrue(basicTypeParameterResolver.supportsParameter(parameterContext(parameter1), null));
		assertTrue(subClassedBasicTypeParameterResolver.supportsParameter(parameterContext(parameter1), null));

		Parameter parameter2 = findParameterOfMethod("methodWithObjectParameter", Object.class);
		assertFalse(basicTypeParameterResolver.supportsParameter(parameterContext(parameter2), null));
	}

	@Test
	void supportsParameterForParametrizedTypes() {
		Parameter parameter1 = findParameterOfMethod("methodWithParametrizedTypeParameter", Map.class);
		assertTrue(parametrizedTypeParameterResolver.supportsParameter(parameterContext(parameter1), null));

		Parameter parameter3 = findParameterOfMethod("methodWithAnotherParametrizedTypeParameter", Map.class);
		assertFalse(parametrizedTypeParameterResolver.supportsParameter(parameterContext(parameter3), null));
	}

	@Test
	void resolve() {
		ExtensionContext extensionContext = extensionContext();
		ParameterContext parameterContext = parameterContext(
			findParameterOfMethod("methodWithBasicTypeParameter", String.class));
		assertEquals("Displaying TestAnnotation",
			basicTypeParameterResolver.resolveParameter(parameterContext, extensionContext));

		Parameter parameter2 = findParameterOfMethod("methodWithParametrizedTypeParameter", Map.class);
		assertEquals(Map.of("ids", asList(1, 42)),
			parametrizedTypeParameterResolver.resolveParameter(parameterContext(parameter2), extensionContext));
	}

	private static ParameterContext parameterContext(Parameter parameter) {
		ParameterContext parameterContext = Mockito.mock(ParameterContext.class);
		when(parameterContext.getParameter()).thenReturn(parameter);
		return parameterContext;
	}

	private static ExtensionContext extensionContext() {
		ExtensionContext extensionContext = Mockito.mock(ExtensionContext.class);
		when(extensionContext.getDisplayName()).thenReturn("Displaying");
		return extensionContext;
	}

	private Parameter findParameterOfMethod(String methodName, Class<?>... parameterTypes) {
		Method method = ReflectionUtils.findMethod(Sample.class, methodName, parameterTypes).get();
		return method.getParameters()[0];
	}

	class BasicTypeParameterResolver extends TypeBasedParameterResolver<String> {

		@Override
		public String resolveParameter(ParameterContext parameterContext, ExtensionContext extensionContext)
				throws ParameterResolutionException {
			Class parameterAnnotation = parameterContext.getParameter().getAnnotations()[0].annotationType();
			return String.format("%s %s", extensionContext.getDisplayName(), parameterAnnotation.getSimpleName());
		}
	}

	class SubClassedBasicTypeParameterResolver extends BasicTypeParameterResolver {
	}

	class ParametrizedTypeParameterResolver extends TypeBasedParameterResolver<Map<String, List<Integer>>> {
		@Override
		public Map<String, List<Integer>> resolveParameter(ParameterContext parameterContext,
				ExtensionContext extensionContext) throws ParameterResolutionException {
			return Map.of("ids", asList(1, 42));
		}
	}

	@Retention(RetentionPolicy.RUNTIME)
	@Target(ElementType.PARAMETER)
	@interface TestAnnotation {
	}

	class Sample {
		void methodWithBasicTypeParameter(@TestAnnotation String string) {
		}

		void methodWithObjectParameter(Object nothing) {
		}

		void methodWithParametrizedTypeParameter(Map<String, List<Integer>> map) {
		}

		void methodWithAnotherParametrizedTypeParameter(Map<String, List<Object>> nothing) {
		}
	}
}
