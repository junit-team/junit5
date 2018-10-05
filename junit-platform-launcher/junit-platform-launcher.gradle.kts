description = "JUnit Platform Launcher"

dependencies {
	api(project(":junit-platform-engine"))
}

tasks.named<Jar>("jar") {
	manifest {
		attributes(
			"Automatic-Module-Name" to "org.junit.platform.launcher"
		)
	}
}
