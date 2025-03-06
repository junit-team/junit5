import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.dsl.KotlinVersion.KOTLIN_2_1
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
	id("junitbuild.java-library-conventions")
	kotlin("jvm")
}

tasks.named("kotlinSourcesJar") {
	enabled = false
}

val javaLibraryExtension = project.the<JavaLibraryExtension>()

tasks.withType<KotlinCompile>().configureEach {
	compilerOptions {
		jvmTarget = javaLibraryExtension.mainJavaVersion.map { JvmTarget.fromTarget(it.toString()) }
		apiVersion = KOTLIN_2_1
		languageVersion = apiVersion
		allWarningsAsErrors.convention(true)
		javaParameters = true
		freeCompilerArgs.addAll("-opt-in=kotlin.RequiresOptIn")
	}
}

tasks.named<KotlinCompile>("compileTestKotlin") {
	compilerOptions.jvmTarget = javaLibraryExtension.testJavaVersion.map { JvmTarget.fromTarget(it.toString()) }
}
