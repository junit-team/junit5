import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
	id("junitbuild.java-library-conventions")
	kotlin("jvm")
}

tasks.named("kotlinSourcesJar") {
	enabled = false
}

tasks.withType<KotlinCompile>().configureEach {
	kotlinOptions {
		apiVersion = "1.6"
		languageVersion = "1.6"
		allWarningsAsErrors = false
		// Compiler arg is required for Kotlin 1.6 and below
		// see https://kotlinlang.org/docs/whatsnew17.html#stable-opt-in-requirements
		freeCompilerArgs += "-opt-in=kotlin.RequiresOptIn"
	}
}

afterEvaluate {
	val extension = project.the<JavaLibraryExtension>()
	tasks {
		withType<KotlinCompile>().configureEach {
			kotlinOptions.jvmTarget = extension.mainJavaVersion.toString()
		}
		named<KotlinCompile>("compileTestKotlin") {
			kotlinOptions.jvmTarget = extension.testJavaVersion.toString()
		}
	}
}
