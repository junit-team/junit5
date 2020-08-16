import com.gradle.scan.plugin.internal.api.BuildScanExtensionWithHiddenFeatures

pluginManagement {
	plugins {
		id("com.gradle.enterprise") version settings.extra["gradle.enterprise.plugin.version"] as String
		id("net.nemerosa.versioning") version settings.extra["versioning.plugin.version"] as String
		id("com.github.ben-manes.versions") version settings.extra["versions.plugin.version"] as String
		id("com.diffplug.spotless") version settings.extra["spotless.plugin.version"] as String
		id("org.ajoberstar.git-publish") version settings.extra["git-publish.plugin.version"] as String
		kotlin("jvm") version settings.extra["kotlin.plugin.version"] as String
		id("org.asciidoctor.jvm.convert") version settings.extra["asciidoctor.plugin.version"] as String
		id("org.asciidoctor.jvm.pdf") version settings.extra["asciidoctor.plugin.version"] as String
		id("me.champeau.gradle.jmh") version settings.extra["jmh.plugin.version"] as String
		id("io.spring.nohttp") version settings.extra["nohttp.plugin.version"] as String
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
include("junit-platform-jfr")
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
