/*
 * Copyright 2015-2020 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * https://www.eclipse.org/legal/epl-v20.html
 */

package org.junit.jupiter.engine.extension;

import static org.junit.platform.commons.util.AnnotationUtils.findAnnotation;

import java.lang.reflect.AnnotatedElement;
import java.util.Optional;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.extension.ConditionEvaluationResult;
import org.junit.jupiter.api.extension.ExecutionCondition;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.platform.commons.util.StringUtils;

/**
 * {@link ExecutionCondition} that supports the {@code @Disabled} annotation.
 *
 * @since 5.0
 * @see Disabled
 * @see #evaluateExecutionCondition(ExtensionContext)
 */
class DisabledCondition implements ExecutionCondition {

	private static final ConditionEvaluationResult ENABLED = ConditionEvaluationResult.enabled(
		"@Disabled is not present");

	/**
	 * Containers/tests are disabled if {@code @Disabled} is present on the test
	 * class or method.
	 */
	@Override
	public ConditionEvaluationResult evaluateExecutionCondition(ExtensionContext context) {
		Optional<AnnotatedElement> element = context.getElement();
		Optional<Disabled> disabled = findAnnotation(element, Disabled.class);
		if (disabled.isPresent()) {
			String reason = disabled.map(Disabled::value).filter(StringUtils::isNotBlank).orElseGet(
				() -> element.get() + " is @Disabled");
			return ConditionEvaluationResult.disabled(reason);
		}

		return ENABLED;
	}

}
