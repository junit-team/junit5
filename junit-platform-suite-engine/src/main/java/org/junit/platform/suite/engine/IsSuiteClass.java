/*
 * Copyright 2015-2024 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * https://www.eclipse.org/legal/epl-v20.html
 */

package org.junit.platform.suite.engine;

import java.util.function.Predicate;

import org.junit.platform.commons.support.AnnotationSupport;
import org.junit.platform.suite.api.Suite;

/**
 * @since 1.8
 */
final class IsSuiteClass implements Predicate<Class<?>> {

	private static final IsPotentialTestContainer isPotentialTestContainer = new IsPotentialTestContainer();

	@Override
	public boolean test(Class<?> testClass) {
		return isPotentialTestContainer.test(testClass) && hasSuiteAnnotation(testClass);
	}

	private boolean hasSuiteAnnotation(Class<?> testClass) {
		return AnnotationSupport.isAnnotated(testClass, Suite.class);
	}

}
