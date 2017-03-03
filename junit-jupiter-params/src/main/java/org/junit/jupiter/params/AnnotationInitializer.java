/*
 * Copyright 2015-2017 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.junit.jupiter.params;

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Method;
import java.util.function.Predicate;

import org.junit.platform.commons.JUnitException;
import org.junit.platform.commons.util.AnnotationUtils;
import org.junit.platform.commons.util.ReflectionUtils;
import org.junit.platform.commons.util.ReflectionUtils.HierarchyTraversalMode;

class AnnotationInitializer {

	@SuppressWarnings("unchecked")
	static <T> T initialize(AnnotatedElement annotatedElement, T instance) {
		if (instance instanceof AnnotationInitialized) {
			Predicate<Method> methodPredicate = method -> method.getName().equals("initialize")
					&& method.getParameterCount() == 1
					&& Annotation.class.isAssignableFrom(method.getParameterTypes()[0]);
			Method method = ReflectionUtils.findMethods(instance.getClass(), methodPredicate,
				HierarchyTraversalMode.BOTTOM_UP).get(0);
			Class<? extends Annotation> annotationType = (Class<? extends Annotation>) method.getParameterTypes()[0];
			Annotation annotation = AnnotationUtils.findAnnotation(annotatedElement, annotationType) //
					.orElseThrow(() -> new JUnitException(instance.getClass().getName() + " needs to be used with a "
							+ annotationType.getName() + " annotation"));
			callInitialize((AnnotationInitialized) instance, annotation);
		}
		return instance;
	}

	private static <A extends Annotation> void callInitialize(AnnotationInitialized<A> instance, A annotation) {
		try {
			instance.initialize(annotation);
		}
		catch (Exception ex) {
			throw new JUnitException("Failed to initialize instance: " + instance, ex);
		}
	}

}
