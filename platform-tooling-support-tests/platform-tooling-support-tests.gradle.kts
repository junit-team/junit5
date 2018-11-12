import org.jetbrains.kotlin.daemon.common.configureDaemonJVMOptions

apply(from = "$rootDir/gradle/testing.gradle.kts")

afterEvaluate {
	java {
		sourceCompatibility = JavaVersion.VERSION_11
		targetCompatibility = JavaVersion.VERSION_11
	}
	tasks.withType<JavaCompile> {
		options.encoding = "UTF-8"
		options.compilerArgs.add("-parameters")
		options.compilerArgs.addAll(listOf("--release", java.targetCompatibility.majorVersion))
	}
}

dependencies {
	implementation("de.sormuras:bartholdy:${Versions.bartholdy}") {
		because("manage external tool installations")
	}
	implementation("commons-io:commons-io:${Versions.commonsIo}") {
		because("moving/deleting directory trees")
	}

	testImplementation("com.tngtech.archunit:archunit-junit5-api:${Versions.archunit}") {
		because("checking the architecture of JUnit 5")
	}
	testRuntimeOnly("com.tngtech.archunit:archunit-junit5-engine:${Versions.archunit}") {
		because("contains the ArchUnit TestEngine implementation")
	}
	testRuntimeOnly("org.slf4j:slf4j-jdk14:${Versions.slf4j}") {
		because("provide appropriate SLF4J binding")
	}
}

tasks.test {
	// Opt-in via system property: '-Dplatform.tooling.support.tests.enabled=true'
	enabled = System.getProperty("platform.tooling.support.tests.enabled")?.toBoolean() ?: false

	// The following if-block is necessary since Gradle will otherwise
	// always publish all mavenizedProjects even if this "test" task
	// is not executed.
	if (enabled) {
		// All maven-aware projects must be installed, i.e. published to the local repository
		val mavenizedProjects = rootProject.extra["mavenizedProjects"] as List<String>
		mavenizedProjects
				.map { name -> rootProject.project(name) }
				.map { project -> project.tasks.named(MavenPublishPlugin.PUBLISH_LOCAL_LIFECYCLE_TASK_NAME)}
				.forEach { dependsOn(it) }
		// Pass "java.home.N" system properties from sources like "~/.gradle/gradle.properties".
		// Values will be picked up by: platform.tooling.support.Helper::getJavaHome
		for (N in 8..99) {
			val home = project.properties["java.home.$N"]
			if (home != null) systemProperty("java.home.$N", home)
		}
		// TODO Enabling parallel execution fails due to Gradle"s listener not being thread-safe:
		//   Received a completed event for test with unknown id "10.5".
		//   Registered test ids: "[:platform-tooling-support-tests:test, 10.1]"
		// systemProperty("junit.jupiter.execution.parallel.enabled", "true")
	}

	filter {
		// Include only tests from this module
		includeTestsMatching("platform.tooling.support.*")
	}
}
