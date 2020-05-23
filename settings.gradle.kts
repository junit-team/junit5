import com.gradle.scan.plugin.internal.api.BuildScanExtensionWithHiddenFeatures

pluginManagement {
	plugins {
		id("com.gradle.enterprise") version "3.3.1"
		id("net.nemerosa.versioning") version "2.12.1"
		id("com.github.ben-manes.versions") version "0.28.0"
		id("com.diffplug.gradle.spotless") version "3.28.1"
		id("org.ajoberstar.git-publish") version "2.1.3"
		kotlin("jvm") version "1.3.71"
		id("org.asciidoctor.jvm.convert") version "3.2.0"
		id("org.asciidoctor.jvm.pdf") version "3.2.0"
		id("me.champeau.gradle.jmh") version "0.5.0"
		id("io.spring.nohttp") version "0.0.4.RELEASE"
	}
}

plugins {
	id("com.gradle.enterprise")
}

val gradleEnterpriseServer = "https://ge.junit.org"
val isCiServer = System.getenv("CI") != null || System.getenv("GITHUB_ACTIONS") != null
val junitBuildCacheUsername: String? by extra
val junitBuildCachePassword: String? by extra

gradleEnterprise {
	buildScan {
		server = gradleEnterpriseServer
		isCaptureTaskInputFiles = true
		isUploadInBackground = !isCiServer
		publishAlways()
		this as BuildScanExtensionWithHiddenFeatures
		publishIfAuthenticated()
		obfuscation {
			if (isCiServer) {
				username { "github" }
			} else {
				hostname { null }
				ipAddresses { emptyList() }
			}
		}
	}
}

buildCache {
	local {
		isEnabled = !isCiServer
	}
	remote<HttpBuildCache> {
		url = uri("$gradleEnterpriseServer/cache/")
		isPush = isCiServer && !junitBuildCacheUsername.isNullOrEmpty() && !junitBuildCachePassword.isNullOrEmpty()
		credentials {
			username = junitBuildCacheUsername?.ifEmpty { null }
			password = junitBuildCachePassword?.ifEmpty { null }
		}
	}
}

val javaVersion = JavaVersion.current()
require(javaVersion.isJava11Compatible) {
	"The JUnit 5 build requires Java 11 or higher. Currently executing with Java ${javaVersion.majorVersion}."
}

rootProject.name = "junit5"

include("dependencies")
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
