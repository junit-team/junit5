/*
 * Copyright 2015-2020 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * https://www.eclipse.org/legal/epl-v20.html
 */

package org.junit.jupiter.api.condition;

import static java.lang.String.format;

import java.lang.reflect.Method;
import java.util.Optional;

import org.junit.jupiter.api.extension.ConditionEvaluationResult;
import org.junit.jupiter.api.extension.ExecutionCondition;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.platform.commons.JUnitException;
import org.junit.platform.commons.util.Preconditions;
import org.junit.platform.commons.util.ReflectionUtils;

abstract class MethodBasedCondition implements ExecutionCondition {

	abstract Optional<String> getMethodName(ExtensionContext context);

	abstract ConditionEvaluationResult getDefaultResult();

	abstract ConditionEvaluationResult getResultBasedOnBoolean(boolean result);

	@Override
	public ConditionEvaluationResult evaluateExecutionCondition(ExtensionContext context) {
		return getMethodName(context) //
				.map(methodName -> getConditionMethod(methodName, context)) //
				.map(method -> (boolean) evaluateCondition(method, context)) //
				.map(this::getResultBasedOnBoolean) //
				.orElse(getDefaultResult());
	}

	private Method getConditionMethod(String methodName, ExtensionContext context) {
		if (methodName.contains("#")) {
			return findMethodByFullyQualifiedName(methodName);
		}
		return findMethod(context.getRequiredTestClass(), methodName);
	}

	private Method findMethodByFullyQualifiedName(String fullyQualifiedMethodName) {
		String[] methodParts = ReflectionUtils.parseFullyQualifiedMethodName(fullyQualifiedMethodName);
		String className = methodParts[0];
		String methodName = methodParts[1];
		Class<?> clazz = ReflectionUtils.tryToLoadClass(className).getOrThrow(
			cause -> new JUnitException(format("Could not load class [%s]", className), cause));
		return findMethod(clazz, methodName);
	}

	private Method findMethod(Class<?> clazz, String methodName) {
		return ReflectionUtils.findMethod(clazz, methodName) //
				.orElseGet(() -> ReflectionUtils.getRequiredMethod(clazz, methodName, ExtensionContext.class));
	}

	private Object evaluateCondition(Method method, ExtensionContext context) {
		Preconditions.condition(method.getReturnType() == boolean.class,
			() -> format("method [%s] should return a boolean", method.getName()));
		Preconditions.condition(areParametersSupported(method),
			() -> format("method [%s] should take either an ExtensionContext or no parameters", method.getName()));

		Object testInstance = context.getTestInstance().orElse(null);
		if (method.getParameterCount() == 0) {
			return ReflectionUtils.invokeMethod(method, testInstance);
		}
		return ReflectionUtils.invokeMethod(method, testInstance, context);
	}

	private boolean areParametersSupported(Method method) {
		switch (method.getParameterCount()) {
			case 0:
				return true;
			case 1:
				return method.getParameterTypes()[0] == ExtensionContext.class;
			default:
				return false;
		}
	}

}
