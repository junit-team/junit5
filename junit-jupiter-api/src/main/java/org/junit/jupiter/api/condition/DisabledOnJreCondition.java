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

import java.util.Arrays;
import java.util.Optional;

import org.junit.jupiter.api.extension.ConditionEvaluationResult;
import org.junit.jupiter.api.extension.ExecutionCondition;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.platform.commons.util.Preconditions;

/**
 * {@link ExecutionCondition} for {@link DisabledOnJre @DisabledOnJre}.
 *
 * @since 5.1
 * @see DisabledOnJre
 */
class DisabledOnJreCondition implements ExecutionCondition {

	private static final ConditionEvaluationResult ENABLED_BY_DEFAULT = enabled("@DisabledOnJre is not present");

	@Override
	public ConditionEvaluationResult evaluateExecutionCondition(ExtensionContext context) {
		Optional<DisabledOnJre> optional = findAnnotation(context.getElement(), DisabledOnJre.class);
		if (optional.isPresent()) {
			JRE[] versions = optional.get().value();
			Preconditions.condition(versions.length > 0, "You must declare at least one JRE in @DisabledOnJre");
			return (Arrays.stream(versions).anyMatch(JRE::isCurrentVersion)) ? DISABLED_ON_CURRENT_JRE
					: ENABLED_ON_CURRENT_JRE;
		}
		return ENABLED_BY_DEFAULT;
	}

}
