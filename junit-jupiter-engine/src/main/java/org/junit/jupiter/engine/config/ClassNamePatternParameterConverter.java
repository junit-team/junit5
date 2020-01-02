/*
 * Copyright 2015-2020 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * https://www.eclipse.org/legal/epl-v20.html
 */

package org.junit.jupiter.engine.config;

import java.util.function.Predicate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.junit.platform.commons.util.StringUtils;
import org.junit.platform.engine.ConfigurationParameters;

/**
 * @since 5.4
 */
class ClassNamePatternParameterConverter {

	private static final Predicate<?> alwaysActivated = object -> true;
	private static final Predicate<?> alwaysDeactivated = object -> false;

	static final String DEACTIVATE_ALL_PATTERN = "*";

	Predicate<?> get(ConfigurationParameters configurationParameters, String key) {
		// @formatter:off
		return configurationParameters.get(key)
				.filter(StringUtils::isNotBlank)
				.map(String::trim)
				.map(patternString -> {
					if (DEACTIVATE_ALL_PATTERN.equals(patternString)) {
						return alwaysDeactivated;
					}
					return matchesRegex(patternString);
				})
				.orElse(alwaysActivated);
		// @formatter:on
	}

	private Predicate<?> matchesRegex(String patternString) {
		Pattern pattern = Pattern.compile(convertToRegEx(patternString));
		return object -> !pattern.matcher(object.getClass().getName()).matches();
	}

	/**
	 * See {@link org.junit.jupiter.engine.Constants#DEACTIVATE_CONDITIONS_PATTERN_PROPERTY_NAME} for
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
