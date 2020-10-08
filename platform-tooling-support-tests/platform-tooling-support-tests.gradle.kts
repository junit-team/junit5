import org.gradle.jvm.toolchain.internal.NoToolchainAvailableException

plugins {
	`java-library-conventions`
	`testing-conventions`
}

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

		// Pass version constants (declared in Versions.kt) to tests as system properties
		systemProperty("Versions.apiGuardian", versions.apiguardian)
		systemProperty("Versions.assertJ", versions.assertj)
		systemProperty("Versions.junit4", versions.junit4)
		systemProperty("Versions.ota4j", versions.opentest4j)

		jvmArgumentProviders += MavenRepo(tempRepoDir)
		jvmArgumentProviders += JavaHomeDir(project, 8)
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

class MavenRepo(@get:InputDirectory @get:PathSensitive(PathSensitivity.RELATIVE) val repoDir: File) : CommandLineArgumentProvider {
	override fun asArguments() = listOf("-Dmaven.repo=$repoDir")
}

class JavaHomeDir(project: Project, @Input val version: Int) : CommandLineArgumentProvider {
	@Internal
	val javaLauncher: Property<JavaLauncher> = project.objects.property<JavaLauncher>()
			.value(project.provider {
				try {
					project.the<JavaToolchainService>().launcherFor {
						languageVersion.set(JavaLanguageVersion.of(version))
					}.get()
				} catch (e: NoToolchainAvailableException) {
					null
				}
			})

	override fun asArguments(): List<String> {
		val metadata = javaLauncher.map { it.metadata }
		val javaHome = metadata.map { it.installationPath.asFile.absolutePath }.orNull
		return javaHome?.let { listOf("-Djava.home.$version=$it") } ?: emptyList()
	}
}
