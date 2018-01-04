/*
 * Copyright 2015-2017 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v20.html
 */

package org.junit.jupiter.params.provider;

import static java.lang.Boolean.FALSE;
import static java.lang.Boolean.TRUE;
import static java.util.stream.Collectors.groupingBy;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.params.support.AnnotationConsumer;
import org.junit.platform.commons.JUnitException;
import org.junit.platform.commons.util.CollectionUtils;
import org.junit.platform.commons.util.ReflectionUtils;

/**
 * @since 5.0
 */
class MethodArgumentsProvider implements ArgumentsProvider, AnnotationConsumer<MethodSource> {

	private String[] methodNames;

	@Override
	public void accept(MethodSource annotation) {
		methodNames = annotation.value();
	}

	@Override
	public Stream<Arguments> provideArguments(ExtensionContext context) {
		Class<?> testClass = context.getRequiredTestClass();
		Object testInstance = context.getTestInstance().orElse(null);
		// @formatter:off
		Stream<Arguments> allArgumentsStream = Arrays.stream(methodNames)
				.map(methodName -> ReflectionUtils.findMethod(testClass, methodName)
					.orElseThrow(() -> new JUnitException("Could not find method: " + methodName)))
				.map(method -> ReflectionUtils.invokeMethod(method, testInstance))
				.flatMap(CollectionUtils::toStream)
				.map(MethodArgumentsProvider::toArguments);
		// @formatter:on
		Map<Boolean, List<Arguments>> byOnly = allArgumentsStream.collect(groupingBy(o -> o.only()));
		return byOnly.getOrDefault(TRUE, byOnly.get(FALSE)).stream();
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
