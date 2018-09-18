import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
	kotlin("jvm") version "1.2.41"
}

repositories {
	mavenLocal()
	mavenCentral()
	maven { url = uri("https://oss.sonatype.org/content/repositories/snapshots") }
}

// don't use `build` as target to prevent Jenkins picking up
project.buildDir = file("bin")

// grab jupiter version from system environment
val jupiterVersion = System.getenv("JUNIT_JUPITER_VERSION")

dependencies {
	testCompile(kotlin("stdlib-jdk8"))
	testCompile("org.junit.jupiter:junit-jupiter-api:$jupiterVersion")
	testCompile("org.junit.jupiter:junit-jupiter-params:$jupiterVersion")
	testRuntime("org.junit.jupiter:junit-jupiter-engine:$jupiterVersion")
}

tasks.withType<KotlinCompile> {
	kotlinOptions {
		jvmTarget = "1.8"
	}
}

tasks.withType<Test> {
	useJUnitPlatform()
	testLogging {
		events("passed", "skipped", "failed")
	}
}
