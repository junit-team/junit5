dependencyResolutionManagement {
	versionCatalogs {
		create("libs") {
			from(files("../libs.versions.toml"))
		}
	}
}

include("build-parameters")
include("common")

enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")
