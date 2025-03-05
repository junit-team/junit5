import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_17
import org.jetbrains.kotlin.gradle.dsl.KotlinVersion.KOTLIN_2_1

plugins {
	kotlin("jvm") version "2.1.20"
}

repositories {
	maven { url = uri(file(System.getProperty("maven.repo"))) }
	mavenCentral()
}

val jupiterVersion: String by project
val platformVersion: String by project

dependencies {
	testImplementation(kotlin("stdlib"))
	testImplementation("org.junit.jupiter:junit-jupiter:$jupiterVersion")
	testRuntimeOnly("org.junit.platform:junit-platform-launcher:$platformVersion")
}

java {
	toolchain {
		languageVersion = JavaLanguageVersion.of(17)
	}
}

tasks.withType<KotlinCompile>().configureEach {
	compilerOptions {
		jvmTarget = JVM_17
		apiVersion = KOTLIN_2_1
		languageVersion = KOTLIN_2_1
		freeCompilerArgs.addAll("-Xskip-prerelease-check")
	}
}

tasks.test {
	useJUnitPlatform()
	testLogging {
		events("passed", "skipped", "failed")
	}
}
