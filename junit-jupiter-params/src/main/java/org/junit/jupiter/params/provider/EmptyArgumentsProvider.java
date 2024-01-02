/*
 * Copyright 2015-2024 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * https://www.eclipse.org/legal/epl-v20.html
 */

package org.junit.jupiter.params.provider;

import static org.junit.jupiter.params.provider.Arguments.arguments;
import static org.junit.platform.commons.util.ReflectionUtils.newInstance;

import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.NavigableMap;
import java.util.NavigableSet;
import java.util.Optional;
import java.util.Set;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.stream.Stream;

import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.platform.commons.PreconditionViolationException;
import org.junit.platform.commons.util.Preconditions;

/**
 * @since 5.4
 * @see EmptySource
 */
class EmptyArgumentsProvider implements ArgumentsProvider {

	@Override
	public Stream<? extends Arguments> provideArguments(ExtensionContext context) {
		Method testMethod = context.getRequiredTestMethod();
		Class<?>[] parameterTypes = testMethod.getParameterTypes();

		Preconditions.condition(parameterTypes.length > 0, () -> String.format(
			"@EmptySource cannot provide an empty argument to method [%s]: the method does not declare any formal parameters.",
			testMethod.toGenericString()));

		Class<?> parameterType = parameterTypes[0];

		if (String.class.equals(parameterType)) {
			return Stream.of(arguments(""));
		}
		if (Collection.class.equals(parameterType)) {
			return Stream.of(arguments(Collections.emptySet()));
		}
		if (List.class.equals(parameterType)) {
			return Stream.of(arguments(Collections.emptyList()));
		}
		if (Set.class.equals(parameterType)) {
			return Stream.of(arguments(Collections.emptySet()));
		}
		if (SortedSet.class.equals(parameterType)) {
			return Stream.of(arguments(Collections.emptySortedSet()));
		}
		if (NavigableSet.class.equals(parameterType)) {
			return Stream.of(arguments(Collections.emptyNavigableSet()));
		}
		if (Map.class.equals(parameterType)) {
			return Stream.of(arguments(Collections.emptyMap()));
		}
		if (SortedMap.class.equals(parameterType)) {
			return Stream.of(arguments(Collections.emptySortedMap()));
		}
		if (NavigableMap.class.equals(parameterType)) {
			return Stream.of(arguments(Collections.emptyNavigableMap()));
		}
		if (Collection.class.isAssignableFrom(parameterType) || Map.class.isAssignableFrom(parameterType)) {
			Optional<Constructor<?>> defaultConstructor = getDefaultConstructor(parameterType);
			if (defaultConstructor.isPresent()) {
				return Stream.of(arguments(newInstance(defaultConstructor.get())));
			}
		}
		if (parameterType.isArray()) {
			Object array = Array.newInstance(parameterType.getComponentType(), 0);
			return Stream.of(arguments(array));
		}
		// else
		throw new PreconditionViolationException(
			String.format("@EmptySource cannot provide an empty argument to method [%s]: [%s] is not a supported type.",
				testMethod.toGenericString(), parameterType.getName()));
	}

	private static Optional<Constructor<?>> getDefaultConstructor(Class<?> clazz) {
		try {
			return Optional.of(clazz.getConstructor());
		}
		catch (NoSuchMethodException e) {
			return Optional.empty();
		}
	}

}
