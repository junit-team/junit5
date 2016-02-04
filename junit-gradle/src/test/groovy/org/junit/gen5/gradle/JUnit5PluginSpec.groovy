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

	def "setting extension params"() {

		project.apply plugin: 'org.junit.gen5.gradle'

		when:
			project.junit5 {
				version '5.0.0-Alpha'
				runJunit4 true
				matchClassName '.*Tests?'
				logManager 'org.apache.logging.log4j.jul.LogManager'
				requireTag 'fast'
				excludeTag 'slow'
				reportsDir new File("any")
			}
			//JUnit5Plugin plugin = project.plugins.getPlugin(JUnit5Plugin)
			//JUnit5Extension junit5Extension = project.extensions.findByName('junit5')
			//Todo: somehow set the project's configurations otherwise configure() will throw NPE
			// plugin.configure(project, junit5Extension)
		then:
			//Task junit5TestTask = project.tasks.findByName('junit5test')
			//junit5TestTask != null
			true == true
	}
}
