import org.assertj.core.api.Assertions.assertThat
import java.io.ByteArrayOutputStream

buildscript {
	repositories {
		mavenCentral()
	}
	dependencies {
		classpath("org.assertj:assertj-core:${Versions.assertJ}") {
			because("the testScanModulePath and testNoJavaScripting tasks needs to check the generated output")
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

val modulePath = files(
		generateDependenciesDirectory.destinationDir,
		generateIntegrationTestsJar.archivePath
).asPath

val testScanModulePath by tasks.creating {
	dependsOn(generateDependenciesDirectory, generateIntegrationTestsJar)
	description = "Execute console launcher on the module-path and checks output"
	doLast {
		val out = ByteArrayOutputStream()
		val err = ByteArrayOutputStream()

		javaexec {
			standardOutput = out
			errorOutput = err
			jvmArgs = listOf(
				"--module-path", modulePath,
				"--add-modules", "ALL-MODULE-PATH,ALL-DEFAULT"
			)
			main = "--module"
			args = listOf("org.junit.platform.console", "--scan-modules")
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
	description = "Executes ConsoleLauncher on the module-path w/o 'java.scripting' and checks output"
	doLast {
		val out = ByteArrayOutputStream()
		val err = ByteArrayOutputStream()

		javaexec {
			isIgnoreExitValue = true
			standardOutput = out
			errorOutput = err
			jvmArgs = listOf(
				"--show-module-resolution",
				"--module-path", modulePath,
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
				"--add-modules", "junit.commons.integration.tests"
			)
			// console launcher with arguments
			main = "--module"
			args = listOf("org.junit.platform.console", "--scan-modules")
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

tasks.named<Test>("test") {
	dependsOn(testScanModulePath, testNoJavaScripting)
	useJUnitPlatform()
	// Exclude "integration" package from default "class-path based" test run.
	// Tests in the "integration" package assume to be run on the module-path.
	exclude("integration/**")
}
