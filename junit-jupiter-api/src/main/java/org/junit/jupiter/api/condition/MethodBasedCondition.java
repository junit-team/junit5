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

/**
 * @since 5.7
 */
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

	@Override
	public ConditionEvaluationResult evaluateExecutionCondition(ExtensionContext context) {
		Optional<A> annotation = findAnnotation(context.getElement(), this.annotationType);
		return annotation //
				.map(this.methodName) //
				.map(methodName -> getConditionMethod(methodName, context)) //
				.map(method -> invokeConditionMethod(method, context)) //
				.map(methodResult -> buildConditionEvaluationResult(methodResult, annotation.get())) //
				.orElseGet(this::enabledByDefault);
	}

	private Method getConditionMethod(String fullyQualifiedMethodName, ExtensionContext context) {
		if (!fullyQualifiedMethodName.contains("#")) {
			return findMethod(context.getRequiredTestClass(), fullyQualifiedMethodName);
		}
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

	private boolean invokeConditionMethod(Method method, ExtensionContext context) {
		Preconditions.condition(method.getReturnType() == boolean.class,
			() -> format("Method [%s] should return a boolean", method.getName()));
		Preconditions.condition(acceptsExtensionContextArgument(method),
			() -> format("Method [%s] should accept either an ExtensionContext or no arguments", method.getName()));

		Object testInstance = context.getTestInstance().orElse(null);
		if (method.getParameterCount() == 0) {
			return (boolean) ReflectionUtils.invokeMethod(method, testInstance);
		}
		return (boolean) ReflectionUtils.invokeMethod(method, testInstance, context);
	}

	private boolean acceptsExtensionContextArgument(Method method) {
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
		String defaultReason = format("Condition provided in @%s evaluates to %s", this.annotationType.getSimpleName(),
			methodResult);
		if (isEnabled(methodResult)) {
			return enabled(defaultReason);
		}
		String customReason = this.customDisabledReason.apply(annotation);
		if (customReason.isEmpty()) {
			return disabled(defaultReason);
		}
		return disabled(customReason);
	}

	protected abstract boolean isEnabled(boolean methodResult);

	private ConditionEvaluationResult enabledByDefault() {
		return enabled(format("@%s is not present", this.annotationType.getSimpleName()));
	}

}
