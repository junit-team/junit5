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

import org.gradle.api.GradleException
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.tasks.JavaExec
import org.gradle.util.GradleVersion
import org.junit.platform.console.ConsoleLauncher
import org.junit.platform.engine.discovery.ClassNameFilter

/**
 * @since 1.0
 */
class JUnitPlatformPlugin implements Plugin<Project> {

	private static final String EXTENSION_NAME = 'junitPlatform';
	private static final String TASK_NAME      = 'junitPlatformTest';

	void apply(Project project) {
		def junitExtension = project.extensions.create(EXTENSION_NAME, JUnitPlatformExtension, project)
		junitExtension.extensions.create('selectors', SelectorsExtension)
		def filters = junitExtension.extensions.create('filters', FiltersExtension)
		filters.extensions.create('tags', TagsExtension)
		filters.extensions.create('engines', EnginesExtension)
		// TODO #554 Remove these
		junitExtension.extensions.create('tags', TagsExtension)
		junitExtension.extensions.create('engines', EnginesExtension)

		// configuration.defaultDependencies used below was introduced in Gradle 2.5
		if (GradleVersion.current().compareTo(GradleVersion.version('2.5')) < 0) {
			throw new GradleException('junit-platform-gradle-plugin requires Gradle version 2.5 or higher')
		}

		// Add required JUnit Platform dependencies to a custom configuration that
		// will later be used to create the classpath for the custom task created
		// by this plugin.
		def configuration = project.configurations.maybeCreate('junitPlatform')
		configuration.defaultDependencies { deps ->
			def version = junitExtension.platformVersion
			deps.add(project.dependencies.create("org.junit.platform:junit-platform-launcher:${version}"))
			deps.add(project.dependencies.create("org.junit.platform:junit-platform-console:${version}"))
		}

		project.afterEvaluate {
			configure(project, junitExtension)
		}
	}

	private void configure(Project project, JUnitPlatformExtension junitExtension) {
		project.task(
				TASK_NAME,
				type: JavaExec,
				group: 'verification',
				description: 'Runs tests on the JUnit Platform.') { junitTask ->

			junitTask.inputs.property('enableStandardTestTask', junitExtension.enableStandardTestTask)
			junitTask.inputs.property('selectors', junitExtension.selectors)
			junitTask.inputs.property('filters', junitExtension.filters)
			junitTask.inputs.property('filters.engines', junitExtension.filters.engines)
			junitTask.inputs.property('filters.tags', junitExtension.filters.tags)

			def reportsDir = junitExtension.reportsDir ?: project.file("$project.buildDir/test-results/junit-platform")
			junitTask.outputs.dir reportsDir

			if (junitExtension.logManager) {
				systemProperty 'java.util.logging.manager', junitExtension.logManager
			}

			configureTaskDependencies(project, junitTask, junitExtension)

			// Build the classpath from the user's test runtime classpath and the JUnit
			// Platform modules.
			//
			// Note: the user's test runtime classpath must come first; otherwise, code
			// instrumented by Clover in JUnit's build will be shadowed by JARs pulled in
			// via the junitPlatform configuration... leading to zero code coverage for
			// the respective modules.
			junitTask.classpath = project.sourceSets.test.runtimeClasspath + project.configurations.junitPlatform

			junitTask.main = ConsoleLauncher.class.getName()
			junitTask.args buildArgs(project, junitExtension, reportsDir)
		}
	}

	private void configureTaskDependencies(project, junitTask, junitExtension) {
		def testClassesTask = project.tasks.getByName('testClasses')
		junitTask.dependsOn testClassesTask

		def testTask = project.tasks.getByName('test')
		testTask.dependsOn junitTask
		testTask.enabled = junitExtension.enableStandardTestTask
	}

	private List<String> buildArgs(project, junitExtension, reportsDir) {

		def args = ['--hide-details']

		addSelectors(project, junitExtension.selectors, args)
		// TODO #554 Remove this if
		if (junitExtension.includeClassNamePattern != ClassNameFilter.STANDARD_INCLUDE_PATTERN
				|| !junitExtension.tags.include.empty  || !junitExtension.tags.exclude.empty
				|| !junitExtension.engines.include.empty  || !junitExtension.engines.exclude.empty) {
			addFilters(junitExtension.includeClassNamePattern, junitExtension.tags, junitExtension.engines, args)
		} else {
			def filters = junitExtension.extensions.filters
			addFilters(filters.includeClassNamePattern, filters.tags, filters.engines, args)
		}

		args.add('--reports-dir')
		args.add(reportsDir.getAbsolutePath())

		return args
	}

	private void addFilters(includeClassNamePattern, tags, engines, args) {
		if (includeClassNamePattern) {
			args.addAll(['-n', includeClassNamePattern])
		}
		tags.include.each { tag ->
			args.addAll(['-t', tag])
		}
		tags.exclude.each { tag ->
			args.addAll(['-T', tag])
		}
		engines.include.each { engineId ->
			args.addAll(['-e', engineId])
		}
		engines.exclude.each { engineId ->
			args.addAll(['-E', engineId])
		}
	}

	private void addSelectors(project, selectors, args) {
		if (selectors.empty) {
			args.add('--scan-class-path')

			def rootDirs = []
			project.sourceSets.each { sourceSet ->
				rootDirs.add(sourceSet.output.classesDir)
				rootDirs.add(sourceSet.output.resourcesDir)
				rootDirs.addAll(sourceSet.output.dirs.files)
			}

			rootDirs.each { File root ->
				args.add(root.getAbsolutePath())
			}
		} else {
			selectors.uris.each { uri ->
				args.addAll(['-u', uri])
			}
			selectors.files.each { file ->
				args.addAll(['-f', file])
			}
			selectors.directories.each { directory ->
				args.addAll(['-d', directory])
			}
			selectors.packages.each { aPackage ->
				args.addAll(['-p', aPackage])
			}
			selectors.classes.each { aClass ->
				args.addAll(['-c', aClass])
			}
			selectors.methods.each { method ->
				args.addAll(['-m', method])
			}
			selectors.resources.each { resource ->
				args.addAll(['-r', resource])
			}
		}
	}

}
