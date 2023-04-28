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
	implementation(libs.gradle.enterprise)
	implementation(libs.gradle.foojayResolver)
	implementation(libs.gradle.shadow)
	implementation(libs.gradle.spotless)
	implementation(libs.gradle.versions)
}
