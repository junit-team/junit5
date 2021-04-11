import org.gradle.api.tasks.PathSensitivity.RELATIVE

plugins {
	`java-library-conventions`
	`junit4-compatibility`
	`testing-conventions`
	id("me.champeau.gradle.jmh")
}

dependencies {
	// --- Things we are testing --------------------------------------------------
	testImplementation(projects.platform.commons)
	testImplementation(projects.platform.console)
	testImplementation(projects.platform.engine)
	testImplementation(projects.platform.jfr)
	testImplementation(projects.platform.launcher)

	// --- Things we are testing with ---------------------------------------------
	testImplementation(projects.platform.runner)
	testImplementation(projects.platform.testkit)
	testImplementation(testFixtures(projects.platform.commons))
	testImplementation(testFixtures(projects.platform.engine))
	testImplementation(testFixtures(projects.platform.launcher))
	testImplementation(projects.jupiter.engine)
	testImplementation(libs.apiguardian)
	testImplementation(libs.jfrunit)
	testImplementation(libs.joox)

	// --- Test run-time dependencies ---------------------------------------------
	testRuntimeOnly(projects.vintage.engine)
	testRuntimeOnly(libs.groovy3) {
		because("`ReflectionUtilsTests.findNestedClassesWithInvalidNestedClassFile` needs it")
	}

	// --- https://openjdk.java.net/projects/code-tools/jmh/ -----------------------
	jmh(libs.jmh.core)
	jmh(projects.jupiter.api)
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
		plusConfigurations.add(projects.platform.console.configurations["shadowed"])
	}
}

idea {
	module {
		scopes["PROVIDED"]!!["plus"]!!.add(projects.platform.console.configurations["shadowed"])
	}
}
