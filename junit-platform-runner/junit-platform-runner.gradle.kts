description = "JUnit Platform Runner"

dependencies {
	api(project(":junit-platform-launcher"))
	api(project(":junit-platform-suite-api"))
	api("junit:junit:${Versions.junit4}")
}

tasks.jar {
	manifest {
		attributes(
			"Automatic-Module-Name" to "org.junit.platform.runner"
		)
	}
}
