plugins {
	`java-library-conventions`
	`junit4-compatibility`
	id("me.champeau.gradle.jmh")
}

apply(from = "$rootDir/gradle/testing.gradle.kts")

dependencies {
	internal(platform(project(":dependencies")))

	// --- Things we are testing --------------------------------------------------
	testImplementation(project(":junit-platform-commons"))
	testImplementation(project(":junit-platform-console"))
	testImplementation(project(":junit-platform-engine"))
	testImplementation(project(":junit-platform-launcher"))

	// --- Things we are testing with ---------------------------------------------
	testImplementation(project(":junit-platform-runner"))
	testImplementation(project(":junit-platform-testkit"))
	testImplementation(testFixtures(project(":junit-platform-engine")))
	testImplementation(testFixtures(project(":junit-platform-launcher")))
	testImplementation(project(":junit-jupiter-engine"))
	testImplementation("org.apiguardian:apiguardian-api")

	// --- Test run-time dependencies ---------------------------------------------
	testRuntimeOnly(project(":junit-vintage-engine"))
	testRuntimeOnly("org.codehaus.groovy:groovy-all") {
		because("`ReflectionUtilsTests.findNestedClassesWithInvalidNestedClassFile` needs it")
	}

	// --- https://openjdk.java.net/projects/code-tools/jmh/ -----------------------
	jmh("org.openjdk.jmh:jmh-core") {
		exclude(module = "jopt-simple")
	}
	jmhAnnotationProcessor(platform(project(":dependencies")))
	jmhAnnotationProcessor("org.openjdk.jmh:jmh-generator-annprocess")
	jmh(platform(project(":dependencies")))
	jmh(project(":junit-jupiter-api"))
	jmh("junit:junit")
}

jmh {
	jmhVersion = versions["jmh"]

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
		jvmArgs = listOf("-Xmx1g")
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
		plusConfigurations.add(project(":junit-platform-console").configurations["shadowed"])
	}
}

idea {
	module {
		scopes["PROVIDED"]!!["plus"]!!.add(project(":junit-platform-console").configurations["shadowed"])
	}
}
