plugins {
    `kotlin-dsl`
}

group = "junitbuild.base"

repositories {
    gradlePluginPortal()
}

dependencies {
    api("gg.jte:jte:3.1.12")
}
