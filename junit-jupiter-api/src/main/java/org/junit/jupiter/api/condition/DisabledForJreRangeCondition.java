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

import static org.junit.jupiter.api.condition.EnabledOnJreCondition.DISABLED_ON_CURRENT_JRE;
import static org.junit.jupiter.api.condition.EnabledOnJreCondition.ENABLED_ON_CURRENT_JRE;
import static org.junit.jupiter.api.extension.ConditionEvaluationResult.enabled;
import static org.junit.platform.commons.util.AnnotationUtils.findAnnotation;

import org.junit.jupiter.api.extension.ConditionEvaluationResult;
import org.junit.jupiter.api.extension.ExecutionCondition;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.platform.commons.util.Preconditions;

/**
 * {@link ExecutionCondition} for {@link DisabledForJreRange @DisabledForJreRange}.
 *
 * @since 5.6
 * @see DisabledForJreRange
 */
class DisabledForJreRangeCondition implements ExecutionCondition {

	private static final ConditionEvaluationResult ENABLED_BY_DEFAULT = enabled("@DisabledForJreRange is not present");

	@Override
	public ConditionEvaluationResult evaluateExecutionCondition(ExtensionContext context) {
		return findAnnotation(context.getElement(), DisabledForJreRange.class) //
				.map(disabledForJreRange -> {
					JRE min = disabledForJreRange.min();
					JRE max = disabledForJreRange.max();
					Preconditions.condition((min != JRE.JAVA_8 || max != JRE.OTHER),
						"You must declare a non-default value for min or max in @DisabledForJreRange");
					Preconditions.condition(max.compareTo(min) >= 0,
						"@DisabledForJreRange.min must be less than or equal to @DisabledForJreRange.max");

					return JRE.isCurrentVersionWithinRange(min, max) ? DISABLED_ON_CURRENT_JRE : ENABLED_ON_CURRENT_JRE;
				}).orElse(ENABLED_BY_DEFAULT);
	}

}
