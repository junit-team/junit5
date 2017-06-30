/*
 * Copyright 2015-2017 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.junit.jupiter.engine.descriptor;

import static org.junit.platform.commons.util.AnnotationUtils.findAnnotatedMethods;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.List;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.platform.commons.JUnitException;
import org.junit.platform.commons.util.ReflectionUtils;
import org.junit.platform.commons.util.ReflectionUtils.HierarchyTraversalMode;

/**
 * Collection of utilities for working with test lifecycle methods.
 *
 * @since 5.0
 */
final class LifecycleMethodUtils {

	///CLOVER:OFF
	private LifecycleMethodUtils() {
		/* no-op */
	}
	///CLOVER:ON

	static List<Method> findBeforeAllMethods(Class<?> testClass, boolean requireStatic) {
		List<Method> methods = findAnnotatedMethods(testClass, BeforeAll.class, HierarchyTraversalMode.TOP_DOWN);
		if (requireStatic) {
			methods.forEach(method -> assertStatic(BeforeAll.class, method));
		}
		return methods;
	}

	static List<Method> findAfterAllMethods(Class<?> testClass, boolean requireStatic) {
		List<Method> methods = findAnnotatedMethods(testClass, AfterAll.class, HierarchyTraversalMode.BOTTOM_UP);
		if (requireStatic) {
			methods.forEach(method -> assertStatic(AfterAll.class, method));
		}
		return methods;
	}

	static List<Method> findBeforeEachMethods(Class<?> testClass) {
		List<Method> methods = findAnnotatedMethods(testClass, BeforeEach.class, HierarchyTraversalMode.TOP_DOWN);
		methods.forEach(method -> assertNonStatic(BeforeEach.class, method));
		return methods;
	}

	static List<Method> findAfterEachMethods(Class<?> testClass) {
		List<Method> methods = findAnnotatedMethods(testClass, AfterEach.class, HierarchyTraversalMode.BOTTOM_UP);
		methods.forEach(method -> assertNonStatic(AfterEach.class, method));
		return methods;
	}

	private static void assertStatic(Class<? extends Annotation> annotationType, Method method) {
		if (!ReflectionUtils.isStatic(method)) {
			throw new JUnitException(String.format(
				"@%s method '%s' must be static unless the test class is annotated with @TestInstance(Lifecycle.PER_CLASS).",
				annotationType.getSimpleName(), method.toGenericString()));
		}
	}

	private static void assertNonStatic(Class<? extends Annotation> annotationType, Method method) {
		if (ReflectionUtils.isStatic(method)) {
			throw new JUnitException(String.format("@%s method '%s' must not be static.",
				annotationType.getSimpleName(), method.toGenericString()));
		}
	}

}
