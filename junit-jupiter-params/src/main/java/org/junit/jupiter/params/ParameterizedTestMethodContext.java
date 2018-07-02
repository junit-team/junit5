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

import static org.junit.jupiter.params.ParameterizedTestMethodContext.ResolverType.AGGREGATOR;
import static org.junit.jupiter.params.ParameterizedTestMethodContext.ResolverType.CONVERTER;
import static org.junit.platform.commons.util.AnnotationUtils.isAnnotated;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.extension.ParameterContext;
import org.junit.jupiter.api.extension.ParameterResolutionException;
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
 * @since 5.3
 */
class ParameterizedTestMethodContext {

	private final List<ResolverType> resolverTypes;
	private final Resolver[] resolvers;

	ParameterizedTestMethodContext(Method testMethod) {
		Parameter[] parameters = testMethod.getParameters();
		this.resolverTypes = new ArrayList<>(parameters.length);
		this.resolvers = new Resolver[parameters.length];
		for (Parameter parameter : parameters) {
			this.resolverTypes.add(isAggregator(parameter) ? AGGREGATOR : CONVERTER);
		}
	}

	private static boolean isAggregator(Parameter parameter) {
		return ArgumentsAccessor.class.isAssignableFrom(parameter.getType())
				|| isAnnotated(parameter, AggregateWith.class);
	}

	boolean hasPotentiallyValidSignature() {
		int indexOfPreviousAggregator = -1;
		for (int i = 0; i < getParameterCount(); i++) {
			if (isAggregator(i)) {
				if ((indexOfPreviousAggregator != -1) && (i != indexOfPreviousAggregator + 1)) {
					return false;
				}
				indexOfPreviousAggregator = i;
			}
		}
		return true;
	}

	int getParameterCount() {
		return resolvers.length;
	}

	boolean hasAggregator() {
		return resolverTypes.contains(AGGREGATOR);
	}

	boolean isAggregator(int parameterIndex) {
		return resolverTypes.get(parameterIndex) == AGGREGATOR;
	}

	int indexOfFirstAggregator() {
		return resolverTypes.indexOf(AGGREGATOR);
	}

	Object resolve(ParameterContext parameterContext, Object[] arguments) {
		return getResolver(parameterContext).resolve(parameterContext, arguments);
	}

	private Resolver getResolver(ParameterContext parameterContext) {
		int index = parameterContext.getIndex();
		if (resolvers[index] == null) {
			resolvers[index] = resolverTypes.get(index).createResolver(parameterContext);
		}
		return resolvers[index];
	}

	enum ResolverType {

		CONVERTER {
			@Override
			Resolver createResolver(ParameterContext parameterContext) {
				return new Converter(parameterContext);
			}
		},

		AGGREGATOR {
			@Override
			Resolver createResolver(ParameterContext parameterContext) {
				return new Aggregator(parameterContext);
			}
		};

		abstract Resolver createResolver(ParameterContext parameterContext);

	}

	interface Resolver {

		Object resolve(ParameterContext parameterContext, Object[] arguments);

		default ParameterResolutionException parameterResolutionException(String message, Exception cause,
				ParameterContext parameterContext) {
			String fullMessage = message + " at index " + parameterContext.getIndex();
			if (StringUtils.isNotBlank(cause.getMessage())) {
				fullMessage += ": " + cause.getMessage();
			}
			return new ParameterResolutionException(fullMessage, cause);
		}

	}

	static class Converter implements Resolver {

		private final ArgumentConverter argumentConverter;

		Converter(ParameterContext parameterContext) {
			try {
				// @formatter:off
				this.argumentConverter = AnnotationUtils.findAnnotation(parameterContext.getParameter(), ConvertWith.class)
						.map(ConvertWith::value)
						.map(clazz -> (ArgumentConverter) ReflectionUtils.newInstance(clazz))
						.map(converter -> AnnotationConsumerInitializer.initialize(parameterContext.getParameter(), converter))
						.orElse(DefaultArgumentConverter.INSTANCE);
                // @formatter:on
			}
			catch (Exception ex) {
				throw parameterResolutionException("Error creating ArgumentConverter", ex, parameterContext);
			}
		}

		@Override
		public Object resolve(ParameterContext parameterContext, Object[] arguments) {
			Object argument = arguments[parameterContext.getIndex()];
			try {
				return this.argumentConverter.convert(argument, parameterContext);
			}
			catch (Exception ex) {
				throw parameterResolutionException("Error converting parameter", ex, parameterContext);
			}
		}

	}

	static class Aggregator implements Resolver {

		private final ArgumentsAggregator argumentsAggregator;

		Aggregator(ParameterContext parameterContext) {
			try {
				// @formatter:off
				this.argumentsAggregator = AnnotationUtils.findAnnotation(parameterContext.getParameter(), AggregateWith.class)
						.map(AggregateWith::value)
						.map(clazz -> (ArgumentsAggregator) ReflectionSupport.newInstance(clazz))
						.orElse((accessor, context) -> accessor);
                // @formatter:on
			}
			catch (Exception ex) {
				throw parameterResolutionException("Error creating ArgumentsAggregator", ex, parameterContext);
			}
		}

		@Override
		public Object resolve(ParameterContext parameterContext, Object[] arguments) {
			ArgumentsAccessor accessor = new DefaultArgumentsAccessor(arguments);
			try {
				return this.argumentsAggregator.aggregateArguments(accessor, parameterContext);
			}
			catch (Exception ex) {
				throw parameterResolutionException("Error aggregating arguments for parameter", ex, parameterContext);
			}
		}

	}

}
