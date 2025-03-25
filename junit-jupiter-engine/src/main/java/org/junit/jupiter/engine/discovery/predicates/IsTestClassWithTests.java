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

import static org.apiguardian.api.API.Status.INTERNAL;

import java.lang.reflect.Method;
import java.util.function.Predicate;

import org.apiguardian.api.API;
import org.junit.platform.commons.support.ReflectionSupport;
import org.junit.platform.commons.util.ReflectionUtils;
import org.junit.platform.engine.support.discovery.DiscoveryIssueReporter;

/**
 * Test if a class is a JUnit Jupiter test class containing executable tests,
 * test factories, test templates, or nested tests.
 *
 * @since 5.0
 */
@API(status = INTERNAL, since = "5.1")
public class IsTestClassWithTests implements Predicate<Class<?>> {

	public final Predicate<Method> isTestOrTestFactoryOrTestTemplateMethod;

	private static final IsPotentialTestContainer isPotentialTestContainer = new IsPotentialTestContainer();

	public final IsNestedTestClass isNestedTestClass = new IsNestedTestClass();

	@API(status = INTERNAL, since = "5.13")
	public IsTestClassWithTests(DiscoveryIssueReporter issueReporter) {
		this.isTestOrTestFactoryOrTestTemplateMethod = new IsTestMethod(issueReporter) //
				.or(new IsTestFactoryMethod(issueReporter)) //
				.or(new IsTestTemplateMethod(issueReporter));
	}

	@Override
	public boolean test(Class<?> candidate) {
		return isPotentialTestContainer.test(candidate)
				&& (hasTestOrTestFactoryOrTestTemplateMethods(candidate) || hasNestedTests(candidate));
	}

	private boolean hasTestOrTestFactoryOrTestTemplateMethods(Class<?> candidate) {
		return ReflectionUtils.isMethodPresent(candidate, isTestOrTestFactoryOrTestTemplateMethod);
	}

	private boolean hasNestedTests(Class<?> candidate) {
		return !ReflectionSupport.findNestedClasses(candidate, isNestedTestClass).isEmpty();
	}

}
