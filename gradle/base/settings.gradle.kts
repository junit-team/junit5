rootProject.name = "base"

dependencyResolutionManagement {
    repositories {
        gradlePluginPortal()
    }
}

include("code-generator-model")
include("dsl-extensions")
