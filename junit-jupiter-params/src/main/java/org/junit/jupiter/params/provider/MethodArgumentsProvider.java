/*
 * Copyright 2015-2018 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v20.html
 */

package org.junit.jupiter.params.provider;

import static java.lang.String.format;

import java.util.Arrays;
import java.util.stream.Stream;

import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.params.support.AnnotationConsumer;
import org.junit.platform.commons.JUnitException;
import org.junit.platform.commons.util.CollectionUtils;
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
		Class<?> testClass = context.getRequiredTestClass();
		Object testInstance = context.getTestInstance().orElse(null);
		// @formatter:off
		return Arrays.stream(this.methodNames)
				.map(methodName -> StringUtils.isNotBlank(methodName) ? methodName : context.getRequiredTestMethod().getName())
				.map(methodName -> ReflectionUtils.findMethod(testClass, methodName)
					.orElseThrow(() -> new JUnitException(
						format("Could not find factory method [%s] in class [%s]", methodName, testClass.getName()))))
				.map(method -> ReflectionUtils.invokeMethod(method, testInstance))
				.flatMap(CollectionUtils::toStream)
				.map(MethodArgumentsProvider::toArguments);
		// @formatter:on
	}

	private static Arguments toArguments(Object item) {
		if (item instanceof Arguments) {
			return (Arguments) item;
		}
		if (item instanceof Object[]) {
			return Arguments.of((Object[]) item);
		}
		return Arguments.of(item);
	}

}
