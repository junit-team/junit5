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
import static org.junit.platform.commons.support.AnnotationSupport.findAnnotation;
import static org.junit.platform.commons.support.AnnotationSupport.isAnnotated;
import static org.junit.platform.commons.support.ModifierSupport.isNotFinal;
import static org.junit.platform.commons.util.ReflectionUtils.isInnerClass;

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Constructor;
import java.lang.reflect.Executable;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.NavigableMap;
import java.util.Optional;
import java.util.Set;
import java.util.TreeMap;
import java.util.stream.Stream;

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
import org.junit.platform.commons.util.StringUtils;

class ResolverFacade {

	static ResolverFacade create(Class<?> clazz, List<Field> fields) {
		Preconditions.notEmpty(fields, "Fields must not be empty");
		NavigableMap<Integer, ParameterDeclaration> regularParameters = new TreeMap<>();
		Set<ParameterDeclaration> aggregatorParameters = new LinkedHashSet<>();
		for (Field field : fields) {
			Parameter annotation = findAnnotation(field, Parameter.class) //
					.orElseThrow(() -> new JUnitException("No @Parameter annotation present"));
			int index = annotation.value();

			Preconditions.condition(isNotFinal(field),
				() -> String.format("@Parameters field [%s] must not be declared as final.", field));
			ReflectionSupport.makeAccessible(field);

			FieldParameterDeclaration declaration = new FieldParameterDeclaration(field, annotation);
			if (isAggregator(declaration)) {
				Preconditions.condition(index == -1,
					() -> String.format(
						"Index must not be declared in @Parameter(%s) annotation on aggregator field [%s].",
						annotation.value(), field));
				aggregatorParameters.add(declaration);
			}
			else {
				if (fields.size() == 1 && index == -1) {
					index = 0;
					declaration = new FieldParameterDeclaration(field, annotation, 0);
				}
				else {
					// TODO #878 Test all preconditions
					Preconditions.condition(index >= 0,
						() -> String.format(
							"Index must be greater than or equal to zero in @Parameter(%s) annotation on field [%s].",
							annotation.value(), field));
					// TODO #878 Test with duplicate `@Parameter(index)` annotations
					// TODO #878 Test with `@Parameter(0)`, and `@Parameter(2)`, but w/o `@Parameter(1)` annotations
					Preconditions.condition(!regularParameters.containsKey(index),
						() -> String.format(
							"Duplicate index declared in @Parameter(%s) annotation on fields [%s] and [%s].",
							annotation.value(), regularParameters.get(annotation.value()).getAnnotatedElement(),
							field));
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
	Object resolve(ParameterContext parameterContext, ExtensionContext extensionContext, EvaluatedArgumentSet arguments,
			int invocationIndex) {
		int parameterIndex = toLogicalIndex(parameterContext);
		ParameterDeclaration parameterDeclaration = this.regularParameterDeclarations.get(parameterIndex) //
				.orElseGet(
					() -> this.aggregatorParameters.stream().filter(it -> it.getIndex() == parameterIndex).findFirst() //
							.orElseThrow(() -> new ParameterResolutionException(
								"Parameter index out of bounds: " + parameterIndex)));
		return getResolver(extensionContext, parameterDeclaration, parameterContext.getParameter()) //
				.resolve(parameterContext, parameterIndex, arguments, invocationIndex);
	}

	void resolveAndInjectFields(Object testInstance, ExtensionContext extensionContext, EvaluatedArgumentSet arguments,
			int invocationIndex) {
		if (this.regularParameterDeclarations.sourceElement.equals(extensionContext.getTestClass().orElse(null))) {
			getAllParameterDeclarations() //
					.filter(FieldParameterDeclaration.class::isInstance) //
					.map(FieldParameterDeclaration.class::cast) //
					.forEach(declaration -> setField(testInstance, declaration, extensionContext, arguments,
						invocationIndex));
		}
	}

	private Stream<ParameterDeclaration> getAllParameterDeclarations() {
		return Stream.concat(this.regularParameterDeclarations.declarationsByIndex.values().stream(),
			aggregatorParameters.stream());
	}

	private void setField(Object testInstance, FieldParameterDeclaration parameterDeclaration,
			ExtensionContext extensionContext, EvaluatedArgumentSet arguments, int invocationIndex) {
		Object argument = resolve(parameterDeclaration, extensionContext, arguments, invocationIndex);
		try {
			parameterDeclaration.getField().set(testInstance, argument);
		}
		catch (Exception e) {
			throw new JUnitException("Failed to inject parameter value into field: " + parameterDeclaration.getField(),
				e);
		}
	}

	private Object resolve(FieldParameterDeclaration parameterDeclaration, ExtensionContext extensionContext,
			EvaluatedArgumentSet arguments, int invocationIndex) {
		return getResolver(extensionContext, parameterDeclaration, parameterDeclaration.getField()) //
				.resolve(parameterDeclaration, arguments, invocationIndex);
	}

	private Resolver getResolver(ExtensionContext extensionContext, ParameterDeclaration parameterDeclaration,
			AnnotatedElement annotatedElement) {
		return this.resolvers.computeIfAbsent(parameterDeclaration,
			__ -> this.aggregatorParameters.contains(parameterDeclaration) //
					? createAggregator(parameterDeclaration.getIndex(), annotatedElement, extensionContext) //
					: createConverter(parameterDeclaration.getIndex(), annotatedElement, extensionContext));
	}

	private int toLogicalIndex(ParameterContext parameterContext) {
		int index = parameterContext.getIndex() - this.parameterIndexOffset;
		Preconditions.condition(index >= 0, () -> "Parameter index must be greater than or equal to zero");
		return index;
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

	private static Converter createConverter(int index, AnnotatedElement annotatedElement,
			ExtensionContext extensionContext) {
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

	private static Aggregator createAggregator(int index, AnnotatedElement annotatedElement,
			ExtensionContext extensionContext) {
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

	private static ParameterResolutionException parameterResolutionException(String message, Exception cause,
			int index) {
		String fullMessage = message + " at index " + index;
		if (StringUtils.isNotBlank(cause.getMessage())) {
			fullMessage += ": " + cause.getMessage();
		}
		return new ParameterResolutionException(fullMessage, cause);
	}

	private interface Resolver {

		Object resolve(ParameterContext parameterContext, int parameterIndex, EvaluatedArgumentSet arguments,
				int invocationIndex);

		Object resolve(FieldContext fieldContext, EvaluatedArgumentSet arguments, int invocationIndex);

	}

	private static class Converter implements Resolver {

		private static final Converter DEFAULT = new Converter(DefaultArgumentConverter.INSTANCE);

		private final ArgumentConverter argumentConverter;

		Converter(ArgumentConverter argumentConverter) {
			this.argumentConverter = argumentConverter;
		}

		@Override
		public Object resolve(ParameterContext parameterContext, int parameterIndex, EvaluatedArgumentSet arguments,
				int invocationIndex) {
			Object argument = arguments.getConsumedPayload(parameterIndex);
			try {
				return this.argumentConverter.convert(argument, parameterContext);
			}
			catch (Exception ex) {
				throw parameterResolutionException("Error converting parameter", ex, parameterContext.getIndex());
			}
		}

		@Override
		public Object resolve(FieldContext fieldContext, EvaluatedArgumentSet arguments, int invocationIndex) {
			Object argument = arguments.getConsumedPayload(fieldContext.getParameterIndex());
			try {
				return this.argumentConverter.convert(argument, fieldContext);
			}
			catch (Exception ex) {
				throw parameterResolutionException("Error converting parameter", ex, fieldContext.getParameterIndex());
			}
		}
	}

	private static class Aggregator implements Resolver {

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
		public Object resolve(ParameterContext parameterContext, int parameterIndex, EvaluatedArgumentSet arguments,
				int invocationIndex) {
			ArgumentsAccessor accessor = DefaultArgumentsAccessor.create(parameterContext, invocationIndex,
				arguments.getConsumedPayloads());
			try {
				return this.argumentsAggregator.aggregateArguments(accessor, parameterContext);
			}
			catch (Exception ex) {
				throw parameterResolutionException("Error aggregating arguments for parameter", ex,
					parameterContext.getIndex());
			}
		}

		@Override
		public Object resolve(FieldContext fieldContext, EvaluatedArgumentSet arguments, int invocationIndex) {
			ArgumentsAccessor accessor = DefaultArgumentsAccessor.create(fieldContext, invocationIndex,
				arguments.getConsumedPayloads());
			try {
				return this.argumentsAggregator.aggregateArguments(accessor, fieldContext);
			}
			catch (Exception ex) {
				throw parameterResolutionException("Error aggregating arguments for parameter", ex,
					fieldContext.getParameterIndex());
			}
		}
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
