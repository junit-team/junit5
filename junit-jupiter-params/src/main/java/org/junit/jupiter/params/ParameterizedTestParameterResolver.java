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

import static org.junit.jupiter.params.aggregator.AggregationUtils.indexOfFirstAggregator;
import static org.junit.jupiter.params.aggregator.AggregationUtils.isAggregator;

import java.lang.reflect.Executable;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.Optional;

import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.ParameterContext;
import org.junit.jupiter.api.extension.ParameterResolutionException;
import org.junit.jupiter.api.extension.ParameterResolver;
import org.junit.jupiter.params.aggregator.AggregateWith;
import org.junit.jupiter.params.aggregator.ArgumentsAccessor;
import org.junit.jupiter.params.aggregator.ArgumentsAggregator;
import org.junit.jupiter.params.aggregator.DefaultArgumentsAccessor;
import org.junit.jupiter.params.converter.ArgumentConverter;
import org.junit.jupiter.params.converter.ConvertWith;
import org.junit.jupiter.params.converter.DefaultArgumentConverter;
import org.junit.jupiter.params.support.AnnotationConsumerInitializer;
import org.junit.platform.commons.support.ReflectionSupport;
import org.junit.platform.commons.util.AnnotationUtils;
import org.junit.platform.commons.util.ReflectionUtils;
import org.junit.platform.commons.util.StringUtils;

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
		Executable declaringExecutable = parameterContext.getDeclaringExecutable();
		Method testMethod = extensionContext.getTestMethod().orElse(null);

		// Not a @ParameterizedTest method?
		if (!declaringExecutable.equals(testMethod)) {
			return false;
		}

		// Current parameter is an aggregator?
		if (isAggregator(parameterContext.getParameter())) {
			return true;
		}

		// Ensure that the current parameter is declared before aggregators.
		// Otherwise, a different ParameterResolver should handle it.
		int indexOfFirstAggregator = indexOfFirstAggregator(testMethod);
		if (indexOfFirstAggregator != -1) {
			return parameterContext.getIndex() < indexOfFirstAggregator;
		}

		// Else fallback to behavior for parameterized test methods without aggregators.
		return parameterContext.getIndex() < this.arguments.length;
	}

	@Override
	public Object resolveParameter(ParameterContext parameterContext, ExtensionContext extensionContext)
			throws ParameterResolutionException {

		return isAggregator(parameterContext.getParameter()) ? aggregate(parameterContext) : convert(parameterContext);
	}

	private Object convert(ParameterContext parameterContext) {
		Parameter parameter = parameterContext.getParameter();
		Object argument = this.arguments[parameterContext.getIndex()];
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
			throw parameterResolutionException("Error converting parameter", ex, parameterContext);
		}
	}

	private Object aggregate(ParameterContext parameterContext) {
		Parameter parameter = parameterContext.getParameter();
		Optional<AggregateWith> annotation = AnnotationUtils.findAnnotation(parameter, AggregateWith.class);
		ArgumentsAccessor accessor = new DefaultArgumentsAccessor(this.arguments);
		if (annotation.isPresent()) {
			return aggregateSafely(annotation.get().value(), accessor, parameterContext);
		}
		return accessor;
	}

	private Object aggregateSafely(Class<? extends ArgumentsAggregator> clazz, ArgumentsAccessor accessor,
			ParameterContext parameterContext) {
		try {
			ArgumentsAggregator aggregator = ReflectionSupport.newInstance(clazz);
			return aggregator.aggregateArguments(accessor, parameterContext);
		}
		catch (Exception ex) {
			throw parameterResolutionException("Error aggregating arguments for parameter", ex, parameterContext);
		}
	}

	private ParameterResolutionException parameterResolutionException(String message, Exception cause,
			ParameterContext context) {
		String fullMessage = message + " at index " + context.getIndex();
		if (StringUtils.isNotBlank(cause.getMessage())) {
			fullMessage += ": " + cause.getMessage();
		}
		return new ParameterResolutionException(fullMessage, cause);
	}

}
