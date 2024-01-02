/*
 * Copyright 2015-2024 the original author or authors.
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
import java.util.function.Supplier;

import org.junit.jupiter.api.extension.ConditionEvaluationResult;
import org.junit.jupiter.api.extension.ExecutionCondition;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.platform.commons.JUnitException;
import org.junit.platform.commons.util.ClassLoaderUtils;
import org.junit.platform.commons.util.Preconditions;
import org.junit.platform.commons.util.ReflectionUtils;
import org.junit.platform.commons.util.StringUtils;

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

	// package-private for testing
	Method getConditionMethod(String fullyQualifiedMethodName, ExtensionContext context) {
		Class<?> testClass = context.getRequiredTestClass();
		if (!fullyQualifiedMethodName.contains("#")) {
			return findMethod(testClass, fullyQualifiedMethodName);
		}
		String[] methodParts = ReflectionUtils.parseFullyQualifiedMethodName(fullyQualifiedMethodName);
		String className = methodParts[0];
		String methodName = methodParts[1];
		ClassLoader classLoader = ClassLoaderUtils.getClassLoader(testClass);
		Class<?> clazz = ReflectionUtils.tryToLoadClass(className, classLoader).getOrThrow(
			cause -> new JUnitException(format("Could not load class [%s]", className), cause));
		return findMethod(clazz, methodName);
	}

	private Method findMethod(Class<?> clazz, String methodName) {
		return ReflectionUtils.findMethod(clazz, methodName) //
				.orElseGet(() -> ReflectionUtils.getRequiredMethod(clazz, methodName, ExtensionContext.class));
	}

	private boolean invokeConditionMethod(Method method, ExtensionContext context) {
		Preconditions.condition(method.getReturnType() == boolean.class,
			() -> format("Method [%s] must return a boolean", method));
		Preconditions.condition(acceptsExtensionContextOrNoArguments(method),
			() -> format("Method [%s] must accept either an ExtensionContext or no arguments", method));

		Object testInstance = context.getTestInstance().orElse(null);
		if (method.getParameterCount() == 0) {
			return (boolean) ReflectionUtils.invokeMethod(method, testInstance);
		}
		return (boolean) ReflectionUtils.invokeMethod(method, testInstance, context);
	}

	private boolean acceptsExtensionContextOrNoArguments(Method method) {
		int parameterCount = method.getParameterCount();
		return parameterCount == 0 || (parameterCount == 1 && method.getParameterTypes()[0] == ExtensionContext.class);
	}

	private ConditionEvaluationResult buildConditionEvaluationResult(boolean methodResult, A annotation) {
		Supplier<String> defaultReason = () -> format("@%s(\"%s\") evaluated to %s",
			this.annotationType.getSimpleName(), this.methodName.apply(annotation), methodResult);
		if (isEnabled(methodResult)) {
			return enabled(defaultReason.get());
		}
		String customReason = this.customDisabledReason.apply(annotation);
		return StringUtils.isNotBlank(customReason) ? disabled(customReason) : disabled(defaultReason.get());
	}

	protected abstract boolean isEnabled(boolean methodResult);

	private ConditionEvaluationResult enabledByDefault() {
		return enabled(format("@%s is not present", this.annotationType.getSimpleName()));
	}

}
