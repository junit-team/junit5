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
 * {@link ExecutionCondition} for {@link DisabledIf @DisabledIf}.
 *
 * @since 5.7
 * @see DisabledIf
 */
class DisabledIfCondition extends MethodBasedCondition {

	private static final ConditionEvaluationResult ENABLED_BY_DEFAULT = enabled("@DisabledIf is not present");
	private static final ConditionEvaluationResult ENABLED = enabled(
		"Condition provided in @DisabledIf evaluates to false");
	private static final ConditionEvaluationResult DISABLED = disabled(
		"Condition provided in @DisabledIf evaluates to true");

	@Override
	Optional<String> getMethodName(ExtensionContext context) {
		return findAnnotation(context.getElement(), DisabledIf.class) //
				.map(DisabledIf::value);
	}

	@Override
	ConditionEvaluationResult getDefaultResult() {
		return ENABLED_BY_DEFAULT;
	}

	@Override
	ConditionEvaluationResult getResultBasedOnBoolean(boolean methodResult, ExtensionContext context) {
		if (!methodResult) {
			return ENABLED;
		}
		String customReason = findAnnotation(context.getElement(), DisabledIf.class) //
				.map(DisabledIf::disabledReason).get();
		if (customReason.isEmpty()) {
			return DISABLED;
		}
		return disabled(customReason);
	}

}
