import org.gradle.api.tasks.PathSensitivity.NONE
import org.gradle.api.tasks.PathSensitivity.RELATIVE

plugins {
	`java-library-conventions`
	`junit4-compatibility`
	`testing-conventions`
	id("me.champeau.jmh")
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
	testImplementation(libs.apiguardian)
	testImplementation(libs.jfrunit)
	testImplementation(libs.joox)
	testImplementation(libs.openTestReporting.tooling)
	testImplementation(libs.bundles.xmlunit)

	// --- Test run-time dependencies ---------------------------------------------
	testRuntimeOnly(projects.junitVintageEngine)
	testRuntimeOnly(libs.groovy4) {
		because("`ReflectionUtilsTests.findNestedClassesWithInvalidNestedClassFile` needs it")
	}

	// --- https://openjdk.java.net/projects/code-tools/jmh/ -----------------------
	jmh(libs.jmh.core)
	jmh(projects.junitJupiterApi)
	jmh(libs.junit4)
	jmhAnnotationProcessor(libs.jmh.generator.annprocess)
}

jmh {
	jmhVersion.set(libs.versions.jmh)

	duplicateClassesStrategy.set(DuplicatesStrategy.WARN)
	fork.set(1)
	warmupIterations.set(1)
	iterations.set(5)
}

tasks {
	withType<Test>().configureEach {
		useJUnitPlatform {
			excludeTags("exclude")
		}
		jvmArgs("-Xmx1g")
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
	checkstyleJmh { // use same style rules as defined for tests
		configFile = rootProject.file("src/checkstyle/checkstyleTest.xml")
	}
}

eclipse {
	classpath {
		plusConfigurations.add(projects.junitPlatformConsole.dependencyProject.configurations["shadowed"])
	}
}

idea {
	module {
		scopes["PROVIDED"]!!["plus"]!!.add(projects.junitPlatformConsole.dependencyProject.configurations["shadowed"])
	}
}
