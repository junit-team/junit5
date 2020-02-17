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

import static org.apiguardian.api.API.Status.INTERNAL;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.apiguardian.api.API;

/**
 * Class-related predicate holder used by execution listener, execution condition predicates
 *
 * <h3>DISCLAIMER</h3>
 *
 * <p>These utilities are intended solely for usage within the JUnit framework
 * itself. <strong>Any usage by external parties is not supported.</strong>
 * Use at your own risk!
 *
 * @since 5.7
 */
@API(status = INTERNAL, since = "5.7")
public class ClassNameFilterUtil {

	private ClassNameFilterUtil() {
		/* no-op */
	}
	private static final Predicate<?> alwaysActivated = object -> true;
	private static final Predicate<?> alwaysDeactivated = object -> false;

	public static final String DEACTIVATE_ALL_PATTERN = "*";

	public static Predicate<?> filterForClassName(String pattern) {
	// @formatter:off
    return Optional.ofNullable(pattern)
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
		List<Pattern> pattern = convertToRegEx(patternString);
		// @formatter:off
		return object -> pattern.stream()
				.noneMatch(pat -> pat.matcher(object.getClass().getName()).matches());
		// @formatter:on
	}

	private static List<Pattern> convertToRegEx(String pattern) {
		pattern = Matcher.quoteReplacement(pattern);
		// Splitting CSV Separated Pattens

		// @formatter:off
		return Arrays.stream(pattern.split("[,]"))
				.filter(StringUtils::isNotBlank)
				.map(String::trim)
				.map(ClassNameFilterUtil::replaceRegExElements)
				.map(Pattern::compile)
				.collect(Collectors.toList());
		// @formatter:on
	}

	private static String replaceRegExElements(String pattern) {
		// Match "." against "." and "$" since users may declare a "." instead of a
		// "$" as the separator between classes and nested classes.
		pattern = pattern.replace(".", "[.$]");

		// Convert our "*" wildcard into a proper RegEx pattern.
		pattern = pattern.replace("*", "[^,]+");

		return pattern;
	}
}
