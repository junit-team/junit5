/*
 * Copyright 2015-2017 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.junit.platform.gradle.plugin

import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.artifacts.Configuration
import org.gradle.api.plugins.JavaPlugin
import org.gradle.api.tasks.JavaExec
import org.gradle.api.tasks.testing.Test
import org.gradle.testfixtures.ProjectBuilder
import org.junit.platform.console.ConsoleLauncher
import org.junit.platform.engine.discovery.ClassNameFilter
import spock.lang.Issue
import spock.lang.Specification

/**
 * @since 1.0
 */
class JUnitPlatformPluginSpec extends Specification {

	private Project project

	def setup() {
		project = ProjectBuilder.builder().build()
	}

	def "plugin does not fail when it is the only plugin applied"() {
		when:
		project.apply plugin: 'org.junit.platform.gradle.plugin'
		project.evaluate()

		then:
		noExceptionThrown()
	}

	def "applying the plugin"() {
		when:
		project.apply plugin: 'org.junit.platform.gradle.plugin'
		then:
		project.plugins.hasPlugin(JavaPlugin)
		project.plugins.hasPlugin(JUnitPlatformPlugin)
		project.plugins.getPlugin(JUnitPlatformPlugin) instanceof JUnitPlatformPlugin
		project.extensions.findByName('junitPlatform') instanceof JUnitPlatformExtension
	}

	def "setting junitPlatform properties"() {
		given:
		project.apply plugin: 'org.junit.platform.gradle.plugin'

		when:
		project.junitPlatform {
			platformVersion '5.0.0-M1'
			enableStandardTestTask true
			logManager 'org.apache.logging.log4j.jul.LogManager'

			filters {
				includeClassNamePattern '.*Tests?'
				excludeClassNamePattern '.*TestCase'
				engines {
					include 'foo'
					exclude 'bar'
				}
				tags {
					include 'fast'
					exclude 'slow'
				}
			}

			reportsDir new File("any")

			details 'NONE'
		}
		then:
		noExceptionThrown()
	}

	def "creating junitPlatformTest task"() {
		given:
		project.apply plugin: 'org.junit.platform.gradle.plugin'

		when:
		project.junitPlatform {
			// enableStandardTestTask // defaults to false
			logManager 'org.apache.logging.log4j.jul.LogManager'

			filters {
				includeClassNamePattern '.*Tests?'
				includeClassNamePatterns 'Foo', 'Bar'
				excludeClassNamePattern '.*TestCase'
				excludeClassNamePatterns 'One', 'Two'
				packages {
					include 'testpackage.included.p1', 'testpackage.included.p2'
					exclude 'testpackage.excluded.p1', 'testpackage.excluded.p2'
				}
				engines {
					include 'foo'
					exclude 'bar'
				}
				tags {
					include 'fast'
					exclude 'slow'
				}
			}

			reportsDir new File("/any")

			details 'FLAT'
		}
		project.evaluate()

		then:
		Task junitTask = project.tasks.findByName('junitPlatformTest')
		junitTask instanceof JavaExec
		junitTask.main == ConsoleLauncher.class.getName()

		junitTask.args.containsAll('--details', 'FLAT')
		junitTask.args.containsAll('-n', '.*Tests?', '-n', 'Foo', '-n', 'Bar')
		junitTask.args.containsAll('-N', '.*TestCase', '-N', 'One', '-N', 'Two')
		junitTask.args.containsAll('--include-package', 'testpackage.included.p1', '--include-package', 'testpackage.included.p2')
		junitTask.args.containsAll('--exclude-package', 'testpackage.excluded.p1', '--exclude-package', 'testpackage.excluded.p2')
		junitTask.args.containsAll('-t', 'fast')
		junitTask.args.containsAll('-T', 'slow')
		junitTask.args.containsAll('-e', 'foo')
		junitTask.args.containsAll('-E', 'bar')
		junitTask.args.containsAll('--reports-dir', new File('/any').getCanonicalFile().toString())
		def classpathToBeScanned = ['build/classes/java/main', 'build/resources/main', 'build/classes/java/test', 'build/resources/test']
				.collect { path -> project.file(path).absolutePath }
				.join(File.pathSeparator)
		junitTask.args.containsAll('--scan-class-path', classpathToBeScanned)

		Task testTask = project.tasks.findByName('test')
		testTask instanceof Test
		testTask.enabled == false
	}

	def "uses standard class name pattern"() {
		given:
		project.apply plugin: 'org.junit.platform.gradle.plugin'

		when:
		project.evaluate()

		then:
		Task junitTask = project.tasks.findByName('junitPlatformTest')
		junitTask.args.containsAll('-n', ClassNameFilter.STANDARD_INCLUDE_PATTERN)
	}

	def "enableStandardTestTask set to true"() {
		given:
		project.apply plugin: 'org.junit.platform.gradle.plugin'

		when:
		project.junitPlatform { enableStandardTestTask true }
		project.evaluate()

		then:
		Task testTask = project.tasks.findByName('test')
		testTask instanceof Test
		testTask.enabled == true
	}

