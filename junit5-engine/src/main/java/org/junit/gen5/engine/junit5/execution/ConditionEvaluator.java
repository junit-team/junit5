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

import org.junit.gen5.api.extension.ConditionEvaluationResult;
import org.junit.gen5.api.extension.ExtensionContext;
import org.junit.gen5.api.extension.ShouldTestBeExecutedCondition;

/**
 * {@code ConditionEvaluator} evaluates all {@link ShouldTestBeExecutedCondition Conditions}
 * registered in a {@link ExtensionContext}.
 *
 * @since 5.0
 * @see ShouldTestBeExecutedCondition
 */
class ConditionEvaluator {

	private static final ConditionEvaluationResult ENABLED = ConditionEvaluationResult.enabled(
		"No 'disabled' conditions encountered");

	/**
	 * Evaluate all {@link ShouldTestBeExecutedCondition Conditions} registered for the supplied
	 * {@link ExtensionContext}.
	 *
	 * @param context the current {@code TestExecutionContext}
	 * @return the first <em>disabled</em> {@code Result}, or a default
	 * <em>enabled</em> {@code Result} if no disabled conditions are
	 * encountered.
	 */
	ConditionEvaluationResult evaluate(TestExtensionRegistry extensionRegistry, ExtensionContext context) {
		// @formatter:off
//		return extensionRegistry.getRegisteredExtensionClasses(ShouldTestBeExecutedCondition.class)
//				.map(condition -> evaluate(context, condition))
//				.filter(ConditionEvaluationResult::isDisabled)
//				.findFirst()
//				.orElse(ENABLED);
		// @formatter:on
		return null;
	}

	private ConditionEvaluationResult evaluate(ExtensionContext context, ShouldTestBeExecutedCondition condition) {
		//		try {
		//			return condition.shouldTestBeExecuted(context);
		//		}
		//		catch (Exception ex) {
		//			throw new IllegalStateException(
		//				String.format("Failed to evaluate condition [%s]", condition.getClass().getName()), ex);
		//		}
		return null;
	}

}
