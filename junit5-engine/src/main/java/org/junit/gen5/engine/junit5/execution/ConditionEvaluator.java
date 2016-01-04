/*
 * Copyright 2015-2016 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.junit.gen5.engine.junit5.execution;

import org.junit.gen5.api.extension.ConditionEvaluationResult;
import org.junit.gen5.api.extension.ContainerExecutionCondition;
import org.junit.gen5.api.extension.ContainerExtensionContext;
import org.junit.gen5.api.extension.TestExecutionCondition;
import org.junit.gen5.api.extension.TestExtensionContext;

/**
 * {@code ConditionEvaluator} evaluates {@link ContainerExecutionCondition}
 * and {@link TestExecutionCondition} extensions.
 *
 * @since 5.0
 * @see ContainerExecutionCondition
 * @see TestExecutionCondition
 */
public class ConditionEvaluator {

	private static final ConditionEvaluationResult ENABLED = ConditionEvaluationResult.enabled(
		"No 'disabled' conditions encountered");

	/**
	 * Evaluate all {@link ContainerExecutionCondition}
	 * extensions registered for the supplied {@link ContainerExtensionContext}.
	 *
	 * @param context the current {@code ContainerExtensionContext}
	 * @return the first <em>disabled</em> {@code ConditionEvaluationResult},
	 * or a default <em>enabled</em> {@code ConditionEvaluationResult} if no
	 * disabled conditions are encountered
	 */
	public ConditionEvaluationResult evaluateForContainer(TestExtensionRegistry extensionRegistry,
			ContainerExtensionContext context) {
		// @formatter:off
		return extensionRegistry.stream(ContainerExecutionCondition.class, TestExtensionRegistry.ApplicationOrder.FORWARD)
				.map(extensionPoint -> extensionPoint.getExtensionPoint())
				.map(condition -> evaluate(condition, context))
				.filter(ConditionEvaluationResult::isDisabled)
				.findFirst()
				.orElse(ENABLED);
		// @formatter:on
	}

	/**
	 * Evaluate all {@link TestExecutionCondition}
	 * extensions registered for the supplied {@link TestExtensionContext}.
	 *
	 * @param context the current {@code TestExtensionContext}
	 * @return the first <em>disabled</em> {@code ConditionEvaluationResult},
	 * or a default <em>enabled</em> {@code ConditionEvaluationResult} if no
	 * disabled conditions are encountered
	 */
	public ConditionEvaluationResult evaluateForTest(TestExtensionRegistry extensionRegistry,
			TestExtensionContext context) {
		// @formatter:off
		return extensionRegistry.stream(TestExecutionCondition.class, TestExtensionRegistry.ApplicationOrder.FORWARD)
				.map(extensionPoint -> extensionPoint.getExtensionPoint())
				.map(condition -> evaluate(condition, context))
				.filter(ConditionEvaluationResult::isDisabled)
				.findFirst()
				.orElse(ENABLED);
		// @formatter:on
	}

	private ConditionEvaluationResult evaluate(ContainerExecutionCondition condition,
			ContainerExtensionContext context) {

		try {
			return condition.evaluate(context);
		}
		catch (Exception ex) {
			throw new IllegalStateException(
				String.format("Failed to evaluate condition [%s]", condition.getClass().getName()), ex);
		}
	}

	private ConditionEvaluationResult evaluate(TestExecutionCondition condition, TestExtensionContext context) {
		try {
			return condition.evaluate(context);
		}
		catch (Exception ex) {
			throw new IllegalStateException(
				String.format("Failed to evaluate condition [%s]", condition.getClass().getName()), ex);
		}
	}

}
