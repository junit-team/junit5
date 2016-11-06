/*
 * Copyright 2015-2016 the original author or authors.
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
	 * classes that should <em>included</em> in the test plan.
	 *
	 * <p>The patterns are combined using OR semantics, i.e. if the fully
	 * qualified name of a class matches against at least one of the patterns,
	 * the class will be included in the test plan.
	 *
	 * <p>Defaults to {@value ClassNameFilter#STANDARD_INCLUDE_PATTERN}.
	 */
	List<String> includeClassNamePatterns

	protected List<String> getIncludeClassNamePatterns() {
		return includeClassNamePatterns ?: [ ClassNameFilter.STANDARD_INCLUDE_PATTERN ];
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

}
