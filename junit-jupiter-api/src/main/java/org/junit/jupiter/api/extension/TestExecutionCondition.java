/*
 * Copyright 2015-2017 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.junit.jupiter.api.extension;

import static org.junit.platform.commons.meta.API.Usage.Experimental;

import org.junit.platform.commons.meta.API;

/**
 * {@code TestExecutionCondition} defines the {@link Extension} API for
 * programmatic, <em>conditional test execution</em>.
 *
 * <p>A {@code TestExecutionCondition} is {@linkplain #evaluate evaluated}
 * to determine if a given test should be executed based on the supplied
 * {@link TestExtensionContext}.
 *
 * <p>Implementations must provide a no-args constructor.
 *
 * @since 5.0
 * @see ContainerExecutionCondition
 * @see org.junit.jupiter.api.Disabled
 */
@FunctionalInterface
@API(Experimental)
public interface TestExecutionCondition extends Extension {

	/**
	 * Evaluate this condition for the supplied {@link TestExtensionContext}.
	 *
	 * <p>An {@linkplain ConditionEvaluationResult#enabled enabled} result
	 * indicates that the test should be executed; whereas, a
	 * {@linkplain ConditionEvaluationResult#disabled disabled} result
	 * indicates that the test should not be executed.
	 *
	 * @param context the current extension context; never {@code null}
	 * @return the result of evaluating this condition; never {@code null}
	 */
	ConditionEvaluationResult evaluate(TestExtensionContext context);

}
