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
			kotlinOptions.jvmTarget = extension.testJavaVersion.toString()
		}
	}
}
