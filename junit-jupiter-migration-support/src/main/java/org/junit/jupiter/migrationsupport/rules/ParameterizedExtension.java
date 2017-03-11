/*
 * Copyright 2015-2017 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.junit.jupiter.migrationsupport.rules;

import static java.util.Collections.singletonList;
import static java.util.function.Function.identity;
import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.toList;
import static org.junit.platform.commons.meta.API.Usage.Experimental;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import org.junit.jupiter.api.extension.BeforeTestExecutionCallback;
import org.junit.jupiter.api.extension.ContainerExtensionContext;
import org.junit.jupiter.api.extension.Extension;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.ParameterContext;
import org.junit.jupiter.api.extension.ParameterResolutionException;
import org.junit.jupiter.api.extension.ParameterResolver;
import org.junit.jupiter.api.extension.TestExtensionContext;
import org.junit.jupiter.api.extension.TestTemplateInvocationContext;
import org.junit.jupiter.api.extension.TestTemplateInvocationContextProvider;
import org.junit.platform.commons.meta.API;
import org.junit.platform.commons.util.ReflectionUtils;
import org.junit.runners.Parameterized;

@API(Experimental)
public class ParameterizedExtension implements TestTemplateInvocationContextProvider, ParameterResolver {
	private static ExtensionContext.Namespace parameters = ExtensionContext.Namespace.create(
		ParameterizedExtension.class);;
	private int parametersCollectionIndex = 0;

	/**
	 * Indicate whether we can provide parameterized support.
	 * This requires the testClass to either have a static {@code @Parameters} method
	 * and correct {@code @Parameter} and their corresponding values
	 * or to have a constructor that could be injected.
	 */
	@Override
	public boolean supports(ContainerExtensionContext context) {
		return hasParametersMethod(context) && hasCorrectParameterFields(context);
	}

	@Override
	public Iterator<TestTemplateInvocationContext> provide(ContainerExtensionContext context) {
		//grabbing the parent ensures the paremeters are stored in the same store.
		return context.getParent().flatMap(ParameterizedExtension::parameters).map(
			ParameterizedExtension::testTemplateContextsFromParameters).orElse(Collections.emptyIterator());
	}

	/**
	 * Since the parameterized runner in JUnit 4 could only resolve constructor parameters
	 * this extension once again here only support them on the constructor and require an {@code @Parameters} method
	 *
	 * @param parameterContext the context for the parameter to be resolved; never
	 * {@code null}
	 * @param extensionContext the extension context for the {@code Executable}
	 * about to be invoked; never {@code null}
	 * @return true if the above is met otherwise false.
	 */
	@Override
	public boolean supports(ParameterContext parameterContext, ExtensionContext extensionContext) {
		return hasParametersMethod(extensionContext)
				&& parameterContext.getDeclaringExecutable() instanceof Constructor;
	}

	@Override
	public Object resolve(ParameterContext parameterContext, ExtensionContext extensionContext)
			throws ParameterResolutionException {
		int parameterCount = parameterContext.getDeclaringExecutable().getParameterCount();
		Object[] parameters = resolveParametersForConstructor(extensionContext, parameterCount);

		int parameterIndex = parameterContext.getIndex();
		//move to the next set of parametersFields
		if (lastParameterToBeResolved(parameterContext)) {
			this.parametersCollectionIndex++;
		}

		return parameters[parameterIndex];
	}

	/**
	 * Retrieves the Object[] of the current iteration we are working on.
	 *
	 * @param extensionContext the extensionContext
	 * @param parameterCount the amount of parameters of the constructor.
	 *
	 * @return the object[] for this parameter iteration.
	 * @throws ParameterResolutionException If the amount of arguments of the constructor doesn't match the amount
	 * of arguments of the currently resolved object[]
	 */
	private Object[] resolveParametersForConstructor(ExtensionContext extensionContext, int parameterCount)
			throws ParameterResolutionException {
		return parameters(extensionContext).map(ArrayList::new).map(l -> l.get(this.parametersCollectionIndex)).filter(
			params -> params.length == parameterCount).orElseThrow(
				ParameterizedExtension::unMatchedAmountOfParametersException);
	}

	private static boolean hasCorrectParameterFields(ExtensionContext context) {
		List<Field> fields = parametersFields(context);
		boolean hasFieldInjection = !fields.isEmpty();

		if (hasArgsConstructor(context) && hasFieldInjection) {
			return false;
		}
		else if (hasFieldInjection) {
			return areParametersFormedCorrectly(fields);
		}

		return true;
	}

	private static boolean areParametersFormedCorrectly(List<Field> fields) {
		List<Integer> parameterValues = parameterIndexes(fields);

		List<Integer> duplicateIndexes = duplicatedIndexes(parameterValues);

		boolean hasAllIndexes = indexRangeComplete(parameterValues);

		return hasAllIndexes && duplicateIndexes.isEmpty();
	}

	private static List<Integer> parameterIndexes(List<Field> fields) {
		return fields.stream().map(f -> f.getAnnotation(Parameterized.Parameter.class)).map(
			Parameterized.Parameter::value).collect(toList());
	}

	private static List<Integer> duplicatedIndexes(List<Integer> parameterValues) {
		return parameterValues.stream().collect(groupingBy(identity())).entrySet().stream().filter(
			e -> e.getValue().size() > 1).map(Map.Entry::getKey).collect(toList());
	}

	private static Boolean indexRangeComplete(List<Integer> parameterValues) {
		return parameterValues.stream().max(Integer::compareTo).map(
			i -> parameterValues.containsAll(IntStream.range(0, i).boxed().collect(toList()))).orElse(false);
	}

	private static boolean lastParameterToBeResolved(ParameterContext parameterContext) {
		return parameterContext.getIndex() == parameterContext.getDeclaringExecutable().getParameterCount() - 1;
	}

	private static Optional<Collection<Object[]>> parameters(ExtensionContext context) {
		return context.getStore(parameters).getOrComputeIfAbsent("parameterMethod",
			k -> new ParameterWrapper(callParameters(context)), ParameterWrapper.class).getValue();

	}

	private static Optional<Collection<Object[]>> callParameters(ExtensionContext context) {
		return findParametersMethod(context).map(m -> ReflectionUtils.invokeMethod(m, null)).map(
			ParameterizedExtension::convertParametersMethodReturnType);
	}

	private static boolean hasParametersMethod(ExtensionContext context) {
		return findParametersMethod(context).isPresent();
	}

	private static Optional<Method> findParametersMethod(ExtensionContext extensionContext) {
		return extensionContext.getTestClass().flatMap(ParameterizedExtension::ensureSingleParametersMethod).filter(
			ReflectionUtils::isPublic);
	}

	private static Optional<Method> ensureSingleParametersMethod(Class<?> testClass) {
		return ReflectionUtils.findMethods(testClass,
			m -> m.isAnnotationPresent(Parameterized.Parameters.class)).stream().findFirst();
	}

	private static Iterator<TestTemplateInvocationContext> testTemplateContextsFromParameters(Collection<Object[]> o) {
		return o.stream().map(ParameterizedExtension::contextFactory).iterator();
	}

	private static TestTemplateInvocationContext contextFactory(Object[] parameters) {
		return new TestTemplateInvocationContext() {
			@Override
			public List<Extension> getAdditionalExtensions() {
				return singletonList(new InjectionExtension(parameters));
			}
		};
	}

	private static class InjectionExtension implements BeforeTestExecutionCallback {
		private final Object[] parameters;

		public InjectionExtension(Object[] parameters) {
			this.parameters = parameters;
		}

		@Override
		public void beforeTestExecution(TestExtensionContext context) throws Exception {
			List<Field> parameters = parametersFields(context);

			if (!parameters.isEmpty() && parameters.size() != this.parameters.length) {
				throw unMatchedAmountOfParametersException();
			}

			for (Field param : parameters) {
				Parameterized.Parameter annotation = param.getAnnotation(Parameterized.Parameter.class);
				int paramIndex = annotation.value();
				param.set(context.getTestInstance(), this.parameters[paramIndex]);
			}
		}
	}

	private static boolean hasArgsConstructor(ExtensionContext context) {
		return context.getTestClass().map(ReflectionUtils::getDeclaredConstructor).filter(
			c -> c.getParameterCount() > 0).isPresent();
	}

	private static List<Field> parametersFields(ExtensionContext context) {
		Stream<Field> fieldStream = context.getTestClass().map(Class::getDeclaredFields).map(Stream::of).orElse(
			Stream.empty());

		return fieldStream.filter(f -> f.isAnnotationPresent(Parameterized.Parameter.class)).filter(
			ReflectionUtils::isPublic).collect(toList());
	}

	private static ParameterResolutionException unMatchedAmountOfParametersException() {
		return new ParameterResolutionException(
			"The amount of parametersFields in the constructor doesn't match those in the provided parametersFields");
	}

	private static ParameterResolutionException wrongParametersReturnType() {
		return new ParameterResolutionException("The @Parameters returns the wrong type");
	}

	@SuppressWarnings("unchecked")
	private static Collection<Object[]> convertParametersMethodReturnType(Object o) {
		if (o instanceof Collection) {
			return (Collection<Object[]>) o;
		}
		else {
			throw wrongParametersReturnType();
		}
	}

	private static class ParameterWrapper {
		private final Optional<Collection<Object[]>> value;

		public ParameterWrapper(Optional<Collection<Object[]>> value) {
			this.value = value;
		}

		public Optional<Collection<Object[]>> getValue() {
			return value;
		}
	}
}
