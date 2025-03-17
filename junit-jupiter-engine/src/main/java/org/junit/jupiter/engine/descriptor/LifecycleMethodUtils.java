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
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Stream;

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

		return findMethodsAndCheckVoidReturnType(testClass, annotationType, traversalMode, issueReporter) //
				.filter(requireStatic ? isStatic(annotationType, issueReporter) : __ -> true) //
				.collect(toUnmodifiableList());
	}

	private static List<Method> findMethodsAndCheckNonStatic(Class<?> testClass,
			Class<? extends Annotation> annotationType, HierarchyTraversalMode traversalMode,
			DiscoveryIssueReporter issueReporter) {

		return findMethodsAndCheckVoidReturnType(testClass, annotationType, traversalMode, issueReporter) //
				.filter(isNotStatic(annotationType, issueReporter)) //
				.collect(toUnmodifiableList());
	}

	private static Stream<Method> findMethodsAndCheckVoidReturnType(Class<?> testClass,
			Class<? extends Annotation> annotationType, HierarchyTraversalMode traversalMode,
			DiscoveryIssueReporter issueReporter) {

		return findAnnotatedMethods(testClass, annotationType, traversalMode).stream() //
				.filter(returnsPrimitiveVoid(annotationType, issueReporter));
	}

	private static Predicate<Method> isStatic(Class<? extends Annotation> annotationType,
			DiscoveryIssueReporter issueReporter) {
		return DiscoveryIssueReportingPredicate.from(issueReporter, ModifierSupport::isStatic, method -> {
			String message = String.format(
				"@%s method '%s' must be static unless the test class is annotated with @TestInstance(Lifecycle.PER_CLASS).",
				annotationType.getSimpleName(), method.toGenericString());
			return DiscoveryIssue.builder(Severity.ERROR, message).source(MethodSource.from(method)).build();
		});
	}

	private static Predicate<Method> isNotStatic(Class<? extends Annotation> annotationType,
			DiscoveryIssueReporter issueReporter) {
		return DiscoveryIssueReportingPredicate.from(issueReporter, ModifierSupport::isNotStatic, method -> {
			String message = String.format("@%s method '%s' must not be static.", annotationType.getSimpleName(),
				method.toGenericString());
			return DiscoveryIssue.builder(Severity.ERROR, message).source(MethodSource.from(method)).build();
		});
	}

	private static Predicate<Method> returnsPrimitiveVoid(Class<? extends Annotation> annotationType,
			DiscoveryIssueReporter issueReporter) {
		return DiscoveryIssueReportingPredicate.from(issueReporter, ReflectionUtils::returnsPrimitiveVoid, method -> {
			String message = String.format("@%s method '%s' must not return a value.", annotationType.getSimpleName(),
				method.toGenericString());
			return DiscoveryIssue.builder(Severity.ERROR, message).source(MethodSource.from(method)).build();
		});
	}

	abstract static class DiscoveryIssueReportingPredicate<T> implements Predicate<T> {

		static <T> DiscoveryIssueReportingPredicate<T> from(DiscoveryIssueReporter reporter, Predicate<T> predicate,
				Function<T, DiscoveryIssue> issueBuilder) {
			return new DiscoveryIssueReportingPredicate<T>(reporter) {
				@Override
				protected boolean checkCondition(T t) {
					return predicate.test(t);
				}

				@Override
				protected DiscoveryIssue createIssue(T t) {
					return issueBuilder.apply(t);
				}
			};
		}

		private final DiscoveryIssueReporter reporter;

		protected DiscoveryIssueReportingPredicate(DiscoveryIssueReporter reporter) {
			this.reporter = reporter;
		}

		@Override
		public final boolean test(T t) {
			if (checkCondition(t)) {
				return true;
			}
			reporter.reportIssue(createIssue(t));
			return false;
		}

		protected abstract boolean checkCondition(T t);

		protected abstract DiscoveryIssue createIssue(T t);
	}

}
