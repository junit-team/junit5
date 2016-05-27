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

import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.tasks.JavaExec
import org.gradle.testfixtures.ProjectBuilder
import spock.lang.Specification

class JUnit5PluginSpec extends Specification {

	Project project

	def setup() {
		project = ProjectBuilder.builder().build()
	}


	def "applying the plugin"() {
		when:
			project.apply plugin: 'org.junit.gen5.gradle'
		then:
			project.plugins.hasPlugin(JUnit5Plugin)
			project.plugins.getPlugin(JUnit5Plugin) instanceof JUnit5Plugin
			project.extensions.findByName('junit5') instanceof JUnit5Extension
	}

	def "setting junit5 properties"() {

		project.apply plugin: 'java'
		project.apply plugin: 'org.junit.gen5.gradle'

		when:
			project.junit5 {
				version '5.0.0-Alpha'
				runJunit4 true
				matchClassName '.*Tests?'
				logManager 'org.apache.logging.log4j.jul.LogManager'
				requireTag 'fast'
				excludeTag 'slow'
				requireEngine 'junit5'
				reportsDir new File("any")
			}
		then:
			true == true
	}

	def "creating junit5Test task"() {

		project.apply plugin: 'java'
		project.apply plugin: 'org.junit.gen5.gradle'

		when:
			project.junit5 {
				//version '5.0.0-Alpha' // cannot be set in micro test
				//runJunit4 true // cannot be set in micro test
				matchClassName '.*Tests?'
				logManager 'org.apache.logging.log4j.jul.LogManager'
				requireTag 'fast'
				excludeTag 'slow'
				requireEngine 'junit5'
				reportsDir new File("/any")
			}
			project.evaluate()

		then:
			Task junit5TestTask = project.tasks.findByName('junit5Test')
			junit5TestTask instanceof JavaExec
			junit5TestTask.main == 'org.junit.gen5.console.ConsoleRunner'

			junit5TestTask.args.contains('--enable-exit-code')
			junit5TestTask.args.contains('--hide-details')
			junit5TestTask.args.contains('--all')
			junit5TestTask.args.containsAll('-n', '.*Tests?')
			junit5TestTask.args.containsAll('-t', 'fast')
			junit5TestTask.args.containsAll('-T', 'slow')
			junit5TestTask.args.containsAll('-e', 'junit5')
			junit5TestTask.args.containsAll('-r', new File('/any').getCanonicalFile().toString())
			junit5TestTask.args.contains(project.file('build/classes/main').absolutePath)
			junit5TestTask.args.contains(project.file('build/resources/main').absolutePath)
			junit5TestTask.args.contains(project.file('build/classes/test').absolutePath)
			junit5TestTask.args.contains(project.file('build/resources/test').absolutePath)
	}
}
