pluginManagement {
	plugins {
		id("org.graalvm.buildtools.native") version "0.10.3"
	}
	repositories {
		mavenCentral()
		gradlePluginPortal()
	}
}

rootProject.name = "graalvm-starter"
