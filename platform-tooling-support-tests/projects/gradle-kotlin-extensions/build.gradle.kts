import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
	kotlin("jvm") version "1.2.71"
}

repositories {
	maven { url = uri(file(System.getProperty("maven.repo"))) }
	mavenCentral()
	maven { url = uri("https://oss.sonatype.org/content/repositories/snapshots") }
}

// don't use `build` as target to prevent Jenkins picking up
project.buildDir = file("bin")

// grab jupiter version from system environment
val jupiterVersion = System.getenv("JUNIT_JUPITER_VERSION")

dependencies {
	testImplementation(kotlin("stdlib-jdk8"))
	testImplementation("org.junit.jupiter:junit-jupiter:$jupiterVersion")
}

tasks.withType<KotlinCompile>().configureEach {
	kotlinOptions {
		jvmTarget = "1.8"
		apiVersion = "1.1"
		languageVersion = "1.1"
	}
}

tasks.test {
	useJUnitPlatform()
	testLogging {
		events("passed", "skipped", "failed")
	}
}
