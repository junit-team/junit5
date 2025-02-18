import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.dsl.KotlinVersion
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
	id("junitbuild.java-library-conventions")
	kotlin("jvm")
}

tasks.named("kotlinSourcesJar") {
	enabled = false
}

tasks.withType<KotlinCompile>().configureEach {
	compilerOptions {
		apiVersion = KotlinVersion.fromVersion("1.6")
		languageVersion = apiVersion
		allWarningsAsErrors = false
		// Compiler arg is required for Kotlin 1.6 and below
		// see https://kotlinlang.org/docs/whatsnew17.html#stable-opt-in-requirements
		freeCompilerArgs.add("-opt-in=kotlin.RequiresOptIn")
	}
}

afterEvaluate {
	val extension = project.the<JavaLibraryExtension>()
	tasks {
		withType<KotlinCompile>().configureEach {
			compilerOptions.jvmTarget = JvmTarget.fromTarget(extension.mainJavaVersion.toString())
			compilerOptions.javaParameters = true
		}
		named<KotlinCompile>("compileTestKotlin") {
			compilerOptions.jvmTarget = JvmTarget.fromTarget(extension.testJavaVersion.toString())
		}
	}
}
