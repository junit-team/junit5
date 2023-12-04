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
