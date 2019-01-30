description = "JUnit Platform Launcher"

dependencies {
	api("org.apiguardian:apiguardian-api:${Versions.apiGuardian}")

	api(project(":junit-platform-engine"))
}

tasks.jar {
	manifest {
		attributes(
			"Automatic-Module-Name" to "org.junit.platform.launcher"
		)
	}
}
