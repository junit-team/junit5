/*
 * Copyright 2015-2017 the original author or authors.
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
import org.gradle.api.GradleException
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.plugins.JavaBasePlugin
import org.gradle.api.tasks.JavaExec
import org.gradle.util.GradleVersion
import org.junit.platform.console.ConsoleLauncher

/**
 * @since 1.0
 */
@API(status = EXPERIMENTAL, since = "1.0")
class JUnitPlatformPlugin implements Plugin<Project> {

	private static final String EXTENSION_NAME = 'junitPlatform'
	private static final String TASK_NAME      = 'junitPlatformTest'

	protected static final String SELECTORS_EXTENSION_NAME = 'selectors'
	protected static final String FILTERS_EXTENSION_NAME = 'filters'
	protected static final String FILTERS_PACKAGES_EXTENSION_NAME = 'packages'
	protected static final String FILTERS_TAGS_EXTENSION_NAME = 'tags'
	protected static final String FILTERS_ENGINES_EXTENSION_NAME = 'engines'

	void apply(Project project) {
		project.pluginManager.apply('java')
		def junitExtension = project.extensions.create(EXTENSION_NAME, JUnitPlatformExtension, project)
		junitExtension.extensions.create(SELECTORS_EXTENSION_NAME, SelectorsExtension)
		junitExtension.extensions.create(FILTERS_EXTENSION_NAME, FiltersExtension)
		junitExtension.filters.extensions.create(FILTERS_PACKAGES_EXTENSION_NAME, PackagesExtension)
		junitExtension.filters.extensions.create(FILTERS_TAGS_EXTENSION_NAME, TagsExtension)
		junitExtension.filters.extensions.create(FILTERS_ENGINES_EXTENSION_NAME, EnginesExtension)
		/* NOTE TO FUTURE DEVELOPERS!
		 * If you are adding another extension make sure you also provide a statically typed configuration method
		 * on the extension class you are dynamically adding to here.
		 * https://github.com/junit-team/junit5/issues/902
		 */

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
			if (version == null) {
				version = readVersionFromPropertiesFile()
			}
			deps.add(project.dependencies.create("org.junit.platform:junit-platform-launcher:${version}"))
			deps.add(project.dependencies.create("org.junit.platform:junit-platform-console:${version}"))
		}

		JavaExec junitTask = project.tasks.create(TASK_NAME, JavaExec) {
			it.with {
				group = JavaBasePlugin.VERIFICATION_GROUP
				description = 'Runs tests on the JUnit Platform.'
			}
		}

		project.afterEvaluate {
			configure(project, junitTask, junitExtension)
		}
	}

	private String readVersionFromPropertiesFile() {
		Properties properties = new Properties()
		getClass().getResourceAsStream("version.properties").withCloseable { inputStream ->
			properties.load(inputStream)
		}
		return properties.getProperty("version")
	}

	private void configure(Project project, JavaExec junitTask, JUnitPlatformExtension junitExtension) {
		junitTask.with {
			group = JavaBasePlugin.VERIFICATION_GROUP
			description = 'Runs tests on the JUnit Platform.'
			inputs.property('enableStandardTestTask', junitExtension.enableStandardTestTask)
			inputs.property('configurationParameters', junitExtension.configurationParameters)
			inputs.property('selectors.uris', junitExtension.selectors.uris)
			inputs.property('selectors.files', junitExtension.selectors.files)
			inputs.property('selectors.directories', junitExtension.selectors.directories)
			inputs.property('selectors.packages', junitExtension.selectors.packages)
			inputs.property('selectors.classes', junitExtension.selectors.classes)
			inputs.property('selectors.methods', junitExtension.selectors.methods)
			inputs.property('selectors.resources', junitExtension.selectors.resources)
			inputs.property('filters.engines.include', junitExtension.filters.engines.include)
			inputs.property('filters.engines.exclude', junitExtension.filters.engines.exclude)
			inputs.property('filters.tags.include', junitExtension.filters.tags.include)
			inputs.property('filters.tags.exclude', junitExtension.filters.tags.exclude)
			inputs.property('filters.includeClassNamePatterns', junitExtension.filters.includeClassNamePatterns)
			inputs.property('filters.packages.include', junitExtension.filters.packages.include)
			inputs.property('filters.packages.exclude', junitExtension.filters.packages.exclude)

			def reportsDir = junitExtension.reportsDir ?: project.file("$project.buildDir/test-results/junit-platform")
			outputs.dir reportsDir

			if (junitExtension.logManager) {
				systemProperty 'java.util.logging.manager', junitExtension.logManager
			}

			configureTaskDependencies(project, it, junitExtension)

			// Build the classpath from the user's test runtime classpath and the JUnit
			// Platform modules.
			//
			// Note: the user's test runtime classpath must come first; otherwise, code
			// instrumented by Clover in JUnit's build will be shadowed by JARs pulled in
			// via the junitPlatform configuration... leading to zero code coverage for
			// the respective modules.
			classpath = project.sourceSets.test.runtimeClasspath + project.configurations.junitPlatform

			main = ConsoleLauncher.class.getName()
			args buildArgs(project, junitExtension, reportsDir)
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

		def args = []

		if (junitExtension.details) {
			args.add('--details')
			args.add(junitExtension.details.name())
		}

		addSelectors(project, junitExtension.selectors, args)
		addFilters(junitExtension.filters, args)

		junitExtension.configurationParameters.each { key, value ->
			args.addAll('--config', "${key}=${value}")
		}

		args.add('--reports-dir')
		args.add(reportsDir.getAbsolutePath())

		return args
	}

	private void addFilters(filters, args) {
		filters.includeClassNamePatterns.each { pattern ->
			args.addAll(['-n', pattern])
		}
		filters.excludeClassNamePatterns.each { pattern ->
			args.addAll(['-N', pattern])
		}
		filters.packages.include.each { includedPackage ->
			args.addAll(['--include-package', includedPackage])
		}
		filters.packages.exclude.each { excludedPackage ->
			args.addAll(['--exclude-package', excludedPackage])
		}
		filters.tags.include.each { tag ->
			args.addAll(['-t', tag])
		}
		filters.tags.exclude.each { tag ->
			args.addAll(['-T', tag])
		}
		filters.engines.include.each { engineId ->
			args.addAll(['-e', engineId])
		}
		filters.engines.exclude.each { engineId ->
			args.addAll(['-E', engineId])
		}
	}

	private void addSelectors(project, selectors, args) {
		if (selectors.empty) {
			def rootDirs = []
			project.sourceSets.each { sourceSet ->
				if (sourceSet.output.hasProperty('classesDirs')) {
					rootDirs.addAll(sourceSet.output.classesDirs)
				} else {
					rootDirs.add(sourceSet.output.classesDir)
				}
				rootDirs.add(sourceSet.output.resourcesDir)
				rootDirs.addAll(sourceSet.output.dirs.files)
			}
			args.addAll(['--scan-class-path', rootDirs.join(File.pathSeparator)])
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
