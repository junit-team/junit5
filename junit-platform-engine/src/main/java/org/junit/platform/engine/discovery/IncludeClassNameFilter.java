/*
 * Copyright 2015-2017 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.junit.platform.engine.discovery;

import static org.junit.platform.engine.FilterResult.excluded;
import static org.junit.platform.engine.FilterResult.included;

import java.util.function.Predicate;
import java.util.regex.Pattern;

import org.junit.platform.engine.FilterResult;

/**
 * {@link ClassNameFilter} that matches fully qualified class names against
 * patterns in the form of regular expressions.
 *
 * <p>If the fully qualified name of a class matches against at least one
 * pattern, the class will be included.
 *
 * @since 1.0
 */
class IncludeClassNameFilter extends AbstractClassNameFilter {

	IncludeClassNameFilter(String... patterns) {
		super(patterns);
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

	@Override
	public String toString() {
		return "Includes class names that match regular expression " + patternDescription;
	}

}
