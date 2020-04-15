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

import java.lang.annotation.Annotation;
import java.util.Optional;

import org.junit.jupiter.api.extension.ConditionEvaluationResult;
import org.junit.jupiter.api.extension.ExecutionCondition;
import org.junit.jupiter.api.extension.ExtensionContext;

public abstract class BooleanExecutionCondition<A extends Annotation> implements ExecutionCondition {

	private final Class<A> annotationType;

	protected BooleanExecutionCondition(Class<A> annotationType) {
		this.annotationType = annotationType;
	}

	abstract ConditionEvaluationResult defaultResult();

	abstract String disabledReason(A annotation);

	abstract String enabledReason(A annotation);

	abstract boolean isEnabled(A annotation);

	@Override
	public ConditionEvaluationResult evaluateExecutionCondition(ExtensionContext context) {
		Optional<A> optional = findAnnotation(context.getElement(), annotationType);
		if (optional.isPresent()) {
			A annotation = optional.get();
			return isEnabled(annotation) ? enabled(enabledReason(annotation)) : disabled(disabledReason(annotation));
		}
		return defaultResult();
	}

}
