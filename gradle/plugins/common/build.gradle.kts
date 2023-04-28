import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
	`kotlin-dsl`
	alias(libs.plugins.versions)
}

repositories {
	gradlePluginPortal()
}

dependencies {
	implementation(project(":build-parameters"))
	implementation(kotlin("gradle-plugin"))
	implementation(libs.gradle.bnd)
	implementation(libs.gradle.spotless)
	implementation(libs.gradle.versions)
	implementation(libs.gradle.shadow)
	compileOnly(libs.gradle.enterprise)
}

tasks {
	withType<JavaCompile>().configureEach {
		options.release.set(11)
	}
	withType<KotlinCompile>().configureEach {
		kotlinOptions {
			jvmTarget = "11"
			allWarningsAsErrors = true
		}
	}
}
