apply(from = "$rootDir/gradle/testing.gradle")

description = "JUnit Jupiter Migration Support"

dependencies {
	api("junit:junit:${Versions.junit4}")
	api(project(":junit-jupiter-api"))

	testImplementation(project(":junit-jupiter-engine"))
	testImplementation(project(":junit-platform-launcher"))
	testImplementation(project(":junit-platform-runner"))
	testImplementation(project(":junit-platform-testkit"))
}

tasks.named<Jar>("jar") {
	manifest {
		attributes(
			"Automatic-Module-Name" to "org.junit.jupiter.migrationsupport"
		)
	}
}
