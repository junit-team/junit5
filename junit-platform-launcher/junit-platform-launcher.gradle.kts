description = "JUnit Platform Launcher"

dependencies {
	api(project(":junit-platform-engine"))
}

tasks.jar {
	manifest {
		attributes(
			"Automatic-Module-Name" to "org.junit.platform.launcher"
		)
	}
}
