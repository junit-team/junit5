pluginManagement {
	repositories {
		gradlePluginPortal()
		maven(url = "https://jitpack.io")
	}
	resolutionStrategy {
		eachPlugin {
			when (requested.id.id) {
				"com.gradle.build-scan" -> useVersion(Versions.buildScanPlugin)
				"net.nemerosa.versioning" -> useVersion(Versions.versioningPlugin)
				"com.github.ben-manes.versions" -> useVersion(Versions.versionsPlugin)
				"com.diffplug.gradle.spotless" -> useVersion(Versions.spotlessPlugin)
				"org.ajoberstar.git-publish" -> useVersion(Versions.gitPublishPlugin)
				"org.jetbrains.kotlin.jvm" -> useVersion(Versions.kotlin)
				"com.github.johnrengelman.shadow" -> useModule("com.github.sormuras:shadow:no-minimize-no-tracker-SNAPSHOT")
				"org.asciidoctor.convert" -> useVersion(Versions.asciidoctorPlugin)
				"me.champeau.gradle.jmh" -> useVersion(Versions.jmhPlugin)
				"de.marcphilipp.nexus-publish" -> useVersion(Versions.nexusPublishPlugin)
			}
		}
	}
}

// Require Java 11 or higher
val javaVersion = JavaVersion.current()
require(javaVersion.isJava11Compatible) {
	"The JUnit 5 build requires Java 11 or higher. Currently executing with Java ${javaVersion.majorVersion}."
}

rootProject.name = "junit5"

include("documentation")
include("junit-jupiter")
include("junit-jupiter-api")
include("junit-jupiter-engine")
include("junit-jupiter-migrationsupport")
include("junit-jupiter-params")
include("junit-platform-commons")
include("junit-platform-console")
include("junit-platform-console-standalone")
include("junit-platform-engine")
include("junit-platform-launcher")
include("junit-platform-reporting")
include("junit-platform-runner")
include("junit-platform-suite-api")
include("junit-platform-testkit")
include("junit-vintage-engine")
include("platform-tests")
include("platform-tooling-support-tests")
include("junit-bom")

// check that every subproject has a custom build file
// based on the project name
rootProject.children.forEach { project ->
	project.buildFileName = "${project.name}.gradle"
	if (!project.buildFile.isFile) {
		project.buildFileName = "${project.name}.gradle.kts"
	}
	require(project.buildFile.isFile) {
		"${project.buildFile} must exist"
	}
}
