/*
 * Copyright 2015-2017 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.junit.jupiter.params.support;

import static org.junit.platform.commons.meta.API.Usage.Internal;

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Method;
import java.util.function.Predicate;

import org.junit.platform.commons.JUnitException;
import org.junit.platform.commons.meta.API;
import org.junit.platform.commons.util.AnnotationUtils;
import org.junit.platform.commons.util.ReflectionUtils;
import org.junit.platform.commons.util.ReflectionUtils.HierarchyTraversalMode;

@API(Internal)
public final class AnnotationInitializer {

	@SuppressWarnings("unchecked")
	public static <T> T initialize(AnnotatedElement annotatedElement, T instance) {
		if (instance instanceof AnnotationConsumer) {
			Predicate<Method> methodPredicate = method -> method.getName().equals("accept")
					&& method.getParameterCount() == 1
					&& Annotation.class.isAssignableFrom(method.getParameterTypes()[0]);
			Method method = ReflectionUtils.findMethods(instance.getClass(), methodPredicate,
				HierarchyTraversalMode.BOTTOM_UP).get(0);
			Class<? extends Annotation> annotationType = (Class<? extends Annotation>) method.getParameterTypes()[0];
			Annotation annotation = AnnotationUtils.findAnnotation(annotatedElement, annotationType) //
					.orElseThrow(() -> new JUnitException(instance.getClass().getName() + " needs to be used with a "
							+ annotationType.getName() + " annotation"));
			callInitialize((AnnotationConsumer) instance, annotation);
		}
		return instance;
	}

	private static <A extends Annotation> void callInitialize(AnnotationConsumer<A> instance, A annotation) {
		try {
			instance.accept(annotation);
		}
		catch (Exception ex) {
			throw new JUnitException("Failed to initialize instance: " + instance, ex);
		}
	}

}
