/*
 * Copyright 2015-2017 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.junit.vintage.engine.discovery;

import static java.util.logging.Level.WARNING;

import java.lang.reflect.Method;
import java.util.function.Predicate;
import java.util.logging.Logger;

import org.junit.internal.builders.AllDefaultPossibilitiesBuilder;
import org.junit.internal.builders.AnnotatedBuilder;
import org.junit.internal.builders.JUnit4Builder;
import org.junit.platform.commons.util.ReflectionUtils;
import org.junit.runner.Runner;
import org.junit.runners.model.RunnerBuilder;

/**
 * Customization of {@link AllDefaultPossibilitiesBuilder} from JUnit 4 to
 * ignore certain classes that would otherwise be reported as errors or cause
 * infinite recursion.
 *
 * @since 4.12
 * @see DefensiveAnnotatedBuilder
 * @see DefensiveJUnit4Builder
 */
class DefensiveAllDefaultPossibilitiesBuilder extends AllDefaultPossibilitiesBuilder {

	private static final Logger LOG = Logger.getLogger(DefensiveAllDefaultPossibilitiesBuilder.class.getName());

	private final AnnotatedBuilder annotatedBuilder;
	private final DefensiveJUnit4Builder defensiveJUnit4Builder;

	DefensiveAllDefaultPossibilitiesBuilder() {
		super(true);
		annotatedBuilder = new DefensiveAnnotatedBuilder(this);
		defensiveJUnit4Builder = new DefensiveJUnit4Builder();
	}

	@Override
	protected AnnotatedBuilder annotatedBuilder() {
		return annotatedBuilder;
	}

	@Override
	protected JUnit4Builder junit4Builder() {
		return defensiveJUnit4Builder;
	}

	/**
	 * Customization of {@link AnnotatedBuilder} that ignores classes annotated
	 * with {@code @RunWith(JUnitPlatform.class)} to avoid infinite recursion.
	 */
	private static class DefensiveAnnotatedBuilder extends AnnotatedBuilder {

		DefensiveAnnotatedBuilder(RunnerBuilder suiteBuilder) {
			super(suiteBuilder);
		}

		@Override
		public Runner buildRunner(Class<? extends Runner> runnerClass, Class<?> testClass) throws Exception {
			// Referenced by name because it might not be available at runtime.
			if ("org.junit.platform.runner.JUnitPlatform".equals(runnerClass.getName())) {
				LOG.log(WARNING, () -> "Ignoring test class using JUnitPlatform runner: " + testClass.getName());
				return null;
			}
			return super.buildRunner(runnerClass, testClass);
		}
	}

	/**
	 * Customization of {@link JUnit4Builder} that ignores classes that do not
	 * contain any test methods in order not to report errors for them.
	 */
	private static class DefensiveJUnit4Builder extends JUnit4Builder {

		private static final Predicate<Method> isPotentialJUnit4TestMethod = new IsPotentialJUnit4TestMethod();

		@Override
		public Runner runnerForClass(Class<?> testClass) throws Throwable {
			if (containsTestMethods(testClass)) {
				return super.runnerForClass(testClass);
			}
			return null;
		}

		private boolean containsTestMethods(Class<?> testClass) {
			return ReflectionUtils.isMethodPresent(testClass, isPotentialJUnit4TestMethod);
		}
	}

}
