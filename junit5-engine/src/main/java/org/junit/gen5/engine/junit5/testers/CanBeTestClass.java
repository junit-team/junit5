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
public class CanBeTestClass extends ReflectionObjectTester implements Predicate<Class<?>> {

	private IsTestMethod methodTester = new IsTestMethod();

	@Override
	public boolean test(Class<?> testClassCandidate) {
		if (isAbstract(testClassCandidate))
			return false;
		if (testClassCandidate.isLocalClass())
			return false;
		return hasTestMethods(testClassCandidate);
	}

	private boolean hasTestMethods(Class<?> testClassCandidate) {
		return !ReflectionUtils.findMethods(testClassCandidate, methodTester::test,
			ReflectionUtils.MethodSortOrder.HierarchyDown).isEmpty();
	}

}
