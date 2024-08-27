plugins {
	`kotlin-dsl`
}

repositories {
	gradlePluginPortal()
}

dependencies {
	implementation("junitbuild.base:code-generator-model")
	implementation(projects.common)
	implementation(libs.jackson.dataformat.yaml)
	implementation(libs.jackson.module.kotlin)
	implementation(libs.jte)
}
