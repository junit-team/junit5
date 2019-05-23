plugins {
	`kotlin-library-conventions`
	groovy
}

apply(from = "$rootDir/gradle/testing.gradle.kts")

description = "JUnit Jupiter Engine"

tasks.compileTestGroovy {
	sourceCompatibility = javaLibrary.testJavaVersion.majorVersion
	targetCompatibility = javaLibrary.testJavaVersion.majorVersion
}

val testArtifacts by configurations.creating {
	extendsFrom(configurations.testRuntime.get())
}

val testJar by tasks.creating(Jar::class) {
	archiveClassifier.set("test")
	from(sourceSets.test.get().output)
}

artifacts {
	add(testArtifacts.name, testJar)
}

dependencies {
	api("org.apiguardian:apiguardian-api:${Versions.apiGuardian}")

	api(project(":junit-platform-engine"))
	api(project(":junit-jupiter-api"))

	testImplementation(project(":junit-platform-launcher"))
	testImplementation(project(":junit-platform-runner"))
	testImplementation(project(":junit-platform-testkit"))
	testImplementation("org.jetbrains.kotlin:kotlin-stdlib")
	testImplementation("org.codehaus.groovy:groovy-all:${Versions.groovy}")
}
