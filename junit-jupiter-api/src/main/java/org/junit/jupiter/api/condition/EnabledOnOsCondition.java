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

import java.util.Arrays;
import java.util.Optional;

import org.junit.jupiter.api.extension.ConditionEvaluationResult;
import org.junit.jupiter.api.extension.ExecutionCondition;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.platform.commons.util.Preconditions;

/**
 * {@link ExecutionCondition} for {@link EnabledOnOs @EnabledOnOs}.
 *
 * @since 5.1
 * @see EnabledOnOs
 */
class EnabledOnOsCondition implements ExecutionCondition {

	private static final ConditionEvaluationResult ENABLED_BY_DEFAULT = enabled("@EnabledOnOs is not present");

	static final String ENABLED_ON_CURRENT_OS = "Enabled on operating system: " + System.getProperty("os.name");

	static final String DISABLED_ON_CURRENT_OS = "Disabled on operating system: " + System.getProperty("os.name");

	@Override
	public ConditionEvaluationResult evaluateExecutionCondition(ExtensionContext context) {
		Optional<EnabledOnOs> optional = findAnnotation(context.getElement(), EnabledOnOs.class);
		if (optional.isPresent()) {
			EnabledOnOs annotation = optional.get();
			OS[] operatingSystems = annotation.value();
			Preconditions.condition(operatingSystems.length > 0, "You must declare at least one OS in @EnabledOnOs");

			return Arrays.stream(operatingSystems).anyMatch(OS::isCurrentOs) ? enabled(ENABLED_ON_CURRENT_OS)
					: disabled(DISABLED_ON_CURRENT_OS, annotation.disabledReason());
		}
		return ENABLED_BY_DEFAULT;
	}

}
