/*
 * Copyright 2015-2020 the original author or authors.
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

	// @formatter:off
	private static final Predicate<Method> isAnnotationConsumerAcceptMethod = method ->
			method.getName().equals("accept")
			&& method.getParameterCount() == 1
			&& method.getParameterTypes()[0].isAnnotation();
	// @formatter:on

	@SuppressWarnings({ "unchecked", "rawtypes" })
	public static <T> T initialize(AnnotatedElement annotatedElement, T instance) {
		if (instance instanceof AnnotationConsumer) {
			Method method = findMethods(instance.getClass(), isAnnotationConsumerAcceptMethod, BOTTOM_UP).get(0);
			Class<? extends Annotation> annotationType = (Class<? extends Annotation>) method.getParameterTypes()[0];
			Annotation annotation = AnnotationUtils.findAnnotation(annotatedElement, annotationType) //
					.orElseThrow(() -> new JUnitException(instance.getClass().getName()
							+ " must be used with an annotation of type " + annotationType.getName()));
			initializeAnnotationConsumer((AnnotationConsumer) instance, annotation);
		}
		return instance;
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
