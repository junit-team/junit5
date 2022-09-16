pluginManagement {
    plugins {
        id("org.graalvm.buildtools.native") version "0.9.13"
    }
    repositories {
        mavenCentral()
        gradlePluginPortal()
    }
}

rootProject.name = "graalvm-starter"
