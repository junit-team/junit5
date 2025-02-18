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

import static java.util.Comparator.comparing;
import static java.util.stream.Collectors.toMap;
import static org.junit.jupiter.params.ResolverFacade.ResolverType.AGGREGATOR;
import static org.junit.jupiter.params.ResolverFacade.ResolverType.CONVERTER;
import static org.junit.platform.commons.support.AnnotationSupport.findAnnotation;
import static org.junit.platform.commons.support.AnnotationSupport.isAnnotated;
import static org.junit.platform.commons.support.HierarchyTraversalMode.BOTTOM_UP;
import static org.junit.platform.commons.support.ReflectionSupport.findFields;

import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Executable;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NavigableMap;
import java.util.Optional;
import java.util.TreeMap;
import java.util.function.Function;

import org.junit.jupiter.api.Named;
import org.junit.jupiter.api.extension.AnnotatedElementContext;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.ParameterContext;
import org.junit.jupiter.api.extension.ParameterResolutionException;
import org.junit.jupiter.params.aggregator.AggregateWith;
import org.junit.jupiter.params.aggregator.ArgumentsAccessor;
import org.junit.jupiter.params.aggregator.ArgumentsAggregationException;
import org.junit.jupiter.params.aggregator.ArgumentsAggregator;
import org.junit.jupiter.params.aggregator.DefaultArgumentsAccessor;
import org.junit.jupiter.params.aggregator.SimpleArgumentsAggregator;
import org.junit.jupiter.params.converter.ArgumentConverter;
import org.junit.jupiter.params.converter.ConvertWith;
import org.junit.jupiter.params.converter.DefaultArgumentConverter;
import org.junit.jupiter.params.provider.ParameterDeclaration;
import org.junit.jupiter.params.provider.ParameterDeclarations;
import org.junit.jupiter.params.support.AnnotationConsumerInitializer;
import org.junit.jupiter.params.support.FieldContext;
import org.junit.platform.commons.JUnitException;
import org.junit.platform.commons.support.ReflectionSupport;
import org.junit.platform.commons.util.Preconditions;
import org.junit.platform.commons.util.ReflectionUtils;
import org.junit.platform.commons.util.StringUtils;

class ResolverFacade {

	static ResolverFacade create(Class<?> clazz) {
		List<Field> fields = findFields(clazz, it -> isAnnotated(it, Parameter.class), BOTTOM_UP);
		if (fields.isEmpty()) {
			return create(ReflectionUtils.getDeclaredConstructor(clazz));
		}
		Map<Integer, ParameterDeclaration> declarations = new TreeMap<>();
		int maxIndex = -1;
		for (Field field : fields) {
			// TODO #878 Test all preconditions
			Preconditions.condition(!ReflectionUtils.isFinal(field), () -> "Field must not be final: " + field);
			ReflectionSupport.makeAccessible(field);
			// TODO #878 Test that composed annotations are supported
			Parameter annotation = findAnnotation(field, Parameter.class) //
					.orElseThrow(() -> new JUnitException("No @Parameter annotation found"));
			int index = annotation.value();
			Preconditions.condition(index >= 0,
				() -> String.format("Index must be greater than or equal to zero in %s on %s", annotation, field));
			Preconditions.condition(!declarations.containsKey(index),
				() -> String.format("Duplicate index %d declared on %s and %s", index,
					declarations.get(index).getAnnotatedElement(), field));
			declarations.put(index, new FieldParameterDeclaration(field, annotation));
			maxIndex = Math.max(maxIndex, index);
		}
		return new ResolverFacade(clazz, maxIndex + 1, declarations.values());
	}

	static ResolverFacade create(Executable executable) {
		java.lang.reflect.Parameter[] parameters = executable.getParameters();
		List<ParameterDeclaration> declarations = new ArrayList<>(parameters.length);
		for (int index = 0; index < parameters.length; index++) {
			// TODO #878 Consider index of first aggregator here?
			declarations.add(new ExecutableParameterDeclaration(parameters[index], index));
		}
		return new ResolverFacade(executable, declarations.size(), declarations);
	}

