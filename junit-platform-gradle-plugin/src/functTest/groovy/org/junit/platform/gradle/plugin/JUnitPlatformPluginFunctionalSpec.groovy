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

import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths

import org.gradle.testkit.runner.BuildResult
import org.gradle.testkit.runner.TaskOutcome

class JUnitPlatformPluginFunctionalSpec extends AbstractFunctionalSpec {

	private List<File> testCompileClasspath
	private List<File> testRuntimeClasspath

	def setup() {
		testCompileClasspath = loadClassPathManifestResource('test-compile-classpath.txt')
		testRuntimeClasspath = loadClassPathManifestResource('test-runtime-classpath.txt')
	}

	def "can be used with 'java' plugin"() {
		given:
		javaPlugin()
		javaFile()
		succeedingTestFile()

		when:
		BuildResult result = build('build')

		then:
		result.task(':junitPlatformTest').outcome == TaskOutcome.SUCCESS
		result.task(':build').outcome == TaskOutcome.SUCCESS
		result.output.contains('1 tests successful')
	}

	def "can be used with 'java-library' plugin"() {
		given:
		javaFile()
		succeedingTestFile()
		javaLibraryPlugin()

		when:
		BuildResult result = build('build')

		then:
		result.task(':junitPlatformTest').outcome == TaskOutcome.SUCCESS
		result.task(':build').outcome == TaskOutcome.SUCCESS
		result.output.contains('1 tests successful')
	}

	def "failing test fails build with 'java' plugin"() {
		given:
		javaFile()
		failingTestFile()
		javaPlugin()

		when:
		BuildResult result = buildAndFail('build')

		then:
		result.task(':junitPlatformTest').outcome == TaskOutcome.FAILED
		result.output.contains('1 tests failed')
	}

	def "failing test fails build with 'java-library' plugin"() {
		given:
		javaFile()
		failingTestFile()
		javaLibraryPlugin()

		when:
		BuildResult result = buildAndFail('build')

		then:
		result.task(':junitPlatformTest').outcome == TaskOutcome.FAILED
		result.output.contains('1 tests failed')
	}

	private static String splitClasspath(List<File> dependencies) {
		return dependencies
				.collect { it.absolutePath.replace('\\', '\\\\') } // escape backslashes in Windows paths
				.collect { "'$it'" }
				.join(', ')
	}

	private void javaPlugin() {
		buildFile << """
apply plugin: 'java'

sourceCompatibility = JavaVersion.VERSION_1_8
targetCompatibility = JavaVersion.VERSION_1_8

dependencies {
	testCompile files(${splitClasspath(testCompileClasspath)})
	testRuntime files(${splitClasspath(testRuntimeClasspath)})
	// Use local dependencies so that defaultDependencies are not used
	junitPlatform files(${splitClasspath(testRuntimeClasspath)})
}

junitPlatform {
	details 'summary'
	filters {
		engines {
			include 'junit-jupiter'
		}
	}
}
"""
	}

	private void javaLibraryPlugin() {
		buildFile << """
apply plugin: 'java-library'

sourceCompatibility = JavaVersion.VERSION_1_8
targetCompatibility = JavaVersion.VERSION_1_8

dependencies {
	testImplementation files(${splitClasspath(testCompileClasspath)})
	testRuntimeOnly files(${splitClasspath(testRuntimeClasspath)})
	// Use local dependencies so that defaultDependencies are not used
	junitPlatform files(${splitClasspath(testRuntimeClasspath)})
}

junitPlatform {
	details 'summary'
	filters {
		engines {
			include 'junit-jupiter'
		}
	}
}
"""
	}

	private void javaFile() {
		Path javaFile = Paths.get(temporaryFolder.root.toString(), 'src', 'main', 'java', 'org', 'junit', 'gradletest', 'Adder.java')
		Files.createDirectories(javaFile.parent)
		javaFile.withWriter {
			it.write(
					'''
package org.junit.gradletest;

public class Adder {
		public int add(int a, int b) {
				return a + b;
		}
}
''')
		}
	}

	private void failingTestFile() {
		Path testPath = Paths.get(temporaryFolder.root.toString(), 'src', 'test', 'java', 'org', 'junit', 'gradletest', 'AdderTest.java')
		Files.createDirectories(testPath.parent)
		testPath.withWriter {
			it.write(
					'''
package org.junit.gradletest;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class AdderTest {
		@Test
		void failingTest() {
				Adder adder = new Adder();
				assertEquals(5, adder.add(3, 3), "This should fail!");
		}
}
''')
		}
	}

	def "when tests are executed again after no changes then the junitPlatformTest task is UP-TO-DATE"() {
		given:
		javaPlugin()
		javaFile()
		succeedingTestFile()

		when:
		BuildResult result = build('test')

		then:
		result.task(':junitPlatformTest').outcome == TaskOutcome.SUCCESS

		when:
		result = build('test')

		then:
		result.task(':junitPlatformTest').outcome == TaskOutcome.UP_TO_DATE
	}

	private void succeedingTestFile() {
		Path testPath = Paths.get(temporaryFolder.root.toString(), 'src', 'test', 'java', 'org', 'junit', 'gradletest', 'AdderTest.java')
		Files.createDirectories(testPath.parent)
		testPath.withWriter { it.write('''
package org.junit.gradletest;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class AdderTest {
		@Test
		void succeedingTest() {
				Adder adder = new Adder();
				assertEquals(10, adder.add(5, 5), "This should succeed!");
		}
}
''') }
	}

	private List<File> loadClassPathManifestResource(String name) {
		InputStream classpathResource = getClass().classLoader.getResourceAsStream(name)
		if (classpathResource == null) {
			throw new IllegalStateException("Did not find required resource with name ${name}")
		}
		return classpathResource.readLines().collect { new File(it) }
	}

}
