pluginManagement {
	plugins {
		id("com.gradle.enterprise") version "3.1"
		id("net.nemerosa.versioning") version "2.8.2"
		id("com.github.ben-manes.versions") version "0.26.0"
		id("com.diffplug.gradle.spotless") version "3.25.0"
		id("org.ajoberstar.git-publish") version "2.1.1"
		kotlin("jvm") version "1.3.50"
		id("com.github.johnrengelman.shadow") version "5.2.0"
		id("org.asciidoctor.convert") version "1.5.8.1"
		id("me.champeau.gradle.jmh") version "0.4.8"
		id("io.spring.nohttp") version "0.0.3.RELEASE"
	}
}

plugins {
	id("com.gradle.enterprise")
}

gradleEnterprise {
	buildScan {
		termsOfServiceUrl = "https://gradle.com/terms-of-service"
		termsOfServiceAgree = "yes"
	}
}

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
