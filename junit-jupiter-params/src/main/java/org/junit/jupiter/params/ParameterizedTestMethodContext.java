/*
 * Copyright 2015-2025 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * https://www.eclipse.org/legal/epl-v20.html
 */

package org.junit.jupiter.params;

import static org.junit.jupiter.params.ParameterizedTestMethodContext.ResolverType.AGGREGATOR;
import static org.junit.jupiter.params.ParameterizedTestMethodContext.ResolverType.CONVERTER;
import static org.junit.platform.commons.support.AnnotationSupport.findAnnotation;
import static org.junit.platform.commons.support.AnnotationSupport.isAnnotated;

import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.extension.ExtensionContext;
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
import org.junit.platform.commons.util.Preconditions;
import org.junit.platform.commons.util.StringUtils;

/**
 * Encapsulates access to the parameters of a parameterized test method and
 * caches the converters and aggregators used to resolve them.
 *
 * @since 5.3
 */
class ParameterizedTestMethodContext implements ParameterizedDeclarationContext<ParameterizedTest> {

	final Method method;
	final ParameterizedTest annotation;

	private final Parameter[] parameters;
	private final Resolver[] resolvers;
	private final List<ResolverType> resolverTypes;

	ParameterizedTestMethodContext(Method method, ParameterizedTest annotation) {
		this.method = Preconditions.notNull(method, "method must not be null");
		this.annotation = Preconditions.notNull(annotation, "annotation must not be null");
		this.parameters = method.getParameters();
		this.resolvers = new Resolver[this.parameters.length];
		this.resolverTypes = new ArrayList<>(this.parameters.length);
		for (Parameter parameter : this.parameters) {
			this.resolverTypes.add(isAggregator(parameter) ? AGGREGATOR : CONVERTER);
		}
	}

	@Override
	public ParameterizedTest getAnnotation() {
		return annotation;
	}

	@Override
	public AnnotatedElement getAnnotatedElement() {
		return method;
	}

	@Override
	public String getDisplayNamePattern() {
		return annotation.name();
	}

	@Override
	public boolean isAllowingZeroInvocations() {
		return annotation.allowZeroInvocations();
	}

	@Override
	public ArgumentCountValidationMode getArgumentCountValidationMode() {
		return annotation.argumentCountValidation();
	}

	/**
	 * Determine if the supplied {@link Parameter} is an aggregator (i.e., of
	 * type {@link ArgumentsAccessor} or annotated with {@link AggregateWith}).
	 *
	 * @return {@code true} if the parameter is an aggregator
	 */
	private static boolean isAggregator(Parameter parameter) {
		return ArgumentsAccessor.class.isAssignableFrom(parameter.getType())
				|| isAnnotated(parameter, AggregateWith.class);
	}

	/**
	 * Determine if the {@link Method} represented by this context has a
	 * <em>potentially</em> valid signature (i.e., formal parameter
	 * declarations) with regard to aggregators.
	 *
	 * <p>This method takes a best-effort approach at enforcing the following
	 * policy for parameterized test methods that accept aggregators as arguments.
	 *
	 * <ol>
	 * <li>zero or more <em>indexed arguments</em> come first.</li>
	 * <li>zero or more <em>aggregators</em> come next.</li>
	 * <li>zero or more arguments supplied by other {@code ParameterResolver}
	 * implementations come last.</li>
	 * </ol>
	 *
	 * @return {@code true} if the method has a potentially valid signature
	 */
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

	/**
	 * Get the number of parameters of the {@link Method} represented by this
	 * context.
	 */
	@Override
	public int getParameterCount() {
		return parameters.length;
	}

	/**
	 * Get the name of the {@link Parameter} with the supplied index, if
	 * it is present and declared before the aggregators.
	 *
	 * @return an {@code Optional} containing the name of the parameter
	 */
	@Override
	public Optional<String> getParameterName(int parameterIndex) {
		if (parameterIndex >= getParameterCount()) {
			return Optional.empty();
		}
		Parameter parameter = this.parameters[parameterIndex];
		if (!parameter.isNamePresent()) {
			return Optional.empty();
		}
		if (hasAggregator() && parameterIndex >= indexOfFirstAggregator()) {
			return Optional.empty();
		}
		return Optional.of(parameter.getName());
	}

	/**
	 * Determine if the {@link Method} represented by this context declares at
	 * least one {@link Parameter} that is an
	 * {@linkplain #isAggregator aggregator}.
	 *
	 * @return {@code true} if the method has an aggregator
	 */
	@Override
	public boolean hasAggregator() {
		return resolverTypes.contains(AGGREGATOR);
	}

