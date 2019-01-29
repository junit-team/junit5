description = "JUnit Platform Commons"

dependencies {
	api("org.apiguardian:apiguardian-api:${Versions.apiGuardian}")
}

tasks.jar {
	manifest {
		attributes(
			"Automatic-Module-Name" to "org.junit.platform.commons"
		)
	}
}
