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

import static java.util.Collections.unmodifiableList;
import static org.junit.jupiter.params.ResolverFacade.ResolverType.AGGREGATOR;
import static org.junit.jupiter.params.ResolverFacade.ResolverType.CONVERTER;
import static org.junit.platform.commons.support.AnnotationSupport.findAnnotation;
import static org.junit.platform.commons.support.AnnotationSupport.isAnnotated;
import static org.junit.platform.commons.support.HierarchyTraversalMode.BOTTOM_UP;
import static org.junit.platform.commons.support.ReflectionSupport.findFields;
import static org.junit.platform.commons.util.ReflectionUtils.isInnerClass;

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Constructor;
import java.lang.reflect.Executable;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.NavigableMap;
import java.util.Optional;
import java.util.Set;
import java.util.TreeMap;
import java.util.stream.Stream;

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

	static ResolverFacade create(Class<?> clazz, ParameterizedContainer containerAnnotation) {
		List<Field> fields = findFields(clazz, it -> isAnnotated(it, Parameter.class), BOTTOM_UP);
		if (fields.isEmpty()) {
			return create(ReflectionUtils.getDeclaredConstructor(clazz), containerAnnotation);
		}
		NavigableMap<Integer, ParameterDeclaration> regularParameters = new TreeMap<>();
		Set<ParameterDeclaration> aggregatorParameters = new LinkedHashSet<>();
		for (Field field : fields) {
			// TODO #878 Test all preconditions
			Preconditions.condition(!ReflectionUtils.isFinal(field), () -> "Field must not be final: " + field);
			ReflectionSupport.makeAccessible(field);

			// TODO #878 Test that composed annotations are supported
			Parameter annotation = findAnnotation(field, Parameter.class) //
					.orElseThrow(() -> new JUnitException("No @Parameter annotation found"));
			int index = annotation.value();

			FieldParameterDeclaration declaration = new FieldParameterDeclaration(field, annotation);
			if (isAggregator(declaration)) {
				Preconditions.condition(index == -1,
					() -> String.format("Index must not be declared in %s on aggregator field %s", annotation, field));
				aggregatorParameters.add(declaration);
			}
			else {
				if (fields.size() == 1 && index == -1) {
					index = 0;
					declaration = new FieldParameterDeclaration(field, annotation, 0);
				}
				else {
					Preconditions.condition(index >= 0,
						() -> String.format("Index must be greater than or equal to zero in %s on %s", annotation,
							field));
					// TODO #878 Test with duplicate `@Parameter(index)` annotations
					// TODO #878 Test with `@Parameter(0)`, and `@Parameter(2)`, but w/o `@Parameter(1)` annotations
					Preconditions.condition(!regularParameters.containsKey(index),
						() -> String.format("Duplicate index %d declared on %s and %s", annotation.value(),
							regularParameters.get(annotation.value()).getAnnotatedElement(), field));
				}
				regularParameters.put(index, declaration);
			}
		}
		return new ResolverFacade(clazz, regularParameters, aggregatorParameters, 0);
	}

	static ResolverFacade create(Constructor<?> constructor, ParameterizedContainer annotation) {
		// Inner classes get the outer instance as first parameter
		return create(constructor, annotation, isInnerClass(constructor.getDeclaringClass()) ? 1 : 0);
	}

	static ResolverFacade create(Method method, ParameterizedTest annotation) {
		return create(method, annotation, 0);
	}

	/**
	 * Create a new {@link ResolverFacade} for the supplied {@link Executable}.
	 *
	 * <p>This method takes a best-effort approach at enforcing the following
	 * policy for parameterized container constructors and parameterized test
	 * methods that accept aggregators as arguments.
	 * <ol>
	 * <li>zero or more <em>indexed arguments</em> come first.</li>
	 * <li>zero or more <em>aggregators</em> come next.</li>
	 * <li>zero or more arguments supplied by other {@code ParameterResolver}
	 * implementations come last.</li>
	 * </ol>
	 */
	private static ResolverFacade create(Executable executable, Annotation annotation, int indexOffset) {
		NavigableMap<Integer, ParameterDeclaration> regularParameters = new TreeMap<>();
		NavigableMap<Integer, ParameterDeclaration> aggregatorParameters = new TreeMap<>();
		java.lang.reflect.Parameter[] parameters = executable.getParameters();
		for (int index = indexOffset; index < parameters.length; index++) {
			ParameterDeclaration declaration = new ExecutableParameterDeclaration(parameters[index],
				index - indexOffset);
			if (isAggregator(declaration)) {
				Preconditions.condition(
					aggregatorParameters.isEmpty() || aggregatorParameters.lastKey() == declaration.getIndex() - 1,
					() -> String.format(
						"@%s %s declares formal parameters in an invalid order: "
								+ "argument aggregators must be declared after any indexed arguments "
								+ "and before any arguments resolved by another ParameterResolver.",
						annotation.annotationType().getSimpleName(),
						DefaultParameterDeclarations.describe(executable)));
				aggregatorParameters.put(declaration.getIndex(), declaration);
			}
			else if (aggregatorParameters.isEmpty()) {
				regularParameters.put(declaration.getIndex(), declaration);
			}
		}
		return new ResolverFacade(executable, regularParameters, new LinkedHashSet<>(aggregatorParameters.values()),
			indexOffset);
	}

	private final int parameterIndexOffset;
	private final Map<ParameterDeclaration, Resolver> resolvers;
	private final DefaultParameterDeclarations regularParameterDeclarations;
	private final Set<ParameterDeclaration> aggregatorParameters;

	private ResolverFacade(AnnotatedElement sourceElement,
			NavigableMap<Integer, ParameterDeclaration> regularParameters,
			Set<ParameterDeclaration> aggregatorParameters, int parameterIndexOffset) {
		this.aggregatorParameters = aggregatorParameters;
		this.parameterIndexOffset = parameterIndexOffset;
		this.resolvers = new HashMap<>(regularParameters.size() + aggregatorParameters.size());
		this.regularParameterDeclarations = new DefaultParameterDeclarations(sourceElement, regularParameters);
	}

	Stream<ParameterDeclaration> getAllParameterDeclarations() {
		return Stream.concat(this.regularParameterDeclarations.declarationsByIndex.values().stream(),
			aggregatorParameters.stream());
	}

	ParameterDeclarations getRegularParameterDeclarations() {
		return this.regularParameterDeclarations;
	}

	boolean isSupportedParameter(ParameterContext parameterContext, EvaluatedArgumentSet arguments) {
		int index = toLogicalIndex(parameterContext);
		if (this.regularParameterDeclarations.get(index).isPresent()) {
			return index < arguments.getConsumedLength();
		}
		return !this.aggregatorParameters.isEmpty()
				&& this.aggregatorParameters.stream().anyMatch(it -> it.getIndex() == index);
	}

	/**
	 * Get the name of the parameter with the supplied index, if it is present
	 * and declared before the aggregators.
	 *
	 * @return an {@code Optional} containing the name of the parameter
	 */
	Optional<String> getParameterName(int parameterIndex) {
		return this.regularParameterDeclarations.get(parameterIndex) //
				.flatMap(ParameterDeclaration::getName);
	}

	/**
	 * Determine the number of arguments that are considered consumed by the
	 * parameter declarations in this resolver.
	 *
	 * <p>If an aggregator is present, all arguments are considered consumed.
	 * Otherwise, the consumed argument count is the minimum of the total length
	 * and the number of regular parameter declarations.
	 */
	int determineConsumedArgumentCount(int totalLength) {
		return this.aggregatorParameters.isEmpty() //
				? Math.min(totalLength, this.regularParameterDeclarations.getCount()) //
				: totalLength;
	}

	/**
	 * Resolve the parameter for the supplied context using the supplied
	 * arguments.
	 */
	Object resolve(ParameterContext parameterContext, ExtensionContext extensionContext, Object[] arguments,
			int invocationIndex) {
		int parameterIndex = toLogicalIndex(parameterContext);
		ParameterDeclaration parameterDeclaration = this.regularParameterDeclarations.get(parameterIndex) //
				.orElseGet(
					() -> this.aggregatorParameters.stream().filter(it -> it.getIndex() == parameterIndex).findFirst() //
							.orElseThrow(() -> new ParameterResolutionException(
								"Parameter index out of bounds: " + parameterIndex)));
		return getResolver(extensionContext, parameterDeclaration, parameterContext.getParameter()) //
				.resolve(parameterContext, parameterIndex, extractPayloads(arguments), invocationIndex);
	}

	/**
	 * Resolve the parameter for the supplied context using the supplied
	 * arguments.
	 */
	Object resolve(FieldParameterDeclaration parameterDeclaration, ExtensionContext extensionContext,
			Object[] arguments, int invocationIndex) {
		return getResolver(extensionContext, parameterDeclaration, parameterDeclaration.getField()) //
				.resolve(parameterDeclaration, extractPayloads(arguments), invocationIndex);
	}

	private Resolver getResolver(ExtensionContext extensionContext, ParameterDeclaration parameterDeclaration,
			AnnotatedElement annotatedElement) {
		return this.resolvers.computeIfAbsent(parameterDeclaration, __ -> {
			ResolverType resolverType = this.aggregatorParameters.contains(parameterDeclaration) ? AGGREGATOR
					: CONVERTER;
			return resolverType.createResolver(parameterDeclaration.getIndex(), annotatedElement, extensionContext);
		});
	}

	private int toLogicalIndex(ParameterContext parameterContext) {
		int index = parameterContext.getIndex() - this.parameterIndexOffset;
		Preconditions.condition(index >= 0, () -> "Parameter index must be greater than or equal to zero");
		return index;
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

	/**
	 * Determine if the supplied {@link Parameter} is an aggregator (i.e., of
	 * type {@link ArgumentsAccessor} or annotated with {@link AggregateWith}).
	 *
	 * @return {@code true} if the parameter is an aggregator
	 */
	private static boolean isAggregator(ParameterDeclaration parameter) {
		return ArgumentsAccessor.class.isAssignableFrom(parameter.getType())
				|| isAnnotated(parameter.getAnnotatedElement(), AggregateWith.class);
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

		Object resolve(ParameterContext parameterContext, int parameterIndex, Object[] arguments, int invocationIndex);

		Object resolve(FieldContext fieldContext, Object[] arguments, int invocationIndex);

	}

	static class Converter implements Resolver {

		private static final Converter DEFAULT = new Converter(DefaultArgumentConverter.INSTANCE);

		private final ArgumentConverter argumentConverter;

		Converter(ArgumentConverter argumentConverter) {
			this.argumentConverter = argumentConverter;
		}

		@Override
		public Object resolve(ParameterContext parameterContext, int parameterIndex, Object[] arguments,
				int invocationIndex) {
			Object argument = arguments[parameterIndex];
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
		public Object resolve(ParameterContext parameterContext, int parameterIndex, Object[] arguments,
				int invocationIndex) {
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
			return this.sourceElement;
		}

		@Override
		public Optional<ParameterDeclaration> getFirst() {
			return this.declarationsByIndex.isEmpty() //
					? Optional.empty() //
					: Optional.of(this.declarationsByIndex.firstEntry().getValue());
		}

		@Override
		public int getCount() {
			return this.declarationsByIndex.isEmpty() ? 0 : this.declarationsByIndex.lastKey() + 1;
		}

		@Override
		public List<ParameterDeclaration> getAll() {
			return unmodifiableList(new ArrayList<>(this.declarationsByIndex.values()));
		}

		@Override
		public Optional<ParameterDeclaration> get(int index) {
			return Optional.ofNullable(this.declarationsByIndex.get(index));
		}

		@Override
		public String getSourceElementDescription() {
			return describe(this.sourceElement);
		}

		static String describe(AnnotatedElement sourceElement) {
			if (sourceElement instanceof Method) {
				return String.format("method [%s]", ((Method) sourceElement).toGenericString());
			}
			if (sourceElement instanceof Constructor) {
				return String.format("constructor [%s]", ((Constructor<?>) sourceElement).toGenericString());
			}
			if (sourceElement instanceof Class) {
				return String.format("class [%s]", ((Class<?>) sourceElement).getName());
			}
			return sourceElement.toString();
		}
	}

}
