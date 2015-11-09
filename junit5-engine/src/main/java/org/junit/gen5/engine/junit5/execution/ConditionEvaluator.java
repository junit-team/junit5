/*
 * Copyright 2015 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.junit.gen5.engine.junit5.execution;

import static org.junit.gen5.commons.util.AnnotationUtils.*;

import java.lang.reflect.Method;
import java.util.Optional;

import org.junit.gen5.api.Condition;
import org.junit.gen5.api.Condition.Context;
import org.junit.gen5.api.Conditional;
import org.junit.gen5.commons.util.ReflectionUtils;
import org.junit.gen5.engine.EngineExecutionContext;
import org.junit.gen5.engine.TestDescriptor;
import org.junit.gen5.engine.junit5.descriptor.ClassTestDescriptor;
import org.junit.gen5.engine.junit5.descriptor.MethodTestDescriptor;
import org.opentestalliance.TestSkippedException;

/**
 * {@code ConditionEvaluator} evaluates {@link Condition Conditions}
 * configured via {@link Conditional @Conditional}.
 *
 * @author Sam Brannen
 * @since 5.0
 * @see Conditional
 * @see Condition
 */
class ConditionEvaluator {

	boolean testEnabled(EngineExecutionContext context, ClassTestDescriptor testDescriptor) {
		return testEnabled(context, testDescriptor, testDescriptor.getTestClass(), null);
	}

	boolean testEnabled(EngineExecutionContext context, MethodTestDescriptor testDescriptor) {
		return testEnabled(context, testDescriptor, testDescriptor.getTestMethod().getDeclaringClass(),
			testDescriptor.getTestMethod());
	}

	private boolean testEnabled(EngineExecutionContext context, TestDescriptor testDescriptor, final Class<?> testClass,
			final Method testMethod) {

		Context conditionContext = new Context() {

			@Override
			public Method getTestMethod() {
				return testMethod;
			}

			@Override
			public Class<?> getTestClass() {
				return testClass;
			}
		};

		Optional<Conditional> classLevelAnno = findAnnotation(testClass, Conditional.class);
		Optional<Conditional> methodLevelAnno = findAnnotation(testMethod, Conditional.class);

		Conditional conditional = classLevelAnno.isPresent() ? classLevelAnno.get()
				: methodLevelAnno.isPresent() ? methodLevelAnno.get() : null;

		if (conditional != null) {
			Class<? extends Condition>[] classes = conditional.value();
			for (Class<? extends Condition> conditionalClass : classes) {
				try {
					Condition condition = ReflectionUtils.newInstance(conditionalClass);

					if (!condition.matches(conditionContext)) {
						Throwable exceptionThrown = new TestSkippedException(String.format(
							"Skipped test method [%s] due to failed condition [%s]",
							(testMethod != null ? testMethod.toGenericString() : "N/A"), conditionalClass.getName()));
						context.getTestExecutionListener().testSkipped(testDescriptor, exceptionThrown);

						// Abort execution of the test completely at this point.
						return false;
					}
				}
				catch (Exception e) {
					throw new IllegalStateException("Failed to evaluate Condition", e);
				}
			}
		}

		return true;
	}

}