	private final AnnotatedElement sourceElement;
	private final List<ParameterDeclaration> parameterDeclarations;
	private final Resolver[] resolvers;
	private final Map<Integer, ResolverType> resolverTypes;

	private ResolverFacade(AnnotatedElement sourceElement, int numParameters,
			Collection<? extends ParameterDeclaration> declarations) {
		this.sourceElement = sourceElement;
		// TODO #878 Split aggregators from regular parameters (converters)?
		this.parameterDeclarations = new ArrayList<>(declarations);
		this.parameterDeclarations.sort(comparing(ParameterDeclaration::getIndex));
		this.resolvers = new Resolver[numParameters];
		this.resolverTypes = new HashMap<>(numParameters);
		for (ParameterDeclaration parameter : declarations) {
			this.resolverTypes.put(parameter.getIndex(), isAggregator(parameter) ? AGGREGATOR : CONVERTER);
		}
	}

	public List<ParameterDeclaration> getParameterDeclarations() {
		return this.parameterDeclarations;
	}

	public ParameterDeclarations getNonAggregatorParameterDeclarations() {
		NavigableMap<Integer, ParameterDeclaration> declarationsByIndex = parameterDeclarations.stream() //
				.filter(parameter -> !isAggregator(parameter.getIndex())) //
				.collect(toMap(ParameterDeclaration::getIndex, Function.identity(), (a, b) -> a, TreeMap::new));
		return new DefaultParameterDeclarations(sourceElement, declarationsByIndex);
	}

	int getParameterCount() {
		return this.resolvers.length;
	}

	/**
	 * Determine if the {@link Method} represented by this context declares at
	 * least one {@link Parameter} that is an
	 * {@linkplain #isAggregator aggregator}.
	 *
	 * @return {@code true} if the method has an aggregator
	 */
	boolean hasAggregator() {
		return this.resolverTypes.containsValue(AGGREGATOR);
	}

	/**
	 * Determine if the {@link Parameter} with the supplied index is an
	 * aggregator (i.e., of type {@link ArgumentsAccessor} or annotated with
	 * {@link AggregateWith}).
	 *
	 * @return {@code true} if the parameter is an aggregator
	 */
	boolean isAggregator(int parameterIndex) {
		return this.resolverTypes.get(parameterIndex) == AGGREGATOR;
	}

	/**
	 * Determine if the supplied {@link Parameter} is an aggregator (i.e., of
	 * type {@link ArgumentsAccessor} or annotated with {@link AggregateWith}).
	 *
	 * @return {@code true} if the parameter is an aggregator
	 */
	private boolean isAggregator(ParameterDeclaration parameter) {
		return ArgumentsAccessor.class.isAssignableFrom(parameter.getType())
				|| isAnnotated(parameter.getAnnotatedElement(), AggregateWith.class);
	}

	/**
	 * Find the index of the first {@linkplain #isAggregator aggregator}
	 * {@link Parameter} in the {@link Method} represented by this context.
	 *
	 * @return the index of the first aggregator, or {@code -1} if not found
	 */
	int indexOfFirstAggregator() {
		return this.resolverTypes.entrySet().stream() //
				.filter(e -> e.getValue() == AGGREGATOR) //
				.mapToInt(Map.Entry::getKey) //
				.min() //
				.orElse(-1);
	}

	/**
	 * Resolve the parameter for the supplied context using the supplied
	 * arguments.
	 */
	Object resolve(ParameterContext parameterContext, ExtensionContext extensionContext, Object[] arguments,
			int invocationIndex) {
		return getResolver(extensionContext, parameterContext.getIndex(), parameterContext.getParameter()) //
				.resolve(parameterContext, extractPayloads(arguments), invocationIndex);
	}

	/**
	 * Resolve the parameter for the supplied context using the supplied
	 * arguments.
	 */
	Object resolve(FieldContext fieldContext, ExtensionContext extensionContext, Object[] arguments,
			int invocationIndex) {
		return getResolver(extensionContext, fieldContext.getParameterIndex(), fieldContext.getField()) //
				.resolve(fieldContext, extractPayloads(arguments), invocationIndex);
	}

