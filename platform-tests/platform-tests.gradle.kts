import org.gradle.api.tasks.PathSensitivity.RELATIVE

plugins {
	`java-library-conventions`
	`junit4-compatibility`
	`testing-conventions`
	id("me.champeau.gradle.jmh")
}

dependencies {
	// --- Things we are testing --------------------------------------------------
	testImplementation(projects.junitPlatformCommons)
	testImplementation(projects.junitPlatformConsole)
	testImplementation(projects.junitPlatformEngine)
	testImplementation(projects.junitPlatformJfr)
	testImplementation(projects.junitPlatformLauncher)

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

	// --- Test run-time dependencies ---------------------------------------------
	testRuntimeOnly(projects.junitVintageEngine)
	testRuntimeOnly(libs.groovy3) {
		because("`ReflectionUtilsTests.findNestedClassesWithInvalidNestedClassFile` needs it")
	}

	// --- https://openjdk.java.net/projects/code-tools/jmh/ -----------------------
	jmh(libs.jmh.core)
	jmh(projects.junitJupiterApi)
	jmh(libs.junit4)
	jmhAnnotationProcessor(libs.jmh.generator.annprocess)
}

jmh {
	jmhVersion = libs.versions.jmh.get()

	duplicateClassesStrategy = DuplicatesStrategy.WARN
	fork = 0 // Too long command line on Windows...
	warmupIterations = 1
	iterations = 5
}

tasks {
	withType<Test>().configureEach {
		useJUnitPlatform {
			excludeTags("exclude")
		}
		jvmArgs("-Xmx1g")
	}
	test {
		inputs.dir("src/test/resources").withPathSensitivity(RELATIVE)
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
