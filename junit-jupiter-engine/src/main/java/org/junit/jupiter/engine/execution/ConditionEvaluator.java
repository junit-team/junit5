/*
 * Copyright 2015-2017 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.junit.jupiter.engine.execution;

import static java.lang.String.format;
import static org.junit.jupiter.engine.Constants.DEACTIVATE_ALL_CONDITIONS_PATTERN;
import static org.junit.jupiter.engine.Constants.DEACTIVATE_CONDITIONS_PATTERN_PROPERTY_NAME;
import static org.junit.platform.commons.meta.API.Usage.Internal;

import java.util.function.Predicate;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.junit.jupiter.api.extension.ConditionEvaluationResult;
import org.junit.jupiter.api.extension.ExecutionCondition;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.engine.Constants;
import org.junit.jupiter.engine.extension.ExtensionRegistry;
import org.junit.platform.commons.meta.API;
import org.junit.platform.commons.util.StringUtils;
import org.junit.platform.engine.ConfigurationParameters;

/**
 * {@code ConditionEvaluator} evaluates {@link ExecutionCondition} extensions.
 *
 * @since 5.0
 * @see ExecutionCondition
 */
@API(Internal)
public class ConditionEvaluator {

	private static final Logger LOG = Logger.getLogger(ConditionEvaluator.class.getName());

	private static final ConditionEvaluationResult ENABLED = ConditionEvaluationResult.enabled(
		"No 'disabled' conditions encountered");

	private static final Predicate<Object> alwaysActivated = condition -> true;

	private static final Predicate<Object> alwaysDeactivated = condition -> false;

	/**
	 * Evaluate all {@link ExecutionCondition} extensions registered for the
	 * supplied {@link ExtensionContext}.
	 *
	 * @param context the current {@code ExtensionContext}
	 * @return the first <em>disabled</em> {@code ConditionEvaluationResult},
	 * or a default <em>enabled</em> {@code ConditionEvaluationResult} if no
	 * disabled conditions are encountered
	 */
	public ConditionEvaluationResult evaluate(ExtensionRegistry extensionRegistry,
			ConfigurationParameters configurationParameters, ExtensionContext context) {

		// @formatter:off
		return extensionRegistry.stream(ExecutionCondition.class)
				.filter(conditionIsActivated(configurationParameters))
				.map(condition -> evaluate(condition, context))
				.filter(ConditionEvaluationResult::isDisabled)
				.findFirst()
				.orElse(ENABLED);
		// @formatter:on
	}

	private ConditionEvaluationResult evaluate(ExecutionCondition condition, ExtensionContext context) {
		try {
			ConditionEvaluationResult result = condition.evaluateExecutionCondition(context);
			logResult(condition.getClass(), result);
			return result;
		}
		catch (Exception ex) {
			throw evaluationException(condition.getClass(), ex);
		}
	}

	private void logResult(Class<?> conditionType, ConditionEvaluationResult result) {
		LOG.finer(() -> format("Evaluation of condition [%s] resulted in: %s", conditionType.getName(), result));
	}

	private ConditionEvaluationException evaluationException(Class<?> conditionType, Exception ex) {
		String cause = StringUtils.isNotBlank(ex.getMessage()) ? ": " + ex.getMessage() : "";
		return new ConditionEvaluationException(
			format("Failed to evaluate condition [%s]%s", conditionType.getName(), cause), ex);
	}

	private Predicate<Object> conditionIsActivated(ConfigurationParameters configurationParameters) {
		String patternString = getDeactivatePatternString(configurationParameters);
		if (patternString != null) {
			if (DEACTIVATE_ALL_CONDITIONS_PATTERN.equals(patternString)) {
				return alwaysDeactivated;
			}
			Pattern pattern = Pattern.compile(convertToRegEx(patternString));
			return condition -> !pattern.matcher(condition.getClass().getName()).matches();
		}
		return alwaysActivated;
	}

	private String getDeactivatePatternString(ConfigurationParameters configurationParameters) {
		// @formatter:off
		return configurationParameters.get(DEACTIVATE_CONDITIONS_PATTERN_PROPERTY_NAME)
				.filter(StringUtils::isNotBlank)
				.map(String::trim)
				.orElse(null);
		// @formatter:on
	}

	/**
	 * See {@link Constants#DEACTIVATE_CONDITIONS_PATTERN_PROPERTY_NAME} for
	 * details on the pattern matching syntax.
	 */
	private String convertToRegEx(String pattern) {
		pattern = Matcher.quoteReplacement(pattern);

		// Match "." against "." and "$" since users may declare a "." instead of a
		// "$" as the separator between classes and nested classes.
		pattern = pattern.replace(".", "[.$]");

		// Convert our "*" wildcard into a proper RegEx pattern.
		pattern = pattern.replace("*", ".+");

		return pattern;
	}

}
