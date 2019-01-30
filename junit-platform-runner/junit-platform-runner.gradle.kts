description = "JUnit Platform Runner"

dependencies {
	api("junit:junit:${Versions.junit4}")
	api("org.apiguardian:apiguardian-api:${Versions.apiGuardian}")

	api(project(":junit-platform-launcher"))
	api(project(":junit-platform-suite-api"))
}

tasks.jar {
	manifest {
		attributes(
			"Automatic-Module-Name" to "org.junit.platform.runner"
		)
	}
}
