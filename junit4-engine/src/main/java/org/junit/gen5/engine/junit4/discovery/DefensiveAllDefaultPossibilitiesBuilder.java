/*
 * Copyright 2015-2016 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.junit.gen5.engine.junit4.discovery;

import java.lang.reflect.Method;
import java.util.List;
import java.util.function.Predicate;

import org.junit.gen5.commons.util.ReflectionUtils;
import org.junit.internal.builders.AllDefaultPossibilitiesBuilder;
import org.junit.internal.builders.JUnit4Builder;
import org.junit.runner.Runner;

class DefensiveAllDefaultPossibilitiesBuilder extends AllDefaultPossibilitiesBuilder {

	private final DefensiveJUnit4Builder defensiveJUnit4Builder = new DefensiveJUnit4Builder();

	DefensiveAllDefaultPossibilitiesBuilder() {
		super(true);
	}

	@Override
	protected JUnit4Builder junit4Builder() {
		return defensiveJUnit4Builder;
	}

	private static class DefensiveJUnit4Builder extends JUnit4Builder {

		private final Predicate<Method> hasTestAnnotation = new IsPotentialJUnit4TestMethod();

		@Override
		public Runner runnerForClass(Class<?> testClass) throws Throwable {
			if (containsTestMethods(testClass)) {
				return super.runnerForClass(testClass);
			}
			return null;
		}

		private boolean containsTestMethods(Class<?> testClass) {
			List<Method> testMethods = ReflectionUtils.findMethods(testClass, hasTestAnnotation);
			return !testMethods.isEmpty();
		}
	}
}