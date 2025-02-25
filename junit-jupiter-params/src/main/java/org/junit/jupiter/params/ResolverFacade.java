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

import static java.lang.System.lineSeparator;
import static java.util.Collections.unmodifiableList;
import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;
import static org.junit.platform.commons.support.AnnotationSupport.findAnnotation;
import static org.junit.platform.commons.support.AnnotationSupport.isAnnotated;
import static org.junit.platform.commons.support.ReflectionSupport.makeAccessible;

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Constructor;
import java.lang.reflect.Executable;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.NavigableMap;
import java.util.Optional;
import java.util.Set;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;
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
import org.junit.jupiter.params.support.AnnotationConsumerInitializer;
import org.junit.jupiter.params.support.FieldContext;
import org.junit.jupiter.params.support.ParameterDeclaration;
import org.junit.jupiter.params.support.ParameterDeclarations;
import org.junit.platform.commons.JUnitException;
import org.junit.platform.commons.PreconditionViolationException;
import org.junit.platform.commons.support.ModifierSupport;
import org.junit.platform.commons.util.Preconditions;
import org.junit.platform.commons.util.StringUtils;

class ResolverFacade {

	static ResolverFacade create(Class<?> clazz, List<Field> fields) {
		Preconditions.notEmpty(fields, "Fields must not be empty");

		NavigableMap<Integer, List<FieldParameterDeclaration>> allIndexedParameters = new TreeMap<>();
		Set<FieldParameterDeclaration> aggregatorParameters = new LinkedHashSet<>();

		for (Field field : fields) {
			Parameter annotation = findAnnotation(field, Parameter.class) //
					.orElseThrow(() -> new JUnitException("No @Parameter annotation present"));
			int index = annotation.value();

			FieldParameterDeclaration declaration = new FieldParameterDeclaration(field, annotation.value());
			if (isAggregator(declaration)) {
				aggregatorParameters.add(declaration);
			}
			else {
				if (fields.size() == 1 && index == Parameter.UNSET_INDEX) {
					index = 0;
					declaration = new FieldParameterDeclaration(field, 0);
				}
				allIndexedParameters.computeIfAbsent(index, __ -> new ArrayList<>()) //
						.add(declaration);
			}
		}

		NavigableMap<Integer, FieldParameterDeclaration> uniqueIndexedParameters = validateFieldDeclarations(
			allIndexedParameters, aggregatorParameters);

		Stream.concat(uniqueIndexedParameters.values().stream(), aggregatorParameters.stream()) //
				.forEach(declaration -> makeAccessible(declaration.getField()));

		return new ResolverFacade(clazz, uniqueIndexedParameters, aggregatorParameters, 0);
	}

	static ResolverFacade create(Constructor<?> constructor, ParameterizedContainer annotation) {
		java.lang.reflect.Parameter[] parameters = constructor.getParameters();
		// Inner classes get the outer instance as first parameter
		int implicitParameters = parameters.length > 0 && parameters[0].isImplicit() ? 1 : 0;
		return create(constructor, annotation, implicitParameters);
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
		NavigableMap<Integer, ParameterDeclaration> indexedParameters = new TreeMap<>();
		NavigableMap<Integer, ParameterDeclaration> aggregatorParameters = new TreeMap<>();
		java.lang.reflect.Parameter[] parameters = executable.getParameters();
		for (int index = indexOffset; index < parameters.length; index++) {
			ParameterDeclaration declaration = new ExecutableParameterDeclaration(parameters[index],
				index - indexOffset);
			if (isAggregator(declaration)) {
				Preconditions.condition(
					aggregatorParameters.isEmpty()
							|| aggregatorParameters.lastKey() == declaration.getParameterIndex() - 1,
					() -> String.format(
						"@%s %s declares formal parameters in an invalid order: "
								+ "argument aggregators must be declared after any indexed arguments "
								+ "and before any arguments resolved by another ParameterResolver.",
						annotation.annotationType().getSimpleName(),
						DefaultParameterDeclarations.describe(executable)));
				aggregatorParameters.put(declaration.getParameterIndex(), declaration);
			}
			else if (aggregatorParameters.isEmpty()) {
				indexedParameters.put(declaration.getParameterIndex(), declaration);
			}
		}
		return new ResolverFacade(executable, indexedParameters, new LinkedHashSet<>(aggregatorParameters.values()),
			indexOffset);
	}

	private final int parameterIndexOffset;
	private final Map<ParameterDeclaration, Resolver> resolvers;
	private final DefaultParameterDeclarations indexedParameterDeclarations;
	private final Set<? extends ParameterDeclaration> aggregatorParameters;

