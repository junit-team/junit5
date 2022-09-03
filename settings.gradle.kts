import com.gradle.enterprise.gradleplugin.internal.extension.BuildScanExtensionWithHiddenFeatures

pluginManagement {
	repositories {
		gradlePluginPortal()
	}
	plugins {
		id("com.gradle.enterprise") version "3.11.1"
		id("com.gradle.enterprise.test-distribution") version "2.3.5" // keep in sync with buildSrc/build.gradle.kts
		id("com.gradle.common-custom-user-data-gradle-plugin") version "1.7.2"
		id("org.ajoberstar.git-publish") version "3.0.0"
		kotlin("jvm") version "1.5.31"
		// Check if workaround in documentation.gradle.kts can be removed when upgrading
		id("org.asciidoctor.jvm.convert") version "3.3.2"
		id("org.asciidoctor.jvm.pdf") version "3.3.2"
		id("me.champeau.jmh") version "0.6.6"
		id("io.spring.nohttp") version "0.0.10"
		id("io.github.gradle-nexus.publish-plugin") version "1.1.0"
	}
}

plugins {
	id("com.gradle.enterprise")
	id("com.gradle.enterprise.test-distribution")
	id("com.gradle.common-custom-user-data-gradle-plugin")
}

dependencyResolutionManagement {
	repositories {
		mavenCentral()
		maven(url = "https://oss.sonatype.org/content/repositories/snapshots") {
			mavenContent {
				snapshotsOnly()
			}
		}
	}
}

val gradleEnterpriseServer = "https://ge.junit.org"
val isCiServer = System.getenv("CI") != null
val junitBuildCacheUrl: String? by extra
val junitBuildCacheUsername: String? by extra
val junitBuildCachePassword: String? by extra

gradleEnterprise {
	buildScan {
		capture.isTaskInputFiles = true
		isUploadInBackground = !isCiServer

		publishAlways()

		// Publish to scans.gradle.com when `--scan` is used explicitly
		if (!gradle.startParameter.isBuildScan) {
			server = gradleEnterpriseServer
			this as BuildScanExtensionWithHiddenFeatures
			publishIfAuthenticated()
		}

		obfuscation {
			if (isCiServer) {
				username { "github" }
			} else {
				hostname { null }
				ipAddresses { emptyList() }
			}
		}

		val enableTestDistribution = providers.gradleProperty("enableTestDistribution")
			.forUseAtConfigurationTime()
			.map(String::toBoolean)
			.getOrElse(false)
		if (enableTestDistribution) {
			tag("test-distribution")
		}
	}
}

buildCache {
	local {
		isEnabled = !isCiServer
	}
	remote<HttpBuildCache> {
		url = uri(junitBuildCacheUrl ?: "$gradleEnterpriseServer/cache/")
		isPush = isCiServer && !junitBuildCacheUsername.isNullOrEmpty() && !junitBuildCachePassword.isNullOrEmpty()
		credentials {
			username = junitBuildCacheUsername?.ifEmpty { null }
			password = junitBuildCachePassword?.ifEmpty { null }
		}
	}
}

val javaVersion = JavaVersion.current()
require(javaVersion == JavaVersion.VERSION_17) {
	"The JUnit 5 build must be executed with Java 17. Currently executing with Java ${javaVersion.majorVersion}."
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
include("junit-platform-jfr")
include("junit-platform-launcher")
include("junit-platform-reporting")
include("junit-platform-runner")
include("junit-platform-suite")
include("junit-platform-suite-api")
include("junit-platform-suite-commons")
include("junit-platform-suite-engine")
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

enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")
