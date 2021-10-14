import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
	`kotlin-dsl`
}

repositories {
	gradlePluginPortal()
}

dependencies {
	implementation(kotlin("gradle-plugin"))
	implementation("biz.aQute.bnd:biz.aQute.bnd.gradle:5.3.0")
    implementation("com.diffplug.spotless:spotless-plugin-gradle:5.15.2")
    implementation("com.github.ben-manes:gradle-versions-plugin:0.39.0")
    implementation("gradle.plugin.com.github.jengelman.gradle.plugins:shadow:7.0.0")
    implementation("gradle.plugin.net.nemerosa:versioning:2.15.0")
	implementation("org.gradle:test-retry-gradle-plugin:1.2.1")
	compileOnly("com.gradle.enterprise:test-distribution-gradle-plugin:2.2") // keep in sync with root settings.gradle.kts
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
