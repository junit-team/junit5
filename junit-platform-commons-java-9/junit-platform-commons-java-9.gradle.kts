import org.assertj.core.api.Assertions.assertThat
import java.io.ByteArrayOutputStream

buildscript {
	repositories {
		mavenCentral()
	}
	dependencies {
		classpath("org.assertj:assertj-core:${Versions.assertJ}") {
			because("the testScanModulepath and testNoJavaScripting tasks needs to check the generated output")
		}
	}
}

dependencies {
	// This is project we are extending.
	implementation(project(":junit-platform-commons"))

	// Needed for testing.
	testImplementation(project(":junit-platform-launcher"))
	testImplementation(project(":junit-jupiter-api"))
	testImplementation(project(":junit-jupiter-engine"))
	testImplementation("junit:junit:${Versions.junit4}")

	// Required by :execNoJavaScripting
	testRuntimeOnly(project(":junit-platform-console"))
	testRuntimeOnly(project(":junit-vintage-engine"))
}

// Compiles against the public, supported and documented Java 9 API.
extra["javacRelease"] = 9

val compileTestJava by tasks.getting(JavaCompile::class)

// Create "junit-commons-integration-tests.jar" archive which will be later
// mounted as an automatic jar into the integration test module-path.
val generateIntegrationTestsJar by tasks.creating(Jar::class) {
	dependsOn(compileTestJava)
	archiveName = "junit-commons-integration-tests.jar"
	from(compileTestJava.destinationDir)
	include("integration/**/*")
}

// Copy runtime dependencies to a single directory.
val generateDependenciesDirectory by tasks.creating(Copy::class) {
	into("$buildDir/deps")
	from(configurations.testRuntimeClasspath)
}

val testScanModulepath by tasks.creating {
	dependsOn(generateDependenciesDirectory, generateIntegrationTestsJar)
	doLast {
		val out = ByteArrayOutputStream()
		val err = ByteArrayOutputStream()

		exec {
			executable = "${System.getProperty("java.home")}/bin/java"
			standardOutput = out
			errorOutput = err
			args = listOf(
				"--module-path", files(
						generateDependenciesDirectory.destinationDir,
						generateIntegrationTestsJar.archivePath
				).asPath,
				"--add-modules", "ALL-MODULE-PATH,ALL-DEFAULT",
				"--module", "org.junit.platform.console",
				"--scan-modules"
			)
		}
		val text = "$err$out"

		// tree node names
		assertThat(text).contains(
				"JUnit Vintage",
					"VintageIntegrationTest",
						"successfulTest",
				"JUnit Jupiter",
					"JupiterIntegrationTests",
						"version()",
						"moduleIsNamed()",
						"packageName()",
						"javaScriptingModuleIsAvailable()")
		// summary
		assertThat(text).contains("Test run finished after")
		// container summary
		assertThat(text).containsSubsequence(
				"4 containers found",
				"0 containers skipped",
				"4 containers started",
				"0 containers aborted",
				"4 containers successful",
				"0 containers failed")
		// tests summary
		assertThat(text).containsSubsequence(
				"6 tests found",
				"0 tests skipped",
				"6 tests started",
				"0 tests aborted",
				"6 tests successful",
				"0 tests failed")
	}
}

val testNoJavaScripting by tasks.creating {
	dependsOn(generateDependenciesDirectory, generateIntegrationTestsJar)
	doLast {
		val out = ByteArrayOutputStream()
		val err = ByteArrayOutputStream()

		// Execute console launcher on the module-path w/o "java.scripting"
		exec {
			isIgnoreExitValue = true
			standardOutput = out
			errorOutput = err
			executable = "${System.getProperty("java.home")}/bin/java"
			args = listOf(
				"--show-module-resolution",
				"--module-path", files(generateDependenciesDirectory.destinationDir, generateIntegrationTestsJar.archivePath).asPath,
				// only "java.base" and "java.logging" are visible
				"--limit-modules", "java.base",
				"--limit-modules", "java.logging",
				// system modules
				//   "--add-modules", "java.scripting",
				//   "--add-modules", "jdk.scripting.nashorn",
				//   "--add-modules", "jdk.dynalink",
				// "JUnit 5" modules
				"--add-modules", "org.junit.platform.commons",
				"--add-modules", "org.junit.platform.engine",
				"--add-modules", "org.junit.platform.launcher",
				"--add-modules", "org.junit.jupiter.api",
				"--add-modules", "org.junit.jupiter.engine",
				"--add-modules", "org.opentest4j",
				"--add-modules", "org.apiguardian.api",
				// local module containing tests
				"--add-modules", "junit.commons.integration.tests",
				// console launcher with arguments
				"--module", "org.junit.platform.console",
				"--scan-modules"
			)
		}
		val text = "$err$out"

		// tree node names
		assertThat(text).contains(
				"JUnit Jupiter",
					"JupiterIntegrationTests",
						"version()",
						"moduleIsNamed()",
						"packageName()",
						"javaScriptingModuleIsAvailable()",
							"Failed to evaluate condition",
							"Class `javax.script.ScriptEngine` is not loadable",
							"script-based test execution is disabled")
		// summary
		assertThat(text).contains("Test run finished after")
		// container summary
		assertThat(text).containsSubsequence(
				"2 containers found",
				"0 containers skipped",
				"2 containers started",
				"0 containers aborted",
				"2 containers successful",
				"0 containers failed")
		// tests summary
		assertThat(text).containsSubsequence(
				"5 tests found",
				"0 tests skipped",
				"5 tests started",
				"0 tests aborted",
				"4 tests successful",
				"1 tests failed")
	}
}

val test = tasks.named<Test>("test") {
	dependsOn(testScanModulepath, testNoJavaScripting)
	useJUnitPlatform()
	// Exclude "integration" package from default "class-path based" test run.
	// Tests in the "integration" package assume to be run on the module-path.
	exclude("integration/**")
}
