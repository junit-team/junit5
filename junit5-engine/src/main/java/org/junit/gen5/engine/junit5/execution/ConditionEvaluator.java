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

import org.junit.gen5.api.extension.Condition;
import org.junit.gen5.api.extension.Condition.Result;
import org.junit.gen5.api.extension.TestExecutionContext;

/**
 * {@code ConditionEvaluator} evaluates all {@link Condition Conditions}
 * registered in a {@link TestExecutionContext}.
 *
 * @since 5.0
 * @see Condition
 */
class ConditionEvaluator {

	private static final Result ENABLED = Result.enabled("No 'disabled' conditions encountered");

	/**
	 * Evaluate all {@link Condition Conditions} registered for the supplied
	 * {@link TestExecutionContext}.
	 *
	 * @param context the current {@code TestExecutionContext}
	 * @return the first <em>disabled</em> {@code Result}, or a default
	 * <em>enabled</em> {@code Result} if no disabled conditions are
	 * encountered.
	 */
	Result evaluate(TestExecutionContext context) {
		// @formatter:off
		return context.getExtensions(Condition.class)
				.map(condition -> evaluate(context, condition))
				.filter(Result::isDisabled)
				.findFirst()
				.orElse(ENABLED);
		// @formatter:on
	}

	private Result evaluate(TestExecutionContext context, Condition condition) {
		try {
			return condition.evaluate(context);
		}
		catch (Exception ex) {
			throw new IllegalStateException(
				String.format("Failed to evaluate condition [%s]", condition.getClass().getName()), ex);
		}
	}

}
