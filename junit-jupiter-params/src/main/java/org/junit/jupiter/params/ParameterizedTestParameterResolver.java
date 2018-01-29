/*
 * Copyright 2015-2018 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v20.html
 */

package org.junit.jupiter.params;

import java.lang.reflect.Executable;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.Optional;

import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.ParameterContext;
import org.junit.jupiter.api.extension.ParameterResolutionException;
import org.junit.jupiter.api.extension.ParameterResolver;
import org.junit.jupiter.params.converter.ArgumentConverter;
import org.junit.jupiter.params.converter.ConvertWith;
import org.junit.jupiter.params.converter.DefaultArgumentConverter;
import org.junit.jupiter.params.support.AnnotationConsumerInitializer;
import org.junit.platform.commons.util.AnnotationUtils;
import org.junit.platform.commons.util.ReflectionUtils;

/**
 * @since 5.0
 */
class ParameterizedTestParameterResolver implements ParameterResolver {

	private final Object[] arguments;

	ParameterizedTestParameterResolver(Object[] arguments) {
		this.arguments = arguments;
	}

	@Override
	public boolean supportsParameter(ParameterContext parameterContext, ExtensionContext extensionContext) {
		Executable declaringExecutable = parameterContext.getParameter().getDeclaringExecutable();
		Method testMethod = extensionContext.getTestMethod().orElse(null);
		return declaringExecutable.equals(testMethod) && parameterContext.getIndex() < arguments.length;
	}

	@Override
	public Object resolveParameter(ParameterContext parameterContext, ExtensionContext extensionContext)
			throws ParameterResolutionException {
		Object argument = arguments[parameterContext.getIndex()];
		Parameter parameter = parameterContext.getParameter();
		Optional<ConvertWith> annotation = AnnotationUtils.findAnnotation(parameter, ConvertWith.class);
		// @formatter:off
		ArgumentConverter argumentConverter = annotation.map(ConvertWith::value)
				.map(clazz -> (ArgumentConverter) ReflectionUtils.newInstance(clazz))
				.map(converter -> AnnotationConsumerInitializer.initialize(parameter, converter))
				.orElse(DefaultArgumentConverter.INSTANCE);
		// @formatter:on
		try {
			return argumentConverter.convert(argument, parameterContext);
		}
		catch (Exception ex) {
			throw new ParameterResolutionException("Error resolving parameter at index " + parameterContext.getIndex(),
				ex);
		}
	}
}