	private ResolverFacade(AnnotatedElement sourceElement,
			NavigableMap<Integer, ? extends ParameterDeclaration> indexedParameters,
			Set<? extends ParameterDeclaration> aggregatorParameters, int parameterIndexOffset) {
		this.aggregatorParameters = aggregatorParameters;
		this.parameterIndexOffset = parameterIndexOffset;
		this.resolvers = new ConcurrentHashMap<>(indexedParameters.size() + aggregatorParameters.size());
		this.indexedParameterDeclarations = new DefaultParameterDeclarations(sourceElement, indexedParameters);
	}

	ParameterDeclarations getIndexedParameterDeclarations() {
		return this.indexedParameterDeclarations;
	}

	boolean isSupportedParameter(ParameterContext parameterContext, EvaluatedArgumentSet arguments) {
		int index = toLogicalIndex(parameterContext);
		if (this.indexedParameterDeclarations.get(index).isPresent()) {
			return index < arguments.getConsumedLength();
		}
		return !this.aggregatorParameters.isEmpty()
				&& this.aggregatorParameters.stream().anyMatch(it -> it.getParameterIndex() == index);
	}

	/**
	 * Get the name of the parameter with the supplied index, if it is present
	 * and declared before the aggregators.
	 *
	 * @return an {@code Optional} containing the name of the parameter
	 */
	Optional<String> getParameterName(int parameterIndex) {
		return this.indexedParameterDeclarations.get(parameterIndex) //
				.flatMap(ParameterDeclaration::getParameterName);
	}

	/**
	 * Determine the length of the arguments array that is considered consumed
	 * by the parameter declarations in this resolver.
	 *
	 * <p>If an aggregator is present, all arguments are considered consumed.
	 * Otherwise, the consumed argument length is the minimum of the total
	 * length and the number of indexed parameter declarations.
	 */
	int determineConsumedArgumentLength(int totalLength) {
		NavigableMap<Integer, ? extends ParameterDeclaration> declarationsByIndex = this.indexedParameterDeclarations.declarationsByIndex;
		return this.aggregatorParameters.isEmpty() //
				? Math.min(totalLength, declarationsByIndex.isEmpty() ? 0 : declarationsByIndex.lastKey() + 1) //
				: totalLength;
	}

	/**
	 * Determine the number of arguments that are considered consumed by the
	 * parameter declarations in this resolver.
	 *
	 * <p>If an aggregator is present, all arguments are considered consumed.
	 * Otherwise, the consumed argument count, is the number of indexes that
	 * correspond to indexed parameter declarations.
	 */
	int determineConsumedArgumentCount(EvaluatedArgumentSet arguments) {
		if (this.aggregatorParameters.isEmpty()) {
			return this.indexedParameterDeclarations.declarationsByIndex.subMap(0,
				arguments.getConsumedLength()).size();
		}
		return arguments.getTotalLength();
	}

	/**
	 * Resolve the parameter for the supplied context using the supplied
	 * arguments.
	 */
	Object resolve(ParameterContext parameterContext, ExtensionContext extensionContext, EvaluatedArgumentSet arguments,
			int invocationIndex) {
		int parameterIndex = toLogicalIndex(parameterContext);
		ParameterDeclaration declaration = this.indexedParameterDeclarations.get(parameterIndex) //
				.orElseGet(() -> this.aggregatorParameters.stream().filter(
					it -> it.getParameterIndex() == parameterIndex).findFirst() //
						.orElseThrow(() -> new ParameterResolutionException(
							"Parameter index out of bounds: " + parameterIndex)));
		return getResolver(extensionContext, declaration, parameterContext.getParameter()) //
				.resolve(parameterContext, parameterIndex, arguments, invocationIndex);
	}

	void resolveAndInjectFields(Object testInstance, ExtensionContext extensionContext, EvaluatedArgumentSet arguments,
			int invocationIndex) {
		if (this.indexedParameterDeclarations.sourceElement.equals(testInstance.getClass())) {
			getAllParameterDeclarations() //
					.filter(FieldParameterDeclaration.class::isInstance) //
					.map(FieldParameterDeclaration.class::cast) //
					.forEach(declaration -> setField(testInstance, declaration, extensionContext, arguments,
						invocationIndex));
		}
	}

