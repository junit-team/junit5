/*
 * Copyright 2015-2025 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * https://www.eclipse.org/legal/epl-v20.html
 */

package org.junit.jupiter.engine.discovery.predicates;

import static org.junit.platform.commons.support.AnnotationSupport.isAnnotated;

import java.lang.annotation.Annotation;
import java.util.function.BiFunction;
import java.util.function.Predicate;

import org.junit.jupiter.engine.support.MethodAdapter;
import org.junit.platform.engine.DiscoveryIssue;
import org.junit.platform.engine.DiscoveryIssue.Severity;
import org.junit.platform.engine.support.descriptor.MethodSource;
import org.junit.platform.engine.support.discovery.DiscoveryIssueReporter;
import org.junit.platform.engine.support.discovery.DiscoveryIssueReporter.Condition;

/**
 * @since 5.0
 */
abstract class IsTestableMethod implements Predicate<MethodAdapter> {

	private final Class<? extends Annotation> annotationType;
	private final Condition<MethodAdapter> condition;

	IsTestableMethod(Class<? extends Annotation> annotationType,
			BiFunction<Class<? extends Annotation>, DiscoveryIssueReporter, Condition<MethodAdapter>> returnTypeConditionFactory,
			DiscoveryIssueReporter issueReporter) {
		this.annotationType = annotationType;
		this.condition = isNotStatic(annotationType, issueReporter) //
				.and(isNotPrivate(annotationType, issueReporter)) //
				.and(isNotAbstract(annotationType, issueReporter)) //
				.and(returnTypeConditionFactory.apply(annotationType, issueReporter));
	}

	@Override
	public boolean test(MethodAdapter candidate) {
		if (isAnnotated(candidate.getMethod(), this.annotationType)) {
			return condition.check(candidate);
		}
		return false;
	}

	private static Condition<MethodAdapter> isNotStatic(Class<? extends Annotation> annotationType,
			DiscoveryIssueReporter issueReporter) {
		return issueReporter.createReportingCondition(MethodAdapter::isNotStatic,
			method -> createIssue(annotationType, method, "must not be static"));
	}

	private static Condition<MethodAdapter> isNotPrivate(Class<? extends Annotation> annotationType,
			DiscoveryIssueReporter issueReporter) {
		return issueReporter.createReportingCondition(MethodAdapter::isNotPrivate,
			method -> createIssue(annotationType, method, "must not be private"));
	}

	private static Condition<MethodAdapter> isNotAbstract(Class<? extends Annotation> annotationType,
			DiscoveryIssueReporter issueReporter) {
		return issueReporter.createReportingCondition(MethodAdapter::isNotAbstract,
			method -> createIssue(annotationType, method, "must not be abstract"));
	}

	protected static Condition<MethodAdapter> hasVoidReturnType(Class<? extends Annotation> annotationType,
			DiscoveryIssueReporter issueReporter) {
		return issueReporter.createReportingCondition(method -> method.getReturnType() == void.class,
			method -> createIssue(annotationType, method, "must not return a value"));
	}

	protected static DiscoveryIssue createIssue(Class<? extends Annotation> annotationType, MethodAdapter method,
			String condition) {
		String message = String.format("@%s method '%s' %s. It will not be executed.", annotationType.getSimpleName(),
			method.toGenericString(), condition);
		return DiscoveryIssue.builder(Severity.WARNING, message) //
				.source(MethodSource.from(method.getMethod())) //
				.build();
	}

}
