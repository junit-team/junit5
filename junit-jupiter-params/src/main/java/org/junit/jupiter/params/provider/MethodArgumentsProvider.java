/*
 * Copyright 2015-2021 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * https://www.eclipse.org/legal/epl-v20.html
 */

package org.junit.jupiter.params.provider;

import static java.lang.String.format;
import static org.junit.jupiter.params.provider.Arguments.arguments;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.stream.Stream;

import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.params.support.AnnotationConsumer;
import org.junit.platform.commons.JUnitException;
import org.junit.platform.commons.function.Try;
import org.junit.platform.commons.util.CollectionUtils;
import org.junit.platform.commons.util.ExceptionUtils;
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
		// @formatter:off
		return Arrays.stream(this.methodNames)
				.map(factoryMethodName -> {
					final Try<Object> fromJavaTry = resolveArgumentsJava(context, factoryMethodName);
					if (fromJavaTry.isSuccess()) {
						return fromJavaTry.getOrThrow(ExceptionUtils::throwAsUncheckedException);
					} else {
						if (MethodSourceKotlinCompanionResolver.INSTANCE.canResolveArguments(context, factoryMethodName)) {
							final Try<Object> fromKotlinTry = resolveArgumentsKotlin(context, factoryMethodName);
							if (fromKotlinTry.isSuccess()) {
								return fromKotlinTry.getOrThrow(ExceptionUtils::throwAsUncheckedException);
							} else {
								final JUnitException e = new JUnitException("Unable to resolve arguments from Java and Kotlin class");
								e.addSuppressed(fromJavaTry.getCause());
								e.addSuppressed(fromKotlinTry.getCause());
								throw e;
							}
						} else {
							return fromJavaTry.getOrThrow(ExceptionUtils::throwAsUncheckedException);
						}
					}
				})
				.flatMap(CollectionUtils::toStream)
				.map(MethodArgumentsProvider::toArguments);
		// @formatter:on
	}

	private Try<Object> resolveArgumentsJava(ExtensionContext context, String factoryMethodName) {
		Object testInstance = context.getTestInstance().orElse(null);
		return Try.call(() -> getMethod(context, factoryMethodName)).andThenTry(
			method -> ReflectionUtils.invokeMethod(method, testInstance));
	}

	private Try<Object> resolveArgumentsKotlin(ExtensionContext context, String factoryMethodName) {
		return Try.call(
			() -> MethodSourceKotlinCompanionResolver.INSTANCE.resolveArguments(context, factoryMethodName));
	}

	private Method getMethod(ExtensionContext context, String factoryMethodName) {
		if (StringUtils.isNotBlank(factoryMethodName)) {
			if (factoryMethodName.contains("#")) {
				return getMethodByFullyQualifiedName(factoryMethodName);
			}
			else {
				return ReflectionUtils.getRequiredMethod(context.getRequiredTestClass(), factoryMethodName);
			}
		}
		return ReflectionUtils.getRequiredMethod(context.getRequiredTestClass(),
			context.getRequiredTestMethod().getName());
	}

	private Method getMethodByFullyQualifiedName(String fullyQualifiedMethodName) {
		String[] methodParts = ReflectionUtils.parseFullyQualifiedMethodName(fullyQualifiedMethodName);
		String className = methodParts[0];
		String methodName = methodParts[1];
		String methodParameters = methodParts[2];

		Preconditions.condition(StringUtils.isBlank(methodParameters),
			() -> format("factory method [%s] must not declare formal parameters", fullyQualifiedMethodName));

		return ReflectionUtils.getRequiredMethod(loadRequiredClass(className), methodName);
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
