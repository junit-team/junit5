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
include("code-generator")

enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")
