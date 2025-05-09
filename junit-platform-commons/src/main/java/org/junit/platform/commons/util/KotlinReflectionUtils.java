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

import static kotlin.jvm.JvmClassMappingKt.getJavaClass;
import static kotlin.reflect.full.KCallables.callSuspendBy;
import static kotlin.reflect.jvm.KCallablesJvm.isAccessible;
import static kotlin.reflect.jvm.KCallablesJvm.setAccessible;
import static kotlin.reflect.jvm.KTypesJvm.getJvmErasure;
import static kotlinx.coroutines.BuildersKt.runBlocking;
import static org.junit.platform.commons.util.ExceptionUtils.throwAsUncheckedException;
import static org.junit.platform.commons.util.ReflectionUtils.getUnderlyingCause;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.junit.platform.commons.function.Try;

import kotlin.coroutines.EmptyCoroutineContext;
import kotlin.reflect.KFunction;
import kotlin.reflect.KParameter;
import kotlin.reflect.jvm.ReflectJvmMapping;

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
		var returnType = getJvmErasure(getKotlinFunction(method).getReturnType());
		if ("kotlin.Unit".equals(returnType.getQualifiedName())) {
			return void.class;
		}
		return getJavaClass(returnType);
	}

	public static Parameter[] getSuspendingFunctionParameters(Method method) {
		var parameterCount = method.getParameterCount();
		if (parameterCount == 1) {
			return new Parameter[0];
		}
		return Arrays.copyOf(method.getParameters(), parameterCount - 1);
	}

	public static Class<?>[] getKotlinSuspendingFunctionParameterTypes(Method method) {
		var parameterCount = method.getParameterCount();
		if (parameterCount == 1) {
			return new Class<?>[0];
		}
		return Arrays.stream(method.getParameterTypes()).limit(parameterCount - 1).toArray(Class<?>[]::new);
	}

	public static Object invokeKotlinSuspendingFunction(Method method, Object target, Object[] args) {
		try {
			return invokeKotlinSuspendingFunction(getKotlinFunction(method), target, args);
		}
		catch (InterruptedException e) {
			throw throwAsUncheckedException(e);
		}
	}

	private static <T> T invokeKotlinSuspendingFunction(KFunction<T> function, Object target, Object[] args)
			throws InterruptedException {
		if (!isAccessible(function)) {
			setAccessible(function, true);
		}
		return runBlocking(EmptyCoroutineContext.INSTANCE, (__, continuation) -> {
			try {
				return callSuspendBy(function, toArgumentMap(target, args, function), continuation);
			}
			catch (Exception e) {
				throw throwAsUncheckedException(getUnderlyingCause(e));
			}
		});
	}

	private static Map<KParameter, Object> toArgumentMap(Object target, Object[] args, KFunction<?> function) {
		Map<KParameter, Object> arguments = new HashMap<>(args.length + 1);
		int index = 0;
		for (KParameter parameter : function.getParameters()) {
			switch (parameter.getKind()) {
				case INSTANCE -> arguments.put(parameter, target);
				case VALUE, EXTENSION_RECEIVER -> {
					arguments.put(parameter, args[index]);
					index++;
				}
			}
		}
		return arguments;
	}

	private static KFunction<?> getKotlinFunction(Method method) {
		return Preconditions.notNull(ReflectJvmMapping.getKotlinFunction(method),
			() -> "Failed to get Kotlin function for method: " + method);
	}

}
