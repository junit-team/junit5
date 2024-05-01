pluginManagement {
    plugins {
        id("org.graalvm.buildtools.native") version "0.10.1"
    }
    repositories {
        mavenCentral()
        gradlePluginPortal()
    }
}

rootProject.name = "graalvm-starter"
