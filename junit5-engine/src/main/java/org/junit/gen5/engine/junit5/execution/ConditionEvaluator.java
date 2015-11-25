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

import static org.junit.gen5.commons.util.AnnotationUtils.findAnnotation;

import java.lang.reflect.Method;
import java.util.Optional;

import org.junit.gen5.api.extension.Condition;
import org.junit.gen5.api.extension.Condition.Result;
import org.junit.gen5.api.extension.Conditional;
import org.junit.gen5.api.extension.TestExecutionContext;
import org.junit.gen5.commons.util.ReflectionUtils;

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

	/**
	 * Determine if the test represented by the supplied {@link TestExecutionContext}
	 * is <em>disabled</em> by evaluating all {@link Condition Conditions}
	 * configured via {@link Conditional @Conditional}.
	 */
	Result evaluate(TestExecutionContext context) {

		final Class<?> testClass = context.getTestClass().orElse(null);
		final Method testMethod = context.getTestMethod().orElse(null);

		// TODO Introduce support for finding *all* @Conditional annotations.
		Optional<Conditional> classLevelAnno = findAnnotation(testClass, Conditional.class);
		Optional<Conditional> methodLevelAnno = findAnnotation(testMethod, Conditional.class);

		Conditional conditional = classLevelAnno.isPresent() ? classLevelAnno.get()
				: methodLevelAnno.isPresent() ? methodLevelAnno.get() : null;

		if (conditional != null) {
			Class<? extends Condition>[] classes = conditional.value();
			for (Class<? extends Condition> conditionClass : classes) {
				try {
					Condition condition = ReflectionUtils.newInstance(conditionClass);

					Result result = condition.evaluate(context);
					if (!result.isSuccess()) {
						// We found a failing condition, so there is no need to continue.
						return result;
					}
				}
				catch (Exception e) {
					throw new IllegalStateException(
						String.format("Failed to evaluate condition [%s]", conditionClass.getName()), e);
				}
			}
		}

		return Result.success("No failed conditions encountered");
	}

}
