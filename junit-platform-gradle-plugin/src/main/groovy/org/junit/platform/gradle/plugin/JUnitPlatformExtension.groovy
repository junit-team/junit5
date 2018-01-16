/*
 * Copyright 2015-2018 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v20.html
 */
package org.junit.platform.gradle.plugin

import static org.apiguardian.api.API.Status.EXPERIMENTAL

import org.apiguardian.api.API
import org.gradle.api.Action
import org.gradle.api.Project
import org.gradle.api.file.FileCollection
import org.gradle.api.tasks.Input
import org.junit.platform.commons.util.Preconditions
import org.junit.platform.console.options.Details

/**
 * Core configuration options for the JUnit Platform Gradle plugin.
 *
 * @since 1.0
 */
@API(status = EXPERIMENTAL, since = "1.0")
class JUnitPlatformExtension {

	private final Project project

	JUnitPlatformExtension(Project project) {
		this.project = project
	}

	/**
	 * The version of the JUnit Platform to use.
	 *
	 * <p>Defaults to the version of the plugin.
	 */
	String platformVersion

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
	 * <p>Defaults to {@code file("$buildDir/test-results/junit-platform")}.
	 */
	File reportsDir

	/**
	 * The configuration parameters to be used.
	 *
	 * <p>Empty by default.
	 */
	final Map<String, String> configurationParameters = [:]

	/**
	 * Accepts a path to the reportsDir. If the object is a {@link java.io.File) it
	 * will be used as is. If the object is anything else, it will convert to File
	 * automatically using {@link org.gradle.api.Project#file(Object)}
	 */
	void setReportsDir(Object reportsDir) {
		// Work around for https://discuss.gradle.org/t/bug-in-project-file-on-windows/19917
		if (reportsDir instanceof File) {
			this.reportsDir = reportsDir
		} else {
			this.reportsDir = project.file(reportsDir)
		}
	}

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
	 * Select test execution plan details mode.
	 *
	 * <p>Defaults to {@link Details#NONE}.
	 */
	Details details = Details.NONE

	/**
	 * Module path entry collection.
	 *
	 * <p>Defaults to an empty file collection.
	 *
	 * @since 1.1
	 */
	FileCollection modulepath = project.files()

	/**
	 * Configure the {@link SelectorsExtension} for this plugin.
	 */
	void selectors(Action<SelectorsExtension> closure) {
		closure.execute(getProperty(JUnitPlatformPlugin.SELECTORS_EXTENSION_NAME) as SelectorsExtension)
	}

	/**
	 * Configure the {@link FiltersExtension} for this plugin.
	 */
	void filters(Action<FiltersExtension> closure) {
		closure.execute(getProperty(JUnitPlatformPlugin.FILTERS_EXTENSION_NAME) as FiltersExtension)
	}

	/**
	 * Add a configuration parameter.
	 *
	 * @param key the parameter key; never {@code null}, must not contain {@code '='}
	 * @param value the parameter value; never {@code null}
	 */
	void configurationParameter(String key, String value) {
		Preconditions.notBlank(key, 'key must not be blank')
		Preconditions.condition(!key.contains('='), { 'key must not contain \'=\': "' + key + '"' })
		Preconditions.notNull(value, { 'value must not be null for key: "' + key + '"' })
		configurationParameters.put(key, value)
	}

	/**
	 * Add a map of configuration parameters.
	 *
	 * @param parameters the parameters to add; never {@code null}.
	 * @see #configurationParameter(String, String)
	 */
	void configurationParameters(Map<String, String> parameters) {
		Preconditions.notNull(parameters, 'parameters must not be null')
		parameters.each { key, value -> configurationParameter(key, value) }
	}

	@Input
	Map<String, String> getConfigurationParameters() {
		return configurationParameters.asImmutable()
	}

}
