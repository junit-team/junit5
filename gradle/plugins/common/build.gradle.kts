plugins {
	`kotlin-dsl`
}

repositories {
	gradlePluginPortal()
}

dependencies {
	implementation(projects.buildParameters)
	implementation(kotlin("gradle-plugin"))
	implementation(libs.gradle.bnd)
	implementation(libs.gradle.commonCustomUserData)
	implementation(libs.gradle.develocity)
	implementation(libs.gradle.foojayResolver)
	implementation(libs.plugins.jmh.map { it.markerCoordinates })
	implementation(libs.gradle.shadow)
	implementation(libs.gradle.spotless)
	implementation(libs.gradle.versions)
}

// see https://docs.gradle.org/current/userguide/plugins.html#sec:plugin_markers
val PluginDependency.markerCoordinates: String
	get() = "${pluginId}:${pluginId}.gradle.plugin:${version}"