	private Stream<ParameterDeclaration> getAllParameterDeclarations() {
		return Stream.concat(this.indexedParameterDeclarations.declarationsByIndex.values().stream(),
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

	private Resolver getResolver(ExtensionContext extensionContext, ParameterDeclaration declaration,
			AnnotatedElement annotatedElement) {
		return this.resolvers.computeIfAbsent(declaration, __ -> this.aggregatorParameters.contains(declaration) //
				? createAggregator(declaration.getParameterIndex(), annotatedElement, extensionContext) //
				: createConverter(declaration.getParameterIndex(), annotatedElement, extensionContext));
	}

	private int toLogicalIndex(ParameterContext parameterContext) {
		int index = parameterContext.getIndex() - this.parameterIndexOffset;
		Preconditions.condition(index >= 0, () -> "Parameter index must be greater than or equal to zero");
		return index;
	}

	private static NavigableMap<Integer, FieldParameterDeclaration> validateFieldDeclarations(
			NavigableMap<Integer, List<FieldParameterDeclaration>> indexedParameters,
			Set<FieldParameterDeclaration> aggregatorParameters) {

		List<String> errors = new ArrayList<>();
		validateIndexedParameters(indexedParameters, errors);
		validateAggregatorParameters(aggregatorParameters, errors);

		if (errors.isEmpty()) {
			return indexedParameters.entrySet().stream() //
					.collect(toMap(Map.Entry::getKey, entry -> entry.getValue().get(0), (d, __) -> d, TreeMap::new));
		}
		else if (errors.size() == 1) {
			throw new PreconditionViolationException("Configuration error: " + errors.get(0) + ".");
		}
		else {
			throw new PreconditionViolationException(String.format("%d configuration errors:%n%s", errors.size(),
				errors.stream().collect(joining(lineSeparator() + "- ", "- ", ""))));
		}
	}

	private static void validateIndexedParameters(
			NavigableMap<Integer, List<FieldParameterDeclaration>> indexedParameters, List<String> errors) {

		if (indexedParameters.isEmpty()) {
			return;
		}

		indexedParameters.forEach(
			(index, declarations) -> validateIndexedParameterDeclarations(index, declarations, errors));

		for (int index = 0; index <= indexedParameters.lastKey(); index++) {
			if (!indexedParameters.containsKey(index)) {
				errors.add(String.format("no field annotated with @Parameter(%d) declared", index));
			}
		}
	}

	private static void validateIndexedParameterDeclarations(int index, List<FieldParameterDeclaration> declarations,
			List<String> errors) {
		List<Field> fields = declarations.stream().map(FieldParameterDeclaration::getField).collect(toList());
		if (index < 0) {
			declarations.stream() //
					.map(declaration -> String.format(
						"index must be greater than or equal to zero in @Parameter(%d) annotation on field [%s]", index,
						declaration.getField())) //
					.forEach(errors::add);
		}
		else if (declarations.size() > 1) {
			errors.add(
				String.format("duplicate index declared in @Parameter(%d) annotation on fields %s", index, fields));
		}
		fields.stream() //
				.filter(ModifierSupport::isFinal) //
				.map(field -> String.format("@Parameter field [%s] must not be declared as final", field)) //
				.forEach(errors::add);
	}

	private static void validateAggregatorParameters(Set<FieldParameterDeclaration> aggregatorParameters,
			List<String> errors) {
		aggregatorParameters.stream() //
				.filter(declaration -> declaration.getParameterIndex() != Parameter.UNSET_INDEX) //
				.map(declaration -> String.format(
					"no index may be declared in @Parameter(%d) annotation on aggregator field [%s]",
					declaration.getParameterIndex(), declaration.getField())) //
				.forEach(errors::add);
	}

	/**
	 * Determine if the supplied {@link Parameter} is an aggregator (i.e., of
	 * type {@link ArgumentsAccessor} or annotated with {@link AggregateWith}).
	 *
	 * @return {@code true} if the parameter is an aggregator
	 */
	private static boolean isAggregator(ParameterDeclaration declaration) {
		return ArgumentsAccessor.class.isAssignableFrom(declaration.getParameterType())
				|| isAnnotated(declaration.getAnnotatedElement(), AggregateWith.class);
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
		private final NavigableMap<Integer, ? extends ParameterDeclaration> declarationsByIndex;

		DefaultParameterDeclarations(AnnotatedElement sourceElement,
				NavigableMap<Integer, ? extends ParameterDeclaration> declarationsByIndex) {
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
		public List<ParameterDeclaration> getAll() {
			return unmodifiableList(new ArrayList<>(this.declarationsByIndex.values()));
		}

		@Override
		public Optional<ParameterDeclaration> get(int parameterIndex) {
			return Optional.ofNullable(this.declarationsByIndex.get(parameterIndex));
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
