/*
 * Copyright 2015-2019 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * https://www.eclipse.org/legal/epl-v20.html
 */

package org.junit.jupiter.engine.extension;

import static java.util.Collections.emptyMap;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

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
	private ParameterResolver templatedTypeParameterResolver = new TemplatedTypeParameterResolver();

	@Test
	void supportsParameterForBasicTypes() {
		Parameter parameter1 = findParameterOfMethod("methodWithBasicTypeParameter", String.class);
		assertTrue(basicTypeParameterResolver.supportsParameter(parameterContext(parameter1), null));

		Parameter parameter2 = findParameterOfMethod("methodWithObjectParameter", Object.class);
		assertFalse(basicTypeParameterResolver.supportsParameter(parameterContext(parameter2), null));
	}

	@Test
	void supportsParameterForTemplatedTypes() {
		Parameter parameter1 = findParameterOfMethod("methodWithTemplatedTypeParameter", Map.class);
		assertTrue(templatedTypeParameterResolver.supportsParameter(parameterContext(parameter1), null));

		Parameter parameter2 = findParameterOfMethod("methodWithAnotherTemplatedTypeParameter", Map.class);
		assertFalse(templatedTypeParameterResolver.supportsParameter(parameterContext(parameter2), null));
	}

	private static ParameterContext parameterContext(Parameter parameter) {
		ParameterContext parameterContext = Mockito.mock(ParameterContext.class);
		when(parameterContext.getParameter()).thenReturn(parameter);
		return parameterContext;
	}

	private Parameter findParameterOfMethod(String methodName, Class<?>... parameterTypes) {
		Method method = ReflectionUtils.findMethod(Sample.class, methodName, parameterTypes).get();
		return method.getParameters()[0];
	}

	class BasicTypeParameterResolver extends TypeBasedParameterResolver<String> {

		@Override
		public Object resolveParameter(ParameterContext parameterContext, ExtensionContext extensionContext)
				throws ParameterResolutionException {
			return "test";
		}
	}

	class TemplatedTypeParameterResolver extends TypeBasedParameterResolver<Map<String, List<Integer>>> {

		@Override
		public Object resolveParameter(ParameterContext parameterContext, ExtensionContext extensionContext)
				throws ParameterResolutionException {
			return emptyMap();
		}
	}

	class Sample {
		void methodWithBasicTypeParameter(String string) {
		}

		void methodWithObjectParameter(Object nothing) {
		}

		void methodWithTemplatedTypeParameter(Map<String, List<Integer>> map) {
		}

		void methodWithAnotherTemplatedTypeParameter(Map<String, List<Object>> nothing) {
		}
	}
}
