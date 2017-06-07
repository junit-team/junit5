/*
 * Copyright 2015-2017 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.junit.platform.gradle.plugin

import org.junit.platform.engine.discovery.ClassNameFilter

/**
 * Test discovery filter options for the JUnit Platform Gradle plugin.
 *
 * @since 1.0
 */
class FiltersExtension {

	/**
	 * List of class name patterns in the form of regular expressions for
	 * classes that should be <em>included</em> in the test plan.
	 *
	 * <p>The patterns are combined using OR semantics, i.e. if the fully
	 * qualified name of a class matches against at least one of the patterns,
	 * the class will be included in the test plan.
	 *
	 * <p>Defaults to {@value ClassNameFilter#STANDARD_INCLUDE_PATTERN}.
	 */
	List<String> includeClassNamePatterns

	/**
	 * List of class name patterns in the form of regular expressions for
	 * classes that should be <em>excluded</em> from the test plan.
	 *
	 * <p>The patterns are combined using OR semantics, i.e. if the fully
	 * qualified name of a class matches against at least one of the patterns,
	 * the class will be excluded from the test plan.
	 */
	List<String> excludeClassNamePatterns

	protected List<String> getIncludeClassNamePatterns() {
		return includeClassNamePatterns ?: [ClassNameFilter.STANDARD_INCLUDE_PATTERN]
	}

	protected List<String> getExcludeClassNamePatterns() {
		return excludeClassNamePatterns
	}

	/**
	 * Add a pattern to the list of <em>included</em> patterns.
	 */
	void includeClassNamePattern(String pattern) {
		includeClassNamePatterns(pattern)
	}

	/**
	 * Add patterns to the list of <em>included</em> patterns.
	 */
	void includeClassNamePatterns(String... patterns) {
		if (includeClassNamePatterns == null) {
			includeClassNamePatterns = []
		}
		includeClassNamePatterns.addAll(patterns)
	}

	/**
	 * Add a pattern to the list of <em>excluded</em> patterns.
	 */
	void excludeClassNamePattern(String pattern) {
		excludeClassNamePatterns(pattern)
	}

	/**
	 * Add patterns to the list of <em>excluded</em> patterns.
	 */
	void excludeClassNamePatterns(String... patterns) {
		if (excludeClassNamePatterns == null) {
			excludeClassNamePatterns = []
		}
		excludeClassNamePatterns.addAll(patterns)
	}
}
