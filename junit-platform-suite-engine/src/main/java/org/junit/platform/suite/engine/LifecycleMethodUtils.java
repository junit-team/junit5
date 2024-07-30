/*
 * Copyright 2015-2024 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * https://www.eclipse.org/legal/epl-v20.html
 */

package org.junit.platform.suite.engine;

import static org.junit.platform.commons.util.AnnotationUtils.findAnnotatedMethods;
import static org.junit.platform.commons.util.ReflectionUtils.returnsPrimitiveVoid;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.List;

import org.junit.platform.commons.JUnitException;
import org.junit.platform.commons.util.ReflectionUtils;
import org.junit.platform.commons.util.ReflectionUtils.HierarchyTraversalMode;
import org.junit.platform.engine.support.hierarchical.ThrowableCollector;
import org.junit.platform.suite.api.AfterSuite;
import org.junit.platform.suite.api.BeforeSuite;

/**
 * Collection of utilities for working with test lifecycle methods.
 *
 * @since 1.11
 */
final class LifecycleMethodUtils {

	private LifecycleMethodUtils() {
		/* no-op */
	}

	static List<Method> findBeforeSuiteMethods(Class<?> testClass, ThrowableCollector throwableCollector) {
		return findMethodsAndAssertStaticAndNonPrivate(testClass, BeforeSuite.class, HierarchyTraversalMode.TOP_DOWN,
			throwableCollector);
	}

	static List<Method> findAfterSuiteMethods(Class<?> testClass, ThrowableCollector throwableCollector) {
		return findMethodsAndAssertStaticAndNonPrivate(testClass, AfterSuite.class, HierarchyTraversalMode.BOTTOM_UP,
			throwableCollector);
	}

	private static List<Method> findMethodsAndAssertStaticAndNonPrivate(Class<?> testClass,
			Class<? extends Annotation> annotationType, HierarchyTraversalMode traversalMode,
			ThrowableCollector throwableCollector) {

		List<Method> methods = findAnnotatedMethods(testClass, annotationType, traversalMode);
		throwableCollector.execute(() -> methods.forEach(method -> {
			assertVoid(annotationType, method);
			assertStatic(annotationType, method);
			assertNonPrivate(annotationType, method);
			assertNoParameters(annotationType, method);
		}));
		return methods;
	}

	private static void assertStatic(Class<? extends Annotation> annotationType, Method method) {
		if (ReflectionUtils.isNotStatic(method)) {
			throw new JUnitException(String.format("@%s method '%s' must be static.", annotationType.getSimpleName(),
				method.toGenericString()));
		}
	}

	private static void assertNonPrivate(Class<? extends Annotation> annotationType, Method method) {
		if (ReflectionUtils.isPrivate(method)) {
			throw new JUnitException(String.format("@%s method '%s' must not be private.",
				annotationType.getSimpleName(), method.toGenericString()));
		}
	}

	private static void assertVoid(Class<? extends Annotation> annotationType, Method method) {
		if (!returnsPrimitiveVoid(method)) {
			throw new JUnitException(String.format("@%s method '%s' must not return a value.",
				annotationType.getSimpleName(), method.toGenericString()));
		}
	}

	private static void assertNoParameters(Class<? extends Annotation> annotationType, Method method) {
		if (method.getParameterCount() > 0) {
			throw new JUnitException(String.format("@%s method '%s' must not accept parameters.",
				annotationType.getSimpleName(), method.toGenericString()));
		}
	}

}
