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

import java.util.Arrays;
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

	private static final Result ENABLED = Result.enabled("No 'disabled' conditions encountered");

	/**
	 * Evaluate all {@link Condition Conditions} configured via
	 * {@link Conditional @Conditional} for the supplied {@link TestExecutionContext}.
	 *
	 * @param context the current {@code TestExecutionContext}
	 * @return the first <em>disabled</em> {@code Result}, or an <em>enabled</em>
	 * {@code Result} if no disabled conditions are encountered.
	 */
	Result evaluate(TestExecutionContext context) {
		Result result = ENABLED;

		// TODO Introduce support for finding *all* @Conditional annotations.
		Optional<Conditional> classLevelAnno = findAnnotation(context.getTestClass(), Conditional.class);
		Optional<Conditional> methodLevelAnno = findAnnotation(context.getTestMethod(), Conditional.class);

		Conditional conditional = classLevelAnno.orElse(methodLevelAnno.orElse(null));
		if (conditional != null) {
			// @formatter:off
			result = Arrays.stream(conditional.value())
					.map(clazz -> evaluate(context, clazz))
					.filter(Result::isDisabled)
					.findFirst()
					.orElse(ENABLED);
			// @formatter:on
		}

		return result;
	}

	private Result evaluate(TestExecutionContext context, Class<? extends Condition> conditionClass) {
		try {
			Condition condition = ReflectionUtils.newInstance(conditionClass);
			return condition.evaluate(context);
		}
		catch (Exception e) {
			throw new IllegalStateException(
				String.format("Failed to evaluate condition [%s]", conditionClass.getName()), e);
		}
	}

}
