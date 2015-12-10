/*
 * Copyright 2015 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.junit.gen5.api.extension;

/**
 * {@code ShouldTestBeExecutedCondition} defines the {@link ExtensionPoint} API for programmatic,
 * <em>conditional test execution</em>.
 *
 * <p>A {@code ShouldTestBeExecutedCondition} is evaluated with {@linkplain #shouldTestBeExecuted evaluated} to determine
 * if a given test should be executed based on the
 * supplied {@link TestExtensionContext}.
 *
 * <p>Implementations must provide a no-args constructor.
 *
 * @since 5.0
 * @see org.junit.gen5.api.Disabled
 */
@FunctionalInterface
public interface ShouldTestBeExecutedCondition extends ExtensionPoint {

	/**
	 * Evaluate this condition for the supplied {@link TestExtensionContext}.
	 *
	 * <p>An {@linkplain ConditionEvaluationResult#enabled enabled} result indicates that the
	 * test should be executed; whereas, a {@linkplain ConditionEvaluationResult#disabled disabled}
	 * result indicates that the test should not be executed.
	 *
	 * @param context the current {@code TestExtensionContext}
	 */
	ConditionEvaluationResult shouldTestBeExecuted(TestExtensionContext context);

}
