import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
	id("java-library-conventions")
	kotlin("jvm")
}

tasks.withType<KotlinCompile>().configureEach {
	kotlinOptions {
		apiVersion = "1.3"
		languageVersion = "1.3"
		allWarningsAsErrors = false
	}
}

afterEvaluate {
	val extension = project.the<JavaLibraryExtension>()
	tasks {
		withType<KotlinCompile>().configureEach {
			kotlinOptions.jvmTarget = extension.mainJavaVersion.toString()
		}
		named<KotlinCompile>("compileTestKotlin") {
			// The Kotlin compiler does not yet support JDK 17 and later (see https://kotlinlang.org/docs/compiler-reference.html#jvm-target-version)
			kotlinOptions.jvmTarget = minOf(JavaVersion.VERSION_16, extension.testJavaVersion).toString()
		}
	}
}
