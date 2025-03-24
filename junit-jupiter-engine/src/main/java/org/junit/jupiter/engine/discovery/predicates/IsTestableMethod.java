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
import static org.junit.platform.commons.util.ReflectionUtils.returnsPrimitiveVoid;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.function.Predicate;

import org.junit.platform.commons.support.ModifierSupport;
import org.junit.platform.commons.util.ReflectionUtils;
import org.junit.platform.engine.DiscoveryIssue;
import org.junit.platform.engine.DiscoveryIssue.Severity;
import org.junit.platform.engine.support.descriptor.MethodSource;
import org.junit.platform.engine.support.discovery.DiscoveryIssueReporter;
import org.junit.platform.engine.support.discovery.DiscoveryIssueReporter.Condition;

/**
 * @since 5.0
 */
abstract class IsTestableMethod implements Predicate<Method> {

	private final Class<? extends Annotation> annotationType;
	private final Condition<Method> condition;

	IsTestableMethod(Class<? extends Annotation> annotationType, boolean mustReturnPrimitiveVoid,
			DiscoveryIssueReporter issueReporter) {
		this.annotationType = annotationType;
		this.condition = isNotStatic(issueReporter) //
				.and(isNotPrivate(issueReporter)) //
				.and(isNotAbstract(issueReporter)) //
				.and(hasCompatibleReturnType(mustReturnPrimitiveVoid, issueReporter));
	}

	@Override
	public boolean test(Method candidate) {
		if (isAnnotated(candidate, this.annotationType)) {
			return condition.test(candidate);
		}
		return false;
	}

	private Condition<Method> isNotStatic(DiscoveryIssueReporter issueReporter) {
		return issueReporter.createReportingCondition(ModifierSupport::isNotStatic,
			method -> createIssue(method, "must not be static"));
	}

	private Condition<Method> isNotPrivate(DiscoveryIssueReporter issueReporter) {
		return issueReporter.createReportingCondition(ModifierSupport::isNotPrivate,
			method -> createIssue(method, "must not be private"));
	}

	private Condition<Method> isNotAbstract(DiscoveryIssueReporter issueReporter) {
		return issueReporter.createReportingCondition(ModifierSupport::isNotAbstract,
			method -> createIssue(method, "must not be abstract"));
	}

	private Condition<Method> hasCompatibleReturnType(boolean mustReturnPrimitiveVoid,
			DiscoveryIssueReporter issueReporter) {
		if (mustReturnPrimitiveVoid) {
			return issueReporter.createReportingCondition(ReflectionUtils::returnsPrimitiveVoid,
				method -> createIssue(method, "must not return a value"));
		}
		else {
			// TODO [#4246] Use `Predicate.not`
			return issueReporter.createReportingCondition(method -> !returnsPrimitiveVoid(method),
				method -> createIssue(method, "must return a value"));
		}
	}

	private DiscoveryIssue createIssue(Method method, String condition) {
		String message = String.format("@%s method '%s' %s. It will be not be executed.",
			this.annotationType.getSimpleName(), method.toGenericString(), condition);
		return DiscoveryIssue.builder(Severity.WARNING, message) //
				.source(MethodSource.from(method)) //
				.build();
	}

}
