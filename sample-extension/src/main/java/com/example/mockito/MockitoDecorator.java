/*
 * Copyright 2015 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v10.html
 */

package com.example.mockito;

import static org.mockito.Mockito.*;

import java.lang.reflect.Parameter;
import java.util.HashMap;
import java.util.Map;

import org.junit.gen5.api.extension.ArgumentResolutionException;
import org.junit.gen5.api.extension.MethodArgumentResolver;
import org.junit.gen5.api.extension.TestExecutionContext;
import org.junit.gen5.commons.util.AnnotationUtils;

/**
 * @author Johannes Link
 * @author Sam Brannen
 * @since 5.0
 */
public class MockitoDecorator implements MethodArgumentResolver {

	private final Map<TestExecutionContext, Map<Class<?>, Object>> mocks = new HashMap<>();

	@Override
	public boolean supports(Parameter parameter) {
		return AnnotationUtils.findAnnotation(parameter, InjectMock.class).isPresent();
	}

	@Override
	public Object resolveArgument(Parameter parameter, TestExecutionContext testExecutionContext)
			throws ArgumentResolutionException {

		Map<Class<?>, Object> contextMocks = mocksFor(testExecutionContext);
		Class<?> mockType = parameter.getType();
		Object mock = contextMocks.get(mockType);
		if (mock == null) {
			mock = mock(mockType);
			contextMocks.put(mockType, mock);
		}
		return mock;
	}

	private Map<Class<?>, Object> mocksFor(TestExecutionContext context) {
		Map<Class<?>, Object> contextMocks = this.mocks.get(context);
		if (contextMocks == null) {
			contextMocks = new HashMap<>();
			this.mocks.put(context, contextMocks);
		}
		return contextMocks;
	}

}
