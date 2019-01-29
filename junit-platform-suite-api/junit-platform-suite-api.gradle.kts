description = "JUnit Platform Suite API"

dependencies {
	api("org.apiguardian:apiguardian-api:${Versions.apiGuardian}")
}

tasks.jar {
	manifest {
		attributes(
			"Automatic-Module-Name" to "org.junit.platform.suite.api"
		)
	}
}
