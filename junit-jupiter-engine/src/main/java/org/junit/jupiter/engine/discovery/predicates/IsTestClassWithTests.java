/*
 * Copyright 2015-2024 the original author or authors.
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
import org.junit.platform.commons.util.ReflectionUtils;

/**
 * Test if a class is a JUnit Jupiter test class containing executable tests,
 * test factories, test templates, or nested tests.
 *
 * @since 5.0
 */
@API(status = INTERNAL, since = "5.1")
public class IsTestClassWithTests implements Predicate<Class<?>> {

	private static final IsTestMethod isTestMethod = new IsTestMethod();

	private static final IsTestFactoryMethod isTestFactoryMethod = new IsTestFactoryMethod();

	private static final IsTestTemplateMethod isTestTemplateMethod = new IsTestTemplateMethod();

	public static final Predicate<Method> isTestOrTestFactoryOrTestTemplateMethod = isTestMethod.or(
		isTestFactoryMethod).or(isTestTemplateMethod);

	private static final IsPotentialTestContainer isPotentialTestContainer = new IsPotentialTestContainer();

	private static final IsNestedTestClass isNestedTestClass = new IsNestedTestClass();

	@Override
	public boolean test(Class<?> candidate) {
		return isPotentialTestContainer.test(candidate)
				&& (hasTestOrTestFactoryOrTestTemplateMethods(candidate) || hasNestedTests(candidate));
	}

	private boolean hasTestOrTestFactoryOrTestTemplateMethods(Class<?> candidate) {
		return ReflectionUtils.isMethodPresent(candidate, isTestOrTestFactoryOrTestTemplateMethod);
	}

	private boolean hasNestedTests(Class<?> candidate) {
		return !ReflectionUtils.findNestedClasses(candidate, isNestedTestClass).isEmpty();
	}

}
