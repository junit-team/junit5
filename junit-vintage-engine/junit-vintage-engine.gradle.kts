plugins {
	`java-library-conventions`
}

apply(from = "$rootDir/gradle/testing.gradle.kts")

description = "JUnit Vintage Engine"

dependencies {
	api("org.apiguardian:apiguardian-api:${Versions.apiGuardian}")
	api(project(":junit-platform-engine"))
	api("junit:junit:${Versions.junit4}")

	testImplementation(project(":junit-platform-launcher"))
	testImplementation(project(":junit-jupiter-api"))
	testImplementation(project(":junit-platform-runner"))
	testImplementation(project(path = ":junit-jupiter-engine", configuration = "testArtifacts"))
	testImplementation(project(":junit-platform-testkit"))
}
