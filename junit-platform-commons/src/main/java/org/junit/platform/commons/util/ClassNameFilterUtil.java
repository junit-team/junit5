/*
 * Copyright 2015-2020 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * https://www.eclipse.org/legal/epl-v20.html
 */

package org.junit.platform.commons.util;

import java.util.Arrays;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class ClassNameFilterUtil {

	private ClassNameFilterUtil() {
		/* no-op */
	}
	private static final Predicate<?> alwaysActivated = object -> true;
	private static final Predicate<?> alwaysDeactivated = object -> false;

	public static final String DEACTIVATE_ALL_PATTERN = "*";
	public static final String DEACTIVATE_CONDITIONS_PATTERN_PROPERTY_NAME = "junit.jupiter.conditions.deactivate";
	public static final String DEACTIVATE_LISTENERS_PATTERN_PROPERTY_NAME = "junit.platform.execution.listeners.deactivate";

	public static Predicate<?> get(Supplier<Optional<String>> pattern) {
	// @formatter:off
    return pattern.get()
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

	private static Predicate<?> matchesRegex(String patternString) {
		Pattern pattern = Pattern.compile(convertToRegEx(patternString));
		return object -> !pattern.matcher(object.getClass().getName()).matches();
	}

	private static String convertToRegEx(String pattern) {
		pattern = Matcher.quoteReplacement(pattern);
		// Splitting CSV Separated Pattens
		pattern = Arrays.stream(pattern.split("[,;]")).map(ClassNameFilterUtil::replaceRegExElements).collect(
			Collectors.joining(")|("));

		pattern = "(" + pattern + ")";

		return pattern;
	}

	private static String replaceRegExElements(String pattern) {
		// Match "." against "." and "$" since users may declare a "." instead of a
		// "$" as the separator between classes and nested classes.
		pattern = pattern.replace(".", "[.$]");

		// Convert our "*" wildcard into a proper RegEx pattern.
		pattern = pattern.replace("*", "[^,;]+");

		return pattern;
	}
}
