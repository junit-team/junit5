/*
 * Copyright 2015-2017 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.junit.jupiter.engine.discovery.predicates;

import java.lang.reflect.Method;
import java.util.function.Predicate;

import org.junit.platform.commons.util.ReflectionUtils;

/**
 * Test if a class is a JUnit Jupiter test class containing executable tests,
 * test factories, or nested tests.
 *
 * @since 5.0
 */
class IsTestClassWithTests implements Predicate<Class<?>> {

	private static final IsTestMethod isTestMethod = new IsTestMethod();

	private static final IsTestFactoryMethod isTestFactoryMethod = new IsTestFactoryMethod();

	private static final IsTestTemplateMethod isTestTemplateMethod = new IsTestTemplateMethod();

	private static final Predicate<Method> isTestOrTestFactoryOrTestTemplateMethod = isTestMethod.or(
		isTestFactoryMethod).or(isTestTemplateMethod);

	private static final IsPotentialTestContainer isPotentialTestContainer = new IsPotentialTestContainer();

	private static final IsNestedTestClass isNestedTestClass = new IsNestedTestClass();

	@Override
	public boolean test(Class<?> candidate) {
		// please do not collapse into single return
		if (!isPotentialTestContainer.test(candidate)) {
			return false;
		}
		return hasTestOrTestFactoryOrTestTemplateMethods(candidate) || hasNestedTests(candidate);
	}

	private boolean hasTestOrTestFactoryOrTestTemplateMethods(Class<?> candidate) {
		return ReflectionUtils.isMethodPresent(candidate, isTestOrTestFactoryOrTestTemplateMethod);
	}

	private boolean hasNestedTests(Class<?> candidate) {
		return !ReflectionUtils.findNestedClasses(candidate, isNestedTestClass).isEmpty();
	}

}
