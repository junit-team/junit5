/*
 * Copyright 2015-2016 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.junit.platform.engine.discovery;

import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toList;
import static org.junit.platform.engine.FilterResult.excluded;
import static org.junit.platform.engine.FilterResult.included;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.regex.Pattern;

import org.junit.platform.commons.util.Preconditions;
import org.junit.platform.engine.FilterResult;

/**
 * {@link ClassNameFilter} that matches fully qualified class names against a
 * pattern in the form of a regular expression.
 *
 * <p>If the fully qualified name of a class matches against the pattern, the
 * class will be included.
 *
 * @since 1.0
 */
class IncludeClassNameFilter implements ClassNameFilter {

	private final List<Pattern> patterns;
	private final String patternDescription;

	IncludeClassNameFilter(String... patterns) {
		Preconditions.notEmpty(patterns, "patterns must not be null or empty");
		Preconditions.containsNoNullElements(patterns, "patterns must not contain null elements");
		this.patterns = Arrays.stream(patterns).map(Pattern::compile).collect(toList());
		this.patternDescription = Arrays.stream(patterns).collect(joining("' OR '", "'", "'"));
	}

	@Override
	public FilterResult apply(String className) {
		return findMatchingPattern(className) //
				.map(pattern -> included(formatInclusionReason(className, pattern))) //
				.orElseGet(() -> excluded(formatExclusionReason(className)));
	}

	private String formatExclusionReason(String className) {
		return String.format("Class name [%s] does not match any included pattern: %s", className, patternDescription);
	}

	private String formatInclusionReason(String className, Pattern pattern) {
		return String.format("Class name [%s] matches included pattern: '%s'", className, pattern);
	}

	@Override
	public Predicate<String> toPredicate() {
		return className -> findMatchingPattern(className).isPresent();
	}

	private Optional<Pattern> findMatchingPattern(String className) {
		return this.patterns.stream().filter(pattern -> pattern.matcher(className).matches()).findAny();
	}

	@Override
	public String toString() {
		return "Includes class names that match regular expression " + patternDescription;
	}

}
