/*
 * Copyright 2015-2025 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * https://www.eclipse.org/legal/epl-v20.html
 */

package org.junit.jupiter.engine.descriptor;

import static org.junit.platform.commons.support.AnnotationSupport.findAnnotatedMethods;
import static org.junit.platform.commons.util.CollectionUtils.toUnmodifiableList;
import static org.junit.platform.engine.support.discovery.DiscoveryIssueReporter.Condition.allOf;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.List;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.platform.commons.support.HierarchyTraversalMode;
import org.junit.platform.commons.support.ModifierSupport;
import org.junit.platform.commons.util.ReflectionUtils;
import org.junit.platform.engine.DiscoveryIssue;
import org.junit.platform.engine.DiscoveryIssue.Severity;
import org.junit.platform.engine.support.descriptor.MethodSource;
import org.junit.platform.engine.support.discovery.DiscoveryIssueReporter;
import org.junit.platform.engine.support.discovery.DiscoveryIssueReporter.Condition;

/**
 * Collection of utilities for working with test lifecycle methods.
 *
 * @since 5.0
 */
final class LifecycleMethodUtils {

	private LifecycleMethodUtils() {
		/* no-op */
	}

	static List<Method> findBeforeAllMethods(Class<?> testClass, boolean requireStatic,
			DiscoveryIssueReporter issueReporter) {
		return findMethodsAndCheckStatic(testClass, requireStatic, BeforeAll.class, HierarchyTraversalMode.TOP_DOWN,
			issueReporter);
	}

	static List<Method> findAfterAllMethods(Class<?> testClass, boolean requireStatic,
			DiscoveryIssueReporter issueReporter) {
		return findMethodsAndCheckStatic(testClass, requireStatic, AfterAll.class, HierarchyTraversalMode.BOTTOM_UP,
			issueReporter);
	}

	static List<Method> findBeforeEachMethods(Class<?> testClass, DiscoveryIssueReporter issueReporter) {
		return findMethodsAndCheckNonStatic(testClass, BeforeEach.class, HierarchyTraversalMode.TOP_DOWN,
			issueReporter);
	}

	static List<Method> findAfterEachMethods(Class<?> testClass, DiscoveryIssueReporter issueReporter) {
		return findMethodsAndCheckNonStatic(testClass, AfterEach.class, HierarchyTraversalMode.BOTTOM_UP,
			issueReporter);
	}

	private static List<Method> findMethodsAndCheckStatic(Class<?> testClass, boolean requireStatic,
			Class<? extends Annotation> annotationType, HierarchyTraversalMode traversalMode,
			DiscoveryIssueReporter issueReporter) {

		Condition<Method> additionalCondition = requireStatic ? isStatic(annotationType, issueReporter) : __ -> true;
		return findMethodsAndCheckVoidReturnType(testClass, annotationType, traversalMode, issueReporter,
			additionalCondition);
	}

	private static List<Method> findMethodsAndCheckNonStatic(Class<?> testClass,
			Class<? extends Annotation> annotationType, HierarchyTraversalMode traversalMode,
			DiscoveryIssueReporter issueReporter) {

		return findMethodsAndCheckVoidReturnType(testClass, annotationType, traversalMode, issueReporter,
			isNotStatic(annotationType, issueReporter));
	}

	private static List<Method> findMethodsAndCheckVoidReturnType(Class<?> testClass,
			Class<? extends Annotation> annotationType, HierarchyTraversalMode traversalMode,
			DiscoveryIssueReporter issueReporter, Condition<? super Method> additionalCondition) {

		return findAnnotatedMethods(testClass, annotationType, traversalMode).stream() //
				.peek(isNotPrivate(annotationType, issueReporter)) //
				.filter(allOf(returnsPrimitiveVoid(annotationType, issueReporter), additionalCondition)) //
				.collect(toUnmodifiableList());
	}

	private static Condition<Method> isStatic(Class<? extends Annotation> annotationType,
			DiscoveryIssueReporter issueReporter) {
		return issueReporter.createReportingCondition(ModifierSupport::isStatic, method -> {
			String message = String.format(
				"@%s method '%s' must be static unless the test class is annotated with @TestInstance(Lifecycle.PER_CLASS).",
				annotationType.getSimpleName(), method.toGenericString());
			return createIssue(Severity.ERROR, message, method);
		});
	}

	private static Condition<Method> isNotStatic(Class<? extends Annotation> annotationType,
			DiscoveryIssueReporter issueReporter) {
		return issueReporter.createReportingCondition(ModifierSupport::isNotStatic, method -> {
			String message = String.format("@%s method '%s' must not be static.", annotationType.getSimpleName(),
				method.toGenericString());
			return createIssue(Severity.ERROR, message, method);
		});
	}

	private static Condition<Method> isNotPrivate(Class<? extends Annotation> annotationType,
			DiscoveryIssueReporter issueReporter) {
		return issueReporter.createReportingCondition(ModifierSupport::isNotPrivate, method -> {
			String message = String.format(
				"@%s method '%s' should not be private. This will be disallowed in a future release.",
				annotationType.getSimpleName(), method.toGenericString());
			return createIssue(Severity.DEPRECATION, message, method);
		});
	}

	private static Condition<Method> returnsPrimitiveVoid(Class<? extends Annotation> annotationType,
			DiscoveryIssueReporter issueReporter) {
		return issueReporter.createReportingCondition(ReflectionUtils::returnsPrimitiveVoid, method -> {
			String message = String.format("@%s method '%s' must not return a value.", annotationType.getSimpleName(),
				method.toGenericString());
			return createIssue(Severity.ERROR, message, method);
		});
	}

	private static DiscoveryIssue createIssue(Severity severity, String message, Method method) {
		return DiscoveryIssue.builder(severity, message).source(MethodSource.from(method)).build();
	}

}
