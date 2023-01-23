/*
 * Copyright 2015-2023 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * https://www.eclipse.org/legal/epl-v20.html
 */

package org.junit.jupiter.params.provider;

import static java.lang.String.format;
import static java.util.Arrays.stream;
import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toList;
import static org.junit.jupiter.params.provider.Arguments.arguments;
import static org.junit.platform.commons.util.AnnotationUtils.isAnnotated;
import static org.junit.platform.commons.util.CollectionUtils.isConvertibleToStream;

import java.lang.reflect.Method;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestFactory;
import org.junit.jupiter.api.TestTemplate;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.params.support.AnnotationConsumer;
import org.junit.platform.commons.JUnitException;
import org.junit.platform.commons.util.CollectionUtils;
import org.junit.platform.commons.util.Preconditions;
import org.junit.platform.commons.util.ReflectionUtils;
import org.junit.platform.commons.util.StringUtils;

/**
 * @since 5.0
 */
class MethodArgumentsProvider implements ArgumentsProvider, AnnotationConsumer<MethodSource> {

	private String[] methodNames;

	@Override
	public void accept(MethodSource annotation) {
		this.methodNames = annotation.value();
	}

	@Override
	public Stream<Arguments> provideArguments(ExtensionContext context) {
		Object testInstance = context.getTestInstance().orElse(null);
		// @formatter:off
		return stream(this.methodNames)
				.map(factoryMethodName -> getFactoryMethod(context, factoryMethodName))
				.map(factoryMethod -> context.getExecutableInvoker().invoke(factoryMethod, testInstance))
				.flatMap(CollectionUtils::toStream)
				.map(MethodArgumentsProvider::toArguments);
		// @formatter:on
	}

	private Method getFactoryMethod(ExtensionContext context, String factoryMethodName) {
		Method testMethod = context.getRequiredTestMethod();
		if (StringUtils.isBlank(factoryMethodName)) {
			factoryMethodName = testMethod.getName();
		}
		if (looksLikeAFullyQualifiedMethodName(factoryMethodName)) {
			return getFactoryMethodByFullyQualifiedName(factoryMethodName);
		}
		return getFactoryMethodBySimpleOrQualifiedName(context.getRequiredTestClass(), testMethod, factoryMethodName);
	}

	private static boolean looksLikeAFullyQualifiedMethodName(String factoryMethodName) {
		if (factoryMethodName.contains("#")) {
			return true;
		}
		int indexOfFirstDot = factoryMethodName.indexOf('.');
		if (indexOfFirstDot == -1) {
			return false;
		}
		int indexOfLastOpeningParenthesis = factoryMethodName.lastIndexOf('(');
		if (indexOfLastOpeningParenthesis > 0) {
			// Exclude simple/local method names with parameters
			return indexOfFirstDot < indexOfLastOpeningParenthesis;
		}
		// If we get this far, we conclude the supplied factory method name "looks"
		// like it was intended to be a fully qualified method name, even if the
		// syntax is invalid. We do this in order to provide better diagnostics for
		// the user when a fully qualified method name is in fact invalid.
		return true;
	}

	private Method getFactoryMethodByFullyQualifiedName(String fullyQualifiedMethodName) {
		String[] methodParts = ReflectionUtils.parseFullyQualifiedMethodName(fullyQualifiedMethodName);
		String className = methodParts[0];
		String methodName = methodParts[1];
		String methodParameters = methodParts[2];

		return ReflectionUtils.findMethod(loadRequiredClass(className), methodName, methodParameters).orElseThrow(
			() -> new JUnitException(format("Could not find factory method [%s(%s)] in class [%s]", methodName,
				methodParameters, className)));
	}

