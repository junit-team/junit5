/*
 * Copyright 2015-2016 the original author or authors.
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
import org.junit.gen5.api.extension.ExtensionContext;
import org.junit.gen5.api.extension.InstancePostProcessor;
import org.junit.gen5.api.extension.MethodContext;
import org.junit.gen5.api.extension.MethodParameterResolver;
import org.junit.gen5.api.extension.ParameterResolutionException;
import org.junit.gen5.api.extension.TestExtensionContext;
import org.junit.gen5.commons.util.AnnotationUtils;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

/**
 * {@code MockitoExtension} showcases the {@link InstancePostProcessor}
 * and {@link MethodParameterResolver} extension points of JUnit 5 by
 * providing dependency injection support at the field level via Mockito's
 * {@link Mock @Mock} annotation and at the method level via our demo
 * {@link InjectMock @InjectMock} annotation.
 *
 * @since 5.0
 */
public class MockitoExtension implements InstancePostProcessor, MethodParameterResolver {

	private final ContextScope<Class<?>, Object> mocksInScope;

	public MockitoExtension() {
		mocksInScope = new ContextScope<Class<?>, Object>(type -> mock(type), ContextScope.Inheritance.Yes);
	}

	@Override
	public void postProcessTestInstance(TestExtensionContext context) {
		MockitoAnnotations.initMocks(context.getTestInstance());
	}

	@Override
	public boolean supports(Parameter parameter, MethodContext methodContext, ExtensionContext extensionContext) {
		return AnnotationUtils.isAnnotated(parameter, InjectMock.class);
	}

	@Override
	public Object resolve(Parameter parameter, MethodContext methodContext, ExtensionContext extensionContext)
			throws ParameterResolutionException {

		Class<?> mockType = parameter.getType();
		return mocksInScope.get(extensionContext, mockType);
	}

}
