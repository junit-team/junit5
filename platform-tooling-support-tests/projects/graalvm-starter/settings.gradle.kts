pluginManagement {
	plugins {
		id("org.graalvm.buildtools.native") version "0.10.3"
	}
	repositories {
		mavenCentral()
		gradlePluginPortal()
	}
}

plugins {
	id("org.gradle.toolchains.foojay-resolver-convention") version "0.8.0"
}

rootProject.name = "graalvm-starter"
