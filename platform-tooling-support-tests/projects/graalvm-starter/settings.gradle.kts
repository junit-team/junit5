pluginManagement {
	plugins {
		// TODO Remove custom config in build.gradle.kts when upgrading
		id("org.graalvm.buildtools.native") version "0.10.6"
	}
	repositories {
		mavenCentral()
		gradlePluginPortal()
	}
}

plugins {
	id("org.gradle.toolchains.foojay-resolver-convention") version "0.10.0"
}

rootProject.name = "graalvm-starter"
