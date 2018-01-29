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

import org.gradle.testkit.runner.BuildResult
import org.gradle.testkit.runner.GradleRunner
import org.junit.Rule
import org.junit.rules.TemporaryFolder
import spock.lang.Specification

abstract class AbstractFunctionalSpec extends Specification {

	@Rule
	TemporaryFolder temporaryFolder = new TemporaryFolder()
	File projectDir
	File buildFile
	File settingsFile

	def setup() {
		projectDir = temporaryFolder.root
		buildFile = temporaryFolder.newFile('build.gradle')
		settingsFile = temporaryFolder.newFile('settings.gradle')

		// Add JaCoCo agent from Properties file written by pl.droidsonroids.jacoco.testkit Gradle plugin
		def testkitGradleProperties = getClass().getClassLoader().getResource('testkit-gradle.properties')
		if (testkitGradleProperties) {
			temporaryFolder.newFile('gradle.properties') << testkitGradleProperties.text
		}

		buildFile << """
			plugins {
				id 'org.junit.platform.gradle.plugin'
			}
		"""
		settingsFile << """
			rootProject.name = '$temporaryFolder.root.name'
		"""
	}

	protected BuildResult build(String... arguments) {
		createAndConfigureGradleRunner(arguments).build()
	}

	protected BuildResult buildAndFail(String... arguments) {
		createAndConfigureGradleRunner(arguments).buildAndFail()
	}

	private GradleRunner createAndConfigureGradleRunner(String... arguments) {
		GradleRunner.create().withProjectDir(projectDir).withArguments(arguments).withPluginClasspath()
	}

	static String mavenCentral() {
		"""
			repositories {
				mavenCentral()
			}
		"""
	}

}
