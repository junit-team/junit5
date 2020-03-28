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

import static java.util.stream.Collectors.toList;
import static org.apiguardian.api.API.Status.INTERNAL;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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

	public static final String DEACTIVATE_ALL_PATTERN = "*";

	public static <T> Predicate<T> filterForClassName(String pattern) {
		// @formatter:off
		return Optional.ofNullable(pattern)
				.filter(StringUtils::isNotBlank)
				.map(String::trim)
				.map(patternString -> {
					if (DEACTIVATE_ALL_PATTERN.equals(patternString)) {
						return (Predicate<T>) object -> false;
					}
					return ClassNameFilterUtil.<T>matchesRegex(patternString);
				})
				.orElse(object -> true);
		// @formatter:on
	}

	private static <T> Predicate<T> matchesRegex(String patternString) {
		List<Pattern> patterns = convertToRegEx(patternString);
		return object -> patterns.stream().noneMatch(it -> it.matcher(object.getClass().getName()).matches());
	}

	private static List<Pattern> convertToRegEx(String pattern) {
		// @formatter:off
		return Arrays.stream(pattern.split(","))
				.filter(StringUtils::isNotBlank)
				.map(String::trim)
				.map(ClassNameFilterUtil::replaceRegExElements)
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
				.replace("*", "[^,]+");
	}
}
