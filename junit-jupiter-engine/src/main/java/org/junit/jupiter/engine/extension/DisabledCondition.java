/*
 * Copyright 2015-2016 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.junit.jupiter.engine.extension;

import static org.junit.platform.commons.util.AnnotationUtils.findAnnotation;

import java.lang.reflect.AnnotatedElement;
import java.util.Optional;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.extension.ConditionEvaluationResult;
import org.junit.jupiter.api.extension.ContainerExecutionCondition;
import org.junit.jupiter.api.extension.ContainerExtensionContext;
import org.junit.jupiter.api.extension.TestExecutionCondition;
import org.junit.jupiter.api.extension.TestExtensionContext;
import org.junit.platform.commons.util.StringUtils;

/**
 * Composite {@link ContainerExecutionCondition} and {@link TestExecutionCondition}
 * that supports the {@code @Disabled} annotation.
 *
 * @since 5.0
 * @see Disabled
 * @see #evaluate(ContainerExtensionContext)
 * @see #evaluate(TestExtensionContext)
 */
class DisabledCondition implements ContainerExecutionCondition, TestExecutionCondition {

	private static final ConditionEvaluationResult ENABLED = ConditionEvaluationResult.enabled(
		"@Disabled is not present");

	/**
	 * Containers are disabled if {@code @Disabled} is present on the test class.
	 */
	@Override
	public ConditionEvaluationResult evaluate(ContainerExtensionContext context) {
		return evaluate(context.getElement());
	}

	/**
	 * Tests are disabled if {@code @Disabled} is present on the test method.
	 */
	@Override
	public ConditionEvaluationResult evaluate(TestExtensionContext context) {
		return evaluate(context.getElement());
	}

	private ConditionEvaluationResult evaluate(AnnotatedElement element) {
		Optional<Disabled> disabled = findAnnotation(element, Disabled.class);
		if (disabled.isPresent()) {
			String reason = disabled.map(Disabled::value).filter(StringUtils::isNotBlank).orElseGet(
				() -> element + " is @Disabled");
			return ConditionEvaluationResult.disabled(reason);
		}

		return ENABLED;
	}

}
