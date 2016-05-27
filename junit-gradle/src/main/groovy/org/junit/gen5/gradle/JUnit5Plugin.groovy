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

/**
 * @since 5.0
 */
class JUnit5Plugin implements Plugin<Project> {
	void apply(Project project) {
		def junit5 = project.extensions.create('junit5', JUnit5Extension)

		project.afterEvaluate {
			configure(project, junit5)
		}
	}

	private void configure(Project project, junit5) {
		if (junit5.version) {
			def junit5Version = junit5.version
			project.dependencies.add("testRuntime", "org.junit:junit-console:${junit5Version}")
			project.dependencies.add("testCompile", "org.junit:junit5-api:${junit5Version}")
			project.dependencies.add("testRuntime", "org.junit:junit5-engine:${junit5Version}")

			if (junit5.runJunit4) {
				project.dependencies.add("testRuntime", "org.junit:junit4-engine:${junit5Version}")
			}
		}

		project.task('junit5Test', group: 'verification', type: JavaExec) { task ->

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

			defineTaskDependencies(project, task, junit5)

			task.classpath = project.sourceSets.test.runtimeClasspath
			task.main = 'org.junit.gen5.console.ConsoleRunner'

			task.args buildArgs(project, junit5, reportsDir)
		}
	}

	private void defineTaskDependencies(project, task, junit5) {
		def test = project.tasks.getByName('test')
		def testClasses = project.tasks.getByName('testClasses')

		task.dependsOn testClasses
		test.dependsOn task
		if (junit5.runJunit4) {
			test.enabled = false
		}
	}

	private ArrayList<String> buildArgs(project, junit5, reportsDir) {

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
}
