pluginManagement {
	plugins {
		id("org.graalvm.buildtools.native") version "0.10.2"
	}
	repositories {
		mavenCentral()
		gradlePluginPortal()
	}
}

rootProject.name = "graalvm-starter"