	private Resolver getResolver(ExtensionContext extensionContext, int index, AnnotatedElement annotatedElement) {
		if (this.resolvers[index] == null) {
			this.resolvers[index] = this.resolverTypes.get(index).createResolver(index, annotatedElement,
				extensionContext);
		}
		return this.resolvers[index];
	}

	private Object[] extractPayloads(Object[] arguments) {
		return Arrays.stream(arguments) //
				.map(argument -> {
					if (argument instanceof Named) {
						return ((Named<?>) argument).getPayload();
					}
					return argument;
				}) //
				.toArray();
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

		Object resolve(FieldContext fieldContext, Object[] arguments, int invocationIndex);

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

		@Override
		public Object resolve(FieldContext fieldContext, Object[] arguments, int invocationIndex) {
			Object argument = arguments[fieldContext.getParameterIndex()];
			try {
				return this.argumentConverter.convert(argument, fieldContext);
			}
			catch (Exception ex) {
				throw parameterResolutionException("Error converting parameter", ex, fieldContext.getParameterIndex());
			}
		}
	}

	static class Aggregator implements Resolver {

		private static final Aggregator DEFAULT = new Aggregator(new SimpleArgumentsAggregator() {
			@Override
			protected Object aggregateArguments(ArgumentsAccessor accessor, Class<?> targetType,
					AnnotatedElementContext context, int parameterIndex) throws ArgumentsAggregationException {
				return accessor;
			}
		});

		private final ArgumentsAggregator argumentsAggregator;

		Aggregator(ArgumentsAggregator argumentsAggregator) {
			this.argumentsAggregator = argumentsAggregator;
		}

		@Override
		public Object resolve(ParameterContext parameterContext, Object[] arguments, int invocationIndex) {
			ArgumentsAccessor accessor = DefaultArgumentsAccessor.create(parameterContext, invocationIndex, arguments);
			try {
				return this.argumentsAggregator.aggregateArguments(accessor, parameterContext);
			}
			catch (Exception ex) {
				throw parameterResolutionException("Error aggregating arguments for parameter", ex,
					parameterContext.getIndex());
			}
		}

		@Override
		public Object resolve(FieldContext fieldContext, Object[] arguments, int invocationIndex) {
			ArgumentsAccessor accessor = DefaultArgumentsAccessor.create(fieldContext, invocationIndex, arguments);
			try {
				return this.argumentsAggregator.aggregateArguments(accessor, fieldContext);
			}
			catch (Exception ex) {
				throw parameterResolutionException("Error aggregating arguments for parameter", ex,
					fieldContext.getParameterIndex());
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

	private static class DefaultParameterDeclarations implements ParameterDeclarations {

		private final AnnotatedElement sourceElement;
		private final NavigableMap<Integer, ParameterDeclaration> declarationsByIndex;

		DefaultParameterDeclarations(AnnotatedElement sourceElement,
				NavigableMap<Integer, ParameterDeclaration> declarationsByIndex) {
			this.sourceElement = sourceElement;
			this.declarationsByIndex = declarationsByIndex;
		}

		@Override
		public AnnotatedElement getSourceElement() {
			return sourceElement;
		}

		@Override
		public Optional<ParameterDeclaration> getFirst() {
			return declarationsByIndex.isEmpty() ? Optional.empty()
					: Optional.of(declarationsByIndex.firstEntry().getValue());
		}

		@Override
		public int getCount() {
			return declarationsByIndex.isEmpty() ? 0 : declarationsByIndex.lastKey() + 1;
		}

		@Override
		public List<ParameterDeclaration> getAll() {
			return Collections.unmodifiableList(new ArrayList<>(declarationsByIndex.values()));
		}

		@Override
		public Optional<ParameterDeclaration> get(int index) {
			return Optional.ofNullable(declarationsByIndex.get(index));
		}
	}

}
