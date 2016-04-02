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

import org.gradle.api.JavaVersion
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.tasks.JavaExec
import org.gradle.testfixtures.ProjectBuilder
import spock.lang.Requires
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
	}

	@Requires({ System.getenv("ANDROID_HOME") })
	def "setting junit5 properties (android)"() {

		Project androidProject = ProjectBuilder.builder().withParent(project).build()
		androidProject.file(".").mkdir();
		androidProject.apply plugin: 'com.android.application'
		androidProject.apply plugin: 'org.junit.gen5.gradle'
		androidProject.repositories {
			jcenter()
		}

		when:
			androidProject.android {
				compileSdkVersion 23
				buildToolsVersion "23.0.2"

				defaultConfig {
					applicationId "org.junit.android.sample"
					minSdkVersion 23
					targetSdkVersion 23
					versionCode 1
					versionName "1.0"
				}

				compileOptions {
					sourceCompatibility JavaVersion.VERSION_1_8
					targetCompatibility JavaVersion.VERSION_1_8
				}
			}
			androidProject.junit5 {
				version '5.0.0-ALPHA'
				runJunit4 true
				matchClassName '.*Tests?'
				logManager 'org.apache.logging.log4j.jul.LogManager'
				requireTag 'fast'
				excludeTag 'slow'
				requireEngine 'junit5'
				reportsDir new File("any")
			}
			androidProject.evaluate()

		then:
			true == true
	}

	@Requires({ System.getenv("ANDROID_HOME") })
	def "creating junit5Test task (android)"() {

		Project androidProject = ProjectBuilder.builder().withParent(project).build()
		androidProject.file(".").mkdir();
		androidProject.apply plugin: 'com.android.application'
		androidProject.apply plugin: 'org.junit.gen5.gradle'
		androidProject.repositories {
			jcenter()
		}

		when:
			androidProject.android {
				compileSdkVersion 23
				buildToolsVersion "23.0.2"

				defaultConfig {
					applicationId "org.junit.android.sample"
					minSdkVersion 23
					targetSdkVersion 23
					versionCode 1
					versionName "1.0"
				}

				compileOptions {
					sourceCompatibility JavaVersion.VERSION_1_8
					targetCompatibility JavaVersion.VERSION_1_8
				}
			}
			androidProject.junit5 {
				//version '5.0.0-Alpha' // cannot be set in micro test
				//runJunit4 true // cannot be set in micro test
				matchClassName '.*Tests?'
				logManager 'org.apache.logging.log4j.jul.LogManager'
				requireTag 'fast'
				excludeTag 'slow'
				requireEngine 'junit5'
				reportsDir new File("/any")
			}
			androidProject.evaluate()

		then:
			Task junit5DebugTestTask = androidProject.tasks.findByName('junit5TestDebug')
			junit5DebugTestTask instanceof JavaExec
			junit5DebugTestTask.main == 'org.junit.gen5.console.ConsoleRunner'

			junit5DebugTestTask.args.contains('--enable-exit-code')
			junit5DebugTestTask.args.contains('--hide-details')
			junit5DebugTestTask.args.contains('--all')
			junit5DebugTestTask.args.containsAll('-n', '.*Tests?')
			junit5DebugTestTask.args.containsAll('-t', 'fast')
			junit5DebugTestTask.args.containsAll('-T', 'slow')
			junit5DebugTestTask.args.containsAll('-e', 'junit5')
			junit5DebugTestTask.args.containsAll('-r', new File('/any').getCanonicalFile().toString())

			Task junit5ReleaseTestTask = androidProject.tasks.findByName('junit5TestRelease')
			junit5ReleaseTestTask instanceof JavaExec
			junit5ReleaseTestTask.main == 'org.junit.gen5.console.ConsoleRunner'

			junit5ReleaseTestTask.args.contains('--enable-exit-code')
			junit5ReleaseTestTask.args.contains('--hide-details')
			junit5ReleaseTestTask.args.contains('--all')
			junit5ReleaseTestTask.args.containsAll('-n', '.*Tests?')
			junit5ReleaseTestTask.args.containsAll('-t', 'fast')
			junit5ReleaseTestTask.args.containsAll('-T', 'slow')
			junit5ReleaseTestTask.args.containsAll('-e', 'junit5')
			junit5ReleaseTestTask.args.containsAll('-r', new File('/any').getCanonicalFile().toString())
	}
}
