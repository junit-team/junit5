import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
	`kotlin-dsl`
	id("com.github.ben-manes.versions") version "0.39.0"
}

repositories {
	gradlePluginPortal()
}

dependencies {
	implementation(kotlin("gradle-plugin"))
	implementation("biz.aQute.bnd:biz.aQute.bnd.gradle:6.1.0")
    implementation("com.diffplug.spotless:spotless-plugin-gradle:6.0.0")
    implementation("com.github.ben-manes:gradle-versions-plugin:0.39.0")
    implementation("gradle.plugin.com.github.johnrengelman:shadow:7.1.2")
    implementation("gradle.plugin.net.nemerosa:versioning:2.15.1")
	implementation("org.gradle:test-retry-gradle-plugin:1.3.1")
	compileOnly("com.gradle.enterprise:test-distribution-gradle-plugin:2.2.2") // keep in sync with root settings.gradle.kts
}

tasks {
	withType<JavaCompile>().configureEach {
		options.release.set(8)
	}
	withType<KotlinCompile>().configureEach {
		kotlinOptions {
			jvmTarget = "1.8"
			allWarningsAsErrors = true
		}
	}
}
