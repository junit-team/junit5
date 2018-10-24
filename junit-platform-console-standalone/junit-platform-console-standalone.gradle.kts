import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import org.assertj.core.api.Assertions.assertThat
import java.io.ByteArrayOutputStream

buildscript {
	dependencies {
		classpath("org.assertj:assertj-core:${Versions.assertJ}") {
			because("the standaloneCheck task needs to check the generated output")
		}
	}
}

plugins {
	`java-library`
	id("com.github.johnrengelman.shadow")
}

description = "JUnit Platform Console Standalone"

dependencies {
	shadowed(project(":junit-platform-console"))
	shadowed(project(":junit-jupiter-engine"))
	shadowed(project(":junit-jupiter-params"))
	shadowed(project(":junit-vintage-engine"))

	testCompileOnly(project(":junit-jupiter-api"))
	testCompileOnly(project(":junit-jupiter-params"))
	testCompileOnly("junit:junit:${Versions.junit4}")
}

val jar by tasks.getting(Jar::class) {
	enabled = false
	manifest {
		// Note: do not add `"Automatic-Module-Name": ...` because this artifact is not
		// meant to be used on the Java 9 module path.
		// See https://github.com/junit-team/junit5/issues/866#issuecomment-306017162
		attributes("Main-Class" to "org.junit.platform.console.ConsoleLauncher")
	}
}

val jupiterVersion = rootProject.version
val vintageVersion = project.properties["vintageVersion"]

val shadowJar by tasks.getting(ShadowJar::class) {
	// Generate shadow jar only if the underlying manifest was regenerated.
	// See https://github.com/junit-team/junit5/issues/631
	onlyIf {
		(rootProject.extra["generateManifest"] as Boolean || !archivePath.exists())
	}

	classifier = ""
	configurations = listOf(project.configurations["shadowed"])

	// https://github.com/junit-team/junit5/issues/761
	// prevent duplicates, add 3rd-party licenses explicitly
	exclude("META-INF/LICENSE*.md")
	from(project(":junit-platform-console").projectDir) {
		include("LICENSE-picocli.md")
		into("META-INF")
	}
	from(project(":junit-jupiter-params").projectDir) {
		include("LICENSE-univocity-parsers.md")
		into("META-INF")
	}

	mergeServiceFiles()
	manifest.apply {
		inheritFrom(jar.manifest)
		attributes(mapOf(
				"Specification-Title" to project.name,
				"Implementation-Title" to project.name,
				// Generate test engine version information in single shared manifest file.
				// Pattern of key and value: `"Engine-Version-{YourTestEngine#getId()}": "47.11"`
				"Engine-Version-junit-jupiter" to jupiterVersion,
				"Engine-Version-junit-vintage" to vintageVersion,
				// Version-aware binaries are already included - set Multi-Release flag here.
				// See http://openjdk.java.net/jeps/238 for details
				// Note: the "jar --update ... --release X" command does not work with the
				// shadowed JAR as it contains nested classes that do comply multi-release jars.
				"Multi-Release" to true
		))
	}
}

jar.dependsOn(shadowJar)

val standaloneCheck by tasks.creating {
	dependsOn(shadowJar, "testClasses")
	doLast {
		val out = ByteArrayOutputStream()
		val err = ByteArrayOutputStream()

		javaexec {
			workingDir("$buildDir/libs")
			isIgnoreExitValue = true
			jvmArgs("-ea")
			systemProperty("java.util.logging.config.file", "$buildDir/resources/test/logging.properties")
			main = "-jar"
			args = listOf(
					shadowJar.archiveName,
					"--scan-classpath",
					"--include-classname", "standalone.*",
					"--classpath", "$buildDir/classes/java/test",
					"--details", "tree"
			)
			standardOutput = out
			errorOutput = err
		}
		val text = "$err$out"

		// engines -- output depends on default logging configuration
		assertThat(text).contains(
				"junit-jupiter (group ID: org.junit.jupiter, artifact ID: junit-jupiter-engine, version: $jupiterVersion",
				"junit-vintage (group ID: org.junit.vintage, artifact ID: junit-vintage-engine, version: $vintageVersion")
		// tree node names
		assertThat(text).contains(
				"JUnit Jupiter",
					"JupiterIntegration",
						"abort()", "Assumption failed: integration-test-abort",
						"successful()",
						"disabled()", "integration-test-disabled",
						"fail()", "integration-test-fail",
					"JupiterParamsIntegration",
						"[1] argument=test",
				"JUnit Vintage",
					"VintageIntegration",
						"ignored", "integr4tion test",
						"f4il", "f4iled",
						"succ3ssful")
		// summary
		assertThat(text).contains("Test run finished after")
		// container summary
		assertThat(text).containsSubsequence(
				"6 containers found",
				"0 containers skipped",
				"6 containers started",
				"0 containers aborted",
				"6 containers successful",
				"0 containers failed")
		// tests summary
		assertThat(text).containsSubsequence(
				"8 tests found",
				"2 tests skipped",
				"6 tests started",
				"1 tests aborted",
				"3 tests successful",
				"2 tests failed")
	}
}

tasks["check"].dependsOn(standaloneCheck)
tasks["test"].enabled = false // prevent supposed-to-fail integration tests from failing the build
