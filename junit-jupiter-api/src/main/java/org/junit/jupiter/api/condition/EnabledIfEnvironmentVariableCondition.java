/*
 * Copyright 2015-2018 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v20.html
 */

package org.junit.jupiter.api.condition;

import static java.lang.String.format;
import static org.junit.jupiter.api.extension.ConditionEvaluationResult.disabled;
import static org.junit.jupiter.api.extension.ConditionEvaluationResult.enabled;
import static org.junit.platform.commons.util.AnnotationUtils.findAnnotation;

import java.util.Optional;

import org.junit.jupiter.api.extension.ConditionEvaluationResult;
import org.junit.jupiter.api.extension.ExecutionCondition;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.platform.commons.util.Preconditions;

/**
 * {@link ExecutionCondition} for {@link EnabledIfEnvironmentVariable @EnabledIfEnvironmentVariable}.
 *
 * @since 5.1
 * @see EnabledIfEnvironmentVariable
 */
class EnabledIfEnvironmentVariableCondition implements ExecutionCondition {

	private static final ConditionEvaluationResult ENABLED_BY_DEFAULT = enabled(
		"@EnabledIfEnvironmentVariable is not present");

	@Override
	public ConditionEvaluationResult evaluateExecutionCondition(ExtensionContext context) {
		Optional<EnabledIfEnvironmentVariable> optional = findAnnotation(context.getElement(),
			EnabledIfEnvironmentVariable.class);

		if (!optional.isPresent()) {
			return ENABLED_BY_DEFAULT;
		}

		EnabledIfEnvironmentVariable annotation = optional.get();
		String name = annotation.named().trim();
		String regex = annotation.matches();
		Preconditions.notBlank(name, () -> "The 'named' attribute must not be blank in " + annotation);
		Preconditions.notBlank(regex, () -> "The 'matches' attribute must not be blank in " + annotation);
		String actual = System.getenv(name);

		// Nothing to match against?
		if (actual == null) {
			return enabled(format("Environment variable [%s] does not exist", name));
		}

		if (actual.matches(regex)) {
			return enabled(format("Environment variable [%s] with value [%s] matches regular expression [%s]", name,
				actual, regex));
		}
		// else
		return disabled(format("Environment variable [%s] with value [%s] does not match regular expression [%s]", name,
			actual, regex));
	}

}
