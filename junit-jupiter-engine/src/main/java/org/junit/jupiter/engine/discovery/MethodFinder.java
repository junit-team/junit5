/*
 * Copyright 2015-2024 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * https://www.eclipse.org/legal/epl-v20.html
 */

package org.junit.jupiter.engine.discovery;

import java.lang.reflect.Method;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.junit.platform.commons.util.Preconditions;
import org.junit.platform.commons.util.ReflectionUtils;

/**
 * @since 5.0
 */
class MethodFinder {

	// Pattern: methodName(comma-separated list of parameter type names)
	private static final Pattern METHOD_PATTERN = Pattern.compile("(.+)\\((.*)\\)");

	Optional<Method> findMethod(String methodSpecPart, Class<?> clazz) {
		Matcher matcher = METHOD_PATTERN.matcher(methodSpecPart);

		Preconditions.condition(matcher.matches(),
			() -> String.format("Method [%s] does not match pattern [%s]", methodSpecPart, METHOD_PATTERN));

		String methodName = matcher.group(1);
		String parameterTypeNames = matcher.group(2);
		return ReflectionUtils.findMethod(clazz, methodName, parameterTypeNames);
	}

}
