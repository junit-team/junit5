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

import static org.mockito.Mockito.mock;

import java.lang.reflect.Parameter;

import org.junit.gen5.api.extension.ContextScope;
import org.junit.gen5.api.extension.MethodParameterResolver;
import org.junit.gen5.api.extension.ParameterResolutionException;
import org.junit.gen5.api.extension.TestExecutionContext;
import org.junit.gen5.commons.util.AnnotationUtils;

/**
 * @author Johannes Link
 * @author Sam Brannen
 * @since 5.0
 */
public class MockitoExtension implements MethodParameterResolver {

	private final ContextScope<Class<?>, Object> mocksInScope;

	public MockitoExtension() {
		mocksInScope = new ContextScope<Class<?>, Object>(type -> mock(type), ContextScope.LifeCycle.OncePerTest,
			ContextScope.Inheritance.Yes);
	}

	@Override
	public boolean supports(Parameter parameter) {
		return AnnotationUtils.isAnnotated(parameter, InjectMock.class);
	}

	@Override
	public Object resolve(Parameter parameter, TestExecutionContext testExecutionContext)
			throws ParameterResolutionException {

		Class<?> mockType = parameter.getType();
		return mocksInScope.get(testExecutionContext, mockType);
	}

}
