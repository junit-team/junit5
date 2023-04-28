plugins {
	`kotlin-dsl`
	alias(libs.plugins.versions)
}

repositories {
	gradlePluginPortal()
}

dependencies {
	implementation(projects.buildParameters)
	implementation(kotlin("gradle-plugin"))
	implementation(libs.gradle.bnd)
	implementation(libs.gradle.spotless)
	implementation(libs.gradle.versions)
	implementation(libs.gradle.shadow)
	compileOnly(libs.gradle.enterprise)
}
