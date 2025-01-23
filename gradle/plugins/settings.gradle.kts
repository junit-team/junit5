val expectedJavaVersion = JavaVersion.VERSION_21
val actualJavaVersion = JavaVersion.current()
require(actualJavaVersion == expectedJavaVersion) {
	"The JUnit 5 build must be executed with Java ${expectedJavaVersion.majorVersion}. Currently executing with Java ${actualJavaVersion.majorVersion}."
}

dependencyResolutionManagement {
	versionCatalogs {
		create("libs") {
			from(files("../libs.versions.toml"))
		}
	}
	repositories {
		gradlePluginPortal()
	}
}

rootProject.name = "plugins"

includeBuild("../base")

include("build-parameters")
include("common")
include("code-generator")

enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")
