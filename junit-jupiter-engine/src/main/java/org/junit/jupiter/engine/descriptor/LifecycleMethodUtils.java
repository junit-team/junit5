/*
 * Copyright 2015-2020 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * https://www.eclipse.org/legal/epl-v20.html
 */

package org.junit.jupiter.engine.descriptor;

import static org.junit.platform.commons.util.AnnotationUtils.findAnnotatedMethods;
import static org.junit.platform.commons.util.ReflectionUtils.returnsVoid;

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

	private LifecycleMethodUtils() {
		/* no-op */
	}

	static List<Method> findBeforeAllMethods(Class<?> testClass, boolean requireStatic) {
		return findMethodsAndAssertStatic(testClass, requireStatic, BeforeAll.class, HierarchyTraversalMode.TOP_DOWN);
	}

	static List<Method> findAfterAllMethods(Class<?> testClass, boolean requireStatic) {
		return findMethodsAndAssertStatic(testClass, requireStatic, AfterAll.class, HierarchyTraversalMode.BOTTOM_UP);
	}

	static List<Method> findBeforeEachMethods(Class<?> testClass) {
		return findMethodsAndAssertNonStatic(testClass, BeforeEach.class, HierarchyTraversalMode.TOP_DOWN);
	}

	static List<Method> findAfterEachMethods(Class<?> testClass) {
		return findMethodsAndAssertNonStatic(testClass, AfterEach.class, HierarchyTraversalMode.BOTTOM_UP);
	}

	private static void assertStatic(Class<? extends Annotation> annotationType, Method method) {
		if (ReflectionUtils.isNotStatic(method)) {
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

	private static void assertVoid(Class<? extends Annotation> annotationType, Method method) {
		if (!returnsVoid(method)) {
			throw new JUnitException(String.format("@%s method '%s' must not return a value.",
				annotationType.getSimpleName(), method.toGenericString()));
		}
	}

	private static List<Method> findMethodsAndAssertStatic(Class<?> testClass, boolean requireStatic,
			Class<? extends Annotation> annotationType, HierarchyTraversalMode traversalMode) {
		List<Method> methods = findMethodsAndCheckVoidReturnType(testClass, annotationType, traversalMode);
		if (requireStatic) {
			methods.forEach(method -> assertStatic(annotationType, method));
		}
		return methods;
	}

	private static List<Method> findMethodsAndAssertNonStatic(Class<?> testClass,
			Class<? extends Annotation> annotationType, HierarchyTraversalMode traversalMode) {
		List<Method> methods = findMethodsAndCheckVoidReturnType(testClass, annotationType, traversalMode);
		methods.forEach(method -> assertNonStatic(annotationType, method));
		return methods;
	}

	private static List<Method> findMethodsAndCheckVoidReturnType(Class<?> testClass,
			Class<? extends Annotation> annotationType, HierarchyTraversalMode traversalMode) {
		List<Method> methods = findAnnotatedMethods(testClass, annotationType, traversalMode);
		methods.forEach(method -> assertVoid(annotationType, method));
		return methods;
	}

}
