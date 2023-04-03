import buildparameters.BuildParametersExtension
import com.gradle.enterprise.gradleplugin.internal.extension.BuildScanExtensionWithHiddenFeatures

pluginManagement {
	repositories {
		includeBuild("gradle/plugins")
		gradlePluginPortal()
	}
	plugins {
		id("com.gradle.enterprise") version "3.12.6" // keep in sync with gradle/plugins/build.gradle.kts
		id("com.gradle.common-custom-user-data-gradle-plugin") version "1.10"
		id("org.gradle.toolchains.foojay-resolver-convention") version "0.4.0"
		id("org.ajoberstar.git-publish") version "4.1.1"
		kotlin("jvm") version "1.8.20"
		// Check if workaround in documentation.gradle.kts can be removed when upgrading
		id("org.asciidoctor.jvm.convert") version "4.0.0-alpha.1"
		id("org.asciidoctor.jvm.pdf") version "4.0.0-alpha.1"
		id("me.champeau.jmh") version "0.7.0"
		id("io.spring.nohttp") version "0.0.11"
		id("io.github.gradle-nexus.publish-plugin") version "1.2.0"
	}
}

plugins {
	id("com.gradle.enterprise")
	id("com.gradle.common-custom-user-data-gradle-plugin")
	id("org.gradle.toolchains.foojay-resolver-convention")
	id("junitbuild.build-parameters")
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

gradleEnterprise {
	buildScan {
		capture.isTaskInputFiles = true
		isUploadInBackground = !buildParameters.ci

		publishAlways()

		// Publish to scans.gradle.com when `--scan` is used explicitly
		if (!gradle.startParameter.isBuildScan) {
			server = gradleEnterpriseServer
			this as BuildScanExtensionWithHiddenFeatures
			publishIfAuthenticated()
		}

		obfuscation {
			if (buildParameters.ci) {
				username { "github" }
			} else {
				hostname { null }
				ipAddresses { emptyList() }
			}
		}

		if (buildParameters.enterprise.testDistribution.enabled) {
			tag("test-distribution")
		}
	}
}

buildCache {
	local {
		isEnabled = !buildParameters.ci
	}
	remote<HttpBuildCache> {
		url = uri(buildParameters.buildCache.url.getOrElse("$gradleEnterpriseServer/cache/"))
		val buildCacheUsername = buildParameters.buildCache.username.map { it.ifBlank { null } }
		val buildCachePassword = buildParameters.buildCache.password.map { it.ifBlank { null } }
		isPush = buildParameters.ci && buildCacheUsername.isPresent && buildCachePassword.isPresent
		credentials {
			username = buildCacheUsername.orNull
			password = buildCachePassword.orNull
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

val buildParameters: BuildParametersExtension
	get() = the()

enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")
