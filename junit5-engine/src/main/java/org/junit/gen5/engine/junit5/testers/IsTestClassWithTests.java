/*
 * Copyright 2015 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.junit.gen5.engine.junit5.testers;

import java.util.function.Predicate;

import org.junit.gen5.commons.util.ReflectionUtils;

/**
 * @since 5.0
 */
public class IsTestClassWithTests extends ReflectionObjectTester implements Predicate<Class<?>> {

	private IsTestMethod isTestMethod = new IsTestMethod();
	private CanBeTestClass canBeTestClass = new CanBeTestClass();

	@Override
	public boolean test(Class<?> testClassCandidate) {
		if (!canBeTestClass.test(testClassCandidate))
			return false;
		return hasTestMethods(testClassCandidate);
	}

	private boolean hasTestMethods(Class<?> testClassCandidate) {
		return !ReflectionUtils.findMethods(testClassCandidate, isTestMethod,
			ReflectionUtils.MethodSortOrder.HierarchyDown).isEmpty();
	}

}
