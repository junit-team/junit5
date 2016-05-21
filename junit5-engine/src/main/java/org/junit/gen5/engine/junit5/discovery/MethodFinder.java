/*
 * Copyright 2015-2016 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.junit.gen5.engine.junit5.discovery;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.junit.gen5.commons.util.ReflectionUtils;

/**
 * @since 5.0
 */
class MethodFinder {

	private static final Class<?>[] EMPTY_CLASS_ARRAY = new Class<?>[0];

	Optional<Method> findMethod(String methodSpecPart, Class<?> clazz) {
		try {
			// TODO [#272] Throw IAE when format wrong. Currently you get IndexOutOfBoundsException.
			int startParams = methodSpecPart.indexOf('(');
			String methodName = methodSpecPart.substring(0, startParams);
			int endParams = methodSpecPart.lastIndexOf(')');
			String paramsPart = methodSpecPart.substring(startParams + 1, endParams);
			Class<?>[] parameterTypes = resolveParameterTypes(paramsPart);
			return findMethod(clazz, methodName, parameterTypes);
		}
		catch (RuntimeException rte) {
			return Optional.empty();
		}
	}

	private Class<?>[] resolveParameterTypes(String paramsPart) {
		if (paramsPart.isEmpty()) {
			return EMPTY_CLASS_ARRAY;
		}

		// @formatter:off
		List<Class<?>> types = Arrays.stream(paramsPart.split(","))
				.map(className -> loadRequiredParameterClass(className))
				.collect(Collectors.toList());
		// @formatter:on

		return types.toArray(new Class<?>[types.size()]);
	}

	private Optional<Method> findMethod(Class<?> clazz, String methodName, Class<?>[] parameterTypes) {
		return ReflectionUtils.findMethod(clazz, methodName, parameterTypes);
	}

	private Class<?> loadRequiredParameterClass(String className) {
		// TODO [#272] Throw JUnitException instead of a RuntimeException.
		return ReflectionUtils.loadClass(className).orElseThrow(() -> new RuntimeException("Not found: " + className));
	}

}
