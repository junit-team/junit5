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

/**
 * @since 1.0
 */
class JUnitPlatformPlugin implements Plugin<Project> {

	private static final String EXTENSION_NAME = 'junitPlatform';
	private static final String TASK_NAME      = 'junitPlatformTest';

	void apply(Project project) {
		def junitExtension = project.extensions.create(EXTENSION_NAME, JUnitPlatformExtension)
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
			if (isAndroidProject(project)) {
				configureAndroid(project, junitExtension)
			} else {
				configureJava(project, junitExtension)
			}
		}
	}

	private void configureAndroid(Project project, junitExtension) {
		// Add the test task to each of the project's unit test variants
		def allVariants = isAndroidLibrary(project) ? "libraryVariants" : "applicationVariants"
		def testVariants = project.android[allVariants].findAll { it.hasProperty("unitTestVariant") }

		testVariants.collect { it.unitTestVariant }.each { variant ->
			def buildType = variant.buildType.name
			def nameSuffix = "${variant.flavorName.capitalize()}${buildType.capitalize()}"

			// Obtain variant properties
			def variantData = variant.variantData
			def variantScope = variantData.scope
			def scopeJavaOutputs = variantScope.hasProperty("javaOutputs") ? variantScope.javaOutputs : variantScope.javaOuptuts

			// Obtain tested variant properties
			def testedVariantData = variant.testedVariant.variantData
			def testedVariantScope = testedVariantData.scope
			def testedScopeJavaOutputs = testedVariantScope.hasProperty("javaOutputs") ? testedVariantScope.javaOutputs : testedVariantScope.javaOuptuts

			// Setup classpath for this variant's tests and add the test task
			// (refer to com.android.build.gradle.tasks.factory.UnitTestConfigAction)
			// 1) Add the compiler's classpath
			def classpaths = new ArrayList<>()
			if (variantData.javacTask) {
				def javaCompiler = variant.javaCompiler
				classpaths.add(javaCompiler.classpath)
				classpaths.add(javaCompiler.outputs.files)
			} else {
				classpaths.add(testedScopeJavaOutputs)
				classpaths.add(scopeJavaOutputs)
			}

			// 2) Add the testApk configuration
			classpaths.add(project.configurations.getByName("testApk"))

			// 3) Add test resources
			classpaths.add(variantData.javaResourcesForUnitTesting)
			classpaths.add(testedVariantData.javaResourcesForUnitTesting)

			// 4) Add filtered boot classpath
			def globalScope = variantScope.globalScope
			classpaths.add(globalScope.androidBuilder.getBootClasspath(false).findAll {
				!it.name.equals("android.jar")
			})

			// 5) Add mocked version of android.jar
			classpaths.add(globalScope.mockableAndroidJarFile)

			addJunitPlattformTask(
					project: project,
					junitExtension: junitExtension,
					nameSuffix: nameSuffix,
					classpath: project.files(classpaths),
					dependentTasks: Collections.singletonList("assemble${nameSuffix}UnitTest"))
		}
	}

	private void configureJava(Project project, junitExtension) {
		addJunitPlattformTask(
				project: project,
				junitExtension: junitExtension,
				classpath: project.sourceSets.test.runtimeClasspath,
				dependentTasks: Collections.singletonList("testClasses"))
	}

	private void addJunitPlattformTask(Map map) {
		Project project = map.project
		def junitExtension = map.junitExtension
		def classpath = map.classpath
		String nameSuffix = map.getOrDefault("nameSuffix", "")
		def dependentTasks = map.dependentTasks
		project.task(
				TASK_NAME + nameSuffix,
				type: JavaExec,
				group: 'verification',
				description: 'Runs tests on the JUnit Platform.') { junitTask ->

			junitTask.inputs.property('enableStandardTestTask', junitExtension.enableStandardTestTask)
			junitTask.inputs.property('includedEngines', junitExtension.engines.include)
			junitTask.inputs.property('excludedEngines', junitExtension.engines.exclude)
			junitTask.inputs.property('includedTags', junitExtension.tags.include)
			junitTask.inputs.property('excludedTags', junitExtension.tags.exclude)
			junitTask.inputs.property('includeClassNamePattern', junitExtension.includeClassNamePattern)

			def reportsDir = junitExtension.reportsDir ?: project.file("build/test-results/junit-platform")
			junitTask.outputs.dir reportsDir

			if (junitExtension.logManager) {
				systemProperty 'java.util.logging.manager', junitExtension.logManager
			}

			configureTaskDependencies(project, junitTask, junitExtension, dependentTasks)

			// Build the classpath from the user's test runtime classpath and the JUnit
			// Platform modules.
			//
			// Note: the user's test runtime classpath must come first; otherwise, code
			// instrumented by Clover in JUnit's build will be shadowed by JARs pulled in
			// via the junitPlatform configuration... leading to zero code coverage for
			// the respective modules.
			junitTask.classpath = classpath + project.configurations.junitPlatform

			junitTask.main = ConsoleLauncher.class.getName()
			junitTask.args buildArgs(project, junitExtension, reportsDir)
		}
	}

	private void configureTaskDependencies(project, junitTask, junitExtension, dependentTasks) {
		if (!dependentTasks) {
			dependentTasks = Collections.emptyList()
		}

		dependentTasks.each {
			def task = project.tasks.findByName(it)
			if (task) {
				junitTask.dependsOn task
			}
		}

		def testTask = project.tasks.getByName('test')
		testTask.dependsOn junitTask
		testTask.enabled = junitExtension.enableStandardTestTask
	}

	private ArrayList<String> buildArgs(project, junitExtension, reportsDir) {

		def args = ['--hide-details', '--scan-class-path']

		if (junitExtension.includeClassNamePattern) {
			args.add('-n')
			args.add(junitExtension.includeClassNamePattern)
		}

		junitExtension.tags.include.each { tag ->
			args.add('-t')
			args.add(tag)
		}

		junitExtension.tags.exclude.each { tag ->
			args.add('-T')
			args.add(tag)
		}

		junitExtension.engines.include.each { engineId ->
			args.add('-e')
			args.add(engineId)
		}

		junitExtension.engines.exclude.each { engineId ->
			args.add('-E')
			args.add(engineId)
		}

		args.add('--reports-dir')
		args.add(reportsDir.getAbsolutePath())

		def rootDirs = []
		project.sourceSets.each { sourceSet ->
			rootDirs.add(sourceSet.output.classesDir)
			rootDirs.add(sourceSet.output.resourcesDir)
			rootDirs.addAll(sourceSet.output.dirs.files)
		}

		rootDirs.each { File root ->
			args.add(root.getAbsolutePath())
		}

		return args
	}

	private static boolean isAndroidProject(Project project) {
		return project.plugins.findPlugin("com.android.application") ||
				project.plugins.findPlugin("android") ||
				project.plugins.findPlugin("com.android.test") ||
				isAndroidLibrary(project)
	}

	private static boolean isAndroidLibrary(Project project) {
		return project.plugins.findPlugin("com.android.library") ||
				project.plugins.findPlugin("android-library")
	}
}
