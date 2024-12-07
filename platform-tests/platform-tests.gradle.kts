
import junitbuild.extensions.capitalized
import org.gradle.api.tasks.PathSensitivity.RELATIVE
import org.gradle.internal.os.OperatingSystem

plugins {
	id("junitbuild.java-library-conventions")
	id("junitbuild.junit4-compatibility")
	id("junitbuild.testing-conventions")
	id("junitbuild.jmh-conventions")
}

val processStarter by sourceSets.creating {
	java {
		srcDir("src/processStarter/java")
	}
}

java {
	registerFeature(processStarter.name) {
		usingSourceSet(processStarter)
	}
}

val woodstox = configurations.dependencyScope("woodstox")
val woodstoxRuntimeClasspath = configurations.resolvable("woodstoxRuntimeClasspath") {
	extendsFrom(configurations.testRuntimeClasspath.get())
	extendsFrom(woodstox.get())
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
	testImplementation(libs.openTestReporting.tooling.core)
	testImplementation(libs.picocli)
	testImplementation(libs.bundles.xmlunit)
	testImplementation(testFixtures(projects.junitJupiterApi))
	testImplementation(testFixtures(projects.junitPlatformReporting))
	testImplementation(projects.platformTests) {
		capabilities {
			requireFeature("process-starter")
		}
	}

	// --- Test run-time dependencies ---------------------------------------------
	testRuntimeOnly(projects.junitVintageEngine)
	testRuntimeOnly(libs.groovy4) {
		because("`ReflectionUtilsTests.findNestedClassesWithInvalidNestedClassFile` needs it")
	}
	woodstox(libs.woodstox)

	// --- https://openjdk.java.net/projects/code-tools/jmh/ ----------------------
	jmh(projects.junitJupiterApi)
	jmh(libs.junit4)

	// --- ProcessStarter dependencies --------------------------------------------
	processStarter.implementationConfigurationName(libs.groovy4) {
		because("it provides convenience methods to handle process output")
	}
	processStarter.implementationConfigurationName(libs.commons.io) {
		because("it uses TeeOutputStream")
	}
	processStarter.implementationConfigurationName(libs.opentest4j) {
		because("it throws TestAbortedException")
	}
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
	}
	test_4_12 {
		useJUnitPlatform {
			includeTags("junit4")
		}
	}
	val testWoodstox by registering(Test::class) {
		val test by testing.suites.existing(JvmTestSuite::class)
		testClassesDirs = files(test.map { it.sources.output.classesDirs })
		classpath = files(sourceSets.main.map { it.output }) + files(test.map { it.sources.output }) + woodstoxRuntimeClasspath.get()
		group = JavaBasePlugin.VERIFICATION_GROUP
		setIncludes(listOf("**/org/junit/platform/reporting/**"))
	}
	check {
		dependsOn(testWoodstox)
	}
	named<JavaCompile>(processStarter.compileJavaTaskName).configure {
		options.release = javaLibrary.testJavaVersion.majorVersion.toInt()
	}
	named<Checkstyle>("checkstyle${processStarter.name.capitalized()}").configure {
		config = resources.text.fromFile(checkstyle.configDirectory.file("checkstyleMain.xml"))
	}
}

eclipse {
	classpath {
		plusConfigurations.add(dependencyProject(projects.junitPlatformConsole).configurations["shadowedClasspath"])
	}
}

idea {
	module {
		scopes["PROVIDED"]!!["plus"]!!.add(dependencyProject(projects.junitPlatformConsole).configurations["shadowedClasspath"])
	}
}
