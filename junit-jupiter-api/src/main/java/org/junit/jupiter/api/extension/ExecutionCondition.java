/*
 * Copyright 2015-2018 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v20.html
 */

package org.junit.jupiter.api.extension;

import static org.apiguardian.api.API.Status.STABLE;

import org.apiguardian.api.API;

/**
 * {@code ExecutionCondition} defines the {@link Extension} API for
 * programmatic, <em>conditional test execution</em>.
 *
 * <p>An {@code ExecutionCondition} is
 * {@linkplain #evaluateExecutionCondition(ExtensionContext) evaluated}
 * to determine if a given container or test should be executed based on the
 * supplied {@link ExtensionContext}.
 *
 * <p>Implementations must provide a no-args constructor.
 *
 * @since 5.0
 * @see org.junit.jupiter.api.Disabled
 */
@FunctionalInterface
@API(status = STABLE, since = "5.0")
public interface ExecutionCondition extends Extension {

	/**
	 * Evaluate this condition for the supplied {@link ExtensionContext}.
	 *
	 * <p>An {@linkplain ConditionEvaluationResult#enabled enabled} result
	 * indicates that the container or test should be executed; whereas, a
	 * {@linkplain ConditionEvaluationResult#disabled disabled} result
	 * indicates that the container or test should not be executed.
	 *
	 * @param context the current extension context; never {@code null}
	 * @return the result of evaluating this condition; never {@code null}
	 */
	ConditionEvaluationResult evaluateExecutionCondition(ExtensionContext context);

}
