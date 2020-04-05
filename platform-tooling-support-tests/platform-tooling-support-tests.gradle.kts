plugins {
	`java-library-conventions`
}

apply(from = "$rootDir/gradle/testing.gradle.kts")

javaLibrary {
	mainJavaVersion = JavaVersion.VERSION_11
}

dependencies {
	internal(platform(project(":dependencies")))

	implementation("de.sormuras:bartholdy") {
		because("manage external tool installations")
	}
	implementation("commons-io:commons-io") {
		because("moving/deleting directory trees")
	}

	testImplementation("org.assertj:assertj-core") {
		because("more assertions")
	}
	testImplementation("com.tngtech.archunit:archunit-junit5-api") {
		because("checking the architecture of JUnit 5")
	}
	testImplementation("org.codehaus.groovy:groovy-all") {
		because("it provides convenience methods to handle process output")
		exclude(group = "org.junit.platform", module = "junit-platform-launcher")
	}
	testImplementation("biz.aQute.bnd:biz.aQute.bndlib") {
		because("parsing OSGi metadata")
	}
	testRuntimeOnly("com.tngtech.archunit:archunit-junit5-engine") {
		because("contains the ArchUnit TestEngine implementation")
	}
	testRuntimeOnly("org.slf4j:slf4j-jdk14") {
		because("provide appropriate SLF4J binding")
	}
}

tasks.test {
	inputs.dir("projects")

	// Opt-in via system property: '-Dplatform.tooling.support.tests.enabled=true'
	enabled = System.getProperty("platform.tooling.support.tests.enabled")?.toBoolean() ?: false

	// The following if-block is necessary since Gradle will otherwise
	// always publish all mavenizedProjects even if this "test" task
	// is not executed.
	if (enabled) {
		// All maven-aware projects must be installed, i.e. published to the local repository
		val mavenizedProjects: List<Project> by rootProject
		val tempRepoName: String by rootProject
		val tempRepoDir: File by rootProject

		(mavenizedProjects + project(":junit-bom"))
				.map { project -> project.tasks.named("publishAllPublicationsTo${tempRepoName.capitalize()}Repository") }
				.forEach { dependsOn(it) }
		// Pass "java.home.N" system properties from sources like "~/.gradle/gradle.properties".
		// Values will be picked up by: platform.tooling.support.Helper::getJavaHome
		for (N in 8..99) {
			val home = project.properties["java.home.$N"] ?: System.getenv("JDK$N")
			if (home != null) systemProperty("java.home.$N", home)
		}
		// TODO Enabling parallel execution fails due to Gradle's listener not being thread-safe:
		//   Received a completed event for test with unknown id "10.5".
		//   Registered test ids: "[:platform-tooling-support-tests:test, 10.1]"
		// systemProperty("junit.jupiter.execution.parallel.enabled", "true")

		// Pass version constants (declared in Versions.kt) to tests as system properties
		systemProperty("Versions.apiGuardian", versions.apiguardian)
		systemProperty("Versions.assertJ", versions.assertj)
		systemProperty("Versions.junit4", versions.junit4)
		systemProperty("Versions.ota4j", versions.opentest4j)
		jvmArgumentProviders += MavenRepo(tempRepoDir)
	}

	filter {
		// Include only tests from this module
		includeTestsMatching("platform.tooling.support.*")
	}

	(options as JUnitPlatformOptions).apply {
		includeEngines("archunit")
	}

	maxParallelForks = 1 // Bartholdy.install is not parallel safe, see https://github.com/sormuras/bartholdy/issues/4
}

class MavenRepo(@get:InputDirectory val repoDir: File) : CommandLineArgumentProvider {
	override fun asArguments() = listOf("-Dmaven.repo=$repoDir")
}
