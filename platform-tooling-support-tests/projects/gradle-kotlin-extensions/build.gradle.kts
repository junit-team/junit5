import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
	kotlin("jvm") version "2.1.10"
}

repositories {
	maven { url = uri(file(System.getProperty("maven.repo"))) }
	mavenCentral()
}

val jupiterVersion: String by project
val platformVersion: String by project

dependencies {
	testImplementation(kotlin("stdlib-jdk8"))
	testImplementation("org.junit.jupiter:junit-jupiter:$jupiterVersion")
	testRuntimeOnly("org.junit.platform:junit-platform-launcher:$platformVersion")
}

java {
	toolchain {
		languageVersion = JavaLanguageVersion.of(8)
	}
}

tasks.withType<KotlinCompile>().configureEach {
	kotlinOptions {
		jvmTarget = "1.8"
		apiVersion = "1.6"
		languageVersion = "1.6"
	}
}

tasks.test {
	useJUnitPlatform()
	testLogging {
		events("passed", "skipped", "failed")
	}
}
