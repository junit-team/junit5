/*
 * Copyright 2015-2016 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.junit.gen5.engine.junit5.descriptor;

import static org.junit.gen5.commons.util.AnnotationUtils.findAnnotatedMethods;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.List;

import org.junit.gen5.api.AfterAll;
import org.junit.gen5.api.AfterEach;
import org.junit.gen5.api.BeforeAll;
import org.junit.gen5.api.BeforeEach;
import org.junit.gen5.commons.JUnitException;
import org.junit.gen5.commons.util.ReflectionUtils;
import org.junit.gen5.commons.util.ReflectionUtils.MethodSortOrder;

/**
 * Collection of utilities for working with test lifecycle methods.
 *
 * @since 5.0
 */
final class LifecycleMethodUtils {

	private LifecycleMethodUtils() {
		/* no-op */
	}

	static List<Method> findBeforeAllMethods(Class<?> testClass) {
		List<Method> methods = findAnnotatedMethods(testClass, BeforeAll.class, MethodSortOrder.HierarchyDown);
		methods.forEach(method -> assertStatic(BeforeAll.class, method));
		return methods;
	}

	static List<Method> findAfterAllMethods(Class<?> testClass) {
		List<Method> methods = findAnnotatedMethods(testClass, AfterAll.class, MethodSortOrder.HierarchyUp);
		methods.forEach(method -> assertStatic(AfterAll.class, method));
		return methods;
	}

	static List<Method> findBeforeEachMethods(Class<?> testClass) {
		List<Method> methods = findAnnotatedMethods(testClass, BeforeEach.class, MethodSortOrder.HierarchyDown);
		methods.forEach(method -> assertNonStatic(BeforeEach.class, method));
		return methods;
	}

	static List<Method> findAfterEachMethods(Class<?> testClass) {
		List<Method> methods = findAnnotatedMethods(testClass, AfterEach.class, MethodSortOrder.HierarchyUp);
		methods.forEach(method -> assertNonStatic(AfterEach.class, method));
		return methods;
	}

	private static void assertStatic(Class<? extends Annotation> annotationType, Method method) {
		if (!ReflectionUtils.isStatic(method)) {
			throw new JUnitException(String.format("@%s method '%s' must be static.", annotationType.getSimpleName(),
				method.toGenericString()));
		}
	}

	private static void assertNonStatic(Class<? extends Annotation> annotationType, Method method) {
		if (ReflectionUtils.isStatic(method)) {
			throw new JUnitException(String.format("@%s method '%s' must not be static.",
				annotationType.getSimpleName(), method.toGenericString()));
		}
	}

}
