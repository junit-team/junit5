/*
 * Copyright 2015-2024 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * https://www.eclipse.org/legal/epl-v20.html
 */

package org.junit.platform.commons.util;

import static java.util.stream.Collectors.toList;
import static org.apiguardian.api.API.Status.INTERNAL;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apiguardian.api.API;

/**
 * Collection of utilities for creating filters based on class names.
 *
 * <h2>DISCLAIMER</h2>
 *
 * <p>These utilities are intended solely for usage within the JUnit framework
 * itself. <strong>Any usage by external parties is not supported.</strong>
 * Use at your own risk!
 *
 * @since 1.7
 */
@API(status = INTERNAL, since = "1.7")
public class ClassNamePatternFilterUtils {

	private ClassNamePatternFilterUtils() {
		/* no-op */
	}

	public static final String DEACTIVATE_ALL_PATTERN = "*";

	/**
	 * Create a {@link Predicate} that can be used to exclude (i.e., filter out)
	 * objects of type {@code T} whose fully qualified class names match any of
	 * the supplied patterns.
	 *
	 * @param patterns a comma-separated list of patterns
	 */
	public static <T> Predicate<T> excludeMatchingClasses(String patterns) {
		return excludeMatchingClasses(patterns, object -> object.getClass().getName());
	}

	/**
	 * Create a {@link Predicate} that can be used to exclude (i.e., filter out)
	 * fully qualified class names matching any of the supplied patterns.
	 *
	 * @param patterns a comma-separated list of patterns
	 */
	public static Predicate<String> excludeMatchingClassNames(String patterns) {
		return excludeMatchingClasses(patterns, Function.identity());
	}

	private static <T> Predicate<T> excludeMatchingClasses(String patterns, Function<T, String> classNameGetter) {
		// @formatter:off
		return Optional.ofNullable(patterns)
				.filter(StringUtils::isNotBlank)
				.map(String::trim)
				.map(trimmedPatterns -> createPredicateFromPatterns(trimmedPatterns, classNameGetter))
				.orElse(object -> true);
		// @formatter:on
	}

	private static <T> Predicate<T> createPredicateFromPatterns(String patterns,
			Function<T, String> classNameProvider) {
		if (DEACTIVATE_ALL_PATTERN.equals(patterns)) {
			return object -> false;
		}
		List<Pattern> patternList = convertToRegularExpressions(patterns);
		return object -> patternList.stream().noneMatch(it -> it.matcher(classNameProvider.apply(object)).matches());
	}

	private static List<Pattern> convertToRegularExpressions(String patterns) {
		// @formatter:off
		return Arrays.stream(patterns.split(","))
				.filter(StringUtils::isNotBlank)
				.map(String::trim)
				.map(ClassNamePatternFilterUtils::replaceRegExElements)
				.map(Pattern::compile)
				.collect(toList());
		// @formatter:on
	}

	private static String replaceRegExElements(String pattern) {
		return Matcher.quoteReplacement(pattern)
				// Match "." against "." and "$" since users may declare a "." instead of a
				// "$" as the separator between classes and nested classes.
				.replace(".", "[.$]")
				// Convert our "*" wildcard into a proper RegEx pattern.
				.replace("*", ".+");
	}

}
