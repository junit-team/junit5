plugins {
	`kotlin-dsl`
}

repositories {
	gradlePluginPortal()
}

dependencies {
	implementation(projects.buildParameters)
	implementation(kotlin("gradle-plugin"))
	implementation(libs.plugins.bnd.markerCoordinates)
	implementation(libs.plugins.commonCustomUserData.markerCoordinates)
	implementation(libs.plugins.develocity.markerCoordinates)
	implementation(libs.plugins.foojayResolver.markerCoordinates)
	implementation(libs.plugins.jmh.markerCoordinates)
	implementation(libs.plugins.shadow.markerCoordinates)
	implementation(libs.plugins.spotless.markerCoordinates)
	implementation(libs.plugins.versions.markerCoordinates)
}

// see https://docs.gradle.org/current/userguide/plugins.html#sec:plugin_markers
val Provider<PluginDependency>.markerCoordinates: Provider<String>
	get() = map { "${it.pluginId}:${it.pluginId}.gradle.plugin:${it.version}" }
