/*
 * Copyright 2015-2016 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.junit.jupiter.engine.discovery;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.junit.platform.commons.JUnitException;
import org.junit.platform.commons.util.Preconditions;
import org.junit.platform.commons.util.ReflectionUtils;
import org.junit.platform.commons.util.StringUtils;

/**
 * @since 5.0
 */
class MethodFinder {

	// Pattern: methodName(comma-separated argument list)
	private static final Pattern METHOD_PATTERN = Pattern.compile("(.+)\\((.*)\\)");

	private static final Class<?>[] EMPTY_CLASS_ARRAY = new Class<?>[0];

	Optional<Method> findMethod(String methodSpecPart, Class<?> clazz) {
		Matcher matcher = METHOD_PATTERN.matcher(methodSpecPart);

		Preconditions.condition(matcher.matches(),
			() -> String.format("Method [%s] does not match pattern [%s]", methodSpecPart, METHOD_PATTERN));

		String methodName = matcher.group(1);
		Class<?>[] parameterTypes = resolveParameterTypes(matcher.group(2));
		return ReflectionUtils.findMethod(clazz, methodName, parameterTypes);
	}

	private Class<?>[] resolveParameterTypes(String params) {
		if (StringUtils.isBlank(params)) {
			return EMPTY_CLASS_ARRAY;
		}

		// @formatter:off
		return Arrays.stream(params.trim().split(","))
				.map(className -> loadRequiredParameterClass(className))
				.toArray(Class[]::new);
		// @formatter:on
	}

	private Class<?> loadRequiredParameterClass(String className) {
		return ReflectionUtils.loadClass(className).orElseThrow(
			() -> new JUnitException(String.format("Failed to load parameter type [%s]", className)));
	}

}
