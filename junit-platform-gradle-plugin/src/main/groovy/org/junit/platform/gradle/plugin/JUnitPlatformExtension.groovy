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
 * Core configuration options for the JUnit Platform Gradle plugin.
 *
 * @since 1.0
 */
class JUnitPlatformExtension {

	/**
	 * The version of the JUnit Platform to use.
	 *
	 * <p>Defaults to {@code '1.+'}.
	 */
	String platformVersion = '1.+'

	/**
	 * The fully qualified class name of the {@link java.util.logging.LogManager}
	 * to use.
	 *
	 * <p>The JUnit plugin will set the {@code java.util.logging.manager}
	 * system property to this value.
	 */
	String logManager

	/**
	 * The directory for the test report files.
	 *
	 * <p>Defaults to {@code file('build/test-results/junit-platform')}.
	 */
	File reportsDir

	/**
	 * Whether or not the standard Gradle {@code test} task should be enabled.
	 *
	 * <p>Set this to {@code true} to have the standard {@code test} task enabled
	 * &mdash; for example, to run TestNG tests via the standard {@code test} task.
	 *
	 * <p>Defaults to {@code false}.
	 */
	boolean enableStandardTestTask = false

	/**
	 * A pattern in the form of a regular expression that is used to match against
	 * fully qualified class names.
	 *
	 * <p>If the fully qualified name of a class matches against the pattern, the
	 * class will be included in the test plan; otherwise, the class will be
	 * excluded.
	 *
	 * <p>Defaults to {@value ClassNameFilter#STANDARD_INCLUDE_PATTERN}.
	 */
	String includeClassNamePattern = ClassNameFilter.STANDARD_INCLUDE_PATTERN

}
