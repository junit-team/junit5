/*
 * Copyright 2015-2024 the original author or authors.
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

import java.lang.annotation.Annotation;
import java.util.function.Function;

import org.junit.jupiter.api.extension.ConditionEvaluationResult;
import org.junit.jupiter.api.extension.ExecutionCondition;
import org.junit.jupiter.api.extension.ExtensionContext;

abstract class BooleanExecutionCondition<A extends Annotation> implements ExecutionCondition {

	private final Class<A> annotationType;
	private final String enabledReason;
	private final String disabledReason;
	private final Function<A, String> customDisabledReason;

	BooleanExecutionCondition(Class<A> annotationType, String enabledReason, String disabledReason,
			Function<A, String> customDisabledReason) {
		this.annotationType = annotationType;
		this.enabledReason = enabledReason;
		this.disabledReason = disabledReason;
		this.customDisabledReason = customDisabledReason;
	}

	abstract boolean isEnabled(A annotation);

	@Override
	public ConditionEvaluationResult evaluateExecutionCondition(ExtensionContext context) {
		return findAnnotation(context.getElement(), annotationType) //
				.map(annotation -> isEnabled(annotation) ? enabled(enabledReason)
						: disabled(disabledReason, customDisabledReason.apply(annotation))) //
				.orElseGet(this::enabledByDefault);
	}

	private ConditionEvaluationResult enabledByDefault() {
		String reason = String.format("@%s is not present", annotationType.getSimpleName());
		return enabled(reason);
	}

}
