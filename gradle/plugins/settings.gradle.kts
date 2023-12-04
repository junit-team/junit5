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
}

rootProject.name = "plugins"

include("build-parameters")
include("common")

enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")
