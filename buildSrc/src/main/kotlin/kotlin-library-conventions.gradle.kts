import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
	id("java-library-conventions")
	kotlin("jvm")
}

tasks.withType<KotlinCompile>().configureEach {
	kotlinOptions {
		jvmTarget = "1.8"
		apiVersion = "1.3"
		languageVersion = "1.3"
		allWarningsAsErrors = true
	}
}
