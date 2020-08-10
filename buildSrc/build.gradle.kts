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
	implementation("biz.aQute.bnd:biz.aQute.bnd.gradle:5.1.2")
	implementation("com.github.jengelman.gradle.plugins:shadow:6.0.0")
	implementation("org.gradle:test-retry-gradle-plugin:1.1.8")
}
