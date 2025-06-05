import org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_21

plugins {
	`kotlin-dsl`
}

dependencies {
	implementation("junitbuild.base:code-generator-model")
	implementation("junitbuild.base:dsl-extensions")
	implementation(projects.common)
	implementation(libs.jackson.dataformat.yaml)
	implementation(libs.jackson.module.kotlin)
	implementation(libs.jte)
}

java {
	sourceCompatibility = JavaVersion.VERSION_21
	targetCompatibility = JavaVersion.VERSION_21
}

kotlin {
	compilerOptions.jvmTarget = JVM_21
}
