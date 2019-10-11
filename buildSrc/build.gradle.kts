plugins {
	`kotlin-dsl`
}

repositories {
	mavenCentral()
	gradlePluginPortal()
}

dependencies {
	implementation(kotlin("gradle-plugin"))
	implementation("de.marcphilipp.gradle:nexus-publish-plugin:0.4.0")
}
