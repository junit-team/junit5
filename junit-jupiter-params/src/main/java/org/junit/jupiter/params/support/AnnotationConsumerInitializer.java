/*
 * Copyright 2015-2022 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * https://www.eclipse.org/legal/epl-v20.html
 */

package org.junit.jupiter.params.support;

import static org.apiguardian.api.API.Status.INTERNAL;
import static org.junit.platform.commons.util.ReflectionUtils.HierarchyTraversalMode.BOTTOM_UP;
import static org.junit.platform.commons.util.ReflectionUtils.findMethods;

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Method;
import java.util.function.Predicate;

import org.apiguardian.api.API;
import org.junit.platform.commons.JUnitException;
import org.junit.platform.commons.util.AnnotationUtils;

/**
 * {@code AnnotationConsumerInitializer} is an internal helper class for
 * initializing {@link AnnotationConsumer AnnotationConsumers}.
 *
 * @since 5.0
 */
@API(status = INTERNAL, since = "5.0")
public final class AnnotationConsumerInitializer {

	private AnnotationConsumerInitializer() {
		/* no-op */
	}

	private static final Predicate<Method> isAcceptOrProvideArgumentsMethod = method -> isAnnotationConsumerAcceptMethod(
		method) || isAnnotationBasedArgumentsProviderMethod(method);

	@SuppressWarnings({ "unchecked", "rawtypes" })
	public static <T> T initialize(AnnotatedElement annotatedElement, T annotationConsumerInstance) {
		if (annotationConsumerInstance instanceof AnnotationConsumer) {
			Method method = findMethods(annotationConsumerInstance.getClass(), isAcceptOrProvideArgumentsMethod,
				BOTTOM_UP).get(0);
			Class<? extends Annotation> annotationType = getAnnotationType(method);
			Annotation annotation = AnnotationUtils.findAnnotation(annotatedElement, annotationType) //
					.orElseThrow(() -> new JUnitException(annotationConsumerInstance.getClass().getName()
							+ " must be used with an annotation of type " + annotationType.getName()));
			initializeAnnotationConsumer((AnnotationConsumer) annotationConsumerInstance, annotation);
		}
		return annotationConsumerInstance;
	}

	private static boolean isAnnotationConsumerAcceptMethod(Method method) {
		return isMethodWith(method, "accept", 1, 0);
	}

	private static boolean isAnnotationBasedArgumentsProviderMethod(Method method) {
		return isMethodWith(method, "provideArguments", 2, 1);
	}

	// @formatter:off
	private static boolean isMethodWith(Method method, String methodName, int parameterCount,
			int annotationInParameterIndex) {
		return method.getName().equals(methodName)
				&& method.getParameterCount() == parameterCount
				&& method.getParameterTypes()[annotationInParameterIndex].isAnnotation();
	}
	// @formatter:on

	@SuppressWarnings({ "unchecked" })
	private static Class<? extends Annotation> getAnnotationType(Method method) {
		if (isAnnotationConsumerAcceptMethod(method)) {
			return (Class<? extends Annotation>) method.getParameterTypes()[0];
		}
		return (Class<? extends Annotation>) method.getParameterTypes()[1];
	}

	private static <A extends Annotation> void initializeAnnotationConsumer(AnnotationConsumer<A> instance,
			A annotation) {
		try {
			instance.accept(annotation);
		}
		catch (Exception ex) {
			throw new JUnitException("Failed to initialize AnnotationConsumer: " + instance, ex);
		}
	}

}
