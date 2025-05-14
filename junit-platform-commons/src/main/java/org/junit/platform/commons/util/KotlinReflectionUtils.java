/*
 * Copyright 2015-2025 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * https://www.eclipse.org/legal/epl-v20.html
 */

package org.junit.platform.commons.util;

import static org.apiguardian.api.API.Status.INTERNAL;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.lang.reflect.Type;

import org.apiguardian.api.API;
import org.junit.platform.commons.function.Try;

/**
 * Internal Kotlin-specific reflection utilities
 *
 * @since 6.0
 */
@API(status = INTERNAL, since = "6.0")
public class KotlinReflectionUtils {

	private static final Class<? extends Annotation> kotlinMetadata;
	private static final Class<?> kotlinCoroutineContinuation;

	static {
		var metadata = tryToLoadKotlinMetadataClass();
		kotlinMetadata = metadata.toOptional().orElse(null);
		kotlinCoroutineContinuation = metadata //
				.andThen(__ -> ReflectionUtils.tryToLoadClass("kotlin.coroutines.Continuation")) //
				.toOptional() //
				.orElse(null);
	}

	@SuppressWarnings("unchecked")
	private static Try<Class<? extends Annotation>> tryToLoadKotlinMetadataClass() {
		return ReflectionUtils.tryToLoadClass("kotlin.Metadata") //
				.andThenTry(it -> (Class<? extends Annotation>) it);
	}

	public static boolean isKotlinSuspendingFunction(Method method) {
		if (kotlinCoroutineContinuation != null && isKotlinType(method.getDeclaringClass())) {
			int parameterCount = method.getParameterCount();
			return parameterCount > 0 //
					&& method.getParameterTypes()[parameterCount - 1] == kotlinCoroutineContinuation;
		}
		return false;
	}

	private static boolean isKotlinType(Class<?> clazz) {
		return kotlinMetadata != null //
				&& clazz.getDeclaredAnnotation(kotlinMetadata) != null;
	}

	public static Class<?> getKotlinSuspendingFunctionReturnType(Method method) {
		return KotlinReflectionUtilsKt.getReturnType(method);
	}

	public static Type getKotlinSuspendingFunctionGenericReturnType(Method method) {
		return KotlinReflectionUtilsKt.getGenericReturnType(method);
	}

	public static Parameter[] getKotlinSuspendingFunctionParameters(Method method) {
		return KotlinReflectionUtilsKt.getParameters(method);
	}

	public static Class<?>[] getKotlinSuspendingFunctionParameterTypes(Method method) {
		return KotlinReflectionUtilsKt.getParameterTypes(method);
	}

	public static Object invokeKotlinSuspendingFunction(Method method, Object target, Object[] args) {
		return KotlinReflectionUtilsKt.invoke(method, target, args);
	}

}
