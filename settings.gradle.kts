import buildparameters.BuildParametersExtension
import com.gradle.enterprise.gradleplugin.internal.extension.BuildScanExtensionWithHiddenFeatures

pluginManagement {
	includeBuild("gradle/plugins")
	repositories {
		gradlePluginPortal()
	}
}

plugins {
	id("junitbuild.build-parameters")
	id("junitbuild.settings-conventions")
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

val buildParameters = the<BuildParametersExtension>()
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
		val buildCacheUsername = buildParameters.buildCache.username.orNull?.ifBlank { null }
		val buildCachePassword = buildParameters.buildCache.password.orNull?.ifBlank { null }
		isPush = buildParameters.ci && buildCacheUsername != null && buildCachePassword != null
		credentials {
			username = buildCacheUsername
			password = buildCachePassword
		}
	}
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
	project.buildFileName = "${project.name}.gradle.kts"
	require(project.buildFile.isFile) {
		"${project.buildFile} must exist"
	}
}

enableFeaturePreview("STABLE_CONFIGURATION_CACHE")
enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")
