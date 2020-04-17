/*
 * Copyright 2015-2020 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * https://www.eclipse.org/legal/epl-v20.html
 */

package org.junit.jupiter.api.condition;

import static org.junit.jupiter.api.extension.ConditionEvaluationResult.disabled;
import static org.junit.jupiter.api.extension.ConditionEvaluationResult.enabled;
import static org.junit.platform.commons.util.AnnotationUtils.findAnnotation;

import java.util.Optional;

import org.junit.jupiter.api.extension.ConditionEvaluationResult;
import org.junit.jupiter.api.extension.ExecutionCondition;
import org.junit.jupiter.api.extension.ExtensionContext;

/**
 * {@link ExecutionCondition} for {@link EnabledIf @EnabledIf}.
 *
 * @since 5.7
 * @see EnabledIf
 */
class EnabledIfCondition extends MethodBasedCondition {

	private static final ConditionEvaluationResult ENABLED_BY_DEFAULT = enabled("@EnabledIf is not present");
	private static final ConditionEvaluationResult ENABLED = enabled(
		"Condition provided in @EnabledIf evaluates to true");
	private static final ConditionEvaluationResult DISABLED = disabled(
		"Condition provided in @EnabledIf evaluates to false");

	@Override
	Optional<String> getMethodName(ExtensionContext context) {
		return findAnnotation(context.getElement(), EnabledIf.class) //
				.map(EnabledIf::value);
	}

	@Override
	ConditionEvaluationResult getDefaultResult() {
		return ENABLED_BY_DEFAULT;
	}

	@Override
	ConditionEvaluationResult getResultBasedOnBoolean(boolean result) {
		return result ? ENABLED : DISABLED;
	}

}
