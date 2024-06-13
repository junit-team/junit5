import org.gradle.api.tasks.PathSensitivity.NONE
import org.gradle.api.tasks.PathSensitivity.RELATIVE
import org.gradle.internal.os.OperatingSystem

plugins {
	id("junitbuild.java-library-conventions")
	id("junitbuild.junit4-compatibility")
	id("junitbuild.testing-conventions")
	id("junitbuild.jmh-conventions")
}

dependencies {
	// --- Things we are testing --------------------------------------------------
	testImplementation(projects.junitPlatformCommons)
	testImplementation(projects.junitPlatformConsole)
	testImplementation(projects.junitPlatformEngine)
	testImplementation(projects.junitPlatformJfr)
	testImplementation(projects.junitPlatformLauncher)
	testImplementation(projects.junitPlatformSuiteCommons)
	testImplementation(projects.junitPlatformSuiteEngine)

	// --- Things we are testing with ---------------------------------------------
	testImplementation(projects.junitPlatformRunner)
	testImplementation(projects.junitPlatformTestkit)
	testImplementation(testFixtures(projects.junitPlatformCommons))
	testImplementation(testFixtures(projects.junitPlatformEngine))
	testImplementation(testFixtures(projects.junitPlatformLauncher))
	testImplementation(projects.junitJupiterEngine)
	testImplementation(testFixtures(projects.junitJupiterEngine))
	testImplementation(libs.apiguardian)
	testImplementation(libs.jfrunit) {
		exclude(group = "org.junit.vintage")
	}
	testImplementation(libs.joox)
	testImplementation(libs.openTestReporting.tooling)
	testImplementation(libs.picocli)
	testImplementation(libs.bundles.xmlunit)
	testImplementation(testFixtures(projects.junitJupiterApi))

	// --- Test run-time dependencies ---------------------------------------------
	testRuntimeOnly(projects.junitVintageEngine)
	testRuntimeOnly(libs.groovy4) {
		because("`ReflectionUtilsTests.findNestedClassesWithInvalidNestedClassFile` needs it")
	}

	// --- https://openjdk.java.net/projects/code-tools/jmh/ -----------------------
	jmh(projects.junitJupiterApi)
	jmh(libs.junit4)
}

jmh {
	duplicateClassesStrategy = DuplicatesStrategy.WARN
	fork = 1
	warmupIterations = 1
	iterations = 5
}

tasks {
	withType<Test>().configureEach {
		useJUnitPlatform {
			excludeTags("exclude")
		}
		jvmArgs("-Xmx1g")
		develocity {
			testDistribution {
				// Retry in a new JVM on Windows to improve chances of successful retries when
				// cached resources are used (e.g. in ClasspathScannerTests)
				retryInSameJvm = !OperatingSystem.current().isWindows
			}
		}
	}
	test {
		// Additional inputs for remote execution with Test Distribution
		inputs.dir("src/test/resources").withPathSensitivity(RELATIVE)
		inputs.file(buildFile).withPathSensitivity(NONE) // for UniqueIdTrackingListenerIntegrationTests
	}
	test_4_12 {
		useJUnitPlatform {
			includeTags("junit4")
		}
	}
}

eclipse {
	classpath {
		plusConfigurations.add(projects.junitPlatformConsole.dependencyProject.configurations["shadowedClasspath"])
	}
}

idea {
	module {
		scopes["PROVIDED"]!!["plus"]!!.add(projects.junitPlatformConsole.dependencyProject.configurations["shadowedClasspath"])
	}
}
