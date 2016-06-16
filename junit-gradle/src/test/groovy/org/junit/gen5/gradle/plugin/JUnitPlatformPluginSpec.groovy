/*
 * Copyright 2015-2016 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.junit.gen5.gradle.plugin

import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.tasks.JavaExec
import org.gradle.api.tasks.testing.Test
import org.gradle.testfixtures.ProjectBuilder
import org.junit.gen5.console.ConsoleRunner
import spock.lang.Specification

/**
 * @since 5.0
 */
class JUnitPlatformPluginSpec extends Specification {

	Project project

	def setup() {
		project = ProjectBuilder.builder().build()
	}


	def "applying the plugin"() {
		when:
			project.apply plugin: 'org.junit.platform.gradle.plugin'
		then:
			project.plugins.hasPlugin(JUnitPlatformPlugin)
			project.plugins.getPlugin(JUnitPlatformPlugin) instanceof JUnitPlatformPlugin
			project.extensions.findByName('junitPlatform') instanceof JUnitPlatformExtension
	}

	def "setting junitPlatform properties"() {

		project.apply plugin: 'java'
		project.apply plugin: 'org.junit.platform.gradle.plugin'

		when:
			project.junitPlatform {
				platformVersion '5.0.0-M1'
				disableStandardTestTask false
				matchClassName '.*Tests?'
				logManager 'org.apache.logging.log4j.jul.LogManager'

				engines {
					include 'foo'
					exclude 'bar'
				}

				tags {
					include 'fast'
					exclude 'slow'
				}

				reportsDir new File("any")
			}
		then:
			true == true
	}

	def "creating junitPlatformTest task"() {

		project.apply plugin: 'java'
		project.apply plugin: 'org.junit.platform.gradle.plugin'

		when:
			project.junitPlatform {
				// disableStandardTestTask // defaults to true
				matchClassName '.*Tests?'
				logManager 'org.apache.logging.log4j.jul.LogManager'

				engines {
					include 'foo'
					exclude 'bar'
				}

				tags {
					include 'fast'
					exclude 'slow'
				}

				reportsDir new File("/any")
			}
			project.evaluate()

		then:
			Task junitTask = project.tasks.findByName('junitPlatformTest')
			junitTask instanceof JavaExec
			junitTask.main == ConsoleRunner.class.getName()

			junitTask.args.contains('--enable-exit-code')
			junitTask.args.contains('--hide-details')
			junitTask.args.contains('--all')
			junitTask.args.containsAll('-n', '.*Tests?')
			junitTask.args.containsAll('-t', 'fast')
			junitTask.args.containsAll('-T', 'slow')
			junitTask.args.containsAll('-e', 'foo')
			junitTask.args.containsAll('-E', 'bar')
			junitTask.args.containsAll('-r', new File('/any').getCanonicalFile().toString())
			junitTask.args.contains(project.file('build/classes/main').absolutePath)
			junitTask.args.contains(project.file('build/resources/main').absolutePath)
			junitTask.args.contains(project.file('build/classes/test').absolutePath)
			junitTask.args.contains(project.file('build/resources/test').absolutePath)

			Task testTask = project.tasks.findByName('test')
			testTask instanceof Test
			testTask.enabled == false
	}

	def "disableStandardTestTask set to false"() {

		project.apply plugin: 'java'
		project.apply plugin: 'org.junit.platform.gradle.plugin'

		when:
			project.junitPlatform {
				disableStandardTestTask false
			}
			project.evaluate()

		then:
			Task testTask = project.tasks.findByName('test')
			testTask instanceof Test
			testTask.enabled == true
	}

}
