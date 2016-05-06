/*
 * Copyright 2015-2016 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.junit.gen5.api.extension;

import static org.junit.gen5.commons.meta.API.Usage.Experimental;

import org.junit.gen5.commons.meta.API;

/**
 * {@code ContainerExecutionCondition} defines the {@link Extension} API for
 * programmatic, <em>conditional container execution</em>.
 *
 * <p>A {@code ContainerExecutionCondition} is {@linkplain #evaluate evaluated}
 * to determine if all tests in a given container should be executed based
 * on the supplied {@link ContainerExtensionContext}.
 *
 * <p>Implementations must provide a no-args constructor.
 *
 * @since 5.0
 * @see TestExecutionCondition
 * @see org.junit.gen5.api.Disabled
 */
@FunctionalInterface
@API(Experimental)
public interface ContainerExecutionCondition extends Extension {

	/**
	 * Evaluate this condition for the supplied {@link ContainerExtensionContext}.
	 *
	 * <p>An {@linkplain ConditionEvaluationResult#enabled enabled} result
	 * indicates that the container should be executed; whereas, a
	 * {@linkplain ConditionEvaluationResult#disabled disabled} result indicates
	 * that the container should not be executed.
	 *
	 * @param context the current {@code ContainerExtensionContext}
	 */
	ConditionEvaluationResult evaluate(ContainerExtensionContext context);

}
