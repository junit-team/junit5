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
