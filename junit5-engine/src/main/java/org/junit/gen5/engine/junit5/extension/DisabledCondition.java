/*
 * Copyright 2015 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.junit.gen5.engine.junit5.extension;

import static org.junit.gen5.commons.util.AnnotationUtils.findAnnotation;

import java.util.Optional;

import org.junit.gen5.api.Disabled;
import org.junit.gen5.api.extension.ConditionEvaluationResult;
import org.junit.gen5.api.extension.ContainerExecutionCondition;
import org.junit.gen5.api.extension.ContainerExtensionContext;
import org.junit.gen5.api.extension.TestExecutionCondition;
import org.junit.gen5.api.extension.TestExtensionContext;
import org.junit.gen5.commons.util.StringUtils;

/**
 * Composite {@link ContainerExecutionCondition} and {@link TestExecutionCondition}
 * that supports the {@code @Disabled} annotation.
 *
 * @since 5.0
 * @see Disabled
 * @see #evaluate(ContainerExtensionContext)
 * @see #evaluate(TestExtensionContext)
 */
public class DisabledCondition implements ContainerExecutionCondition, TestExecutionCondition {

	private static final ConditionEvaluationResult ENABLED = ConditionEvaluationResult.enabled(
		"@Disabled is not present");

	/**
	 * Containers are disabled if {@code @Disabled} is present on the test class.
	 */
	@Override
	public ConditionEvaluationResult evaluate(ContainerExtensionContext context) {
		return evaluate(findAnnotation(context.getTestClass(), Disabled.class));
	}

	/**
	 * Tests are disabled if {@code @Disabled} is present on the test method.
	 */
	@Override
	public ConditionEvaluationResult evaluate(TestExtensionContext context) {
		return evaluate(findAnnotation(context.getTestMethod(), Disabled.class));
	}

	private ConditionEvaluationResult evaluate(Optional<Disabled> disabled) {
		if (disabled.isPresent()) {
			String reason = disabled.map(Disabled::value).filter(StringUtils::isNotBlank).orElse("Test is @Disabled");
			return ConditionEvaluationResult.disabled(reason);
		}

		return ENABLED;
	}

}
