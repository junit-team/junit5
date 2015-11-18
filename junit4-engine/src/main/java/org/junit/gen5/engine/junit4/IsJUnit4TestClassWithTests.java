/*
 * Copyright 2015 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.junit.gen5.engine.junit4;

import static org.junit.gen5.commons.util.ReflectionUtils.MethodSortOrder.HierarchyDown;

import java.util.function.Predicate;

import org.junit.gen5.commons.util.ReflectionUtils;
import org.junit.runner.RunWith;

/**
 * @since 5.0
 */
public class IsJUnit4TestClassWithTests implements Predicate<Class<?>> {

	private static final IsJUnit4TestMethod isTestMethod = new IsJUnit4TestMethod();

	private static final CanBeJUnit4TestClass canBeTestClass = new CanBeJUnit4TestClass();

	@Override
	public boolean test(Class<?> testClassCandidate) {
		if (!canBeTestClass.test(testClassCandidate))
			return false;
		//Don't use AnnotationUtils.hasAnnotation since JUnit4 does not support meta annotations
		return testClassCandidate.isAnnotationPresent(RunWith.class) || hasTestMethods(testClassCandidate);
	}

	private boolean hasTestMethods(Class<?> testClassCandidate) {
		return !ReflectionUtils.findMethods(testClassCandidate, isTestMethod, HierarchyDown).isEmpty();
	}

}
