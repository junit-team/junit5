dependencyResolutionManagement {
	versionCatalogs {
		create("libs") {
			from(files("../libs.versions.toml"))
		}
	}
}

rootProject.name = "base"

include("code-generator-model")
