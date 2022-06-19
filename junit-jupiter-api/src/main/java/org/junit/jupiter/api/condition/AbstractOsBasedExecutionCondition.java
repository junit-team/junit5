/*
 * Copyright 2015-2022 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * https://www.eclipse.org/legal/epl-v20.html
 */

package org.junit.jupiter.api.condition;

import static org.junit.jupiter.api.extension.ConditionEvaluationResult.enabled;
import static org.junit.platform.commons.util.AnnotationUtils.findAnnotation;

import java.lang.annotation.Annotation;

import org.junit.jupiter.api.extension.ConditionEvaluationResult;
import org.junit.jupiter.api.extension.ExecutionCondition;
import org.junit.jupiter.api.extension.ExtensionContext;

abstract class AbstractOsBasedExecutionCondition<A extends Annotation> implements ExecutionCondition {
	private final Class<A> annotationType;

	AbstractOsBasedExecutionCondition(Class<A> annotationType) {
		this.annotationType = annotationType;
	}

	abstract ConditionEvaluationResult evaluateExecutionCondition(A annotation);

	@Override
	public ConditionEvaluationResult evaluateExecutionCondition(ExtensionContext context) {
		return findAnnotation(context.getElement(), annotationType) //
				.map(this::evaluateExecutionCondition).orElseGet(this::enabledByDefault);
	}

	protected String createReason(boolean enabled, boolean osSpecified, boolean archSpecified) {
		StringBuilder reason = new StringBuilder().append(enabled ? "Enabled" : "Disabled").append(
			osSpecified ? " on operating system: " : " on architecture: ");

		if (osSpecified && archSpecified) {
			reason.append(String.format("%s (%s)", System.getProperty("os.name"), System.getProperty("os.arch")));
		}
		else if (osSpecified) {
			reason.append(System.getProperty("os.name"));
		}
		else {
			reason.append(System.getProperty("os.arch"));
		}

		return reason.toString();
	}

	private ConditionEvaluationResult enabledByDefault() {
		String reason = String.format("@%s is not present", annotationType.getSimpleName());
		return enabled(reason);
	}

}
