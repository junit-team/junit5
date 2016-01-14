/*
 * Copyright 2015-2016 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.junit.gen5.gradle

/**
 * Configuration options for the {@link org.junit.gen5.gradle.JUnit5Plugin}.
 *
 * @since 5.0
 */
class JUnit5Extension {

	/**
	 * The JUnit 5 version to use.
	 */
	String version

	/**
	 * The location of the test report file.
	 *
	 * <p>Defaults to {@code "build/test-results/junit5-report.txt"}.
	 */
	File reportFile

	/**
	 * Whether or not to execute JUnit 4 tests alongside JUnit 5 tests.
	 *
	 * <p>Defaults to {@code false}.
	 */
	boolean runJunit4

	/**
	 * A regular expression used to match against test class names.
	 *
	 * <p>If the supplied regular expression does not match against a
	 * particular class name, that class will be filtered out of the test
	 * plan (i.e., excluded).
	 */
	String classNameFilter

	/**
	 * A list of <em>tags</em> to include when building the test plan
	 * specification.
	 */
	List includeTags = []

	/**
	 * A list of <em>tags</em> to include when building the test plan
	 * specification.
	 */
	List excludeTags = []

	/**
	 * Add a <em>tag</em> to be included when building the test plan
	 * specification.
	 */
	void includeTag(tag) {
		includeTags.add tag
	}

	/**
	 * Add a <em>tag</em> to be excluded when building the test plan
	 * specification.
	 */
	void excludeTag(tag) {
		excludeTags.add tag
	}

	/**
	 * Set the regular expression to be used to match against test class names.
	 *
	 * <p>If the supplied regular expression does not match against a
	 * particular class name, that class will be filtered out of the test
	 * plan (i.e., excluded).
	 */
	void matchClassName(regex) {
		classNameFilter = regex
	}

}
