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

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.platform.commons.support.HierarchyTraversalMode;
import org.junit.platform.commons.support.ModifierSupport;
import org.junit.platform.commons.util.Preconditions;
import org.junit.platform.commons.util.ReflectionUtils;
import org.junit.platform.engine.DiscoveryIssue;
import org.junit.platform.engine.DiscoveryIssue.Severity;
import org.junit.platform.engine.support.descriptor.MethodSource;
import org.junit.platform.engine.support.discovery.DiscoveryIssueReporter;

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

		Predicate<Method> additionalCondition = requireStatic ? isStatic(annotationType, issueReporter) : __ -> true;
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
			DiscoveryIssueReporter issueReporter, Predicate<? super Method> additionalCondition) {

		return findAnnotatedMethods(testClass, annotationType, traversalMode).stream() //
				.peek(isNotPrivate(annotationType, issueReporter)) //
				.filter(allOf(returnsPrimitiveVoid(annotationType, issueReporter), additionalCondition)) //
				.collect(toUnmodifiableList());
	}

	private static Predicate<Method> isStatic(Class<? extends Annotation> annotationType,
			DiscoveryIssueReporter issueReporter) {
		return DiscoveryIssueReportingPredicate.from(ModifierSupport::isStatic, issueReporter, method -> {
			String message = String.format(
				"@%s method '%s' must be static unless the test class is annotated with @TestInstance(Lifecycle.PER_CLASS).",
				annotationType.getSimpleName(), method.toGenericString());
			return createIssue(Severity.ERROR, message, method);
		});
	}

	private static Predicate<Method> isNotStatic(Class<? extends Annotation> annotationType,
			DiscoveryIssueReporter issueReporter) {
		return DiscoveryIssueReportingPredicate.from(ModifierSupport::isNotStatic, issueReporter, method -> {
			String message = String.format("@%s method '%s' must not be static.", annotationType.getSimpleName(),
				method.toGenericString());
			return createIssue(Severity.ERROR, message, method);
		});
	}

	private static Consumer<Method> isNotPrivate(Class<? extends Annotation> annotationType,
			DiscoveryIssueReporter issueReporter) {
		return DiscoveryIssueReportingPredicate.from(ModifierSupport::isNotPrivate, issueReporter, method -> {
			String message = String.format(
				"@%s method '%s' should not be private. This will be disallowed in a future release.",
				annotationType.getSimpleName(), method.toGenericString());
			return createIssue(Severity.DEPRECATION, message, method);
		});
	}

	private static Predicate<Method> returnsPrimitiveVoid(Class<? extends Annotation> annotationType,
			DiscoveryIssueReporter issueReporter) {
		return DiscoveryIssueReportingPredicate.from(ReflectionUtils::returnsPrimitiveVoid, issueReporter, method -> {
			String message = String.format("@%s method '%s' must not return a value.", annotationType.getSimpleName(),
				method.toGenericString());
			return createIssue(Severity.ERROR, message, method);
		});
	}

	private static DiscoveryIssue createIssue(Severity severity, String message, Method method) {
		return DiscoveryIssue.builder(severity, message).source(MethodSource.from(method)).build();
	}

	@SafeVarargs
	@SuppressWarnings("varargs")
	private static <T> Predicate<T> allOf(Predicate<? super T>... predicates) {
		Preconditions.notNull(predicates, "predicates must not be null");
		Preconditions.notEmpty(predicates, "predicates must not be empty");
		Preconditions.containsNoNullElements(predicates, "predicates must not contain null elements");
		return value -> {
			boolean result = true;
			for (Predicate<? super T> predicate : predicates) {
				result &= predicate.test(value);
			}
			return result;
		};
	}

	private abstract static class DiscoveryIssueReportingPredicate<T> implements Predicate<T>, Consumer<T> {

		static <T> DiscoveryIssueReportingPredicate<T> from(Predicate<T> predicate, DiscoveryIssueReporter reporter,
				Function<T, DiscoveryIssue> issueBuilder) {
			return new DiscoveryIssueReportingPredicate<T>(reporter) {
				@Override
				protected boolean checkCondition(T value) {
					return predicate.test(value);
				}

				@Override
				protected DiscoveryIssue createIssue(T value) {
					return issueBuilder.apply(value);
				}
			};
		}

		private final DiscoveryIssueReporter reporter;

		protected DiscoveryIssueReportingPredicate(DiscoveryIssueReporter reporter) {
			this.reporter = reporter;
		}

		@Override
		public final boolean test(T value) {
			if (checkCondition(value)) {
				return true;
			}
			reporter.reportIssue(createIssue(value));
			return false;
		}

		@Override
		public void accept(T value) {
			test(value);
		}

		protected abstract boolean checkCondition(T value);

		protected abstract DiscoveryIssue createIssue(T value);
	}

}
