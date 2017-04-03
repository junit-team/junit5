/*
 * Copyright 2015-2017 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.junit.jupiter.params.provider;

import static org.junit.jupiter.params.provider.ObjectArrayArguments.arguments;

import java.util.Arrays;
import java.util.stream.Stream;

import org.junit.jupiter.api.extension.ContainerExtensionContext;
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
		methodNames = annotation.names();
	}

	@Override
	public Stream<Arguments> provideArguments(ContainerExtensionContext context) {
		Class<?> testClass = context.getTestClass() //
				.orElseThrow(() -> new JUnitException("Cannot invoke method without test class"));
		// @formatter:off
		return Arrays.stream(methodNames)
				.map(methodName -> ReflectionUtils.findMethod(testClass, methodName)
                        .orElseThrow(() -> new JUnitException("Could not find method: " + methodName)))
				.map(method -> ReflectionUtils.invokeMethod(method, null))
				.flatMap(CollectionUtils::toStream)
				.map(MethodArgumentsProvider::toArguments);
		// @formatter:on
	}

	private static Arguments toArguments(Object item) {
		if (item instanceof Arguments) {
			return (Arguments) item;
		}
		if (item instanceof Object[]) {
			return arguments((Object[]) item);
		}
		return arguments(item);
	}

}
