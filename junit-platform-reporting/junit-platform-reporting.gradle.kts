description = "JUnit Platform Reporting"

dependencies {
	api("org.apiguardian:apiguardian-api:${Versions.apiGuardian}")

	api(project(":junit-platform-launcher"))
}

tasks.named<Jar>("jar") {
	manifest {
		attributes(
				"Automatic-Module-Name" to "org.junit.platform.reporting"
		)
	}
}
