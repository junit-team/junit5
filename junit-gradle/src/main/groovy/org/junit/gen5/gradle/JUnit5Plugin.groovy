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

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.tasks.JavaExec

class JUnit5Plugin implements Plugin<Project> {

	void apply(Project project) {
		def junit5 = project.extensions.create('junit5', JUnit5Extension)

		project.afterEvaluate {
			if (isAndroidProject(project)) {
				configureAndroid(project, junit5)
			} else {
				configureJava(project, junit5)
			}
		}
	}

	private static void configureJava(Project project, junit5) {
		// Add JUnit dependencies
		addJUnitDependencies(project, junit5, "testCompile", "testRuntime")

		// Add the test task
		addJUnitTask(
				project: project,
				junit5: junit5,
				classpath: project.sourceSets.test.runtimeClasspath,
				dependentTasks: Collections.singletonList("testClasses")
		)
	}

	private static void configureAndroid(Project project, junit5) {
		// Add JUnit dependencies
		addJUnitDependencies(project, junit5, "testCompile", "testApk")

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

			addJUnitTask(
					project: project,
					junit5: junit5,
					nameSuffix: nameSuffix,
					classpath: project.files(classpaths),
					dependentTasks: Collections.singletonList("assemble${nameSuffix}UnitTest")
			)
		}
	}

	private static void addJUnitDependencies(project, junit5, compileConfigName, runtimeConfigName) {
		if (junit5.version) {
			project.dependencies.add(runtimeConfigName, "org.junit:junit-console:${junit5.version}")
			project.dependencies.add(compileConfigName, "org.junit:junit5-api:${junit5.version}")
			project.dependencies.add(runtimeConfigName, "org.junit:junit5-engine:${junit5.version}")

			if (junit5.runJunit4) {
				project.dependencies.add(runtimeConfigName, "org.junit:junit4-engine:${junit5.version}")
			}
		}
	}

	private static void addJUnitTask(Map map) {
		Project project = map.project
		def junit5 = map.junit5
		def classpath = map.classpath
		String nameSuffix = map.getOrDefault('nameSuffix', '')
		def dependentTasks = map.dependentTasks

		project.task("junit5Test${nameSuffix}", group: 'verification', type: JavaExec) { task ->

			task.description = 'Runs JUnit 5 tests.'

			task.inputs.property('version', junit5.version)
			task.inputs.property('runJunit4', junit5.runJunit4)
			task.inputs.property('classNameFilter', junit5.classNameFilter)
			task.inputs.property('requireTags', junit5.requireTags)
			task.inputs.property('excludeTags', junit5.excludeTags)
			task.inputs.property('requiredEngine', junit5.requiredEngine)

			def reportsDir = junit5.reportsDir ?: project.file("build/test-results/junit5")
			task.outputs.dir reportsDir

			if (junit5.logManager) {
				systemProperty 'java.util.logging.manager', junit5.logManager
			}

			// Define dependencies and setup task
			defineTaskDependencies(project, task, junit5, dependentTasks)
			task.classpath = classpath
			task.main = 'org.junit.gen5.console.ConsoleRunner'

			// Add arguments
			task.args buildArgs(project, junit5, classpath, reportsDir)
		}
	}

	private static void defineTaskDependencies(project, task, junit5, dependentTasks) {
		if (!dependentTasks) dependentTasks = Collections.emptyList()

		def test = project.tasks.getByName('test')

		dependentTasks.each {
			def t = project.tasks.findByName(it)
			if (t) {
				task.dependsOn t
			}
		}

		test.dependsOn task
		if (junit5.runJunit4) {
			test.enabled = false
		}
	}

	private static ArrayList<String> buildArgs(project, junit5, classpath, reportsDir) {
		def args = ['--enable-exit-code', '--hide-details', '--all']

		if (junit5.classNameFilter) {
			args.add('-n')
			args.add(junit5.classNameFilter)
		}

		junit5.requireTags.each { String tag ->
			args.add('-t')
			args.add(tag)
		}

		junit5.excludeTags.each { String tag ->
			args.add('-T')
			args.add(tag)
		}

		if (junit5.requiredEngine) {
			args.add('-e')
			args.add(junit5.requiredEngine)
		}

		args.add('-r')
		args.add(reportsDir.absolutePath)

		def classpathRoots = classpath.files

		def rootDirs = classpathRoots.findAll { it.isDirectory() }
		rootDirs.each { File root ->
			args.add(root.absolutePath)
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
