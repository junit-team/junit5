apply(from = "$rootDir/gradle/testing.gradle")

description = "JUnit Vintage Engine"

dependencies {
	api(project(":junit-platform-engine"))
	implementation("junit:junit:${Versions.junit4}")

	testImplementation(project(":junit-platform-launcher"))
	testImplementation(project(":junit-jupiter-api"))
	testImplementation(project(":junit-platform-runner"))
	testImplementation(project(path = ":junit-jupiter-engine", configuration = "testArtifacts"))
	testImplementation(project(":junit-platform-testkit"))
}

tasks.named<Jar>("jar") {
	manifest {
		attributes(
			"Automatic-Module-Name" to "org.junit.vintage.engine"
		)
	}
}