	def "when buildDir is set to non-standard location, it will be honored"() {
		given:
		project.apply plugin: 'java'
		project.apply plugin: 'org.junit.platform.gradle.plugin'

		when:
		project.buildDir = new File('/foo/bar/build')
		project.evaluate()

		then:
		Task junitTask = project.tasks.findByName('junitPlatformTest')
		junitTask.args.containsAll('--reports-dir', new File(project.buildDir, 'test-results/junit-platform').getCanonicalFile().toString())
	}

	def "users can set buildDir to be a GString, and it will be converted to file"() {
		given:
		project.apply plugin: 'org.junit.platform.gradle.plugin'

		when:
		project.junitPlatform {
			reportsDir = "$project.buildDir/foo/bar/baz"
		}
		project.evaluate()

		then:
		Task junitTask = project.tasks.findByName('junitPlatformTest')
		junitTask.args.containsAll('--reports-dir', new File(project.buildDir, 'foo/bar/baz').getCanonicalFile().toString())
	}

	def "selectors can be specified"() {
		given:
		project.apply plugin: 'org.junit.platform.gradle.plugin'

		when:
		project.junitPlatform {
			selectors {
				uris 'u:foo', 'u:bar'
				uri 'u:qux'
				files 'foo.txt', 'bar.csv'
				file 'qux.json'
				directories 'foo/bar', 'bar/qux'
				directory 'qux/bar'
				packages 'com.acme.foo', 'com.acme.bar'
				aPackage 'com.example.app'
				classes 'com.acme.Foo', 'com.acme.Bar'
				aClass 'com.example.app.Application'
				methods 'com.acme.Foo#a', 'com.acme.Foo#b'
				method 'com.example.app.Application#run(java.lang.String[])'
				resources '/bar.csv', '/foo/input.json'
				resource '/com/acme/my.properties'
			}
		}
		project.evaluate()

		then:
		Task junitTask = project.tasks.findByName('junitPlatformTest')

		!junitTask.args.contains('--scan-class-path')
		!junitTask.args.contains(project.file('build/classes/main').absolutePath)

		junitTask.args.containsAll('-u', 'u:foo', '-u', 'u:bar', '-u', 'u:qux')
		junitTask.args.containsAll('-f', 'foo.txt', '-f', 'bar.csv', '-f', 'qux.json')
		junitTask.args.containsAll('-d', 'foo/bar', '-d', 'bar/qux', '-d', 'qux/bar')
		junitTask.args.containsAll('-p', 'com.acme.foo', '-p', 'com.acme.bar', '-p', 'com.example.app')
		junitTask.args.containsAll('-c', 'com.acme.Foo', '-c', 'com.acme.Bar', '-c', 'com.example.app.Application')
		junitTask.args.containsAll('-m', 'com.acme.Foo#a', '-m', 'com.acme.Foo#b', '-m', 'com.example.app.Application#run(java.lang.String[])')
		junitTask.args.containsAll('-r', '/bar.csv', '-r', '/foo/input.json', '-r', '/com/acme/my.properties')
	}

	def "adds dependencies to configuration"() {
		given:
		project.apply plugin: 'org.junit.platform.gradle.plugin'

		when:
		project.junitPlatform {
			platformVersion '1.0.0'
		}
		project.evaluate()

		then:
		Configuration configuration = project.configurations.getByName("junitPlatform")
		configuration.triggerWhenEmptyActionsIfNecessary()

		configuration.getAllDependencies().contains(
				project.dependencies.create("org.junit.platform:junit-platform-launcher:1.0.0"),
		)

		configuration.getAllDependencies().contains(
				project.dependencies.create("org.junit.platform:junit-platform-console:1.0.0")
		)
	}

	def "adds dependencies with fixed version when not explicitly configured"() {
		given:
		project.apply plugin: 'org.junit.platform.gradle.plugin'

		when:
		project.evaluate()

		then:
		Configuration configuration = project.configurations.getByName("junitPlatform")
		configuration.triggerWhenEmptyActionsIfNecessary()

		configuration.getAllDependencies()
			.findAll { dependency -> "org.junit.platform" == dependency.getGroup() }
			.collect { dependency -> dependency.getVersion() }
			.findAll { version -> version.startsWith("1.") && !version.contains("+")}
			.size() == 2
	}

	@Issue('https://github.com/junit-team/junit5/issues/708')
	def "can configure the junitPlatformTest task during the configuration phase"() {
		given:
		String customDescription = 'My custom description'
		project.apply plugin: 'org.junit.platform.gradle.plugin'

		when:
		final junitPlatformTest = project.tasks.getByName('junitPlatformTest')
		junitPlatformTest.description = customDescription

		then:
		junitPlatformTest.description == customDescription
	}
}