	private Method getFactoryMethodBySimpleOrQualifiedName(Class<?> testClass, Method testMethod,
			String simpleOrQualifiedMethodName) {
		String[] methodParts = ReflectionUtils.parseQualifiedMethodName(simpleOrQualifiedMethodName);
		String methodSimpleName = methodParts[0];
		String methodParameters = methodParts[1];

		List<Method> factoryMethods = findFactoryMethodsBySimpleName(testClass, testMethod, methodSimpleName);
		if (factoryMethods.size() == 1) {
			return factoryMethods.get(0);
		}

		List<Method> exactMatches = filterFactoryMethodsWithMatchingParameters(factoryMethods,
			simpleOrQualifiedMethodName, methodParameters);
		Preconditions.condition(exactMatches.size() == 1,
			() -> format("%d factory methods named [%s] were found in class [%s]: %s", factoryMethods.size(),
				simpleOrQualifiedMethodName, testClass.getName(), factoryMethods));
		return exactMatches.get(0);
	}

	/**
	 * Find all methods in the given {@code testClass} with the desired {@code factoryMethodName}
	 * which have return types that can be converted to a {@link Stream}, ignoring the
	 * {@code testMethod} itself as well as any {@code @Test}, {@code @TestTemplate},
	 * or {@code @TestFactory} methods with the same name.
	 */
	private List<Method> findFactoryMethodsBySimpleName(Class<?> testClass, Method testMethod,
			String factoryMethodName) {
		Predicate<Method> isCandidate = candidate -> factoryMethodName.equals(candidate.getName())
				&& !testMethod.equals(candidate);
		List<Method> candidates = ReflectionUtils.findMethods(testClass, isCandidate);

		Predicate<Method> isFactoryMethod = method -> isConvertibleToStream(method.getReturnType())
				&& !isTestMethod(method);
		List<Method> factoryMethods = candidates.stream().filter(isFactoryMethod).collect(toList());

		Preconditions.condition(factoryMethods.size() > 0, () -> {
			// If we didn't find the factory method using the isFactoryMethod Predicate, perhaps
			// the specified factory method has an invalid return type or is a test method.
			// In that case, we report the invalid candidates that were found.
			if (candidates.size() > 0) {
				return format(
					"Could not find valid factory method [%s] in class [%s] but found the following invalid candidates: %s",
					factoryMethodName, testClass.getName(), candidates);
			}
			// Otherwise, report that we didn't find anything.
			return format("Could not find factory method [%s] in class [%s]", factoryMethodName, testClass.getName());
		});
		return factoryMethods;
	}

	private static List<Method> filterFactoryMethodsWithMatchingParameters(List<Method> factoryMethods,
			String factoryMethodName, String factoryMethodParameters) {

		if (!factoryMethodName.endsWith(")")) {
			// If parameters are not specified, nothing is filtered.
			return factoryMethods;
		}

		// Compare against canonical parameter list, ignoring whitespace.
		String parameterList = factoryMethodParameters.replaceAll("\\s+", "");
		Predicate<Method> hasRequiredParameters = method -> {
			if (parameterList.isEmpty()) {
				return method.getParameterCount() == 0;
			}
			return parameterList.equals(stream(method.getParameterTypes()).map(Class::getName).collect(joining(",")));
		};

		return factoryMethods.stream().filter(hasRequiredParameters).collect(toList());
	}

	private boolean isTestMethod(Method candidate) {
		return isAnnotated(candidate, Test.class) || isAnnotated(candidate, TestTemplate.class)
				|| isAnnotated(candidate, TestFactory.class);
	}

	private Class<?> loadRequiredClass(String className) {
		return ReflectionUtils.tryToLoadClass(className).getOrThrow(
			cause -> new JUnitException(format("Could not load class [%s]", className), cause));
	}

	private static Arguments toArguments(Object item) {

		// Nothing to do except cast.
		if (item instanceof Arguments) {
			return (Arguments) item;
		}

		// Pass all multidimensional arrays "as is", in contrast to Object[].
		// See https://github.com/junit-team/junit5/issues/1665
		if (ReflectionUtils.isMultidimensionalArray(item)) {
			return arguments(item);
		}

		// Special treatment for one-dimensional reference arrays.
		// See https://github.com/junit-team/junit5/issues/1665
		if (item instanceof Object[]) {
			return arguments((Object[]) item);
		}

		// Pass everything else "as is".
		return arguments(item);
	}

}
