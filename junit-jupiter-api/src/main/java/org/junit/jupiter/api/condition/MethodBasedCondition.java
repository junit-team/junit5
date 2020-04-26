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
import static org.junit.jupiter.api.extension.ConditionEvaluationResult.disabled;
import static org.junit.jupiter.api.extension.ConditionEvaluationResult.enabled;
import static org.junit.platform.commons.util.AnnotationUtils.findAnnotation;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.Optional;
import java.util.function.Function;

import org.junit.jupiter.api.extension.ConditionEvaluationResult;
import org.junit.jupiter.api.extension.ExecutionCondition;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.platform.commons.JUnitException;
import org.junit.platform.commons.util.Preconditions;
import org.junit.platform.commons.util.ReflectionUtils;

abstract class MethodBasedCondition<A extends Annotation> implements ExecutionCondition {

	private final Class<A> annotationType;
	private final Function<A, String> methodName;
	private final Function<A, String> customDisabledReason;

	MethodBasedCondition(Class<A> annotationType, Function<A, String> methodName,
			Function<A, String> customDisabledReason) {
		this.annotationType = annotationType;
		this.methodName = methodName;
		this.customDisabledReason = customDisabledReason;
	}

	protected abstract boolean isEnabled(boolean methodResult);

	@Override
	public ConditionEvaluationResult evaluateExecutionCondition(ExtensionContext context) {
		Optional<A> annotation = findAnnotation(context.getElement(), annotationType);
		return annotation //
				.map(methodName) //
				.map(methodName -> getConditionMethod(methodName, context)) //
				.map(method -> (boolean) invokeMethod(method, context)) //
				.map(methodResult -> buildConditionEvaluationResult(methodResult, annotation.get())) //
				.orElse(enabledByDefault());
	}

	private Method getConditionMethod(String methodName, ExtensionContext context) {
		if (!methodName.contains("#")) {
			return findMethod(context.getRequiredTestClass(), methodName);
		}
		String[] methodParts = ReflectionUtils.parseFullyQualifiedMethodName(methodName);
		String className = methodParts[0];
		String methodName1 = methodParts[1];
		Class<?> clazz = ReflectionUtils.tryToLoadClass(className).getOrThrow(
			cause -> new JUnitException(format("Could not load class [%s]", className), cause));
		return findMethod(clazz, methodName1);
	}

	private Method findMethod(Class<?> clazz, String methodName) {
		return ReflectionUtils.findMethod(clazz, methodName) //
				.orElseGet(() -> ReflectionUtils.getRequiredMethod(clazz, methodName, ExtensionContext.class));
	}

	private Object invokeMethod(Method method, ExtensionContext context) {
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

	private ConditionEvaluationResult buildConditionEvaluationResult(boolean methodResult, A annotation) {
		String defaultReason = format("Condition provided in @%s evaluates to %s", annotationType.getSimpleName(),
			methodResult);
		if (isEnabled(methodResult)) {
			return enabled(defaultReason);
		}
		String customReason = customDisabledReason.apply(annotation);
		if (customReason.isEmpty()) {
			return disabled(defaultReason);
		}
		return disabled(customReason);
	}

	private ConditionEvaluationResult enabledByDefault() {
		String reason = String.format("@%s is not present", annotationType.getSimpleName());
		return enabled(reason);
	}

}