	/**
	 * Determine if the {@link Parameter} with the supplied index is an
	 * aggregator (i.e., of type {@link ArgumentsAccessor} or annotated with
	 * {@link AggregateWith}).
	 *
	 * @return {@code true} if the parameter is an aggregator
	 */
	@Override
	public boolean isAggregator(int parameterIndex) {
		return resolverTypes.get(parameterIndex) == AGGREGATOR;
	}

	/**
	 * Find the index of the first {@linkplain #isAggregator aggregator}
	 * {@link Parameter} in the {@link Method} represented by this context.
	 *
	 * @return the index of the first aggregator, or {@code -1} if not found
	 */
	@Override
	public int indexOfFirstAggregator() {
		return resolverTypes.indexOf(AGGREGATOR);
	}

	/**
	 * Resolve the parameter for the supplied context using the supplied
	 * arguments.
	 */
	Object resolve(ParameterContext parameterContext, ExtensionContext extensionContext, Object[] arguments,
			int invocationIndex) {
		return getResolver(parameterContext, extensionContext).resolve(parameterContext, arguments, invocationIndex);
	}

	private Resolver getResolver(ParameterContext parameterContext, ExtensionContext extensionContext) {
		int index = parameterContext.getIndex();
		if (resolvers[index] == null) {
			resolvers[index] = resolverTypes.get(index).createResolver(index, parameterContext.getParameter(),
				extensionContext);
		}
		return resolvers[index];
	}

	enum ResolverType {

		CONVERTER {
			@Override
			Resolver createResolver(int index, AnnotatedElement annotatedElement, ExtensionContext extensionContext) {
				try { // @formatter:off
					return findAnnotation(annotatedElement, ConvertWith.class)
							.map(ConvertWith::value)
							.map(clazz -> ParameterizedTestSpiInstantiator.instantiate(ArgumentConverter.class, clazz, extensionContext))
							.map(converter -> AnnotationConsumerInitializer.initialize(annotatedElement, converter))
							.map(Converter::new)
							.orElse(Converter.DEFAULT);
				} // @formatter:on
				catch (Exception ex) {
					throw parameterResolutionException("Error creating ArgumentConverter", ex, index);
				}
			}
		},

		AGGREGATOR {
			@Override
			Resolver createResolver(int index, AnnotatedElement annotatedElement, ExtensionContext extensionContext) {
				try { // @formatter:off
					return findAnnotation(annotatedElement, AggregateWith.class)
							.map(AggregateWith::value)
							.map(clazz -> ParameterizedTestSpiInstantiator.instantiate(ArgumentsAggregator.class, clazz, extensionContext))
							.map(Aggregator::new)
							.orElse(Aggregator.DEFAULT);
				} // @formatter:on
				catch (Exception ex) {
					throw parameterResolutionException("Error creating ArgumentsAggregator", ex, index);
				}
			}
		};

		abstract Resolver createResolver(int index, AnnotatedElement annotatedElement,
				ExtensionContext extensionContext);

	}

	interface Resolver {

		Object resolve(ParameterContext parameterContext, Object[] arguments, int invocationIndex);

	}

	static class Converter implements Resolver {

		private static final Converter DEFAULT = new Converter(DefaultArgumentConverter.INSTANCE);

		private final ArgumentConverter argumentConverter;

		Converter(ArgumentConverter argumentConverter) {
			this.argumentConverter = argumentConverter;
		}

		@Override
		public Object resolve(ParameterContext parameterContext, Object[] arguments, int invocationIndex) {
			Object argument = arguments[parameterContext.getIndex()];
			try {
				return this.argumentConverter.convert(argument, parameterContext);
			}
			catch (Exception ex) {
				throw parameterResolutionException("Error converting parameter", ex, parameterContext.getIndex());
			}
		}

	}

	static class Aggregator implements Resolver {

		private static final Aggregator DEFAULT = new Aggregator((accessor, context) -> accessor);

		private final ArgumentsAggregator argumentsAggregator;

		Aggregator(ArgumentsAggregator argumentsAggregator) {
			this.argumentsAggregator = argumentsAggregator;
		}

		@Override
		public Object resolve(ParameterContext parameterContext, Object[] arguments, int invocationIndex) {
			ArgumentsAccessor accessor = new DefaultArgumentsAccessor(parameterContext, invocationIndex, arguments);
			try {
				return this.argumentsAggregator.aggregateArguments(accessor, parameterContext);
			}
			catch (Exception ex) {
				throw parameterResolutionException("Error aggregating arguments for parameter", ex,
					parameterContext.getIndex());
			}
		}

	}

	private static ParameterResolutionException parameterResolutionException(String message, Exception cause,
			int index) {
		String fullMessage = message + " at index " + index;
		if (StringUtils.isNotBlank(cause.getMessage())) {
			fullMessage += ": " + cause.getMessage();
		}
		return new ParameterResolutionException(fullMessage, cause);
	}

}
