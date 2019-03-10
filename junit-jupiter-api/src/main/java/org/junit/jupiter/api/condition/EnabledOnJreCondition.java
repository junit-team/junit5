/*
 * Copyright 2015-2019 the original author or authors.
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

import java.util.Arrays;
import java.util.Optional;

import org.junit.jupiter.api.extension.ConditionEvaluationResult;
import org.junit.jupiter.api.extension.ExecutionCondition;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.platform.commons.util.Preconditions;

/**
 * {@link ExecutionCondition} for {@link EnabledOnJre @EnabledOnJre}.
 *
 * @since 5.1
 * @see EnabledOnJre
 */
class EnabledOnJreCondition implements ExecutionCondition {

	private static final ConditionEvaluationResult ENABLED_BY_DEFAULT = enabled("@EnabledOnJre is not present");

	static final ConditionEvaluationResult ENABLED_ON_CURRENT_JRE = //
		enabled("Enabled on JRE version: " + System.getProperty("java.version"));

	static final ConditionEvaluationResult DISABLED_ON_CURRENT_JRE = //
		disabled("Disabled on JRE version: " + System.getProperty("java.version"));

	@Override
	public ConditionEvaluationResult evaluateExecutionCondition(ExtensionContext context) {
		Optional<EnabledOnJre> optional = findAnnotation(context.getElement(), EnabledOnJre.class);
		if (optional.isPresent()) {
			JRE[] versions = optional.get().value();
			Preconditions.condition(versions.length > 0, "You must declare at least one JRE in @EnabledOnJre");
			return (Arrays.stream(versions).anyMatch(JRE::isCurrentVersion)) ? ENABLED_ON_CURRENT_JRE
					: DISABLED_ON_CURRENT_JRE;
		}
		return ENABLED_BY_DEFAULT;
	}

}
