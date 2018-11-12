description = "JUnit Platform Engine API"

dependencies {
	api(project(":junit-platform-commons"))
	api("org.opentest4j:opentest4j:${Versions.ota4j}")

	testImplementation("org.assertj:assertj-core:${Versions.assertJ}")
}

tasks.jar {
	manifest {
		attributes(
			"Automatic-Module-Name" to "org.junit.platform.engine"
		)
	}
}
